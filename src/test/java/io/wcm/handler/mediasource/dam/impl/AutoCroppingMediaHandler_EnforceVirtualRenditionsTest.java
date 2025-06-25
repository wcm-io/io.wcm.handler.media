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
package io.wcm.handler.mediasource.dam.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Constants;

import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.testcontext.DummyMediaHandlerConfig;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * Same tests as in {@link AutoCroppingMediaHandlerTest}, but with forced
 * virtual renditions enabled.
 */
@ExtendWith(AemContextExtension.class)
class AutoCroppingMediaHandler_EnforceVirtualRenditionsTest extends AutoCroppingMediaHandlerTest {

  @Override
  @BeforeEach
  void setUp() {
    DummyMediaHandlerConfig config = new DummyMediaHandlerConfig();
    config.setEnforceVirtualRenditions(true);
    context.registerService(MediaHandlerConfig.class, config,
        Constants.SERVICE_RANKING, 500);

    super.setUp();
  }

}
