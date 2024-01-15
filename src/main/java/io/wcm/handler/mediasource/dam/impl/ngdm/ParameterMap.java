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
package io.wcm.handler.mediasource.dam.impl.ngdm;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;

final class ParameterMap {

  static final String PARAM_PATH = "path";
  static final String PARAM_SEO_NAME = "seoname";
  static final String PARAM_FORMAT = "format";

  static final String PARAM_WIDTH = "width";
  static final String PARAM_HEIGHT = "height";
  static final String PARAM_CROP = "c";
  static final String PARAM_ROTATE = "r";
  static final String PARAM_PREFER_WEBP = "preferwebp";

  private ParameterMap() {
    // static methods only
  }

  @NotNull
  static Map<String, Object> build(@NotNull Asset asset, @NotNull WebOptimizedImageDeliveryParams params) {
    String path = asset.getPath();
    String seoName = FilenameUtils.getBaseName(asset.getName());
    String format = FilenameUtils.getExtension(asset.getName());

    Long width = params.getWidth();
    Long height = params.getHeight();
    CropDimension cropDimension = params.getCropDimension();
    Integer rotation = params.getRotation();

    Map<String, Object> map = new HashMap<>();
    map.put(PARAM_PATH, path);
    map.put(PARAM_SEO_NAME, seoName);
    map.put(PARAM_FORMAT, format);
    map.put(PARAM_PREFER_WEBP, true);
    if (width != null) {
      map.put(PARAM_WIDTH, width);
    }
    if (height != null) {
      map.put(PARAM_HEIGHT, height);
    }
    if (cropDimension != null) {
      map.put(PARAM_CROP, cropDimension.getCropStringWidthHeight());
    }
    if (rotation != null && rotation != 0) {
      map.put(PARAM_ROTATE, rotation);
    }
    return map;
  }

}
