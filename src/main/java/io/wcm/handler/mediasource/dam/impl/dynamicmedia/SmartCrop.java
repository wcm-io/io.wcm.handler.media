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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.format.Ratio;
import io.wcm.handler.mediasource.dam.impl.DamContext;

/**
 * Apply Dynamic Media Smart Cropping.
 */
final class SmartCrop {

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

}
