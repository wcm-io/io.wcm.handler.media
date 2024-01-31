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

import java.util.Date;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaFileType;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.mediasource.ngdm.impl.ImageQualityPercentage;
import io.wcm.handler.mediasource.ngdm.impl.MediaArgsDimension;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaBinaryUrlBuilder;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaContext;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaImageDeliveryParams;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaImageUrlBuilder;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReference;

/**
 * {@link Rendition} implementation for Next Gen. Dynamic Media remote assets.
 */
final class NextGenDynamicMediaRendition implements Rendition {

  private final NextGenDynamicMediaContext context;
  private final NextGenDynamicMediaReference reference;
  private final MediaArgs mediaArgs;
  private final String url;
  private MediaFormat resolvedMediaFormat;
  private long width;
  private long height;

  NextGenDynamicMediaRendition(@NotNull NextGenDynamicMediaContext context, @NotNull MediaArgs mediaArgs) {
    this.context = context;
    this.reference = context.getReference();
    this.mediaArgs = mediaArgs;
    this.width = mediaArgs.getFixedWidth();
    this.height = mediaArgs.getFixedHeight();

    // set first media format as resolved format - because only the first is supported
    MediaFormat firstMediaFormat = MediaArgsDimension.getFirstMediaFormat(mediaArgs);
    if (firstMediaFormat != null) {
      this.resolvedMediaFormat = firstMediaFormat;
      if (this.width == 0) {
        this.width = firstMediaFormat.getEffectiveMinWidth();
      }
    }

    if (isVectorImage() || !isImage()) {
      // deliver as binary
      this.url = buildBinaryUrl();
    }
    else {
      // deliver scaled image rendition
      this.url = buildImageRenditionUrl();
    }
  }

  /**
   * Build image rendition URL which is dynamically scaled and/or cropped.
   */
  private String buildImageRenditionUrl() {
    // calculate height
    if (this.width > 0) {
      double ratio = MediaArgsDimension.getRequestedRatio(mediaArgs);
      if (ratio > 0) {
        this.height = Math.round(this.width / ratio);
      }
    }

    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .rotation(context.getMedia().getRotation())
        .quality(ImageQualityPercentage.getAsInteger(mediaArgs, context.getMediaHandlerConfig()));
    if (this.width > 0) {
      params.width(this.width);
    }
    Dimension ratioDimension = MediaArgsDimension.getRequestedRatioAsWidthHeight(mediaArgs);
    if (ratioDimension != null) {
      params.cropSmartRatio(ratioDimension);
    }

    return new NextGenDynamicMediaImageUrlBuilder(context).build(params);
  }

  /**
   * Build URL which points directly to the binary file.
   */
  private String buildBinaryUrl() {
    return new NextGenDynamicMediaBinaryUrlBuilder(context).build();
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
    String extension = mediaArgs.getEnforceOutputFileExtension();
    if (StringUtils.isEmpty(extension)) {
      extension = FilenameUtils.getExtension(reference.getFileName());
    }
    return extension;
  }

  @Override
  public long getFileSize() {
    // file size is unknown
    return -1;
  }

  @Override
  public @Nullable String getMimeType() {
    return context.getMimeTypeService().getMimeType(getFileExtension());
  }

  @Override
  public @Nullable MediaFormat getMediaFormat() {
    return this.resolvedMediaFormat;
  }

  @Override
  public @NotNull ValueMap getProperties() {
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
  public boolean isDownload() {
    return !isImage();
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
    if (!isImage() || isVectorImage()) {
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
    return Objects.toString(url, "#invalid");
  }

}
