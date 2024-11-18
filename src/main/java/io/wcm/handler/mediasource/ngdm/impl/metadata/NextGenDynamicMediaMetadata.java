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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataResponse.AssetMetadata;
import io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataResponse.RepositoryMetadata;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * Metadata for Next Gen Dynamic Media asset fetched from the HTTP API.
 */
public final class NextGenDynamicMediaMetadata {

  private final String mimeType;
  private final Dimension dimension;
  private final String assetStatus;
  private final List<SmartCrop> smartCrops;

  private static final JsonMapper OBJECT_MAPPER = new JsonMapper();
  static final String RT_RENDITION_SMARTCROP = "dam/rendition/smartcrop";

  NextGenDynamicMediaMetadata(@Nullable String mimeType, @Nullable Dimension dimension,
      @Nullable String assetStatus, @Nullable List<SmartCrop> smartCrops) {
    this.mimeType = mimeType;
    this.dimension = dimension;
    this.assetStatus = assetStatus;
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
    return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
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
    AssetMetadata assetMetadata = response.assetMetadata;

    long width = 0;
    long height = 0;
    String assetStatus = null;
    if (assetMetadata != null) {
      width = assetMetadata.tiffImageWidth;
      height = assetMetadata.tiffImageLength;
      assetStatus = assetMetadata.assetStatus;
    }
    Dimension dimension = toDimension(width, height);

    String mimeType = null;
    List<SmartCrop> smartCrops = null;
    if (respositoryMetadata != null) {
      mimeType = respositoryMetadata.dcFormat;
      if (respositoryMetadata.smartCrops != null && dimension != null) {
        smartCrops = respositoryMetadata.smartCrops.entrySet().stream()
            .filter(entry -> isSmartCropDefinitionValid(entry.getKey(), entry.getValue()))
            .map(entry -> new SmartCrop(entry.getKey(), entry.getValue(), dimension))
            .collect(Collectors.toList());
      }
    }

    return new NextGenDynamicMediaMetadata(mimeType, dimension, assetStatus, smartCrops);
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
