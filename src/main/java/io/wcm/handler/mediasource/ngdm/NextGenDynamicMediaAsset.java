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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.Asset;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaContext;

/**
 * {@link Asset} implementation for Next Gen. Dynamic Media remote assets.
 */
final class NextGenDynamicMediaAsset implements Asset {

  private final NextGenDynamicMediaContext context;

  NextGenDynamicMediaAsset(@NotNull NextGenDynamicMediaContext context) {
    this.context = context;
  }

  @Override
  public @Nullable String getTitle() {
    return context.getReference().getFileName();
  }

  @Override
  public @Nullable String getAltText() {
    if (context.getDefaultMediaArgs().isDecorative()) {
      return "";
    }
    else {
      return context.getDefaultMediaArgs().getAltText();
    }
  }

  @Override
  public @Nullable String getDescription() {
    // not supported
    return null;
  }

  @Override
  public @Nullable String getPath() {
    return context.getReference().toReference();
  }

  @Override
  public @NotNull ValueMap getProperties() {
    return ValueMap.EMPTY;
  }

  @Override
  public @Nullable Rendition getDefaultRendition() {
    return getRendition(this.context.getDefaultMediaArgs());
  }

  @Override
  public @Nullable Rendition getRendition(@NotNull MediaArgs mediaArgs) {
    Rendition rendition = new NextGenDynamicMediaRendition(context, mediaArgs);

    // check if rendition is valid - otherwise return null
    if (StringUtils.isEmpty(rendition.getUrl())) {
      rendition = null;
    }

    return rendition;
  }

  @Override
  public @Nullable Rendition getImageRendition(@NotNull MediaArgs mediaArgs) {
    Rendition rendition = getRendition(mediaArgs);
    if (rendition != null && rendition.isImage()) {
      return rendition;
    }
    return null;
  }

  @Override
  public @Nullable Rendition getDownloadRendition(@NotNull MediaArgs mediaArgs) {
    Rendition rendition = getRendition(mediaArgs);
    if (rendition != null && rendition.isDownload()) {
      return rendition;
    }
    return null;
  }

  @Override
  public @NotNull UriTemplate getUriTemplate(@NotNull UriTemplateType type) {
    return new NextGenDynamicMediaUriTemplate(context, type);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <AdapterType> @Nullable AdapterType adaptTo(@NotNull Class<AdapterType> type) {
    com.day.cq.dam.api.Asset asset = context.getReference().getAsset();
    if (asset != null) {
      if (type == com.day.cq.dam.api.Asset.class) {
        return (AdapterType)asset;
      }
      if (type == Resource.class) {
        return (AdapterType)asset.adaptTo(Resource.class);
      }
    }
    return null;
  }

}
