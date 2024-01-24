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

import org.apache.commons.io.FilenameUtils;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaFileType;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.mediasource.ngdm.impl.ImageQualityPercentage;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaContext;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaImageDeliveryParams;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReference;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaUrlBuilder;

/**
 * {@link Rendition} implementation for Next Gen. Dynamic Media remote assets.
 */
final class NextGenDynamicMediaRendition implements Rendition {

  private final NextGenDynamicMediaContext context;
  private final NextGenDynamicMediaReference reference;
  private final String url;

  NextGenDynamicMediaRendition(@NotNull NextGenDynamicMediaContext context, @NotNull MediaArgs mediaArgs) {
    this.context = context;
    this.reference = context.getReference();

    // TODO: build URL properly
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .rotation(context.getMedia().getRotation())
        .cropDimension(context.getMedia().getCropDimension());

    // set image quality.
    params.quality(ImageQualityPercentage.getAsInteger(mediaArgs, context.getMediaHandlerConfig()));

    this.url = new NextGenDynamicMediaUrlBuilder(context).build(params);
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
    // TODO: selected/forced file name or original file name
    return FilenameUtils.getExtension(reference.getFileName());
  }

  @Override
  public long getFileSize() {
    // file size is unknown
    return -1;
  }

  @Override
  public @Nullable String getMimeType() {
    return context.getMimeTypeService().getExtension(getFileExtension());
  }

  @Override
  public @Nullable MediaFormat getMediaFormat() {
    // TODO: resolved media format
    return null;
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
    // TODO: width/height?
    return 0;
  }

  @Override
  public long getHeight() {
    // TODO: width/height?
    return 0;
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
    return new NextGenDynamicMediaUriTemplate(context, type);
  }

  @Override
  public <AdapterType> @Nullable AdapterType adaptTo(@NotNull Class<AdapterType> arg0) {
    // not adaption supported
    return null;
  }

}
