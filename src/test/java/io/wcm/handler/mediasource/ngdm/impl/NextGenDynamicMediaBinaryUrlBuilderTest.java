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

import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_REFERENCE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.sling.commons.mime.MimeTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class NextGenDynamicMediaBinaryUrlBuilderTest {

  private final AemContext context = AppAemContext.newAemContext();

  private NextGenDynamicMediaConfigService nextGenDynamicMediaConfig;
  private MediaHandlerConfig mediaHandlerConfig;
  private MimeTypeService mimeTypeService;

  @BeforeEach
  void setUp() throws Exception {
    context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class)
        .setRepositoryId("repo1");
    nextGenDynamicMediaConfig = context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class);

    mediaHandlerConfig = AdaptTo.notNull(context.request(), MediaHandlerConfig.class);
    mimeTypeService = context.getService(MimeTypeService.class);
  }

  @Test
  void testBuild() {
    NextGenDynamicMediaBinaryUrlBuilder underTest = getBuilder(new MediaArgs());

    assertEquals("https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/original/as/my-image.jpg?accept-experimental=1",
        underTest.build());
  }

  @SuppressWarnings("null")
  private NextGenDynamicMediaBinaryUrlBuilder getBuilder(MediaArgs mediaArgs) {
    NextGenDynamicMediaContext ctx = new NextGenDynamicMediaContext(
        NextGenDynamicMediaReference.fromReference(SAMPLE_REFERENCE),
        null,
        null,
        mediaArgs,
        nextGenDynamicMediaConfig,
        mediaHandlerConfig,
        mimeTypeService);
    return new NextGenDynamicMediaBinaryUrlBuilder(ctx);
  }

}
