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
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
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
      name = "wcm.io Media Handler Dynamic Media with OpenAPI Support",
      description = "Support for Next Generation Dynamic Media.")
  @interface Config {

    @AttributeDefinition(
        name = "Remote Assets",
        description = "Enable Dynamic Media with OpenAPI for remote assets.")
    boolean enabledRemoteAssets() default true;

    @AttributeDefinition(
        name = "Local Assets",
        description = "Enable Next Dynamic Media with OpenAPI for local assets in this AEMaaCS instance.")
    boolean enabledLocalAssets() default false;

    @AttributeDefinition(
        name = "Repository ID for Local Assets",
        description = "Dynamic Media with OpenAPI Delivery host name for local assets. Mandatory if local assets is enabled.")
    String localAssetsRepositoryId();

    @AttributeDefinition(
        name = "Image Delivery Base Path",
        description = "Base path with placeholders to deliver image renditions. "
            + "Placeholders: " + PLACEHOLDER_ASSET_ID + ", " + PLACEHOLDER_SEO_NAME + ", " + PLACEHOLDER_FORMAT + ". "
            + "If not set, the default value from the NextGenDynamicMediaConfig service will be used.")
    String imageDeliveryBasePath() default ADOBE_ASSETS_PREFIX + PLACEHOLDER_ASSET_ID + "/as/"
        + PLACEHOLDER_SEO_NAME + "." + PLACEHOLDER_FORMAT;

    @AttributeDefinition(
        name = "Asset Original Binary Delivery Path",
        description = "Base path with placeholders to deliver asset original binaries. "
            + "Placeholders: " + PLACEHOLDER_ASSET_ID + ", " + PLACEHOLDER_SEO_NAME + ". "
            + "If not set, the default value from the NextGenDynamicMediaConfig service will be used.")
    String assetOriginalBinaryDeliveryPath() default ADOBE_ASSETS_PREFIX + PLACEHOLDER_ASSET_ID + "/original/as/"
        + PLACEHOLDER_SEO_NAME;

    @AttributeDefinition(
        name = "Asset Metadata Path",
        description = "Base path to get asset metadata. "
            + "Placeholder: " + PLACEHOLDER_ASSET_ID + ". "
            + "If not set, the default value from the NextGenDynamicMediaConfig service will be used.")
    String assetMetadataPath() default ADOBE_ASSETS_PREFIX + PLACEHOLDER_ASSET_ID + "/metadata";

    @AttributeDefinition(
        name = "Default image width/height",
        description = "Default width/height (longest edge) when requesting image renditions without explicit dimension.")
    long imageWidthHeightDefault() default 2048;

  }

  private static final String ADOBE_ASSETS_PREFIX = "/adobe/assets/";
  private static final Logger log = LoggerFactory.getLogger(NextGenDynamicMediaConfigServiceImpl.class);

  private boolean enabledRemoteAssets;
  private boolean enabledLocalAssets;
  private String localAssetsRepositoryId;
  private String imageDeliveryBasePath;
  private String assetOriginalBinaryDeliveryPath;
  private String assetMetadataPath;
  private long imageWidthHeightDefault;

  @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
  private NextGenDynamicMediaConfig nextGenDynamicMediaConfig;

  @Activate
  private void activate(Config config) {
    enabledRemoteAssets = config.enabledRemoteAssets();
    if (enabledRemoteAssets) {
      if (nextGenDynamicMediaConfig == null) {
        log.debug("NextGenDynamicMediaConfig service is not available, disable remote assets.");
        enabledRemoteAssets = false;
      }
      else {
        log.debug("NextGenDynamicMediaConfig: enabled={}, repositoryId={}, apiKey={}, env={}, imsClient={}",
            nextGenDynamicMediaConfig.enabled(), nextGenDynamicMediaConfig.getRepositoryId(),
            nextGenDynamicMediaConfig.getApiKey(), nextGenDynamicMediaConfig.getEnv(), nextGenDynamicMediaConfig.getImsClient());
      }
    }

    imageDeliveryBasePath = StringUtils.defaultIfBlank(config.imageDeliveryBasePath(),
        nextGenDynamicMediaConfig != null ? nextGenDynamicMediaConfig.getImageDeliveryBasePath() : null);
    assetOriginalBinaryDeliveryPath = StringUtils.defaultIfBlank(config.assetOriginalBinaryDeliveryPath(),
        nextGenDynamicMediaConfig != null ? nextGenDynamicMediaConfig.getAssetOriginalBinaryDeliveryPath() : null);
    assetMetadataPath = StringUtils.defaultIfBlank(config.assetMetadataPath(),
        nextGenDynamicMediaConfig != null ? nextGenDynamicMediaConfig.getAssetMetadataPath() : null);

    enabledLocalAssets = config.enabledLocalAssets();
    localAssetsRepositoryId = config.localAssetsRepositoryId();
    if (enabledLocalAssets && StringUtils.isBlank(localAssetsRepositoryId)) {
      log.debug("localAssetsRepositoryId is not configured, disable local assets.");
      enabledLocalAssets = false;
    }

    imageWidthHeightDefault = config.imageWidthHeightDefault();
  }

  @Override
  public boolean isEnabledRemoteAssets() {
    return enabledRemoteAssets && nextGenDynamicMediaConfig != null && nextGenDynamicMediaConfig.enabled();
  }

  @Override
  public boolean isEnabledLocalAssets() {
    return enabledLocalAssets;
  }

  @Override
  public @Nullable String getAssetSelectorsJsUrl() {
    return nextGenDynamicMediaConfig != null ? nextGenDynamicMediaConfig.getAssetSelectorsJsUrl() : null;
  }

  @Override
  public @Nullable String getImageDeliveryBasePath() {
    return imageDeliveryBasePath;
  }

  @Override
  public @Nullable String getVideoDeliveryPath() {
    return nextGenDynamicMediaConfig != null ? nextGenDynamicMediaConfig.getVideoDeliveryPath() : null;
  }

  @Override
  public @Nullable String getAssetOriginalBinaryDeliveryPath() {
    return assetOriginalBinaryDeliveryPath;
  }

  @Override
  public @Nullable String getAssetMetadataPath() {
    return assetMetadataPath;
  }

  @Override
  public @Nullable String getRemoteAssetsRepositoryId() {
    return nextGenDynamicMediaConfig != null ? nextGenDynamicMediaConfig.getRepositoryId() : null;
  }

  @Override
  public @Nullable String getLocalAssetsRepositoryId() {
    return localAssetsRepositoryId;
  }

  @Override
  public @Nullable String getApiKey() {
    return nextGenDynamicMediaConfig != null ? nextGenDynamicMediaConfig.getApiKey() : null;
  }

  @Override
  public @Nullable String getEnv() {
    return nextGenDynamicMediaConfig != null ? nextGenDynamicMediaConfig.getEnv() : null;
  }

  @Override
  public @Nullable String getImsClient() {
    return nextGenDynamicMediaConfig != null ? nextGenDynamicMediaConfig.getImsClient() : null;
  }

  @Override
  public long getImageWidthHeightDefault() {
    return imageWidthHeightDefault;
  }

}
