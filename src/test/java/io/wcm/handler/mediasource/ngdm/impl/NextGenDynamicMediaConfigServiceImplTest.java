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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class NextGenDynamicMediaConfigServiceImplTest {

  private final AemContext context = AppAemContext.newAemContext();

  @BeforeEach
  void setUp() {
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

  @Test
  void testPropertiesDefaultConfig() {
    NextGenDynamicMediaConfigService underTest = context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class);
    assertTrue(underTest.enabled());
    assertEquals("/selector1", underTest.getAssetSelectorsJsUrl());
    assertEquals("/adobe/assets/{asset-id}/as/{seo-name}.{format}?accept-experimental=1", underTest.getImageDeliveryBasePath());
    assertEquals("/videopath1", underTest.getVideoDeliveryPath());
    assertEquals("/adobe/assets/{asset-id}/original/as/{seo-name}?accept-experimental=1", underTest.getAssetOriginalBinaryDeliveryPath());
    assertEquals("/adobe/assets/{asset-id}/metadata", underTest.getAssetMetadataPath());
    assertEquals(Map.of("X-Adobe-Accept-Experimental", "1"), underTest.getAssetMetadataHeaders());
    assertEquals("repo1", underTest.getRepositoryId());
    assertEquals("key1", underTest.getApiKey());
    assertEquals("env1", underTest.getEnv());
    assertEquals("client1", underTest.getImsClient());
  }

  @Test
  void testPropertiesEmptyConfig() {
    NextGenDynamicMediaConfigService underTest = context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class,
        "imageDeliveryBasePath", "",
        "assetOriginalBinaryDeliveryPath", "",
        "assetMetadataPath", "",
        "assetMetadataHeaders", new String[0]);
    assertTrue(underTest.enabled());
    assertEquals("/selector1", underTest.getAssetSelectorsJsUrl());
    assertEquals("/imagepath1", underTest.getImageDeliveryBasePath());
    assertEquals("/videopath1", underTest.getVideoDeliveryPath());
    assertEquals("/assetpath1", underTest.getAssetOriginalBinaryDeliveryPath());
    assertEquals("/metadatapath1", underTest.getAssetMetadataPath());
    assertEquals(Map.of(), underTest.getAssetMetadataHeaders());
    assertEquals("repo1", underTest.getRepositoryId());
    assertEquals("key1", underTest.getApiKey());
    assertEquals("env1", underTest.getEnv());
    assertEquals("client1", underTest.getImsClient());
  }

}
