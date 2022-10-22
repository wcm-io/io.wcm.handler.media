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
import static com.day.cq.dam.api.DamConstants.RENDITIONS_FOLDER;

import java.util.Arrays;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.format.Ratio;
import io.wcm.handler.mediasource.dam.AssetRendition;

/**
 * Apply Dynamic Media Smart Cropping.
 */
public final class SmartCrop {

  /**
   * Normalized width (double value 0..1 as percentage of original image).
   */
  public static final String PN_NORMALIZED_WIDTH = "normalizedWidth";

  /**
   * Normalized height (double value 0..1 as percentage of original image).
   */
  public static final String PN_NORMALIZED_HEIGHT = "normalizedHeight";

  /**
   * Left margin (double value 0..1 as percentage of original image).
   */
  public static final String PN_LEFT = "left";

  /**
   * Top margin (double value 0..1 as percentage of original image).
   */
  public static final String PN_TOP = "top";

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
  public static boolean canApply(@Nullable CropDimension cropDimension, @Nullable Integer rotation) {
    return (cropDimension == null || cropDimension.isAutoCrop()) && rotation == null;
  }

  /**
   * Checks DM image profile for a smart cropping definition matching the ratio of the requested ratio.
   * @param imageProfile Image profile from DAM context (null if no is defined)
   * @param requestedRatio Requested ratio
   * @return Named dimension or null. The provided width/height can usually be ignored, because they
   *         are the width/height from the image profile which only describe the aspect ratio, but not
   *         any width/height values used in reality.
   */
  public static @Nullable NamedDimension getDimensionForRatio(@Nullable ImageProfile imageProfile, double requestedRatio) {
    if (imageProfile == null) {
      return null;
    }
    return imageProfile.getSmartCropDefinitions().stream()
        .filter(def -> Ratio.matches(Ratio.get(def), requestedRatio))
        .findFirst().orElse(null);
  }

  /**
   * Checks DM image profile for a smart cropping definition matching the ratio of the requested width/height.
   * @param imageProfile Image profile from DAM context (null if no is defined)
   * @param width Width
   * @param height Height
   * @return Smart cropping definition with requested width/height - or null if no match
   */
  public static @Nullable NamedDimension getDimensionForWidthHeight(@Nullable ImageProfile imageProfile, long width, long height) {
    Double requestedRatio = Ratio.get(width, height);
    NamedDimension matchingDimension = getDimensionForRatio(imageProfile, requestedRatio);
    if (matchingDimension != null) {
      // create new named dimension with actual requested width/height
      return new NamedDimension(matchingDimension.getName(), width, height);
    }
    else {
      return null;
    }
  }

  /**
   * Gets the actual smart-cropped dimension for the given asset and smart cropping definition (aspect ratio).
   * @param asset Asset
   * @param resourceResolver Resource resolver
   * @param smartCropDef Smart cropping definition from image profile
   * @return Actual dimension of the smart cropping area or null if not found
   */
  @SuppressWarnings("java:S1075") // no filesystem paths
  public static @Nullable CropDimension getCropDimensionForAsset(@NotNull Asset asset,
      @NotNull ResourceResolver resourceResolver, @NotNull NamedDimension smartCropDef) {
    // at this path smart cropping parameters may be stored for each ratio (esp. if manual cropping was applied)
    String smartCropRenditionPath = asset.getPath()
        + "/" + JCR_CONTENT
        + "/" + RENDITIONS_FOLDER
        + "/" + smartCropDef.getName()
        + "/" + JCR_CONTENT;
    Resource smartCropRendition = resourceResolver.getResource(smartCropRenditionPath);
    if (smartCropRendition == null) {
      // on AEMaaCS this path should always exist, in AEMaaCS SDK it seems to be created only when manual cropping
      // is applied in the Assets UI
      return null;
    }
    ValueMap props = smartCropRendition.getValueMap();
    double leftPercentage = props.get(PN_LEFT, 0d);
    double topPercentage = props.get(PN_TOP, 0d);
    double widthPercentage = props.get(PN_NORMALIZED_WIDTH, 0d);
    double heightPercentage = props.get(PN_NORMALIZED_HEIGHT, 0d);
    Dimension originalDimension = AssetRendition.getDimension(asset.getOriginal());
    if (originalDimension == null
        || !isValidTopLeft(leftPercentage, topPercentage)
        || !isValidWidthHeight(widthPercentage, heightPercentage)) {
      // ignore smart cropping rendition with invalid dimension
      return null;
    }

    // calculate actual cropping dimension
    long originalWidth = originalDimension.getWidth();
    long originalHeight = originalDimension.getHeight();
    long left = Math.round(originalWidth * leftPercentage);
    long top = Math.round(originalHeight * topPercentage);
    long width = Math.round(originalWidth * widthPercentage);
    long height = Math.round(originalHeight * heightPercentage);
    return new CropDimension(left, top, width, height, true);
  }

  /**
   * Verifies that the actual image area picked in smart cropping (either automatic or manual) results in
   * a rendition size that fulfills at least the requested width/height.
   * @param asset DAM asset
   * @param resourceResolver Resource resolve
   * @param smartCropDef Smart cropping dimension
   * @param width Requested width
   * @param height Requested height
   * @return true if size is matching, or no width/height information for the cropped area is available
   */
  public static boolean isMatchingSize(@NotNull Asset asset, @NotNull ResourceResolver resourceResolver,
      @NotNull NamedDimension smartCropDef, long width, long height) {
    CropDimension cropDimension = getCropDimensionForAsset(asset, resourceResolver, smartCropDef);
    if (cropDimension == null) {
      // no smart cropping rendition is not found in repository or it contains invalid values,
      // we assume the size should be fine and skip further checking
      return true;
    }

    // check if smart cropping area is large enough
    long croppedWidth = cropDimension.getWidth();
    long croppedHeight = cropDimension.getHeight();
    boolean isMatchingSize = (cropDimension.getWidth() >= width || croppedHeight >= height);
    if (!isMatchingSize) {
      log.debug("Smart cropping area '{}' for asset {} is too small ({} x {}) for requested size {} x {}.",
          smartCropDef.getName(), asset.getPath(), croppedWidth, croppedHeight, width, height);
    }
    return isMatchingSize;
  }

  private static boolean isValidTopLeft(double... numbers) {
    return Arrays.stream(numbers).allMatch(value -> value >= 0 && value <= 1);
  }

  private static boolean isValidWidthHeight(double... numbers) {
    return Arrays.stream(numbers).allMatch(value -> value >= MIN_NORMALIZED_WIDTH_HEIGHT && value <= 1);
  }

}
