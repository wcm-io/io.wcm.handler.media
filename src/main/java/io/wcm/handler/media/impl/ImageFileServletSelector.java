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
package io.wcm.handler.media.impl;

import static io.wcm.handler.media.impl.ImageTransformation.isValidRotation;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.CropDimension;

/**
 * Helper class to build and parse selector strings for {@link ImageFileServlet}.
 */
public final class ImageFileServletSelector {

  private int width;
  private int height;
  private CropDimension cropDimension;
  private int rotation;
  private int quality;

  ImageFileServletSelector(@NotNull String[] selectors) {
    parseParams(selectors);
    validateParams();
  }

  /**
   * Parse parameters out of selector string.
   * @param selectors Selectors
   */
  private void parseParams(@NotNull String[] selectors) {
    // skip first selector "image_file"

    // width and height
    if (selectors.length >= 3) {
      width = NumberUtils.toInt(selectors[1]);
      height = NumberUtils.toInt(selectors[2]);
    }

    // cropping parameter
    if (selectors.length >= 4) {
      String cropString = selectors[3];
      if (!StringUtils.equals(cropString, "-")) {
        try {
          cropDimension = CropDimension.fromCropString(cropString);
        }
        catch (IllegalArgumentException ex) {
          // ignore
        }
      }
    }

    // rotation parameter
    if (selectors.length >= 5) {
      String rotationString = selectors[4];
      rotation = NumberUtils.toInt(rotationString);
    }

    // image quality
    if (selectors.length >= 6) {
      String qualityString = selectors[5];
      quality = NumberUtils.toInt(qualityString);
    }
  }

  /**
   * Validate parameters. Invalid parameters are reset.
   */
  private void validateParams() {
    if (width < 0) {
      width = 0;
    }
    if (height < 0) {
      height = 0;
    }
    if (!isValidRotation(rotation)) {
      rotation = 0;
    }
    if (quality < 0 || quality > 100) {
      quality = 0;
    }
  }

  int getWidth() {
    return width;
  }

  int getHeight() {
    return height;
  }

  @Nullable
  CropDimension getCropDimension() {
    return cropDimension;
  }

  int getRotation() {
    return rotation;
  }

  int getQuality() {
    return quality;
  }

  /**
   * Build selector string for this servlet.
   * @param width Width
   * @param height Height
   * @param cropDimension Crop dimension
   * @param rotation Rotation
   * @param contentDispositionAttachment Content disposition attachment
   * @param imageQualityPercentage Image quality percentage (0..1)
   * @return Selector string
   */
  public static @NotNull String build(long width, long height,
      @Nullable CropDimension cropDimension, @Nullable Integer rotation, @Nullable Double imageQualityPercentage,
      boolean contentDispositionAttachment) {
    StringBuilder result = new StringBuilder()
        .append(ImageFileServlet.SELECTOR)
        .append(".").append(Long.toString(width))
        .append(".").append(Long.toString(height));

    if (cropDimension != null) {
      result.append(".").append(cropDimension.getCropString());
    }
    else if (rotation != null || imageQualityPercentage != null) {
      result.append(".-");
    }
    if (rotation != null) {
      result.append(".").append(rotation.toString());
    }
    else if (imageQualityPercentage != null) {
      result.append(".0");
    }
    if (imageQualityPercentage != null) {
      long quality = Math.round(imageQualityPercentage * 100);
      if (quality > 0) {
        result.append(".").append(Long.toString(quality));
      }
    }
    if (contentDispositionAttachment) {
      result.append(".").append(MediaFileServletConstants.SELECTOR_DOWNLOAD);
    }

    return result.toString();
  }

}
