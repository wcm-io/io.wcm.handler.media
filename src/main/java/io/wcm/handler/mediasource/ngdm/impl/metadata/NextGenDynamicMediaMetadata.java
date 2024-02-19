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

/**
 * Metadata for Next Gen Dynamic Media asset fetched from the HTTP API.
 */
public final class NextGenDynamicMediaMetadata {

  private final long width;
  private final long height;
  private final String mimeType;

  private static final JsonMapper OBJECT_MAPPER = new JsonMapper();

  NextGenDynamicMediaMetadata(long width, long height, @Nullable String mimeType) {
    this.width = width;
    this.height = height;
    this.mimeType = mimeType;
  }

  /**
   * @return Width
   */
  public long getWidth() {
    return width;
  }

  /**
   * @return Height
   */
  public long getHeight() {
    return height;
  }

  /**
   * @return Mime type
   */
  public @Nullable String getMimeType() {
    return mimeType;
  }

  boolean isValid() {
    return width > 0 && height > 0 && mimeType != null;
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

    long width = 0;
    long height = 0;
    if (response.assetMetadata != null) {
      width = response.assetMetadata.tiffImageWidth;
      height = response.assetMetadata.tiffImageLength;
    }

    String mimeType = null;
    if (response.repositoryMetadata != null) {
      mimeType = response.repositoryMetadata.dcFormat;
    }

    return new NextGenDynamicMediaMetadata(width, height, mimeType);
  }

}
