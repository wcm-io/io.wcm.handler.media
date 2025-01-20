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

import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService.PLACEHOLDER_ASSET_ID;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService.PLACEHOLDER_SEO_NAME;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builds URL to reference a binary file via NextGen Dynamic Media.
 *
 * <p>
 * Example URL that might be build:
 * https://host/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/original/as/my-file.pdf
 * </p>
 */
public final class NextGenDynamicMediaBinaryUrlBuilder {

  private final NextGenDynamicMediaContext context;

  static final String PARAM_ATTACHMENT = "attachment";

  /**
   * @param context Context
   */
  public NextGenDynamicMediaBinaryUrlBuilder(@NotNull NextGenDynamicMediaContext context) {
    this.context = context;
  }

  /**
   * Builds the URL for a binary.
   * @return URL or null if invalid/not possible
   */
  public @Nullable String build(boolean contentDispositionAttachment) {

    // get parameters from nextgen dynamic media config for URL parameters
    String repositoryId;
    if (context.getReference().getAsset() != null) {
      repositoryId = context.getNextGenDynamicMediaConfig().getLocalAssetsRepositoryId();
    }
    else {
      repositoryId = context.getNextGenDynamicMediaConfig().getRemoteAssetsRepositoryId();
    }
    String binaryDeliveryPath = context.getNextGenDynamicMediaConfig().getAssetOriginalBinaryDeliveryPath();
    if (StringUtils.isAnyBlank(repositoryId, binaryDeliveryPath)) {
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
    if (contentDispositionAttachment) {
      url.append("?").append(PARAM_ATTACHMENT).append("=true");
    }
    return url.toString();
  }

}
