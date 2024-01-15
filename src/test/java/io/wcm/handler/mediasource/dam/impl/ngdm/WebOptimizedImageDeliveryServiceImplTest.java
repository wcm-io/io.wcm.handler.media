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
package io.wcm.handler.mediasource.dam.impl.ngdm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.dam.MockAssetDelivery;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class WebOptimizedImageDeliveryServiceImplTest {

  private final AemContext context = new AemContext();

  @Test
  void testEnabled_AssetDeliveryNotPresent() {
    WebOptimizedImageDeliveryService underTest = context.registerInjectActivateService(WebOptimizedImageDeliveryServiceImpl.class);
    assertFalse(underTest.isEnabled());
  }

  @Test
  void testEnabled_AssetDeliveryPresent() {
    context.registerInjectActivateService(MockAssetDelivery.class);
    WebOptimizedImageDeliveryService underTest = context.registerInjectActivateService(WebOptimizedImageDeliveryServiceImpl.class);
    assertTrue(underTest.isEnabled());
  }

  @Test
  void testEnabled_AssetDeliveryPresent_Disabled() {
    context.registerInjectActivateService(MockAssetDelivery.class);
    WebOptimizedImageDeliveryService underTest = context.registerInjectActivateService(WebOptimizedImageDeliveryServiceImpl.class,
        "enabled", false);
    assertFalse(underTest.isEnabled());
  }

}
