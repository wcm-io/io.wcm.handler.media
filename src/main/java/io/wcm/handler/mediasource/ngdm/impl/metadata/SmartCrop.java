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
package io.wcm.handler.mediasource.ngdm.impl.metadata;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.format.Ratio;

/**
 * Named smart cropping definition.
 */
public class SmartCrop {
  private final String name;
  private final CropDimension cropDimension;
  private final double ratio;

  SmartCrop(String name,
      double leftPercentage, double topPercentage, double widthPercentage, double heightPercentage,
      Dimension originalDimension) {

    // calculate actual cropping dimension
    long originalWidth = originalDimension.getWidth();
    long originalHeight = originalDimension.getHeight();
    long left = Math.round(originalWidth * leftPercentage);
    long top = Math.round(originalHeight * topPercentage);
    long width = Math.round(originalWidth * widthPercentage);
    long height = Math.round(originalHeight * heightPercentage);

    this.name = name;
    this.cropDimension = new CropDimension(left, top, width, height, true);
    this.ratio = Ratio.get(width, height);
  }

  SmartCrop(String name, MetadataResponse.SmartCrop smartCrop, Dimension originalDimension) {
    this(name, smartCrop.left, smartCrop.top, smartCrop.normalizedWidth, smartCrop.normalizedHeight,
        originalDimension);
  }

  public String getName() {
    return name;
  }

  public CropDimension getCropDimension() {
    return this.cropDimension;
  }

  public double getRatio() {
    return this.ratio;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
  }
}