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

import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService.PLACEHOLDER_ASSET_ID;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReference;

/**
 * Builds URL to reference a asset metadata via NextGen Dynamic Media.
 * <p>
 * Example URL that might be build:
 * https://host/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/metadata
 * </p>
 */
final class NextGenDynamicMediaMetadataUrlBuilder {

  private final NextGenDynamicMediaConfigService config;

  /**
   * @param config Config
   */
  NextGenDynamicMediaMetadataUrlBuilder(@NotNull NextGenDynamicMediaConfigService config) {
    this.config = config;
  }

  /**
   * Builds the URL for metadata.
   * @return URL or null if invalid/not possible
   */
  public @Nullable String build(@NotNull NextGenDynamicMediaReference reference) {

    // get parameters from nextgen dynamic media config for URL parameters
    String repositoryId = config.getRemoteAssetsRepositoryId();
    String metadataPath = config.getAssetMetadataPath();
    if (StringUtils.isAnyEmpty(repositoryId, metadataPath)) {
      return null;
    }

    // replace placeholders in delivery path
    metadataPath = StringUtils.replace(metadataPath, PLACEHOLDER_ASSET_ID, reference.getAssetId());

    // build URL
    StringBuilder url = new StringBuilder();
    if (StringUtils.startsWith(repositoryId, "localhost:")) {
      // switch to HTTP for unit tests/local testing
      url.append("http");
    }
    else {
      url.append("https");
    }
    url.append("://")
        .append(repositoryId)
        .append(metadataPath);
    return url.toString();
  }

}
