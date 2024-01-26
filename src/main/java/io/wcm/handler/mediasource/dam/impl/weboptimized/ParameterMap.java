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
package io.wcm.handler.mediasource.dam.impl.weboptimized;

import static io.wcm.handler.mediasource.ngdm.impl.SeoNameSanitizer.sanitizeSeoName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;
import io.wcm.wcm.commons.contenttype.FileExtension;

final class ParameterMap {

  static final String PARAM_PATH = "path";
  static final String PARAM_SEO_NAME = "seoname";
  static final String PARAM_FORMAT = "format";

  static final String PARAM_PREFER_WEBP = "preferwebp";
  static final String PARAM_WIDTH = "width";
  static final String PARAM_CROP = "c";
  static final String PARAM_ROTATE = "r";
  static final String PARAM_QUALITY = "quality";

  private static final Set<String> SUPPORTED_FORMATS = Set.of(
      FileExtension.JPEG,
      FileExtension.PNG,
      FileExtension.GIF,
      FileExtension.WEBP);

  private ParameterMap() {
    // static methods only
  }

  @NotNull
  static Map<String, Object> build(@NotNull Asset asset, @NotNull WebOptimizedImageDeliveryParams params) {
    String path = asset.getPath();
    String seoName = FilenameUtils.getBaseName(asset.getName());
    String format = StringUtils.toRootLowerCase(FilenameUtils.getExtension(asset.getName()));

    if (!SUPPORTED_FORMATS.contains(format)) {
      format = FileExtension.JPEG;
    }

    Long width = params.getWidth();
    CropDimension cropDimension = params.getCropDimension();
    Integer rotation = params.getRotation();
    Integer quality = params.getQuality();

    // please note: AssetDelivery API expects all values as strings (although the expected map supports other types)
    Map<String, Object> map = new HashMap<>();
    map.put(PARAM_PATH, path);
    map.put(PARAM_SEO_NAME, sanitizeSeoName(seoName));
    map.put(PARAM_FORMAT, format);
    map.put(PARAM_PREFER_WEBP, "true");
    if (width != null) {
      map.put(PARAM_WIDTH, width.toString());
    }
    if (cropDimension != null) {
      map.put(PARAM_CROP, cropDimension.getCropStringWidthHeight());
    }
    if (rotation != null && rotation != 0) {
      map.put(PARAM_ROTATE, rotation.toString());
    }
    if (quality != null) {
      map.put(PARAM_QUALITY, quality.toString());
    }
    return map;
  }

}
