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

import static com.day.cq.dam.api.DamConstants.TIFF_IMAGELENGTH;
import static com.day.cq.dam.api.DamConstants.TIFF_IMAGEWIDTH;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataResponse.RepositoryMetadata;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.util.ToStringStyle;

/**
 * Metadata for Next Gen Dynamic Media asset fetched from the HTTP API.
 */
public final class NextGenDynamicMediaMetadata {

  private final String mimeType;
  private final Long fileSize;
  private final Dimension dimension;
  private final String assetStatus;
  private final ValueMap properties;
  private final List<SmartCrop> smartCrops;

  private static final JsonMapper OBJECT_MAPPER = new JsonMapper();
  static final String RT_RENDITION_SMARTCROP = "dam/rendition/smartcrop";

  NextGenDynamicMediaMetadata(@Nullable String mimeType, @Nullable Long fileSize, @Nullable Dimension dimension,
      @Nullable String assetStatus, @Nullable ValueMap properties, @Nullable List<SmartCrop> smartCrops) {
    this.mimeType = mimeType;
    this.fileSize = fileSize;
    this.dimension = dimension;
    this.assetStatus = assetStatus;
    if (properties != null) {
      this.properties = properties;
    }
    else {
      this.properties = ValueMap.EMPTY;
    }
    if (smartCrops != null) {
      this.smartCrops = smartCrops;
    }
    else {
      this.smartCrops = Collections.emptyList();
    }
  }

  /**
   * @return Mime type
   */
  public @NotNull String getMimeType() {
    return Objects.toString(mimeType, ContentType.OCTET_STREAM);
  }

  /**
   * @return File size (in bytes) or null if not available
   */
  public @Nullable Long getFileSize() {
    return fileSize;
  }

  /**
   * @return Image Dimension or null if no image or dimension not available
   */
  public @Nullable Dimension getDimension() {
    return dimension;
  }

  /**
   * @return Asset review status
   */
  public String getAssetStatus() {
    return this.assetStatus;
  }

  /**
   * @return Asset properties
   */
  public ValueMap getProperties() {
    return properties;
  }

  /**
   * @return Named smart crop definitions.
   */
  public List<SmartCrop> getSmartCrops() {
    return Collections.unmodifiableList(smartCrops);
  }

  /**
   * @return true if metadata is valid (has mime type)
   */
  public boolean isValid() {
    return mimeType != null;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE)
        .append("mimeType", mimeType)
        .append("fileSize", fileSize)
        .append("dimension", dimension)
        .append("assetStatus", assetStatus)
        .append("properties", properties.isEmpty() ? null : new TreeMap<String, Object>(properties))
        .append("smartCrops", smartCrops.isEmpty() ? null : smartCrops)
        .toString();
  }

  /**
   * Converts JSON response from NGDM API to metadata object.
   * @param jsonResponse JSON response
   * @return Metadata object
   * @throws JsonProcessingException If JSON parsing fails
   */
  @SuppressWarnings("null")
  public static @NotNull NextGenDynamicMediaMetadata fromJson(@NotNull String jsonResponse) throws JsonProcessingException {
    MetadataResponse response = OBJECT_MAPPER.readValue(jsonResponse, MetadataResponse.class);
    RepositoryMetadata respositoryMetadata = response.repositoryMetadata;
    Map<String, Object> assetMetadata = response.assetMetadata;
    ValueMap properties = null;

    long width = 0;
    long height = 0;
    String assetStatus = null;
    if (assetMetadata != null) {
      properties = new ValueMapDecorator(assetMetadata);
      width = properties.get(TIFF_IMAGEWIDTH, 0L);
      height = properties.get(TIFF_IMAGELENGTH, 0L);
      // fallback to video-specific dimension properties
      if (width == 0) {
        width = properties.get("xmpDM:videoFrameSize_stDim:w", 0L);
      }
      if (height == 0) {
        height = properties.get("xmpDM:videoFrameSize_stDim:h", 0L);
      }
      assetStatus = properties.get("dam:assetStatus", String.class);
    }
    Dimension dimension = toDimension(width, height);

    String mimeType = null;
    Long fileSize = null;
    List<SmartCrop> smartCrops = null;
    if (respositoryMetadata != null) {
      mimeType = respositoryMetadata.dcFormat;
      fileSize = respositoryMetadata.repoSize;
      if (respositoryMetadata.smartCrops != null && dimension != null) {
        smartCrops = respositoryMetadata.smartCrops.entrySet().stream()
            .filter(entry -> isSmartCropDefinitionValid(entry.getKey(), entry.getValue()))
            .map(entry -> new SmartCrop(entry.getKey(), entry.getValue(), dimension))
            .collect(Collectors.toList());
      }
    }

    return new NextGenDynamicMediaMetadata(mimeType, fileSize, dimension, assetStatus, properties, smartCrops);
  }

  private static @Nullable Dimension toDimension(long width, long height) {
    if (width > 0 && height > 0) {
      return new Dimension(width, height);
    }
    return null;
  }

  private static boolean isSmartCropDefinitionValid(@NotNull String name, @NotNull MetadataResponse.SmartCrop smartCop) {
    return StringUtils.isNotBlank(name)
        && smartCop.normalizedWidth > 0
        && smartCop.normalizedHeight > 0
        && smartCop.left >= 0
        && smartCop.top >= 0;
  }

}
