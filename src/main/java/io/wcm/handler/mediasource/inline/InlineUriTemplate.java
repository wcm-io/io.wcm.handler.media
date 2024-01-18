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
package io.wcm.handler.mediasource.inline;

import static io.wcm.handler.media.MediaNameConstants.URI_TEMPLATE_PLACEHOLDER_HEIGHT;
import static io.wcm.handler.media.MediaNameConstants.URI_TEMPLATE_PLACEHOLDER_WIDTH;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.impl.ImageFileServlet;
import io.wcm.handler.media.impl.ImageFileServletSelector;
import io.wcm.handler.media.impl.JcrBinary;
import io.wcm.handler.media.impl.MediaFileServlet;
import io.wcm.handler.url.UrlHandler;
import io.wcm.sling.commons.adapter.AdaptTo;

final class InlineUriTemplate implements UriTemplate {

  private final String uriTemplate;
  private final UriTemplateType type;
  private final Dimension dimension;

  @SuppressWarnings("java:S107") // allow more than 7 params
  InlineUriTemplate(@NotNull UriTemplateType type, @NotNull Dimension dimension,
      @NotNull Resource resource, @NotNull String fileName,
      @Nullable CropDimension cropDimension, @Nullable Integer rotation,
      @NotNull MediaArgs mediaArgs, @NotNull Adaptable adaptable) {
    this.uriTemplate = buildUriTemplate(type, resource, fileName, cropDimension, rotation, mediaArgs, adaptable);
    this.type = type;
    this.dimension = dimension;
  }

  @SuppressWarnings("java:S1075") // not a file path
  private static String buildUriTemplate(@NotNull UriTemplateType type, @NotNull Resource resource,
      @NotNull String fileName, @Nullable CropDimension cropDimension, @Nullable Integer rotation,
      @NotNull MediaArgs mediaArgs, @NotNull Adaptable adaptable) {
    String resourcePath = resource.getPath();

    // if parent resource is a nt:file resource, use this one as path for scaled image
    Resource parentResource = resource.getParent();
    if (parentResource != null && JcrBinary.isNtFile(parentResource)) {
      resourcePath = parentResource.getPath();
    }

    // URL to render scaled image via {@link InlineRenditionServlet}
    final long DUMMY_WIDTH = 999991;
    final long DUMMY_HEIGHT = 999992;
    String path = resourcePath
        + "." + ImageFileServletSelector.build(DUMMY_WIDTH, DUMMY_HEIGHT, cropDimension, rotation,
            mediaArgs.getImageQualityPercentage(), false)
        + "." + MediaFileServlet.EXTENSION
        // replace extension based on the format supported by ImageFileServlet for rendering for this rendition
        + "/" + ImageFileServlet.getImageFileName(fileName, mediaArgs.getEnforceOutputFileExtension());

    // build externalized URL
    UrlHandler urlHandler = AdaptTo.notNull(adaptable, UrlHandler.class);
    String url = urlHandler.get(path).urlMode(mediaArgs.getUrlMode()).buildExternalResourceUrl(resource);

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
