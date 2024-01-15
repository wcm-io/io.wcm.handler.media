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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.CropDimension;

/**
 * Parameters for rendering a rendition.
 */
public class WebOptimizedImageDeliveryParams {

  private Long width;
  private Long height;
  private CropDimension cropDimension;
  private Integer rotation;

  /**
   * @return Width
   */
  public @Nullable Long getWidth() {
    return this.width;
  }

  /**
   * @param value Width
   * @return this
   */
  public @NotNull WebOptimizedImageDeliveryParams width(@Nullable Long value) {
    this.width = value;
    return this;
  }

  /**
   * @return Height
   */
  public @Nullable Long getHeight() {
    return this.height;
  }

  /**
   * @param value Height
   * @return this
   */
  public @NotNull WebOptimizedImageDeliveryParams height(@Nullable Long value) {
    this.height = value;
    return this;
  }

  /**
   * @return Crop dimension
   */
  public @Nullable CropDimension getCropDimension() {
    return this.cropDimension;
  }

  /**
   * @param value Crop dimension
   * @return this
   */
  public @NotNull WebOptimizedImageDeliveryParams cropDimension(@Nullable CropDimension value) {
    this.cropDimension = value;
    return this;
  }

  /**
   * @return Rotation
   */
  public @Nullable Integer getRotation() {
    return this.rotation;
  }

  /**
   * @param value Rotation
   * @return this
   */
  public @NotNull WebOptimizedImageDeliveryParams rotation(@Nullable Integer value) {
    this.rotation = value;
    return this;
  }

}
