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
package io.wcm.handler.mediasource.dam.impl.dynamicmedia;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.DamConstants;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.format.Ratio;
import io.wcm.handler.mediasource.dam.AssetRendition;
import io.wcm.handler.mediasource.dam.impl.DamContext;

/**
 * Apply Dynamic Media Smart Cropping.
 */
final class SmartCrop {

  private static final double MIN_NORMALIZED_WIDTH_HEIGHT = 0.0001;
  private static final Logger log = LoggerFactory.getLogger(SmartCrop.class);

  private SmartCrop() {
    // static methods only
  }

  /**
   * Smart cropping can be applied when no manual cropping was applied, or auto cropping is enabled.
   * Additionally, combination with rotation is not allowed.
   * @param cropDimension Manual crop definition
   * @param rotation Rotation
   * @return true if Smart Cropping can be applied
   */
  static boolean canApply(@Nullable CropDimension cropDimension, @Nullable Integer rotation) {
    return (cropDimension == null || cropDimension.isAutoCrop()) && rotation == null;
  }

  /**
   * Checks DM image profile for a smart cropping definition matching the ratio of the requested width/height.
   * @param damContext DAM context
   * @param width Width
   * @param height Height
   * @return Smart cropping definition with requested with/height - or null if no match
   */
  static @Nullable NamedDimension getDimension(@NotNull DamContext damContext, long width, long height) {
    ImageProfile imageProfile = damContext.getImageProfile();
    if (imageProfile == null) {
      return null;
    }
    Double requestedRatio = Ratio.get(width, height);
    NamedDimension matchingDimension = imageProfile.getSmartCropDefinitions().stream()
        .filter(def -> Ratio.matches(Ratio.get(def), requestedRatio))
        .findFirst().orElse(null);
    if (matchingDimension != null) {
      // create new named dimension with actual requested width/height
      return new NamedDimension(matchingDimension.getName(), width, height);
    }
    else {
      return null;
    }
  }

  /**
   * Verifies that the actual image area picked in smart cropping (either automatic or manual) results in
   * a renditions size that fulfills at least the requested width/height.
   * @param damContext DAM context
   * @param smartCropDef Smart cropping dimension
   * @param width Requested with
   * @param height Requested height
   * @return true if size is matching, or no width/height information for the cropped area is available
   */
  @SuppressWarnings("java:S1075") // no filesystem paths
  static boolean isMatchingSize(@NotNull DamContext damContext, @NotNull NamedDimension smartCropDef, long width, long height) {
    // at this path smart cropping parameters may be stored for each ratio (esp. if manual cropping was applied)
    String smartCropRenditionPath = damContext.getAsset().getPath()
        + "/" + JCR_CONTENT
        + "/" + DamConstants.RENDITIONS_FOLDER
        + "/" + smartCropDef.getName()
        + "/" + JCR_CONTENT;
    Resource smartCropRendition = damContext.getResourceResolver().getResource(smartCropRenditionPath);
    if (smartCropRendition == null) {
      // if this rendition is not found in repository, we assume the size should be fine
      // on AEMaaCS this path should always exist, in AEMaaCS SDK it seems to be created only when manual cropping
      // is applied in the Assets UI
      return true;
    }
    ValueMap props = smartCropRendition.getValueMap();
    double normalizedWidth = props.get("normalizedWidth", 0d);
    double normalizedHeight = props.get("normalizedHeight", 0d);
    Dimension originalDimension = AssetRendition.getDimension(damContext.getAsset().getOriginal());
    if (normalizedWidth < MIN_NORMALIZED_WIDTH_HEIGHT || normalizedHeight < MIN_NORMALIZED_WIDTH_HEIGHT
        || originalDimension == null) {
      // skip further validation if dimensions are not found
      return true;
    }

    long croppedWidth = Math.round(originalDimension.getWidth() * normalizedWidth);
    long croppedHeight = Math.round(originalDimension.getHeight() * normalizedHeight);
    boolean isMatchingSize = (croppedWidth >= width || croppedHeight >= height);
    if (!isMatchingSize) {
      log.debug("Smart cropping area '{}' for asset {} is too small ({} x {}) for requested size {} x {}.",
          smartCropDef.getName(), damContext.getAsset().getPath(), croppedWidth, croppedHeight, width, height);
    }
    return isMatchingSize;
  }

}
