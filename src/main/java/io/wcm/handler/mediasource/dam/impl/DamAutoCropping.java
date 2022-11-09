/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.impl.ImageTransformation;
import io.wcm.handler.mediasource.dam.AssetRendition;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.NamedDimension;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.SmartCrop;

/**
 * Helper class for calculating crop dimensions for auto-cropping.
 */
class DamAutoCropping {

  private final DamContext damContext;
  private final MediaArgs mediaArgs;

  DamAutoCropping(@NotNull DamContext damContext, @NotNull MediaArgs mediaArgs) {
    this.damContext = damContext;
    this.mediaArgs = mediaArgs;
  }

  /**
   * Get possible cropping dimension for all given media formats.
   * @return List of matching cropping definitions
   */
  public List<CropDimension> calculateAutoCropDimensions() {
    Stream<MediaFormat> mediaFormats = Arrays.stream(
        ObjectUtils.defaultIfNull(mediaArgs.getMediaFormats(), new MediaFormat[0]));
    return mediaFormats
        .map(this::calculateAutoCropDimension)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Get or calculate cropping dimension for given media format (if it has an actual ratio defined).
   * @param mediaFormat Media format
   * @return Cropping dimension or null if not found
   */
  private @Nullable CropDimension calculateAutoCropDimension(@NotNull MediaFormat mediaFormat) {
    CropDimension result = null;

    double ratio = mediaFormat.getRatio();
    if (ratio > 0) {
      // first check is DM is enabled, and a fitting smart crop rendition for this aspect ratio is defined
      result = getDynamicMediaCropDimension(ratio);

      // otherwise calculate auto-cropping dimension based on original image
      if (result == null) {
        Dimension dimension = AssetRendition.getDimension(damContext.getAsset().getOriginal());
        if (dimension != null && dimension.getWidth() > 0 && dimension.getHeight() > 0) {
          result = ImageTransformation.calculateAutoCropDimension(dimension.getWidth(), dimension.getHeight(), ratio);
        }
      }
    }

    return result;
  }

  /**
   * Try to get actual smart crop dimension for the requested ratio for the current asset to be used for auto-cropping.
   * @param requestedRatio Requested ratio
   * @return Cropping dimension or null if not found
   */
  private @Nullable CropDimension getDynamicMediaCropDimension(double requestedRatio) {
    if (damContext.isDynamicMediaEnabled() && damContext.isDynamicMediaAsset()
        && damContext.isDynamicMediaValidateSmartCropRenditionSizes()) {
      NamedDimension smartCropDef = SmartCrop.getDimensionForRatio(damContext.getImageProfile(), requestedRatio);
      if (smartCropDef != null) {
        return SmartCrop.getCropDimensionForAsset(damContext.getAsset(), damContext.getResourceResolver(), smartCropDef);
      }
    }
    return null;
  }

}
