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

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.dam.api.DamConstants.ASSET_STATUS_PROPERTY;
import static com.day.cq.dam.api.DamConstants.RENDITIONS_FOLDER;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.SmartCrop.PN_LEFT;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.SmartCrop.PN_NORMALIZED_HEIGHT;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.SmartCrop.PN_NORMALIZED_WIDTH;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.SmartCrop.PN_TOP;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.dam.api.Asset;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.mediasource.dam.AssetRendition;
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

  /**
   * Gets metadata from DAM asset.
   * @param asset Asset
   * @return Metadata object
   */
  @SuppressWarnings("null")
  public static @NotNull NextGenDynamicMediaMetadata fromAsset(@NotNull Asset asset) {
    String mimeType = asset.getMimeType();

    Dimension dimension = AssetRendition.getDimension(asset.getOriginal());
    String assetStatus = asset.getMetadataValueFromJcr(ASSET_STATUS_PROPERTY);
    List<SmartCrop> smartCrops = null;

    if (dimension != null) {
      smartCrops = getRenditionResources(asset)
          .filter(rendition -> rendition.isResourceType(RT_RENDITION_SMARTCROP))
          .map(rendition -> Map.entry(rendition.getName(), renditionToSmartCropDefinition(rendition)))
          .filter(entry -> isSmartCropDefinitionValid(entry.getKey(), entry.getValue()))
          .map(entry -> new SmartCrop(entry.getKey(), entry.getValue(), dimension))
          .collect(Collectors.toList());
    }

    return new NextGenDynamicMediaMetadata(mimeType, dimension, assetStatus, smartCrops);
  }

  private static Stream<Resource> getRenditionResources(@NotNull Asset asset) {
    Resource assetResource = asset.adaptTo(Resource.class);
    if (assetResource != null) {
      Resource renditionsFolder = assetResource.getChild(JCR_CONTENT + "/" + RENDITIONS_FOLDER);
      if (renditionsFolder != null) {
        return StreamSupport.stream(renditionsFolder.getChildren().spliterator(), false);
      }
    }
    return Stream.empty();
  }

  private static boolean isSmartCropDefinitionValid(@NotNull String name, @NotNull MetadataResponse.SmartCrop smartCop) {
    return StringUtils.isNotBlank(name)
        && smartCop.normalizedWidth > 0
        && smartCop.normalizedHeight > 0
        && smartCop.left >= 0
        && smartCop.top >= 0;
  }

  private static @NotNull MetadataResponse.SmartCrop renditionToSmartCropDefinition(Resource rendition) {
    MetadataResponse.SmartCrop result = new MetadataResponse.SmartCrop();
    Resource content = rendition.getChild(JCR_CONTENT);
    if (content != null) {
      ValueMap props = content.getValueMap();
      result.left = props.get(PN_LEFT, 0d);
      result.top = props.get(PN_TOP, 0d);
      result.normalizedWidth = props.get(PN_NORMALIZED_WIDTH, 0d);
      result.normalizedHeight = props.get(PN_NORMALIZED_HEIGHT, 0d);
    }
    return result;
  }

}
