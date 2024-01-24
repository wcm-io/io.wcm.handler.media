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
package io.wcm.handler.mediasource.ngdm.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parses and validates Next Generation Dynamic Media references.
 * <p>
 * Example: <code>/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/my-image.jpg</code>
 * </p>
 */
public final class NextGenDynamicMediaReference {

  private static final Pattern REFERENCE_PATTERN = Pattern.compile("^/(urn:[^/]+)/([^/]+)$");
  private static final String ASSET_ID_PREFIX = "urn:";

  private final String assetId;
  private final String fileName;

  /**
   * @param assetId Asset ID (has to start with "urn:")
   * @param fileName File name
   */
  public NextGenDynamicMediaReference(@NotNull String assetId, @NotNull String fileName) {
    if (!StringUtils.startsWith(assetId, ASSET_ID_PREFIX)) {
      throw new IllegalArgumentException("Asset ID must start with '" + ASSET_ID_PREFIX + "'");
    }
    this.assetId = assetId;
    this.fileName = fileName;
  }

  /**
   * @return Asset ID
   */
  public @NotNull String getAssetId() {
    return assetId;
  }

  /**
   * @return File name
   */
  public @NotNull String getFileName() {
    return fileName;
  }

  /**
   * @return Reference
   */
  public @NotNull String toReference() {
    return "/" + assetId + "/" + fileName;
  }

  /**
   * Parses a next generation dynamic media reference.
   * @param reference Reference
   * @return Parsed reference or null if reference is invalid
   */
  public static @Nullable NextGenDynamicMediaReference fromReference(@Nullable String reference) {
    if (reference == null) {
      return null;
    }
    Matcher matcher = REFERENCE_PATTERN.matcher(reference);
    if (!matcher.matches()) {
      return null;
    }
    String assetId = matcher.group(1);
    String fileName = matcher.group(2);
    return new NextGenDynamicMediaReference(assetId, fileName);
  }

  /**
   * Checks if given string is a valid next generation dynamic media reference.
   * @param reference Reference
   * @return true if reference is valid
   */
  public static boolean isReference(@Nullable String reference) {
    return reference != null && REFERENCE_PATTERN.matcher(reference).matches();
  }

  @Override
  public String toString() {
    return toReference();
  }

}
