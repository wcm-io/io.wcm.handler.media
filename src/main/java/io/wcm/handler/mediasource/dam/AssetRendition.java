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
package io.wcm.handler.mediasource.dam;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.dam.api.DamConstants.EXIF_PIXELXDIMENSION;
import static com.day.cq.dam.api.DamConstants.EXIF_PIXELYDIMENSION;
import static com.day.cq.dam.api.DamConstants.METADATA_FOLDER;
import static com.day.cq.dam.api.DamConstants.ORIGINAL_FILE;
import static com.day.cq.dam.api.DamConstants.TIFF_IMAGELENGTH;
import static com.day.cq.dam.api.DamConstants.TIFF_IMAGEWIDTH;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.NN_RENDITIONS_METADATA;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.PN_IMAGE_HEIGHT;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.PN_IMAGE_WIDTH;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.image.Layer;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaFileType;
import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Helper methods for getting metadata for DAM renditions.
 */
@ProviderType
public final class AssetRendition {

  private static final Logger log = LoggerFactory.getLogger(AssetRendition.class);

  private AssetRendition() {
    // static methods only
  }

  /**
   * Get dimension (width, height) of given DAM rendition.
   *
   * <p>
   * It reads the dimension information from the
   * asset metadata for the original rendition, or from the rendition metadata generated by the
   * "DamRenditionMetadataService". If both is not available it gets the dimension from the renditions
   * binary file, but this is inefficient and should not happen under sound conditions.
   * </p>
   * @param rendition Rendition
   * @return Dimension or null if dimension could not be detected, not even in fallback mode
   */
  public static @Nullable Dimension getDimension(@NotNull Rendition rendition) {
    return getDimension(rendition, false);
  }

  /**
   * Get dimension (width, height) of given DAM rendition.
   *
   * <p>
   * It reads the dimension information from the
   * asset metadata for the original rendition, or from the rendition metadata generated by the
   * "DamRenditionMetadataService". If both is not available it gets the dimension from the renditions
   * binary file, but this is inefficient and should not happen under sound conditions.
   * </p>
   * @param rendition Rendition
   * @param suppressLogWarningNoRenditionsMetadata If set to true, no log warnings is generated when
   *          renditions metadata containing the width/height of the rendition does not exist (yet).
   * @return Dimension or null if dimension could not be detected, not even in fallback mode
   */
  public static @Nullable Dimension getDimension(@NotNull Rendition rendition,
      boolean suppressLogWarningNoRenditionsMetadata) {

    boolean isOriginal = isOriginal(rendition);
    String fileExtension = FilenameUtils.getExtension(getFilename(rendition));

    // get image width/height
    Dimension dimension = null;
    if (isOriginal) {
      // get width/height from metadata for original renditions
      dimension = getDimensionFromOriginal(rendition);
    }

    // dimensions for non-original renditions only supported for image binaries
    if (MediaFileType.isImage(fileExtension)) {
      if (dimension == null) {
        // check if rendition metadata is present in <rendition>/jcr:content/metadata provided by AEMaaCS asset compute
        dimension = getDimensionFromAemRenditionMetadata(rendition);
      }

      if (dimension == null) {
        // otherwise get from rendition metadata written by {@link DamRenditionMetadataService}
        dimension = getDimensionFromMediaHandlerRenditionMetadata(rendition);
      }

      // fallback: if width/height could not be read from either asset or rendition metadata load the image
      // into memory and get width/height from there - but log an warning because this is inefficient
      if (dimension == null) {
        dimension = getDimensionFromImageBinary(rendition, suppressLogWarningNoRenditionsMetadata);
      }
    }

    return dimension;
  }

  /**
   * Read dimension for original rendition from asset metadata.
   * @param rendition Rendition
   * @return Dimension or null
   */
  private static @Nullable Dimension getDimensionFromOriginal(@NotNull Rendition rendition) {
    Asset asset = rendition.getAsset();
    // asset may have stored dimension in different property names
    long width = getAssetMetadataValueAsLong(asset, TIFF_IMAGEWIDTH, EXIF_PIXELXDIMENSION);
    long height = getAssetMetadataValueAsLong(asset, TIFF_IMAGELENGTH, EXIF_PIXELYDIMENSION);
    return toValidDimension(width, height);
  }

  private static long getAssetMetadataValueAsLong(Asset asset, String... propertyNames) {
    for (String propertyName : propertyNames) {
      long value = NumberUtils.toLong(StringUtils.defaultString(asset.getMetadataValueFromJcr(propertyName), "0"));
      if (value > 0L) {
        return value;
      }
    }
    return 0L;
  }

  /**
   * Read dimension for non-original rendition from renditions metadata generated by "DamRenditionMetadataService".
   * @param rendition Rendition
   * @return Dimension or null
   */
  @SuppressWarnings("java:S1075") // not a file path
  private static @Nullable Dimension getDimensionFromMediaHandlerRenditionMetadata(@NotNull Rendition rendition) {
    Asset asset = rendition.getAsset();
    String metadataPath = JCR_CONTENT + "/" + NN_RENDITIONS_METADATA + "/" + rendition.getName();
    Resource metadataResource = AdaptTo.notNull(asset, Resource.class).getChild(metadataPath);
    if (metadataResource != null) {
      ValueMap props = metadataResource.getValueMap();
      long width = props.get(PN_IMAGE_WIDTH, 0L);
      long height = props.get(PN_IMAGE_HEIGHT, 0L);
      return toValidDimension(width, height);
    }
    return null;
  }

  /**
   * Asset Compute from AEMaaCS writes rendition metadata including width/height to jcr:content/metadata of the
   * rendition resource - try to read it from there (it may be missing for not fully processed assets, or in local
   * AEMaaCS SDK or AEM 6.5 instances).
   * @param rendition Rendition
   * @return Dimension or null
   */
  private static @Nullable Dimension getDimensionFromAemRenditionMetadata(@NotNull Rendition rendition) {
    Resource metadataResource = rendition.getChild(JCR_CONTENT + "/" + METADATA_FOLDER);
    if (metadataResource != null) {
      ValueMap props = metadataResource.getValueMap();
      long width = props.get(TIFF_IMAGEWIDTH, 0L);
      long height = props.get(TIFF_IMAGELENGTH, 0L);
      return toValidDimension(width, height);
    }
    return null;
  }

  /**
   * Fallback: Read dimension by loading image binary into memory.
   * @param rendition Rendition
   * @param suppressLogWarningNoRenditionsMetadata If set to true, no log warnings is generated when
   *          renditions metadata containing the width/height of the rendition does not exist (yet).
   * @return Dimension or null
   */
  @SuppressWarnings("PMD.GuardLogStatement")
  private static @Nullable Dimension getDimensionFromImageBinary(@NotNull Rendition rendition,
      boolean suppressLogWarningNoRenditionsMetadata) {
    try (InputStream is = rendition.getStream()) {
      if (is != null) {
        Layer layer = new Layer(is);
        long width = layer.getWidth();
        long height = layer.getHeight();
        Dimension dimension = toValidDimension(width, height);
        if (!suppressLogWarningNoRenditionsMetadata) {
          log.warn("Unable to detect rendition metadata for {}, "
              + "fallback to inefficient detection by loading image into in memory (detected dimension={}). "
              + "Please check if the service user for the bundle 'io.wcm.handler.media' is configured properly.",
              rendition.getPath(), dimension);
        }
        return dimension;
      }
      else {
        log.warn("Unable to get binary stream for rendition {}", rendition.getPath());
      }
    }
    catch (IOException ex) {
      log.warn("Unable to read binary stream to layer for rendition {}", rendition.getPath(), ex);
    }
    return null;
  }

  /**
   * Convert width/height to dimension.
   * @param width Width
   * @param height Height
   * @return Dimension or null if width or height are not valid
   */
  private static @Nullable Dimension toValidDimension(long width, long height) {
    if (width > 0L && height > 0L) {
      return new Dimension(width, height);
    }
    return null;
  }

  /**
   * Checks if the given rendition is the original file of the asset
   * @param rendition DAM rendition
   * @return true if rendition is the original
   */
  public static boolean isOriginal(@NotNull Rendition rendition) {
    return StringUtils.equals(rendition.getName(), ORIGINAL_FILE);
  }

  /**
   * Checks if the given rendition is a thumbnail rendition generated automatically by AEM
   * (with <code>cq5dam.thumbnail.</code> prefix).
   * @param rendition DAM rendition
   * @return true if rendition is a thumbnail rendition
   */
  public static boolean isThumbnailRendition(@NotNull Rendition rendition) {
    return AemRenditionType.THUMBNAIL_RENDITION.matches(rendition);
  }

  /**
   * Checks if the given rendition is a web rendition generated automatically by AEM for the image editor/cropping
   * (with <code>cq5dam.web.</code> prefix).
   * @param rendition DAM rendition
   * @return true if rendition is a web rendition
   */
  public static boolean isWebRendition(@NotNull Rendition rendition) {
    return AemRenditionType.WEB_RENDITION.matches(rendition);
  }

  /**
   * Get file name of given rendition. If it is the original rendition get asset name as file name.
   * @param rendition Rendition
   * @return File extension or null if it could not be detected
   */
  public static String getFilename(@NotNull Rendition rendition) {
    boolean isOriginal = isOriginal(rendition);
    if (isOriginal) {
      return rendition.getAsset().getName();
    }
    else {
      return rendition.getName();
    }
  }

}
