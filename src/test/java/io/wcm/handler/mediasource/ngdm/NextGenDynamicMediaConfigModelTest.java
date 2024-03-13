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
package io.wcm.handler.mediasource.ngdm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;

import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigServiceImpl;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class NextGenDynamicMediaConfigModelTest {

  private final AemContext context = AppAemContext.newAemContext();

  @BeforeEach
  void setUp() {
    MockNextGenDynamicMediaConfig config = context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class);
    config.setEnabled(true);
    config.setAssetSelectorsJsUrl("/selector1");
    config.setRepositoryId("repo1");
    config.setApiKey("key1");
    config.setEnv("env1");
    config.setImsClient("client1");
  }

  @Test
  void testWithConfigService() throws JSONException {
    context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class);

    NextGenDynamicMediaConfigModel underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaConfigModel.class);
    assertTrue(underTest.isEnabled());
    assertEquals("/selector1", underTest.getAssetSelectorsJsUrl());
    JSONAssert.assertEquals("{repositoryId:'repo1',apiKey:'key1',env:'env1'}",
        underTest.getConfigJson(), true);
  }

  @Test
  void testWithoutConfigService() {
    NextGenDynamicMediaConfigModel underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaConfigModel.class);
    assertFalse(underTest.isEnabled());
    assertNull(underTest.getAssetSelectorsJsUrl());
    assertNull(underTest.getConfigJson());
  }

}
