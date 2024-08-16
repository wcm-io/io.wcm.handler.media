/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2024 wcm.io
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
package io.wcm.handler.mediasource.ngdm.impl;

import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService.PLACEHOLDER_ASSET_ID;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService.PLACEHOLDER_FORMAT;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService.PLACEHOLDER_SEO_NAME;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.format.Ratio;
import io.wcm.handler.media.impl.ImageTransformation;
import io.wcm.handler.mediasource.ngdm.impl.metadata.NextGenDynamicMediaMetadata;
import io.wcm.handler.mediasource.ngdm.impl.metadata.SmartCrop;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Builds URL to render image rendition via NextGen Dynamic Media.
 * <p>
 * Example URL that might be build:
 * https://host/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image.jpg?preferwebp=true&quality=85&width=300&crop=16:9,smart
 * </p>
 */
public final class NextGenDynamicMediaImageUrlBuilder {

  static final String PARAM_PREFER_WEBP = "preferwebp";
  static final String PARAM_WIDTH = "width";
  static final String PARAM_HEIGHT = "height";
  static final String PARAM_CROP = "crop";
  static final String PARAM_SMARTCROP = "smartcrop";
  static final String PARAM_ROTATE = "rotate";
  static final String PARAM_QUALITY = "quality";

  private static final Set<String> SUPPORTED_FORMATS = Set.of(
      FileExtension.JPEG,
      FileExtension.PNG,
      FileExtension.GIF,
      FileExtension.WEBP);

  private final NextGenDynamicMediaContext context;

  /**
   * @param context Context
   */
  public NextGenDynamicMediaImageUrlBuilder(@NotNull NextGenDynamicMediaContext context) {
    this.context = context;
  }

  /**
   * Builds the URL for a rendition.
   * @param params Parameters
   * @return URL or null if invalid/not possible
   */
  public @Nullable String build(@NotNull NextGenDynamicMediaImageDeliveryParams params) {

    // get original image metadata
    NextGenDynamicMediaMetadata metadata = context.getMetadata();
    Dimension orginalDimension = null;
    if (metadata != null) {
      orginalDimension = metadata.getDimension();
    }

    // get parameters from nextgen dynamic media config for URL parameters
    String repositoryId;
    if (context.getReference().getAsset() != null) {
      repositoryId = context.getNextGenDynamicMediaConfig().getLocalAssetsRepositoryId();
    }
    else {
      repositoryId = context.getNextGenDynamicMediaConfig().getRemoteAssetsRepositoryId();
    }
    String imageDeliveryPath = context.getNextGenDynamicMediaConfig().getImageDeliveryBasePath();
    if (repositoryId == null || imageDeliveryPath == null || StringUtils.isAnyBlank(repositoryId, imageDeliveryPath)) {
      return null;
    }

    // replace placeholders in delivery path
    String seoName = FilenameUtils.getBaseName(context.getReference().getFileName());
    String format = getFileExtension();
    imageDeliveryPath = StringUtils.replace(imageDeliveryPath, PLACEHOLDER_ASSET_ID, context.getReference().getAssetId());
    imageDeliveryPath = StringUtils.replace(imageDeliveryPath, PLACEHOLDER_SEO_NAME, seoName);
    imageDeliveryPath = StringUtils.replace(imageDeliveryPath, PLACEHOLDER_FORMAT, format);

    // prepare URL params
    Dimension cropSmartRatio = params.getCropSmartRatio();
    Integer rotation = params.getRotation();
    Integer quality = params.getQuality();

    SortedMap<String, String> urlParamMap = new TreeMap<>();
    urlParamMap.put(PARAM_PREFER_WEBP, "true");

    // check for a matching named smart cropping profile
    SmartCrop namedSmartCrop = getMatchingNamedSmartCrop(metadata, cropSmartRatio);
    if (namedSmartCrop != null) {
      urlParamMap.put(PARAM_SMARTCROP, namedSmartCrop.getName());
      boolean widthOrHeightDefined = applyWidthOrPlaceholder(params, urlParamMap) || applyHeightOrPlaceholder(params, urlParamMap);
      if (!widthOrHeightDefined) {
        // if no width or height given apply default width/height to not rely on dimensions defined in AEM image profile
        String imageWidthHeightDefault = Long.toString(context.getNextGenDynamicMediaConfig().getImageWidthHeightDefault());
        if (namedSmartCrop.getCropDimension().getWidth() >= namedSmartCrop.getCropDimension().getHeight()) {
          urlParamMap.put(PARAM_WIDTH, imageWidthHeightDefault);
        }
        else {
          urlParamMap.put(PARAM_HEIGHT, imageWidthHeightDefault);
        }
      }
    }
    else if (orginalDimension != null && cropSmartRatio != null && isAutoCroppingRequired(orginalDimension, cropSmartRatio)) {
      // apply static auto crop (center-cropping)
      CropDimension cropDimension = ImageTransformation.calculateAutoCropDimension(
          orginalDimension.getWidth(), orginalDimension.getHeight(), Ratio.get(cropSmartRatio));
      urlParamMap.put(PARAM_CROP, cropDimension.getCropStringWidthHeight());
      if (!applyWidthOrPlaceholder(params, urlParamMap)) {
        applyHeightOrPlaceholder(params, urlParamMap);
      }
    }
    else {
      // No cropping required or insufficient metadata available to detect cropping
      boolean widthDefined = applyWidthOrPlaceholder(params, urlParamMap);
      boolean heightDefined = applyHeightOrPlaceholder(params, urlParamMap);
      if (!(widthDefined || heightDefined) && cropSmartRatio != null) {
        // if no width or height given apply default width/height respecting the requested aspect ratio
        double ratio = Ratio.get(cropSmartRatio);
        long width = context.getNextGenDynamicMediaConfig().getImageWidthHeightDefault();
        long height = context.getNextGenDynamicMediaConfig().getImageWidthHeightDefault();
        if (ratio > 1) {
          height = Math.round(width / ratio);
        }
        else if (ratio < 1) {
          width = Math.round(height * ratio);
        }
        urlParamMap.put(PARAM_WIDTH, Long.toString(width));
        urlParamMap.put(PARAM_HEIGHT, Long.toString(height));
      }
    }

    if (rotation != null && rotation != 0) {
      urlParamMap.put(PARAM_ROTATE, rotation.toString());
    }
    if (quality != null) {
      urlParamMap.put(PARAM_QUALITY, quality.toString());
    }

    // build URL
    return buildImageUrl(repositoryId, imageDeliveryPath, urlParamMap);
  }

  /**
   * Builds image URL based on URL parameter map.
   */
  private static @NotNull String buildImageUrl(@NotNull String repositoryId, @NotNull String imageDeliveryPath,
      @NotNull SortedMap<String, String> urlParamMap) {
    StringBuilder url = new StringBuilder();
    url.append("https://")
        .append(repositoryId)
        .append(imageDeliveryPath);
    String urlParams = urlParamMap.entrySet().stream()
        .map(entry -> toUrlParam(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining("&"));
    if (StringUtils.isNotEmpty(urlParams)) {
      if (url.indexOf("?") < 0) {
        url.append("?");
      }
      else {
        url.append("&");
      }
      url.append(urlParams);
    }
    return url.toString();
  }

  /**
   * Generates URL parameter key/value pair with escaping where appropriate.
   */
  private static @NotNull String toUrlParam(@NotNull String key, @NotNull String value) {
    StringBuilder sb = new StringBuilder();
    sb.append(key).append("=");
    // we only need to encode crop, all other parameters are numbers only
    if (StringUtils.equalsAny(key, PARAM_CROP, PARAM_SMARTCROP)) {
      sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
    else {
      sb.append(value);
    }
    return sb.toString();
  }

  /**
   * Looks up named smart crop definition matching the requested ratio.
   * @param cropSmartRatio Requested ratio
   * @return Matching named smart crop or null if none found
   */
  private @Nullable SmartCrop getMatchingNamedSmartCrop(@Nullable NextGenDynamicMediaMetadata metadata, @Nullable Dimension cropSmartRatio) {
    if (metadata == null || cropSmartRatio == null) {
      return null;
    }
    double requestedRatio = Ratio.get(cropSmartRatio);
    return metadata.getSmartCrops().stream()
        .filter(smartCrop -> Ratio.matches(smartCrop.getRatio(), requestedRatio))
        .findFirst()
        .orElse(null);
  }

  /**
   * Checks if auto cropping is required.
   * @param originalDimension Dimension of original image
   * @param cropSmartRatio Requested aspect ratio
   * @return true if auto cropping is required. False if original image matches the requested ratio.
   */
  private boolean isAutoCroppingRequired(@NotNull Dimension originalDimension, @NotNull Dimension cropSmartRatio) {
    return !Ratio.matches(Ratio.get(originalDimension), Ratio.get(cropSmartRatio));
  }

  /**
   * Apply either width value or width placeholder, if available.
   * @param params Parameters
   * @param urlParamMap URL parameter map
   * @return true if any width and/or height value or placeholder was applied
   */
  private boolean applyWidthOrPlaceholder(@NotNull NextGenDynamicMediaImageDeliveryParams params, @NotNull SortedMap<String, String> urlParamMap) {
    Long width = params.getWidth();
    String widthPlaceholder = params.getWidthPlaceholder();
    boolean anyApplied = false;
    if (widthPlaceholder != null) {
      urlParamMap.put(PARAM_WIDTH, widthPlaceholder);
      anyApplied = true;
    }
    else if (width != null) {
      urlParamMap.put(PARAM_WIDTH, width.toString());
      anyApplied = true;
    }
    return anyApplied;
  }

  /**
   * Apply either height value or height placeholder, if available.
   * @param params Parameters
   * @param urlParamMap URL parameter map
   * @return true if any width and/or height value or placeholder was applied
   */
  private boolean applyHeightOrPlaceholder(@NotNull NextGenDynamicMediaImageDeliveryParams params, @NotNull SortedMap<String, String> urlParamMap) {
    Long height = params.getHeight();
    String heightPlaceholder = params.getHeightPlaceholder();
    boolean anyApplied = false;
    if (heightPlaceholder != null) {
      urlParamMap.put(PARAM_HEIGHT, heightPlaceholder);
      anyApplied = true;
    }
    else if (height != null) {
      urlParamMap.put(PARAM_HEIGHT, height.toString());
      anyApplied = true;
    }
    return anyApplied;
  }

  /**
   * @return Get file extension used for rendering via DM API.
   */
  public @NotNull String getFileExtension() {
    String format = context.getDefaultMediaArgs().getEnforceOutputFileExtension();
    if (StringUtils.isEmpty(format)) {
      format = StringUtils.toRootLowerCase(FilenameUtils.getExtension(context.getReference().getFileName()));
    }
    if (format == null || !SUPPORTED_FORMATS.contains(format)) {
      format = FileExtension.JPEG;
    }
    return format;
  }

}
