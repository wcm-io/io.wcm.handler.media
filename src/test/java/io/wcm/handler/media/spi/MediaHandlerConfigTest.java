/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.handler.media.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.wcm.handler.media.markup.DummyImageMediaMarkupBuilder;
import io.wcm.handler.media.markup.SimpleImageMediaMarkupBuilder;
import io.wcm.handler.mediasource.dam.DamMediaSource;

class MediaHandlerConfigTest {

  private MediaHandlerConfig underTest = new MediaHandlerConfig() {
    // inherit default
  };

  @Test
  void testGetSources() {
    assertEquals(1, underTest.getSources().size());
    assertEquals(DamMediaSource.class, underTest.getSources().get(0));
  }

  @Test
  void testGetMarkupBuilders() {
    assertEquals(2, underTest.getMarkupBuilders().size());
    assertEquals(SimpleImageMediaMarkupBuilder.class, underTest.getMarkupBuilders().get(0));
    assertEquals(DummyImageMediaMarkupBuilder.class, underTest.getMarkupBuilders().get(1));
  }

  @Test
  void testGetProcessors() {
    assertTrue(underTest.getPreProcessors().isEmpty());
    assertTrue(underTest.getPostProcessors().isEmpty());
  }

  @Test
  void testGetDefaultImageQuality() {
    assertEquals(1d, underTest.getDefaultImageQuality("image/png"), 0.001d);
    assertEquals(256d, underTest.getDefaultImageQuality("image/gif"), 0.001d);
    assertEquals(MediaHandlerConfig.DEFAULT_IMAGE_QUALITY, underTest.getDefaultImageQuality("image/jpeg"), 0.001d);
    assertEquals(MediaHandlerConfig.DEFAULT_IMAGE_QUALITY, underTest.getDefaultImageQuality("IMAGE/JPEG"), 0.001d);
    assertEquals(1d, underTest.getDefaultImageQuality(""), 0.001d);
    assertEquals(1d, underTest.getDefaultImageQuality(null), 0.001d);
  }

}
