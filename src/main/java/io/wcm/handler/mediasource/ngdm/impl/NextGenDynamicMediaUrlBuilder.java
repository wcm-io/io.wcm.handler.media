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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Rendition;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Builds URL to render image rendition via NextGen Dynamic Media.
 */
public final class NextGenDynamicMediaUrlBuilder {

  static final String PLACEHOLDER_ASSET_ID = "{asset-id}";
  static final String PLACEHOLDER_SEO_NAME = "{seo-name}";
  static final String PLACEHOLDER_FORMAT = "{format}";

  static final String PARAM_PREFER_WEBP = "preferwebp";
  static final String PARAM_WIDTH = "width";
  static final String PARAM_CROP = "crop";
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
  public NextGenDynamicMediaUrlBuilder(@NotNull NextGenDynamicMediaContext context) {
    this.context = context;
  }

  /**
   * Builds the URL for a rendition.
   * @param rendition Rendition
   * @param params Parameters
   * @return URL or null if invalid/not possible
   */
  public @Nullable String build(@NotNull Rendition rendition, @NotNull NextGenDynamicMediaImageDeliveryParams params) {

    // get parameters from nextgen dynamic media config for URL parameters
    String repositoryId = context.getNextGenDynamicMediaConfig().getRepositoryId();
    String imageDeliveryPath = context.getNextGenDynamicMediaConfig().getImageDeliveryBasePath();
    if (StringUtils.isAnyEmpty(repositoryId, imageDeliveryPath)) {
      return null;
    }

    // prepare URL params
    Long width = params.getWidth();
    CropDimension cropDimension = params.getCropDimension();
    Integer rotation = params.getRotation();
    Integer quality = params.getQuality();

    SortedMap<String, String> urlParams = new TreeMap<>();
    urlParams.put(PARAM_PREFER_WEBP, "true");
    if (width != null) {
      urlParams.put(PARAM_WIDTH, width.toString());
    }
    // TODO: different support for cropping?
    if (cropDimension != null) {
      urlParams.put(PARAM_CROP, cropDimension.getCropStringWidthHeight());
    }
    if (rotation != null && rotation != 0) {
      urlParams.put(PARAM_ROTATE, rotation.toString());
    }
    if (quality != null) {
      urlParams.put(PARAM_QUALITY, quality.toString());
    }

    // replace placeholders in image delivery path
    String seoName = FilenameUtils.getBaseName(rendition.getFileName());
    String format = StringUtils.toRootLowerCase(rendition.getFileExtension());
    if (!SUPPORTED_FORMATS.contains(format)) {
      format = FileExtension.JPEG;
    }
    imageDeliveryPath = StringUtils.replace(imageDeliveryPath, PLACEHOLDER_ASSET_ID, context.getReference().getAssetId());
    imageDeliveryPath = StringUtils.replace(imageDeliveryPath, PLACEHOLDER_SEO_NAME, seoName);
    imageDeliveryPath = StringUtils.replace(imageDeliveryPath, PLACEHOLDER_FORMAT, format);

    // build URL
    StringBuilder url = new StringBuilder();
    url.append("https://")
        .append(repositoryId)
        .append(imageDeliveryPath);
    boolean firstParam = true;
    for (Map.Entry<String, String> entry : urlParams.entrySet()) {
      if (firstParam) {
        url.append("?");
        firstParam = false;
      }
      else {
        url.append("&");
      }
      url.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
          .append("=")
          .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
    }
    return url.toString();
  }

}
