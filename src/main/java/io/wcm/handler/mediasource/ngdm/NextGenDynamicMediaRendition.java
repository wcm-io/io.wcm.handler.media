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
package io.wcm.handler.mediasource.ngdm;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaFileType;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.Ratio;
import io.wcm.handler.media.impl.ImageQualityPercentage;
import io.wcm.handler.mediasource.ngdm.impl.MediaArgsDimension;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaBinaryUrlBuilder;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaContext;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaImageDeliveryParams;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaImageUrlBuilder;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReference;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaVideoUrlBuilder;
import io.wcm.handler.mediasource.ngdm.impl.metadata.NextGenDynamicMediaMetadata;

/**
 * {@link Rendition} implementation for Next Gen. Dynamic Media remote assets.
 */
final class NextGenDynamicMediaRendition implements Rendition {

  private final NextGenDynamicMediaContext context;
  private final NextGenDynamicMediaMetadata metadata;
  private final Dimension originalDimension;
  private final NextGenDynamicMediaReference reference;
  private final MediaArgs mediaArgs;
  private final String url;
  private MediaFormat resolvedMediaFormat;
  private long requestedWidth;
  private long requestedHeight;
  private long width;
  private long height;
  private String fileExtension;
  private final String originalFileExtension;
  private String videoManifestFormat;

  private static final Logger log = LoggerFactory.getLogger(NextGenDynamicMediaRendition.class);

  NextGenDynamicMediaRendition(@NotNull NextGenDynamicMediaContext context, @NotNull MediaArgs mediaArgs) {
    this.context = context;
    this.metadata = context.getMetadata();
    if (this.metadata != null) {
      this.originalDimension = metadata.getDimension();
    }
    else {
      this.originalDimension = null;
    }
    this.reference = context.getReference();
    this.mediaArgs = mediaArgs;
    this.requestedWidth = mediaArgs.getFixedWidth();
    this.requestedHeight = mediaArgs.getFixedHeight();

    // set first media format as resolved format - because only the first is supported
    MediaFormat firstMediaFormat = MediaArgsDimension.getFirstMediaFormat(mediaArgs);
    if (firstMediaFormat != null) {
      this.resolvedMediaFormat = firstMediaFormat;
      if (this.requestedWidth == 0) {
        this.requestedWidth = firstMediaFormat.getEffectiveMinWidth();
        this.requestedHeight = firstMediaFormat.getEffectiveMinHeight();
      }
    }

    this.originalFileExtension = FilenameUtils.getExtension(reference.getFileName());
    this.fileExtension = mediaArgs.getEnforceOutputFileExtension();
    if (StringUtils.isEmpty(this.fileExtension)) {
      this.fileExtension = this.originalFileExtension;
    }

    if (mediaArgs.isDownload()) {
      // force binary download regardless of type
      this.url = buildBinaryUrl();
    }
    else if (isVideo()) {
      // video handling with adaptive streaming
      setVideoDimensionsFromMetadata();
      this.url = buildVideoUrl();
    }
    else if (!isImage()) {
      // generic binary (PDF, etc.)
      this.url = buildBinaryUrl();
    }
    else if (isVectorImage()) {
      // calculate width/height for rendition metadata
      calculateWidthHeightVectorImage();
      // deliver as binary
      this.url = buildBinaryUrl();
    }
    else {
      // calculate width/height for rendition metadata
      calculateWidthHeight();
      if (isRequestedDimensionLargerThanOriginal()) {
        // image upscaling is not supported
        this.url = null;
      }
      else {
        // deliver scaled image rendition
        this.url = buildImageRenditionUrl();
        this.fileExtension = new NextGenDynamicMediaImageUrlBuilder(context).getFileExtension();
      }
    }
  }

  /**
   * Recalculates width and/or height based on requested media format, ratio and original dimensions.
   */
  private void calculateWidthHeight() {
    double requestedRatio = MediaArgsDimension.getRequestedRatio(mediaArgs);

    // use given width/height if fixed dimension is requested
    if (requestedWidth > 0 && requestedHeight > 0) {
      this.width = requestedWidth;
      this.height = requestedHeight;
    }

    // set original sizes if not width/height is requested
    else if (this.requestedWidth == 0 && this.requestedHeight == 0 && this.originalDimension != null) {
      this.width = this.originalDimension.getWidth();
      this.height = this.originalDimension.getHeight();
    }

    // calculate height if only width is requested
    else if (this.requestedWidth > 0 && this.requestedHeight == 0) {
      this.width = requestedWidth;
      if (requestedRatio > 0) {
        this.height = Math.round(this.requestedWidth / requestedRatio);
        this.requestedHeight = this.height;
      }
      else if (originalDimension != null) {
        this.height = Math.round(this.requestedWidth / Ratio.get(originalDimension));
      }
    }

    // calculate width if only height is requested
    else if (this.requestedHeight > 0 && this.requestedWidth == 0) {
      this.height = requestedHeight;
      if (requestedRatio > 0) {
        this.width = Math.round(this.requestedHeight * requestedRatio);
        this.requestedWidth = this.width;
      }
      else if (originalDimension != null) {
        this.width = Math.round(this.requestedHeight * Ratio.get(originalDimension));
      }
    }
  }

  /**
   * Recalculates width and/or height based on requested media format, ratio and original dimensions.
   * For vector images, original dimensions are used, if present.
   */
  private void calculateWidthHeightVectorImage() {
    if (this.originalDimension != null) {
      this.width = this.originalDimension.getWidth();
      this.height = this.originalDimension.getHeight();
    }
    else {
      calculateWidthHeight();
    }
  }

  /**
   * Set width/height from metadata for videos.
   * Video dimensions reflect the original asset size and are not used for URL building
   * (videos use adaptive streaming, not server-side scaling).
   */
  private void setVideoDimensionsFromMetadata() {
    if (this.originalDimension != null) {
      this.width = this.originalDimension.getWidth();
      this.height = this.originalDimension.getHeight();
    }
  }

  /**
   * Build image rendition URL which is dynamically scaled and/or cropped.
   */
  private String buildImageRenditionUrl() {
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .rotation(context.getMedia().getRotation())
        .quality(ImageQualityPercentage.getAsInteger(mediaArgs, context.getMediaHandlerConfig()));
    if (this.requestedWidth > 0) {
      params.width(this.requestedWidth);
    }
    if (this.requestedHeight > 0) {
      params.height(this.requestedHeight);
    }
    Dimension ratioDimension = MediaArgsDimension.getRequestedRatioAsWidthHeight(mediaArgs);
    if (ratioDimension != null) {
      params.ratio(ratioDimension);
    }

    return new NextGenDynamicMediaImageUrlBuilder(context).build(params);
  }

  /**
   * Build video URL based on mediaArgs settings. As a side effect, also sets {@link #videoManifestFormat}
   * and {@link #fileExtension} based on the resolved delivery mode.
   * <ul>
   *   <li>If hostedVideoPlayer is true: returns hosted player URL (iframe)</li>
   *   <li>Otherwise: returns streaming manifest URL (HLS/DASH)</li>
   *   <li>Falls back to binary download if configuration is incomplete</li>
   * </ul>
   * @return Video URL
   */
  private String buildVideoUrl() {
    NextGenDynamicMediaVideoUrlBuilder builder = new NextGenDynamicMediaVideoUrlBuilder(context);

    // check if hosted video player is requested
    if (mediaArgs.isHostedVideoPlayer()) {
      String playerUrl = builder.buildPlayerUrl();
      if (playerUrl != null) {
        this.videoManifestFormat = null;
        this.fileExtension = "html";
        return playerUrl;
      }
      // fall through to streaming if player URL unavailable
    }

    // default: streaming manifest URL (HLS/DASH)
    String format = mediaArgs.getVideoManifestFormat();
    if (StringUtils.isBlank(format)) {
      format = context.getNextGenDynamicMediaConfig().getDefaultVideoManifestFormat();
    }
    String manifestUrl = builder.buildManifestUrl(format);
    if (manifestUrl != null) {
      this.videoManifestFormat = format;
      this.fileExtension = format;
      return manifestUrl;
    }

    // fallback: binary download
    this.videoManifestFormat = null;
    this.fileExtension = this.originalFileExtension;
    return buildBinaryUrl();
  }


  /**
   * Checks if the original dimension is available in remote asset metadata, and if that dimension
   * is smaller than the requested width/height. Upscaling should be avoided.
   * @return true if requested dimension is larger than original dimension
   */
  private boolean isRequestedDimensionLargerThanOriginal() {
    if (originalDimension != null
        && (this.requestedWidth > originalDimension.getWidth() || this.requestedHeight > originalDimension.getHeight())) {
      if (log.isTraceEnabled()) {
        log.trace("Requested dimension {} is larger than original image dimension {} of {}",
            new Dimension(this.requestedWidth, this.requestedHeight), originalDimension, context.getReference());
      }
      return true;
    }
    return false;
  }

  /**
   * Build URL which points directly to the binary file.
   */
  private String buildBinaryUrl() {
    return new NextGenDynamicMediaBinaryUrlBuilder(context).build(mediaArgs.isContentDispositionAttachment());
  }

  @Override
  public @Nullable String getUrl() {
    return url;
  }

  @Override
  public @Nullable String getPath() {
    // not supported
    return null;
  }

  @Override
  public @Nullable String getFileName() {
    return reference.getFileName();
  }

  @Override
  public @Nullable String getFileExtension() {
    return this.fileExtension;
  }

  @Override
  public long getFileSize() {
    if (this.metadata != null) {
      Long fileSize = this.metadata.getFileSize();
      if (fileSize != null) {
        return fileSize;
      }
    }
    // file size is unknown
    return -1;
  }

  @Override
  public @Nullable String getMimeType() {
    if (this.videoManifestFormat != null) {
      MediaFileType fileType = MediaFileType.getByFileExtensions(this.videoManifestFormat);
      if (fileType != null) {
        return fileType.getContentTypes().iterator().next();
      }
    }
    if (this.metadata != null) {
      return this.metadata.getMimeType();
    }
    else {
      return context.getMimeTypeService().getMimeType(getFileExtension());
    }
  }

  @Override
  public @Nullable MediaFormat getMediaFormat() {
    return this.resolvedMediaFormat;
  }

  /**
   * For video renditions, provides a "posterUrl" property with the auto-generated thumbnail URL.
   * This is used by {@link io.wcm.handler.mediasource.ngdm.markup.NextGenDynamicMediaVideoMarkupBuilder}
   * to set the poster attribute on the HTML5 video element.
   */
  @Override
  public @NotNull ValueMap getProperties() {
    if (isVideo()) {
      String posterUrl = new NextGenDynamicMediaVideoUrlBuilder(context).buildThumbnailUrl();
      if (posterUrl != null) {
        return new ValueMapDecorator(Collections.singletonMap("posterUrl", posterUrl));
      }
    }
    return ValueMap.EMPTY;
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
  public boolean isVideo() {
    return MediaFileType.isVideo(this.originalFileExtension);
  }

  @Override
  public boolean isDownload() {
    return !isImage() && !isVideo();
  }

  @Override
  public long getWidth() {
    return width;
  }

  @Override
  public long getHeight() {
    return height;
  }

  @Override
  public @Nullable Date getModificationDate() {
    // not supported
    return null;
  }

  @Override
  public boolean isFallback() {
    // not supported
    return false;
  }

  @Override
  public @NotNull UriTemplate getUriTemplate(@NotNull UriTemplateType type) {
    // URI templates are only supported for dynamically scaled raster images
    if (!isImage() || isVectorImage() || isVideo()) {
      throw new UnsupportedOperationException("Unable to build URI template for " + reference.toReference());
    }
    return new NextGenDynamicMediaUriTemplate(context, type);
  }

  @Override
  public <AdapterType> @Nullable AdapterType adaptTo(@NotNull Class<AdapterType> arg0) {
    // not adaption supported
    return null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(Objects.toString(url, "#invalid"));
    if (width > 0 || height > 0) {
      sb.append(" (").append(Long.toString(width)).append("x").append(Long.toString(height)).append(")");
    }
    return sb.toString();
  }

}
