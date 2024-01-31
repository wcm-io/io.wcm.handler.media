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

  private NextGenDynamicMediaConfigService underTest;

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
    underTest = context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class);
  }

  @Test
  void testProperties() {
    assertTrue(underTest.enabled());
    assertEquals("/selector1", underTest.getAssetSelectorsJsUrl());
    assertEquals("/imagepath1", underTest.getImageDeliveryBasePath());
    assertEquals("/videopath1", underTest.getVideoDeliveryPath());
    assertEquals("/assetpath1", underTest.getAssetOriginalBinaryDeliveryPath());
    assertEquals("/metadatapath1", underTest.getAssetMetadataPath());
    assertEquals("repo1", underTest.getRepositoryId());
    assertEquals("key1", underTest.getApiKey());
    assertEquals("env1", underTest.getEnv());
    assertEquals("client1", underTest.getImsClient());
  }

}
