/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.handler.mediasource.dam.impl.dynamicmedia;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.format.Ratio;
import io.wcm.handler.media.impl.ImageQualityPercentage;
import io.wcm.handler.mediasource.dam.impl.DamContext;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * Build part of dynamic media/scene7 URL to render renditions.
 */
public final class DynamicMediaPath {

  /**
   * Fixed path part for dynamic media image serving API for serving images.
   */
  @SuppressWarnings("java:S1075") // not a file path
  private static final String IMAGE_SERVER_PATH = "/is/image/";

  /**
   * Fixed path part for dynamic media image serving API for serving static content.
   */
  @SuppressWarnings("java:S1075") // not a file path
  private static final String CONTENT_SERVER_PATH = "/is/content/";

  /**
   * Suffix is appended to static content dynamic media URLs that should be served with
   * Content-Disposition: attachment header.
   * This is configured via a custom ruleset, see https://wcm.io/handler/media/dynamic-media.html
   */
  public static final String DOWNLOAD_SUFFIX = "?cdh=attachment";

  private static final Logger log = LoggerFactory.getLogger(DynamicMediaPath.class);

  private DynamicMediaPath() {
    // static methods only
  }

  /**
   * Build media path for serving static content via dynamic media/scene7.
   * @param damContext DAM context objects
   * @param contentDispositionAttachment Whether to send content disposition: attachment header for downloads
   * @return Media path
   */
  public static @NotNull String buildContent(@NotNull DamContext damContext, boolean contentDispositionAttachment) {
    StringBuilder result = new StringBuilder();
    result.append(CONTENT_SERVER_PATH).append(encodeDynamicMediaObject(damContext));
    if (contentDispositionAttachment) {
      result.append(DOWNLOAD_SUFFIX);
    }
    return result.toString();
  }

  /**
   * Build media path for rendering image via dynamic media/scene7.
   * @param damContext DAM context objects
   * @return Media path
   */
  public static @NotNull String buildImage(@NotNull DamContext damContext) {
    return IMAGE_SERVER_PATH + encodeDynamicMediaObject(damContext);
  }

  /**
   * Build media path for rendering image with dynamic media/scene7.
   * @param damContext DAM context objects
   * @param width Width
   * @param height Height
   * @return Media path
   */
  public static @Nullable String buildImage(@NotNull DamContext damContext, long width, long height) {
    return buildImage(damContext, width, height, null, null);
  }

  /**
   * Build media path for rendering image with dynamic media/scene7.
   * @param damContext DAM context objects
   * @param width Width
   * @param height Height
   * @param cropDimension Crop dimension
   * @param rotation Rotation
   * @return Media path
   */
  public static @Nullable String buildImage(@NotNull DamContext damContext, long width, long height,
      @Nullable CropDimension cropDimension, @Nullable Integer rotation) {
    Dimension dimension = calcWidthHeight(damContext, width, height);

    StringBuilder result = new StringBuilder();
    result.append(IMAGE_SERVER_PATH).append(encodeDynamicMediaObject(damContext));

    // check for smart cropping when no cropping was applied by default, or auto-crop is enabled
    if (SmartCrop.canApply(cropDimension, rotation)) {
      // check for matching image profile and use predefined cropping preset if match found
      NamedDimension smartCropDef = SmartCrop.getDimensionForWidthHeight(damContext.getImageProfile(), width, height);
      if (smartCropDef != null) {
        if (damContext.isDynamicMediaValidateSmartCropRenditionSizes()
            && !SmartCrop.isMatchingSize(damContext.getAsset(), damContext.getResourceResolver(), smartCropDef, width, height)) {
          // smart crop should be applied, but selected area is too small, treat as invalid
          logResult(damContext, "<too small for " + width + "x" + height + ">");
          return null;
        }
        result.append("%3A").append(smartCropDef.getName()).append("?");
        appendWidthHeigtFormatQuality(result, dimension, damContext);
        logResult(damContext, result);
        return result.toString();
      }
    }

    result.append("?");
    if (cropDimension != null) {
      result.append("crop=").append(cropDimension.getCropStringWidthHeight()).append("&");
    }
    if (rotation != null) {
      result.append("rotate=").append(rotation).append("&");
    }
    appendWidthHeigtFormatQuality(result, dimension, damContext);
    logResult(damContext, result);
    return result.toString();
  }

  private static void appendWidthHeigtFormatQuality(@NotNull StringBuilder result, @NotNull Dimension dimension, @NotNull DamContext damContext) {
    result.append("wid=").append(dimension.getWidth())
        .append("&hei=").append(dimension.getHeight())
        // cropping/width/height is pre-calculated to fit with original ratio, make sure there are no 1px background lines visible
        .append("&fit=stretch");
    // if original image may have an alpha channel, make sure it's preserved in the output format
    if (mayHaveAlphaChannel(damContext)) {
      applyFmt(result, damContext.getDynamicMediaDefaultFmtAlpha());
    }
    else {
      applyFmt(result, damContext.getDynamicMediaDefaultFmt());
    }
    if (damContext.isDynamicMediaSetImageQuality() && !isLosslessImageFormat(damContext)) {
      // it not PNG lossy format is used, apply image quality setting
      result.append("&qlt=").append(ImageQualityPercentage.getAsInteger(damContext.getMediaArgs(), damContext.getMediaHandlerConfig()));
    }
  }

  private static void applyFmt(@NotNull StringBuilder result, @NotNull String fmt) {
    if (StringUtils.isNotBlank(fmt)) {
      result.append("&fmt=").append(fmt);
    }
  }

  private static void logResult(@NotNull DamContext damContext, @NotNull CharSequence result) {
    if (log.isTraceEnabled()) {
      log.trace("Build dynamic media path for {}: {}", damContext.getAsset().getPath(), result);
    }
  }

  /**
   * Checks if width or height is bigger than the allowed max. width/height.
   * Reduces both to the max limit keeping aspect ration is required.
   * @param width With
   * @param height Height
   * @return Dimension with capped width/height
   */
  private static Dimension calcWidthHeight(@NotNull DamContext damContext, long width, long height) {
    Dimension sizeLimit = damContext.getDynamicMediaImageSizeLimit();
    if (width > sizeLimit.getWidth()) {
      double ratio = Ratio.get(width, height);
      long newWidth = sizeLimit.getWidth();
      long newHeight = Math.round(newWidth / ratio);
      return calcWidthHeight(damContext, newWidth, newHeight);
    }
    if (height > sizeLimit.getHeight()) {
      double ratio = Ratio.get(width, height);
      long newHeight = sizeLimit.getHeight();
      long newWidth = Math.round(newHeight * ratio);
      return new Dimension(newWidth, newHeight);
    }
    return new Dimension(width, height);
  }

  /**
   * Splits dynamic media folder and file name and URL-encodes them separately (may contain spaces or special chars).
   * @param damContext DAM context
   * @return Encoded path
   */
  private static String encodeDynamicMediaObject(@NotNull DamContext damContext) {
    String[] pathParts = StringUtils.split(damContext.getDynamicMediaObject(), "/");
    for (int i = 0; i < pathParts.length; i++) {
      pathParts[i] = URLEncoder.encode(pathParts[i], StandardCharsets.UTF_8);
      // replace "+" with %20 in URL paths
      pathParts[i] = StringUtils.replace(pathParts[i], "+", "%20");
    }
    return StringUtils.join(pathParts, "/");
  }

  /**
   * Checks if the asset is an image format that may have an alpha channel.
   * @param damContext DAM context
   * @return true if the asset may have an alpha channel
   */
  private static boolean mayHaveAlphaChannel(@NotNull DamContext damContext) {
    String mimeType = damContext.getAsset().getMimeType();
    return StringUtils.equalsAny(mimeType, ContentType.PNG, ContentType.WEBP);
  }

  /**
   * Checks if the asset is a lossless image format.
   * @param damContext DAM context
   * @return true if the asset has a lossless image format
   */
  private static boolean isLosslessImageFormat(@NotNull DamContext damContext) {
    String mimeType = damContext.getAsset().getMimeType();
    return StringUtils.equals(mimeType, ContentType.PNG);
  }

}
