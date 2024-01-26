/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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

import static io.wcm.handler.media.impl.ImageTransformation.isValidRotation;
import static io.wcm.handler.media.impl.ImageTransformation.rotateMapHeight;
import static io.wcm.handler.media.impl.ImageTransformation.rotateMapWidth;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.MediaArgs;

/**
 * Extended rendition handler supporting cropping and rotating of images.
 */
public class TransformedRenditionHandler extends DefaultRenditionHandler {

  private final CropDimension cropDimension;
  private final Integer rotation;

  /**
   * @param cropDimension Crop dimension
   * @param rotation Rotation
   * @param damContext DAM context objects
   */
  TransformedRenditionHandler(CropDimension cropDimension, Integer rotation, DamContext damContext) {
    super(damContext);
    this.cropDimension = cropDimension;
    this.rotation = (rotation != null && isValidRotation(rotation)) ? rotation : null;
  }

  /**
   * Searches for the biggest web enabled rendition and, if exists,
   * adds a {@link VirtualTransformedRenditionMetadata} to the list.
   * @param candidates Candidates
   * @param mediaArgs Media args
   * @return {@link Set} of {@link RenditionMetadata}
   */
  @Override
  protected Set<RenditionMetadata> postProcessCandidates(Set<RenditionMetadata> candidates, MediaArgs mediaArgs) {
    NavigableSet<RenditionMetadata> processedCandidates = new TreeSet<>(candidates);
    if (cropDimension != null) {
      VirtualTransformedRenditionMetadata cropRendition = getCropRendition(mediaArgs);
      if (cropRendition != null) {
        // return only cropped rendition
        processedCandidates.clear();
        processedCandidates.add(cropRendition);
        return processedCandidates;
      }
    }
    return rotateSourceRenditions(processedCandidates, mediaArgs);
  }

  /**
   * Rotates all source renditions if configured.
   * @param candidates Candidate renditions
   * @param mediaArgs Media args
   * @return Virtual-rotated and sorted candidate renditions
   */
  private NavigableSet<RenditionMetadata> rotateSourceRenditions(Set<RenditionMetadata> candidates, MediaArgs mediaArgs) {
    if (rotation == null) {
      return new TreeSet<>(candidates);
    }
    return candidates.stream()
        .filter(rendition -> !rendition.isVectorImage())
        .map(rendition -> new VirtualTransformedRenditionMetadata(rendition.getRendition(),
            rotateMapWidth(rendition.getWidth(), rendition.getHeight(), rotation),
            rotateMapHeight(rendition.getWidth(), rendition.getHeight(), rotation),
            mediaArgs.getEnforceOutputFileExtension(), null, rotation, mediaArgs.getImageQualityPercentage()))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  /**
   * Searches for the biggest web-enabled rendition that matches the crop dimensions width and height or is bigger.
   * @param mediaArgs Media args
   * @return Rendition or null if no match found
   */
  private VirtualTransformedRenditionMetadata getCropRendition(MediaArgs mediaArgs) {
    RenditionMetadata original = getOriginalRendition();
    if (original == null || original.isVectorImage()) {
      return null;
    }
    return new VirtualTransformedRenditionMetadata(original.getRendition(),
        rotateMapWidth(cropDimension.getWidth(), cropDimension.getHeight(), rotation),
        rotateMapHeight(cropDimension.getWidth(), cropDimension.getHeight(), rotation),
        mediaArgs.getEnforceOutputFileExtension(), cropDimension, rotation, mediaArgs.getImageQualityPercentage());
  }

}
