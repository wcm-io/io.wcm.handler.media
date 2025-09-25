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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Dimension;

/**
 * Creates relative crop string with percentage values as required by the Web-Optimized Image Delivery API.
 * It uses one fractional digit for the percentage values.
 */
public final class RelativeCroppingString {

  private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));

  private RelativeCroppingString() {
    // static methods only
  }

  /**
   * Create relative cropping string from crop dimension and image dimension.
   * @param cropDimension Crop dimension
   * @param imageDimension Image dimension
   * @return Cropping string
   */
  public static @NotNull String createFromCropDimension(
      @NotNull CropDimension cropDimension, @NotNull Dimension imageDimension) {
    double x1 = cropDimension.getLeft();
    double y1 = cropDimension.getTop();
    double left = x1 / imageDimension.getWidth();
    double top = y1 / imageDimension.getHeight();
    double width = (double)cropDimension.getWidth() / imageDimension.getWidth();
    double height = (double)cropDimension.getHeight() / imageDimension.getHeight();
    return create(left, top, width, height);
  }

  /**
   * Create relative cropping string from percentage values.
   * @param left Left
   * @param top Top
   * @param width Width
   * @param height Height
   * @return Cropping string
   */
  public static @NotNull String create(double left, double top, double width, double height) {
    return String.format("%sp,%sp,%sp,%sp",
        toPercentage(left), toPercentage(top),
        toPercentage(width), toPercentage(height));
  }

  private static String toPercentage(double fraction) {
    double percentage = Math.round(fraction * 1000d) / 10d;
    percentage = Math.max(0.0, percentage);
    percentage = Math.min(100.0, percentage);
    return DECIMAL_FORMAT.format(percentage);
  }

}
