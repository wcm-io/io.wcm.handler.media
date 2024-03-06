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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.wcm.handler.media.Dimension;

/**
 * Metadata for Next Gen Dynamic Media asset fetched from the HTTP API.
 */
public final class NextGenDynamicMediaMetadata {

  private final String mimeType;
  private final Dimension dimension;

  private static final JsonMapper OBJECT_MAPPER = new JsonMapper();

  NextGenDynamicMediaMetadata(@Nullable String mimeType, long width, long height) {
    this.mimeType = mimeType;
    if (width > 0 && height > 0) {
      this.dimension = new Dimension(width, height);
    }
    else {
      this.dimension = null;
    }
  }

  /**
   * @return Mime type
   */
  public @Nullable String getMimeType() {
    return mimeType;
  }

  /**
   * @return Image Dimension or null if no image or dimension not available
   */
  public @Nullable Dimension getDimension() {
    return dimension;
  }

  boolean isValid() {
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

    String mimeType = null;
    if (response.repositoryMetadata != null) {
      mimeType = response.repositoryMetadata.dcFormat;
    }

    long width = 0;
    long height = 0;
    if (response.assetMetadata != null) {
      width = response.assetMetadata.tiffImageWidth;
      height = response.assetMetadata.tiffImageLength;
    }

    return new NextGenDynamicMediaMetadata(mimeType, width, height);
  }

}
