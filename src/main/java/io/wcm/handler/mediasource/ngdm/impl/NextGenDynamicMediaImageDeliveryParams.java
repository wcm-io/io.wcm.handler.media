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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.Dimension;

/**
 * Parameters for rendering a rendition.
 */
public class NextGenDynamicMediaImageDeliveryParams {

  private Long width;
  private String widthPlaceholder;
  private Dimension cropSmartRatio;
  private Integer rotation;
  private Integer quality;

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
  public @NotNull NextGenDynamicMediaImageDeliveryParams width(@Nullable Long value) {
    this.width = value;
    return this;
  }

  /**
   * @return Width placeholder
   */
  public @Nullable String getWidthPlaceholder() {
    return this.widthPlaceholder;
  }

  /**
   * @param value Width placeholder
   * @return this
   */
  public @NotNull NextGenDynamicMediaImageDeliveryParams widthPlaceholder(@Nullable String value) {
    this.widthPlaceholder = value;
    return this;
  }

  /**
   * @return Dimension with aspect ratio for smart cropping
   */
  public @Nullable Dimension getCropSmartRatio() {
    return this.cropSmartRatio;
  }

  /**
   * @param value Dimension with aspect ratio for smart cropping
   * @return this
   */
  public @NotNull NextGenDynamicMediaImageDeliveryParams cropSmartRatio(@Nullable Dimension value) {
    this.cropSmartRatio = value;
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
  public @NotNull NextGenDynamicMediaImageDeliveryParams rotation(@Nullable Integer value) {
    this.rotation = value;
    return this;
  }

  /**
   * @return Quality
   */
  public @Nullable Integer getQuality() {
    return this.quality;
  }

  /**
   * @param value Quality
   * @return this
   */
  public @NotNull NextGenDynamicMediaImageDeliveryParams quality(@Nullable Integer value) {
    this.quality = value;
    return this;
  }

}
