/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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

import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaFileType;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.url.UrlHandler;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.caching.ModificationDate;

/**
 * {@link Rendition} implementation for DAM asset renditions.
 */
class DamRendition extends SlingAdaptable implements Rendition {

  private final DamContext damContext;
  private final MediaArgs mediaArgs;
  private final RenditionMetadata rendition;
  private boolean fallback;

  private static final Logger log = LoggerFactory.getLogger(DamRendition.class);

  /**
   * @param cropDimension Crop dimension
   * @param mediaArgs Media args
   * @param damContext DAM context objects
   */
  DamRendition(CropDimension cropDimension, Integer rotation, MediaArgs mediaArgs, DamContext damContext) {
    this.damContext = damContext;
    this.mediaArgs = mediaArgs;
    RenditionMetadata resolvedRendition = null;

    // if no transformation parameters are given find non-transformed matching rendition
    if (cropDimension == null && rotation == null) {
      RenditionHandler renditionHandler = new DefaultRenditionHandler(damContext);
      resolvedRendition = renditionHandler.getRendition(mediaArgs);
    }

    else {
      // try to match with all transformations that are configured
      RenditionHandler renditionHandler = new TransformedRenditionHandler(cropDimension, rotation, damContext);
      resolvedRendition = renditionHandler.getRendition(mediaArgs);

      // if no match was found check against renditions without applying the explicit cropping
      if (resolvedRendition == null && cropDimension != null) {
        if (rotation != null) {
          renditionHandler = new TransformedRenditionHandler(null, rotation, damContext);
          resolvedRendition = renditionHandler.getRendition(mediaArgs);
        }
        else {
          renditionHandler = new DefaultRenditionHandler(damContext);
          resolvedRendition = renditionHandler.getRendition(mediaArgs);
        }
        if (resolvedRendition != null) {
          fallback = true;
        }
      }
    }

    // if no match was found and auto-cropping is enabled, try to build a transformed rendition
    // with automatically devised cropping parameters
    if (resolvedRendition == null && mediaArgs.isAutoCrop()) {
      DamAutoCropping autoCropping = new DamAutoCropping(damContext, mediaArgs);
      List<CropDimension> autoCropDimensions = autoCropping.calculateAutoCropDimensions();
      for (CropDimension autoCropDimension : autoCropDimensions) {
        RenditionHandler renditionHandler = new TransformedRenditionHandler(autoCropDimension, rotation, damContext);
        resolvedRendition = renditionHandler.getRendition(mediaArgs);
        if (resolvedRendition != null) {
          break;
        }
      }
    }

    if (log.isTraceEnabled()) {
      log.trace("DamRendition: resolvedRendition={}, mediaArgs={}, cropDimension={}, rotation={}",
          resolvedRendition, mediaArgs, cropDimension, rotation);
    }

    this.rendition = resolvedRendition;
  }

  @Override
  public String getUrl() {
    if (rendition == null) {
      return null;
    }
    String url = null;

    // check for dynamic media support
    if (damContext.isDynamicMediaEnabled()) {
      if (damContext.isDynamicMediaAsset()) {
        url = buildDynamicMediaUrl();
        if (url == null) {
          // asset is valid DM asset, but no valid rendition could be generated
          // reason might be that the smart-cropped rendition was too small for the requested size
          return null;
        }
      }
      else {
        // DM is enabled, but given asset is not a DM asset
        if (damContext.isDynamicMediaAemFallbackDisabled()) {
          log.warn("Asset is not a valid DM asset, fallback disabled, rendition invalid: {}", rendition.getRendition().getPath());
          return null;
        }
        else {
          log.trace("Asset is not a valid DM asset, fallback to AEM-rendered rendition: {}", rendition.getRendition().getPath());
        }
      }
    }

    // check for web-optimized image delivery
    if (url == null) {
      url = buildWebOptimizedImageDeliveryUrl();
    }

    // Fallback: Render renditions in AEM - build externalized URL
    if (url == null) {
      UrlHandler urlHandler = AdaptTo.notNull(damContext, UrlHandler.class);
      String mediaPath = rendition.getMediaPath(mediaArgs.isContentDispositionAttachment());
      url = urlHandler.get(mediaPath).urlMode(mediaArgs.getUrlMode())
          .buildExternalResourceUrl(rendition.adaptTo(Resource.class));
    }

    return url;
  }

  /**
   * Build DM URL for this rendition based on the calculated DM path and the configured DM hostname.
   * @return DM URL or null if either DM path or configured DM hostname is null
   */
  private @Nullable String buildDynamicMediaUrl() {
    String dynamicMediaPath = rendition.getDynamicMediaPath(mediaArgs.isContentDispositionAttachment(), damContext);
    String productionAssetUrl = damContext.getDynamicMediaServerUrl();
    if (dynamicMediaPath != null && productionAssetUrl != null) {
      return productionAssetUrl + dynamicMediaPath;
    }
    else {
      return null;
    }
  }

  /**
   * Build web-optimized image delivery URL if this is a raster image.
   * @return URL or null
   */
  private @Nullable String buildWebOptimizedImageDeliveryUrl() {
    if (MediaFileType.isImage(getFileExtension())
        && !MediaFileType.isVectorImage(getFileExtension())
        && !mediaArgs.isContentDispositionAttachment()
        && !mediaArgs.isWebOptimizedImageDeliveryDisabled()) {
      return rendition.getWebOptimizedImageDeliveryPath(damContext);
    }
    else {
      return null;
    }
  }


  @Override
  public String getPath() {
    if (this.rendition != null) {
      return this.rendition.getRendition().getPath();
    }
    else {
      return null;
    }
  }

  @Override
  public String getFileName() {
    if (this.rendition != null) {
      return this.rendition.getFileName(this.mediaArgs.isContentDispositionAttachment());
    }
    else {
      return null;
    }
  }

  @Override
  public String getFileExtension() {
    return FilenameUtils.getExtension(getFileName());
  }

  @Override
  public long getFileSize() {
    if (this.rendition != null) {
      return this.rendition.getFileSize();
    }
    else {
      return 0L;
    }
  }

  @Override
  public String getMimeType() {
    if (this.rendition != null) {
      return this.rendition.getMimeType();
    }
    else {
      return null;
    }
  }

  @Override
  public Date getModificationDate() {
    if (this.rendition != null) {
      return ModificationDate.get(this.rendition.getRendition().adaptTo(Resource.class));
    }
    else {
      return null;
    }
  }

  @Override
  public MediaFormat getMediaFormat() {
    if (this.rendition != null) {
      return this.rendition.getMediaFormat();
    }
    else {
      return null;
    }
  }

  @Override
  @SuppressWarnings("null")
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public @NotNull ValueMap getProperties() {
    if (this.rendition != null) {
      return this.rendition.getRendition().adaptTo(Resource.class).getValueMap();
    }
    else {
      return ValueMap.EMPTY;
    }
  }

  @Override
  public boolean isImage() {
    return MediaFileType.isImage(getFileExtension());
  }

  @Override
  public boolean isBrowserImage() {
    return MediaFileType.isBrowserImage(getFileExtension());
  }

  @Override
  public boolean isVectorImage() {
    return MediaFileType.isVectorImage(getFileExtension());
  }

  @Override
  public boolean isDownload() {
    return !isImage();
  }

  @Override
  public long getWidth() {
    if (this.rendition != null) {
      return this.rendition.getWidth();
    }
    else {
      return 0;
    }
  }

  @Override
  public long getHeight() {
    if (this.rendition != null) {
      return this.rendition.getHeight();
    }
    else {
      return 0;
    }
  }

  @Override
  public boolean isFallback() {
    return fallback;
  }

  @Override
  public @NotNull UriTemplate getUriTemplate(@NotNull UriTemplateType type) {
    if (this.rendition == null) {
      throw new IllegalStateException("Rendition is not valid.");
    }
    if (type == UriTemplateType.CROP_CENTER) {
      throw new IllegalArgumentException("CROP_CENTER not supported for rendition URI templates.");
    }
    return this.rendition.getUriTemplate(type, damContext);
  }

  @Override
  @SuppressWarnings("null")
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (this.rendition != null) {
      AdapterType result = this.rendition.adaptTo(type);
      if (result != null) {
        return result;
      }
    }
    return super.adaptTo(type);
  }

  @Override
  public String toString() {
    if (rendition != null) {
      return rendition.toString();
    }
    return super.toString();
  }

}
