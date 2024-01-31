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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builds URL to reference a binary file via NextGen Dynamic Media.
 * <p>
 * Example URL that might be build:
 * https://host/adobe/assets/deliver/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/my-file.pdf
 * </p>
 */
public final class NextGenDynamicMediaBinaryUrlBuilder {

  static final String PLACEHOLDER_ASSET_ID = "{asset-id}";
  static final String PLACEHOLDER_SEO_NAME = "{seo-name}";

  private final NextGenDynamicMediaContext context;

  /**
   * @param context Context
   */
  public NextGenDynamicMediaBinaryUrlBuilder(@NotNull NextGenDynamicMediaContext context) {
    this.context = context;
  }

  /**
   * Builds the URL for a rendition.
   * @return URL or null if invalid/not possible
   */
  public @Nullable String build() {

    // get parameters from nextgen dynamic media config for URL parameters
    String repositoryId = context.getNextGenDynamicMediaConfig().getRepositoryId();
    String binaryDeliveryPath = context.getNextGenDynamicMediaConfig().getAssetOriginalBinaryDeliveryPath();
    if (StringUtils.isAnyEmpty(repositoryId, binaryDeliveryPath)) {
      return null;
    }

    // replace placeholders in delivery path
    String seoName = context.getReference().getFileName();
    binaryDeliveryPath = StringUtils.replace(binaryDeliveryPath, PLACEHOLDER_ASSET_ID, context.getReference().getAssetId());
    binaryDeliveryPath = StringUtils.replace(binaryDeliveryPath, PLACEHOLDER_SEO_NAME, seoName);

    // build URL
    StringBuilder url = new StringBuilder();
    url.append("https://")
        .append(repositoryId)
        .append(binaryDeliveryPath);
    return url.toString();
  }

}
