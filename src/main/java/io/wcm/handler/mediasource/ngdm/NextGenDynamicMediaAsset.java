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

import static com.day.cq.dam.api.DamConstants.DC_DESCRIPTION;
import static com.day.cq.dam.api.DamConstants.DC_TITLE;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
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
import io.wcm.handler.mediasource.ngdm.impl.metadata.NextGenDynamicMediaMetadata;

/**
 * {@link Asset} implementation for Next Gen. Dynamic Media remote assets.
 */
final class NextGenDynamicMediaAsset implements Asset {

  private final NextGenDynamicMediaContext context;
  private final MediaArgs defaultMediaArgs;
  private final ValueMap properties;

  NextGenDynamicMediaAsset(@NotNull NextGenDynamicMediaContext context) {
    this.context = context;
    this.defaultMediaArgs = context.getDefaultMediaArgs();

    NextGenDynamicMediaMetadata metadata = context.getMetadata();
    if (metadata != null) {
      this.properties = metadata.getProperties();
    }
    else {
      this.properties = ValueMap.EMPTY;
    }
  }

  @Override
  public @Nullable String getTitle() {
    return StringUtils.defaultString(properties.get(DC_TITLE, String.class),
        context.getReference().getFileName());
  }

  @Override
  public @Nullable String getAltText() {
    if (defaultMediaArgs.isDecorative()) {
      return "";
    }
    if (!defaultMediaArgs.isForceAltValueFromAsset() && StringUtils.isNotEmpty(defaultMediaArgs.getAltText())) {
      return defaultMediaArgs.getAltText();
    }
    return StringUtils.defaultString(getDescription(), getTitle());
  }

  @Override
  public @Nullable String getDescription() {
    return properties.get(DC_DESCRIPTION, String.class);
  }

  @Override
  public @Nullable String getPath() {
    return context.getReference().toReference();
  }

  @Override
  public @NotNull ValueMap getProperties() {
    return properties;
  }

  @Override
  public @Nullable Rendition getDefaultRendition() {
    return getRendition(defaultMediaArgs);
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

  @Override
  public String toString() {
    ToStringBuilder sb = new ToStringBuilder(this)
        .append("reference", context.getReference())
        .append("metadata", context.getMetadata());
    com.day.cq.dam.api.Asset asset = context.getReference().getAsset();
    if (asset != null) {
      sb.append("asset", asset.getPath());
    }
    return sb.toString();
  }

}
