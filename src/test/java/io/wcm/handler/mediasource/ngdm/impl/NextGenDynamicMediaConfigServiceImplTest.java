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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class NextGenDynamicMediaConfigServiceImplTest {

  private final AemContext context = AppAemContext.newAemContext();

  @Test
  void testPropertiesDefaultConfig() {
    registerNextGenDynamicMediaConfig(context);
    NextGenDynamicMediaConfigService underTest = context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class,
        "enabledLocalAssets", true,
        "localAssetsRepositoryId", "localrepo1");
    assertTrue(underTest.isEnabledRemoteAssets());
    assertTrue(underTest.isEnabledLocalAssets());
    assertEquals("/selector1", underTest.getAssetSelectorsJsUrl());
    assertEquals("/adobe/assets/{asset-id}/as/{seo-name}.{format}", underTest.getImageDeliveryBasePath());
    assertEquals("/videopath1", underTest.getVideoDeliveryPath());
    assertEquals("/adobe/assets/{asset-id}/original/as/{seo-name}", underTest.getAssetOriginalBinaryDeliveryPath());
    assertEquals("/adobe/assets/{asset-id}/metadata", underTest.getAssetMetadataPath());
    assertEquals("repo1", underTest.getRemoteAssetsRepositoryId());
    assertEquals("localrepo1", underTest.getLocalAssetsRepositoryId());
    assertEquals("key1", underTest.getApiKey());
    assertEquals("env1", underTest.getEnv());
    assertEquals("client1", underTest.getImsClient());
  }

  @Test
  void testPropertiesEmptyConfig() {
    registerNextGenDynamicMediaConfig(context);
    NextGenDynamicMediaConfigService underTest = context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class,
        "enabledRemoteAssets", false,
        "enabledLocalAssets", false,
        "localAssetsRepositoryId", "",
        "imageDeliveryBasePath", "",
        "assetOriginalBinaryDeliveryPath", "",
        "assetMetadataPath", "");
    assertFalse(underTest.isEnabledRemoteAssets());
    assertFalse(underTest.isEnabledLocalAssets());
    assertEquals("/selector1", underTest.getAssetSelectorsJsUrl());
    assertEquals("/imagepath1", underTest.getImageDeliveryBasePath());
    assertEquals("/videopath1", underTest.getVideoDeliveryPath());
    assertEquals("/assetpath1", underTest.getAssetOriginalBinaryDeliveryPath());
    assertEquals("/metadatapath1", underTest.getAssetMetadataPath());
    assertEquals("repo1", underTest.getRemoteAssetsRepositoryId());
    assertEquals("", underTest.getLocalAssetsRepositoryId());
    assertEquals("key1", underTest.getApiKey());
    assertEquals("env1", underTest.getEnv());
    assertEquals("client1", underTest.getImsClient());
  }

  @Test
  void testNoNextGenDynamicMediaConfig() {
    NextGenDynamicMediaConfigService underTest = context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class);
    assertFalse(underTest.isEnabledRemoteAssets());
    assertFalse(underTest.isEnabledLocalAssets());
    assertNull(underTest.getAssetSelectorsJsUrl());
    assertEquals("/adobe/assets/{asset-id}/as/{seo-name}.{format}", underTest.getImageDeliveryBasePath());
    assertNull(underTest.getVideoDeliveryPath());
    assertEquals("/adobe/assets/{asset-id}/original/as/{seo-name}", underTest.getAssetOriginalBinaryDeliveryPath());
    assertEquals("/adobe/assets/{asset-id}/metadata", underTest.getAssetMetadataPath());
    assertNull(underTest.getRemoteAssetsRepositoryId());
    assertNull(underTest.getApiKey());
    assertNull(underTest.getEnv());
    assertNull(underTest.getImsClient());
  }

  private static void registerNextGenDynamicMediaConfig(AemContext context) {
    MockNextGenDynamicMediaConfig config = context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class);
    config.setEnabled(true);
    config.setAssetSelectorsJsUrl("/selector1");
    config.setImageDeliveryBasePath("/imagepath1");
    config.setVideoDeliveryPath("/videopath1");
    config.setAssetOriginalBinaryDeliveryPath("/assetpath1");
    config.setAssetMetadataPath("/metadatapath1");
    config.setRepositoryId("repo1");
    config.setApiKey("key1");
    config.setEnv("env1");
    config.setImsClient("client1");
  }

}
