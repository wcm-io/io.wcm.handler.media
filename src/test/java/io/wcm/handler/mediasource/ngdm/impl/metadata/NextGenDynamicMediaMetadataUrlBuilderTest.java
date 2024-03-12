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

import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_ASSET_ID;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_FILENAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigServiceImpl;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReference;
import io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class NextGenDynamicMediaMetadataUrlBuilderTest {

  private final AemContext context = AppAemContext.newAemContext();

  private NextGenDynamicMediaConfigService nextGenDynamicMediaConfig;

  @BeforeEach
  void setUp() throws Exception {
    context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class)
        .setRepositoryId("repo1");
    nextGenDynamicMediaConfig = context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class);
  }

  @Test
  void testBuild() {
    NextGenDynamicMediaMetadataUrlBuilder underTest = new NextGenDynamicMediaMetadataUrlBuilder(nextGenDynamicMediaConfig);

    assertEquals("https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/metadata",
        underTest.build(new NextGenDynamicMediaReference(SAMPLE_ASSET_ID, SAMPLE_FILENAME)));
  }

}
