/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.handler.mediasource.dam.impl;

import static io.wcm.handler.media.MediaNameConstants.URI_TEMPLATE_PLACEHOLDER_HEIGHT;
import static io.wcm.handler.media.MediaNameConstants.URI_TEMPLATE_PLACEHOLDER_WIDTH;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.dam.api.Rendition;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.impl.ImageFileServlet;
import io.wcm.handler.media.impl.MediaFileServlet;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.DynamicMediaPath;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.NamedDimension;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.SmartCrop;
import io.wcm.handler.mediasource.dam.impl.ngdm.WebOptimizedImageDeliveryParams;
import io.wcm.handler.url.UrlHandler;
import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Generates URI templates for asset renditions - with or without Dynamic Media.
 */
final class DamUriTemplate implements UriTemplate {

  private static final long DUMMY_WIDTH = 999991;
  private static final long DUMMY_HEIGHT = 999992;

  private final UriTemplateType type;
  private final String uriTemplate;
  private final Dimension dimension;

  DamUriTemplate(@NotNull UriTemplateType type, @NotNull Dimension dimension,
      @NotNull Rendition rendition, @Nullable CropDimension cropDimension, @Nullable Integer rotation,
      @Nullable Double ratio, @NotNull DamContext damContext) {
    this.type = type;

    String url = null;
    Dimension validatedDimension = null;
    if (damContext.isDynamicMediaEnabled() && damContext.isDynamicMediaAsset()) {
      // if DM is enabled: try to get rendition URL from dynamic media
      NamedDimension smartCropDef = getDynamicMediaSmartCropDef(cropDimension, rotation, ratio, damContext);
      url = buildUriTemplateDynamicMedia(type, cropDimension, rotation, smartCropDef, damContext);
      // get actual max. dimension from smart crop rendition
      if (url != null && smartCropDef != null) {
        validatedDimension = SmartCrop.getCropDimensionForAsset(damContext.getAsset(), damContext.getResourceResolver(), smartCropDef);
      }
    }
    if (url == null && (!damContext.isDynamicMediaEnabled() || !damContext.isDynamicMediaAemFallbackDisabled())) {
      if (damContext.isWebOptimizedImageDeliveryEnabled()) {
        // Render renditions via web-optimized image delivery: build externalized URL
        url = buildUriTemplateWebOptimizedImageDelivery(type, cropDimension, rotation, damContext);
      }
      if (url == null) {
        // Render renditions in AEM: build externalized URL
        url = buildUriTemplateDam(type, rendition, cropDimension, rotation, damContext);
      }
    }
    this.uriTemplate = url;

    if (validatedDimension == null) {
      validatedDimension = dimension;
    }
    this.dimension = validatedDimension;
  }

  private static String buildUriTemplateDam(@NotNull UriTemplateType type, @NotNull Rendition rendition,
      @Nullable CropDimension cropDimension, @Nullable Integer rotation,
      @NotNull DamContext damContext) {

    // build rendition URL with dummy width/height parameters (otherwise externalization will fail)
    MediaArgs mediaArgs = damContext.getMediaArgs();
    String mediaPath = RenditionMetadata.buildMediaPath(rendition.getPath()
        + "." + ImageFileServlet.buildSelectorString(DUMMY_WIDTH, DUMMY_HEIGHT, cropDimension, rotation, false)
        + "." + MediaFileServlet.EXTENSION,
        ImageFileServlet.getImageFileName(damContext.getAsset().getName(), mediaArgs.getEnforceOutputFileExtension()));
    UrlHandler urlHandler = AdaptTo.notNull(damContext, UrlHandler.class);
    String url = urlHandler.get(mediaPath).urlMode(mediaArgs.getUrlMode())
        .buildExternalResourceUrl(damContext.getAsset().adaptTo(Resource.class));

    // replace dummy width/height parameters with actual placeholders
    switch (type) {
      case CROP_CENTER:
        url = StringUtils.replace(url, Long.toString(DUMMY_WIDTH), URI_TEMPLATE_PLACEHOLDER_WIDTH);
        url = StringUtils.replace(url, Long.toString(DUMMY_HEIGHT), URI_TEMPLATE_PLACEHOLDER_HEIGHT);
        break;
      case SCALE_WIDTH:
        url = StringUtils.replace(url, Long.toString(DUMMY_WIDTH), URI_TEMPLATE_PLACEHOLDER_WIDTH);
        url = StringUtils.replace(url, Long.toString(DUMMY_HEIGHT), "0");
        break;
      case SCALE_HEIGHT:
        url = StringUtils.replace(url, Long.toString(DUMMY_WIDTH), "0");
        url = StringUtils.replace(url, Long.toString(DUMMY_HEIGHT), URI_TEMPLATE_PLACEHOLDER_HEIGHT);
        break;
      default:
        throw new IllegalArgumentException("Unsupported type: " + type);
    }
    return url;
  }

  private static String buildUriTemplateWebOptimizedImageDelivery(@NotNull UriTemplateType type,
      @Nullable CropDimension cropDimension, @Nullable Integer rotation, @NotNull DamContext damContext) {
    // scale by height is not supported by Web-Optimized Image Delivery
    if (type == UriTemplateType.SCALE_HEIGHT) {
      return null;
    }

    // build rendition URL with dummy width/height parameters (otherwise API call will fail)
    String url = damContext.getWebOptimizedImageDeliveryUrl(new WebOptimizedImageDeliveryParams()
        .width(DUMMY_WIDTH).cropDimension(cropDimension).rotation(rotation));
    if (url == null) {
      return null;
    }

    // replace dummy width/height parameters with actual placeholders
    switch (type) {
      case CROP_CENTER:
        url = StringUtils.replace(url, Long.toString(DUMMY_WIDTH), URI_TEMPLATE_PLACEHOLDER_WIDTH);
        break;
      case SCALE_WIDTH:
        url = StringUtils.replace(url, Long.toString(DUMMY_WIDTH), URI_TEMPLATE_PLACEHOLDER_WIDTH);
        break;
      default:
        throw new IllegalArgumentException("Unsupported type for Web-optimized image delivery: " + type);
    }
    return url;
  }

  private static @Nullable String buildUriTemplateDynamicMedia(@NotNull UriTemplateType type,
      @Nullable CropDimension cropDimension, @Nullable Integer rotation, @Nullable NamedDimension smartCropDef,
      @NotNull DamContext damContext) {
    String productionAssetUrl = damContext.getDynamicMediaServerUrl();
    if (productionAssetUrl == null) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    result.append(productionAssetUrl).append(DynamicMediaPath.buildImage(damContext));

    // build DM URL with smart cropping
    if (smartCropDef != null) {
      result.append("%3A").append(smartCropDef.getName()).append("?")
          .append(getDynamicMediaWidthHeightParameters(type))
          .append("&fit=constrain");
      return result.toString();
    }

    // build DM URL without smart cropping
    result.append("?");
    if (cropDimension != null) {
      result.append("crop=").append(cropDimension.getCropStringWidthHeight()).append("&");
    }
    if (rotation != null) {
      result.append("rotate=").append(rotation).append("&");
    }
    result.append(getDynamicMediaWidthHeightParameters(type));
    return result.toString();
  }

  private static String getDynamicMediaWidthHeightParameters(UriTemplateType type) {
    switch (type) {
      case CROP_CENTER:
        return "wid=" + URI_TEMPLATE_PLACEHOLDER_WIDTH + "&hei=" + URI_TEMPLATE_PLACEHOLDER_HEIGHT + "&fit=crop";
      case SCALE_WIDTH:
        return "wid=" + URI_TEMPLATE_PLACEHOLDER_WIDTH;
      case SCALE_HEIGHT:
        return "hei=" + URI_TEMPLATE_PLACEHOLDER_HEIGHT;
      default:
        throw new IllegalArgumentException("Unsupported type for Dynamic Media: " + type);
    }
  }

  private static NamedDimension getDynamicMediaSmartCropDef(@Nullable CropDimension cropDimension, @Nullable Integer rotation,
      @Nullable Double ratio, @NotNull DamContext damContext) {
    if (SmartCrop.canApply(cropDimension, rotation) && ratio != null) {
      // check for matching image profile and use predefined cropping preset if match found
      return SmartCrop.getDimensionForRatio(damContext.getImageProfile(), ratio);
    }
    return null;
  }

  @Override
  public @NotNull UriTemplateType getType() {
    return type;
  }

  @Override
  public @NotNull String getUriTemplate() {
    return uriTemplate;
  }

  @Override
  public long getMaxWidth() {
    return dimension.getWidth();
  }

  @Override
  public long getMaxHeight() {
    return dimension.getHeight();
  }

  @Override
  public String toString() {
    return uriTemplate;
  }

}
