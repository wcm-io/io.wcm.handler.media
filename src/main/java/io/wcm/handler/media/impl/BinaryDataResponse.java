/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2025 wcm.io
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
package io.wcm.handler.media.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Response object for getting binary data or redirect URL for {@link AbstractMediaFileServlet}.
 */
final class BinaryDataResponse {

  private final byte[] binaryData;
  private final String redirectUrl;

  private BinaryDataResponse(byte @Nullable [] binaryData, @Nullable String redirectUrl) {
    this.binaryData = binaryData;
    this.redirectUrl = redirectUrl;
  }

  byte @Nullable [] getBinaryData() {
    return this.binaryData;
  }

  @Nullable
  String getRedirectUrl() {
    return this.redirectUrl;
  }

  static BinaryDataResponse binaryData(byte @NotNull [] binaryData) {
    return new BinaryDataResponse(binaryData, null);
  }

  static BinaryDataResponse redirectUrl(@NotNull String redirectUrl) {
    return new BinaryDataResponse(null, redirectUrl);
  }

  static BinaryDataResponse invalid() {
    return new BinaryDataResponse(null, null);
  }

}
