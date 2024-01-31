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

import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService.PLACEHOLDER_ASSET_ID;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService.PLACEHOLDER_FORMAT;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService.PLACEHOLDER_SEO_NAME;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.Dimension;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Builds URL to render image rendition via NextGen Dynamic Media.
 * <p>
 * Example URL that might be build:
 * https://host/adobe/dynamicmedia/deliver/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/my-image.jpg?preferwebp=true&quality=85&width=300&crop=16:9,smart
 * </p>
 */
public final class NextGenDynamicMediaImageUrlBuilder {

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
  public NextGenDynamicMediaImageUrlBuilder(@NotNull NextGenDynamicMediaContext context) {
    this.context = context;
  }

  /**
   * Builds the URL for a rendition.
   * @param params Parameters
   * @return URL or null if invalid/not possible
   */
  public @Nullable String build(@NotNull NextGenDynamicMediaImageDeliveryParams params) {

    // get parameters from nextgen dynamic media config for URL parameters
    String repositoryId = context.getNextGenDynamicMediaConfig().getRepositoryId();
    String imageDeliveryPath = context.getNextGenDynamicMediaConfig().getImageDeliveryBasePath();
    if (StringUtils.isAnyEmpty(repositoryId, imageDeliveryPath)) {
      return null;
    }

    // replace placeholders in delivery path
    String seoName = FilenameUtils.getBaseName(context.getReference().getFileName());
    String format = context.getDefaultMediaArgs().getEnforceOutputFileExtension();
    if (StringUtils.isEmpty(format)) {
      format = StringUtils.toRootLowerCase(FilenameUtils.getExtension(context.getReference().getFileName()));
    }
    if (!SUPPORTED_FORMATS.contains(format)) {
      format = FileExtension.JPEG;
    }
    imageDeliveryPath = StringUtils.replace(imageDeliveryPath, PLACEHOLDER_ASSET_ID, context.getReference().getAssetId());
    imageDeliveryPath = StringUtils.replace(imageDeliveryPath, PLACEHOLDER_SEO_NAME, seoName);
    imageDeliveryPath = StringUtils.replace(imageDeliveryPath, PLACEHOLDER_FORMAT, format);

    // prepare URL params
    Long width = params.getWidth();
    String widthPlaceholder = params.getWidthPlaceholder();
    Dimension cropSmartRatio = params.getCropSmartRatio();
    Integer rotation = params.getRotation();
    Integer quality = params.getQuality();

    SortedMap<String, String> urlParamMap = new TreeMap<>();
    urlParamMap.put(PARAM_PREFER_WEBP, "true");
    if (widthPlaceholder != null) {
      urlParamMap.put(PARAM_WIDTH, widthPlaceholder);
    }
    else if (width != null) {
      urlParamMap.put(PARAM_WIDTH, width.toString());
    }
    if (cropSmartRatio != null) {
      urlParamMap.put(PARAM_CROP, cropSmartRatio.getWidth() + ":" + cropSmartRatio.getHeight() + ",smart");
    }
    if (rotation != null && rotation != 0) {
      urlParamMap.put(PARAM_ROTATE, rotation.toString());
    }
    if (quality != null) {
      urlParamMap.put(PARAM_QUALITY, quality.toString());
    }

    // build URL
    StringBuilder url = new StringBuilder();
    url.append("https://")
        .append(repositoryId)
        .append(imageDeliveryPath);
    String urlParams = urlParamMap.entrySet().stream()
        .map(entry -> toUrlParam(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining("&"));
    if (StringUtils.isNotEmpty(urlParams)) {
      url.append("?").append(urlParams);
    }
    return url.toString();
  }

  private static @NotNull String toUrlParam(@NotNull String key, @NotNull String value) {
    StringBuilder sb = new StringBuilder();
    sb.append(key).append("=");
    // we only need to encode crop, all other parameters are numbers only
    if (StringUtils.equals(key, PARAM_CROP)) {
      sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
    else {
      sb.append(value);
    }
    return sb.toString();
  }

}
