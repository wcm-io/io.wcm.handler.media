/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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
package io.wcm.handler.mediasource.dam.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.format.Ratio;
import io.wcm.handler.mediasource.dam.AssetRendition;

/**
 * Helper class for calculating crop dimensions for auto-cropping.
 */
final class WebEnabledRenditionCropping {

  private WebEnabledRenditionCropping() {
    // static methods only
  }

  /**
   * Rescales the a crop dimension that is based on the web-enabled rendition to apply to the original rendition
   * of the asset (which is the actual base for the cropping).
   * @param asset Asset
   * @param cropDimensionForWebRendition Crop dimension calculated based on web rendition
   * @return Rendition or null if no match found
   */
  public static @NotNull CropDimension getCropDimensionForOriginal(@NotNull Asset asset,
      @NotNull CropDimension cropDimensionForWebRendition) {
    RenditionMetadata original = new RenditionMetadata(asset.getOriginal());
    Double scaleFactor = getCropScaleFactor(asset, original);
    long scaledLeft = Math.round(cropDimensionForWebRendition.getLeft() * scaleFactor);
    long scaledTop = Math.round(cropDimensionForWebRendition.getTop() * scaleFactor);
    long scaledWidth = Math.round(cropDimensionForWebRendition.getWidth() * scaleFactor);
    if (scaledWidth > original.getWidth()) {
      scaledWidth = original.getWidth();
    }
    long scaledHeight = Math.round(cropDimensionForWebRendition.getHeight() * scaleFactor);
    if (scaledHeight > original.getHeight()) {
      scaledHeight = original.getHeight();
    }
    return new CropDimension(scaledLeft, scaledTop, scaledWidth, scaledHeight,
        cropDimensionForWebRendition.isAutoCrop());
  }

  /**
   * The cropping coordinates are stored with coordinates relating to the web-enabled rendition. But we want
   * to crop the original image, so we have to scale those values to match the coordinates in the original image.
   * @return Scale factor
   */
  private static double getCropScaleFactor(@NotNull Asset asset, @NotNull RenditionMetadata original) {
    RenditionMetadata webEnabled = getWebEnabledRendition(asset);
    if (webEnabled == null || original.getWidth() == 0 || webEnabled.getWidth() == 0) {
      return 1d;
    }
    return Ratio.get(original.getWidth(), webEnabled.getWidth());
  }

  /**
   * Get web first rendition for asset.
   * This is the same logic as implemented in
   * <code>/libs/cq/gui/components/authoring/editors/clientlibs/core/inlineediting/js/ImageEditor.js</code>.
   * @param asset Asset
   * @return Web rendition or null if none found
   */
  private static @Nullable RenditionMetadata getWebEnabledRendition(@NotNull Asset asset) {
    return asset.getRenditions().stream()
        .filter(AssetRendition::isWebRendition)
        .findFirst()
        .map(RenditionMetadata::new)
        .orElse(null);
  }

}
