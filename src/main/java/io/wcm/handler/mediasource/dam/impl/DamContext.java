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
package io.wcm.handler.mediasource.dam.impl;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.impl.ImageQualityPercentage;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.DynamicMediaSupportService;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.ImageProfile;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.NamedDimension;
import io.wcm.handler.mediasource.dam.impl.weboptimized.WebOptimizedImageDeliveryParams;
import io.wcm.handler.mediasource.dam.impl.weboptimized.WebOptimizedImageDeliveryService;

/**
 * Context objects require in DAM support implementation.
 */
public final class DamContext implements Adaptable {

  private final Asset asset;
  private final MediaArgs mediaArgs;
  private final MediaHandlerConfig mediaHandlerConfig;
  private final DynamicMediaSupportService dynamicMediaSupportService;
  private final WebOptimizedImageDeliveryService webOptimizedImageDeliveryService;
  private final Adaptable adaptable;

  private String dynamicMediaObject;
  private String dynamicMediaServerUrl;
  private Dimension dynamicMediaImageSizeLimit;
  private ImageProfile imageProfile;

  private static final ImageProfile NO_IMAGE_PROFILE = new ImageProfile() {
    @Override
    public @NotNull List<NamedDimension> getSmartCropDefinitions() {
      return Collections.emptyList();
    }
  };

  /**
   * @param asset DAM asset
   * @param mediaArgs Media Args from media request
   * @param mediaHandlerConfig Media handler config
   * @param dynamicMediaSupportService Dynamic media support service
   * @param webOptimizedImageDeliveryService Web optimized image delivery service
   * @param adaptable Adaptable from current context
   */
  public DamContext(@NotNull Asset asset, @NotNull MediaArgs mediaArgs, @NotNull MediaHandlerConfig mediaHandlerConfig,
      @NotNull DynamicMediaSupportService dynamicMediaSupportService,
      @NotNull WebOptimizedImageDeliveryService webOptimizedImageDeliveryService,
      @NotNull Adaptable adaptable) {
    this.asset = asset;
    this.mediaArgs = mediaArgs;
    this.mediaHandlerConfig = mediaHandlerConfig;
    this.dynamicMediaSupportService = dynamicMediaSupportService;
    this.webOptimizedImageDeliveryService = webOptimizedImageDeliveryService;
    this.adaptable = adaptable;
  }

  /**
   * @return DAM asset
   */
  public Asset getAsset() {
    return asset;
  }

  /**
   * @return Media Args from media request
   */
  public MediaArgs getMediaArgs() {
    return mediaArgs;
  }

  /**
   * @return Media handler config
   */
  public MediaHandlerConfig getMediaHandlerConfig() {
    return this.mediaHandlerConfig;
  }

  /**
   * @return Whether dynamic media is enabled on this AEM instance
   */
  public boolean isDynamicMediaEnabled() {
    // check that DM is not disabled globally
    return dynamicMediaSupportService.isDynamicMediaEnabled()
        // check that DM capability is enabled for the given asset
        && dynamicMediaSupportService.isDynamicMediaCapabilityEnabled(isDynamicMediaAsset())
        // ensure DM is not disabled within MediaArgs for this media request
        && !mediaArgs.isDynamicMediaDisabled();
  }

  /**
   * @return Whether a transparent fallback to Media Handler-based rendering of renditions is allowed
   *         if the appropriate Dynamic Media metadata is not preset for an asset.
   */
  public boolean isDynamicMediaAemFallbackDisabled() {
    return dynamicMediaSupportService.isAemFallbackDisabled();
  }

  /**
   * @return Dynamic media object identifier (value of dam:scene7File property).
   */
  public @Nullable String getDynamicMediaObject() {
    if (dynamicMediaObject == null) {
      dynamicMediaObject = asset.getMetadataValueFromJcr(Scene7Constants.PN_S7_FILE);
    }
    return dynamicMediaObject;
  }

  /**
   * @return true if the DAM asset from this context has dynamic media metadata applied.
   */
  public boolean isDynamicMediaAsset() {
    return StringUtils.isNotBlank(getDynamicMediaObject());
  }

  /**
   * @return Get scene7 host for publish environment. Empty string if author preview mode is active.
   */
  public @Nullable String getDynamicMediaServerUrl() {
    if (dynamicMediaServerUrl == null) {
      dynamicMediaServerUrl = dynamicMediaSupportService.getDynamicMediaServerUrl(asset, mediaArgs.getUrlMode(), adaptable);
    }
    return dynamicMediaServerUrl;
  }

  /**
   * @return Whether to validate that the renditions defined via smart cropping fulfill the requested image width/height
   *         to avoid upscaling or white borders.
   */
  public boolean isDynamicMediaValidateSmartCropRenditionSizes() {
    return dynamicMediaSupportService.isValidateSmartCropRenditionSizes();
  }

  /**
   * @return Whether to control image quality for lossy output formats for each media request via 'qlt' URL parameter
   *         (instead of relying on default setting within Dynamic Media).
   */
  public boolean isDynamicMediaSetImageQuality() {
    return dynamicMediaSupportService.isSetImageQuality();
  }

  /**
   * @return Default response image format. If empty, the default setting that is configured on the Dynamic Media server
   *         environment is used. Accepts the same values as the 'fmt' parameter from the Dynamic Media Image Service
   *         API.
   */
  public @NotNull String getDynamicMediaDefaultFmt() {
    return dynamicMediaSupportService.getDefaultFmt();
  }

  /**
   * @return Default response image format for source images that may have an alpha channel (e.g. for PNG). Accepts the
   *         same values as the 'fmt' parameter from the Dynamic Media Image Service API.
   */
  public @NotNull String getDynamicMediaDefaultFmtAlpha() {
    return dynamicMediaSupportService.getDefaultFmtAlpha();
  }

  /**
   * @return Dynamic media reply image size limit
   */
  public @NotNull Dimension getDynamicMediaImageSizeLimit() {
    if (dynamicMediaImageSizeLimit == null) {
      dynamicMediaImageSizeLimit = dynamicMediaSupportService.getImageSizeLimit();
    }
    return dynamicMediaImageSizeLimit;
  }

  /**
   * Get image profile for current DAM asset.
   * @return Image profile or null if none associated/found
   */
  public @Nullable ImageProfile getImageProfile() {
    if (imageProfile == null) {
      imageProfile = dynamicMediaSupportService.getImageProfileForAsset(asset);
      if (imageProfile == null) {
        imageProfile = NO_IMAGE_PROFILE;
      }
    }
    if (imageProfile == NO_IMAGE_PROFILE) {
      return null;
    }
    else {
      return imageProfile;
    }
  }

  /**
   * @return Whether web-optimized image delivery is enabled on this AEM instance
   */
  public boolean isWebOptimizedImageDeliveryEnabled() {
    return webOptimizedImageDeliveryService.isEnabled();
  }

  /**
   * Get web-optimized image delivery URL for a rendition of an asset.
   * @param params Parameters
   * @return Delivery URL or null if not supported or not enabled
   */
  public @Nullable String getWebOptimizedImageDeliveryUrl(@NotNull WebOptimizedImageDeliveryParams params) {

    // set image quality.
    params.quality(ImageQualityPercentage.getAsInteger(this.mediaArgs, this.mediaHandlerConfig));

    return webOptimizedImageDeliveryService.getDeliveryUrl(asset, params);
  }

  /**
   * @return Resource resolver from current context
   */
  public @NotNull ResourceResolver getResourceResolver() {
    if (adaptable instanceof Resource) {
      return ((Resource)adaptable).getResourceResolver();
    }
    else if (adaptable instanceof SlingHttpServletRequest) {
      return ((SlingHttpServletRequest)adaptable).getResourceResolver();
    }
    else {
      throw new IllegalStateException("Adaptable is neither Resoucre nor SlingHttpServletRequest");
    }
  }

  @Override
  public <AdapterType> @Nullable AdapterType adaptTo(@NotNull Class<AdapterType> type) {
    return adaptable.adaptTo(type);
  }

}
