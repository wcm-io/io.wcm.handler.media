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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.ui.wcm.commons.config.NextGenDynamicMediaConfig;

/**
 * Wraps access to NextGenDynamicMediaConfig - which is deployed but not accessible on AEM 6.5.
 */
@Component(service = NextGenDynamicMediaConfigService.class, immediate = true)
@Designate(ocd = NextGenDynamicMediaConfigServiceImpl.Config.class)
public class NextGenDynamicMediaConfigServiceImpl implements NextGenDynamicMediaConfigService {

  @ObjectClassDefinition(
      name = "wcm.io Next Generation Dynamic Media Support",
      description = "Support for Next Generation Dynamic Media.")
  @interface Config {

    @AttributeDefinition(
        name = "Image Delivery Base Path",
        description = "Base path with placeholders to deliver image renditions. "
            + "Placeholders: " + PLACEHOLDER_ASSET_ID + ", " + PLACEHOLDER_SEO_NAME + ", " + PLACEHOLDER_FORMAT + ". "
            + "If not set, the default value from the NextGenDynamicMediaConfig service will be used.")
    String imageDeliveryBasePath() default ADOBE_ASSETS_PREFIX + PLACEHOLDER_ASSET_ID + "/as/"
        + PLACEHOLDER_SEO_NAME + "." + PLACEHOLDER_FORMAT + "?accept-experimental=1";

    @AttributeDefinition(
        name = "Asset Original Binary Delivery Path",
        description = "Base path with placeholders to deliver asset original binaries. "
            + "Placeholders: " + PLACEHOLDER_ASSET_ID + ", " + PLACEHOLDER_SEO_NAME + ". "
            + "If not set, the default value from the NextGenDynamicMediaConfig service will be used.")
    String assetOriginalBinaryDeliveryPath() default ADOBE_ASSETS_PREFIX + PLACEHOLDER_ASSET_ID + "/original/as/"
        + PLACEHOLDER_SEO_NAME + "?accept-experimental=1";

    @AttributeDefinition(
        name = "Asset Metadata Path",
        description = "Base path to get asset metadata. "
            + "Placeholder: " + PLACEHOLDER_ASSET_ID + ". "
            + "If not set, the default value from the NextGenDynamicMediaConfig service will be used.")
    String assetMetadataPath() default ADOBE_ASSETS_PREFIX + PLACEHOLDER_ASSET_ID + "/metadata";

    @AttributeDefinition(
        name = "Asset Metadata Headers",
        description = "HTTP headers to be send with the asset metadata request. "
            + "Format: 'header1:value1'.")
    String[] assetMetadataHeaders() default { "X-Adobe-Accept-Experimental:1" };

  }

  private static final String ADOBE_ASSETS_PREFIX = "/adobe/assets/";
  private static final Logger log = LoggerFactory.getLogger(NextGenDynamicMediaConfigServiceImpl.class);

  private String imageDeliveryBasePath;
  private String assetOriginalBinaryDeliveryPath;
  private String assetMetadataPath;
  private Map<String, String> assetMetadataHeaders;

  @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY)
  private NextGenDynamicMediaConfig nextGenDynamicMediaConfig;

  @Activate
  private void activate(Config config) {
    log.debug("NGDM config: enabled={}, repositoryId={}, apiKey={}, env={}, imsClient={}",
        enabled(), getRepositoryId(), getApiKey(), getEnv(), getImsClient());

    this.imageDeliveryBasePath = StringUtils.defaultIfBlank(config.imageDeliveryBasePath(),
        this.nextGenDynamicMediaConfig.getImageDeliveryBasePath());
    this.assetOriginalBinaryDeliveryPath = StringUtils.defaultIfBlank(config.assetOriginalBinaryDeliveryPath(),
        this.nextGenDynamicMediaConfig.getAssetOriginalBinaryDeliveryPath());
    this.assetMetadataPath = StringUtils.defaultIfBlank(config.assetMetadataPath(),
        this.nextGenDynamicMediaConfig.getAssetMetadataPath());
    this.assetMetadataHeaders = headersToMap(config.assetMetadataHeaders());

  }

  private static Map<String, String> headersToMap(String[] headers) {
    Map<String, String> map = new LinkedHashMap<>();
    for (String header : headers) {
      String[] parts = header.split(":", 2);
      if (parts.length == 2) {
        map.put(parts[0], parts[1]);
      }
    }
    return map;
  }

  @Override
  public boolean enabled() {
    return this.nextGenDynamicMediaConfig.enabled();
  }

  @Override
  public String getAssetSelectorsJsUrl() {
    return this.nextGenDynamicMediaConfig.getAssetSelectorsJsUrl();
  }

  @Override
  public String getImageDeliveryBasePath() {
    return imageDeliveryBasePath;
  }

  @Override
  public String getVideoDeliveryPath() {
    return this.nextGenDynamicMediaConfig.getVideoDeliveryPath();
  }

  @Override
  public String getAssetOriginalBinaryDeliveryPath() {
    return assetOriginalBinaryDeliveryPath;
  }

  @Override
  public String getAssetMetadataPath() {
    return assetMetadataPath;
  }

  @Override
  public @NotNull Map<String, String> getAssetMetadataHeaders() {
    return assetMetadataHeaders;
  }

  @Override
  public String getRepositoryId() {
    return this.nextGenDynamicMediaConfig.getRepositoryId();
  }

  @Override
  public String getApiKey() {
    return this.nextGenDynamicMediaConfig.getApiKey();
  }

  @Override
  public String getEnv() {
    return this.nextGenDynamicMediaConfig.getEnv();
  }

  @Override
  public String getImsClient() {
    return this.nextGenDynamicMediaConfig.getImsClient();
  }

}
