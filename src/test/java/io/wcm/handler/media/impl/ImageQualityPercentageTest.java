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
package io.wcm.handler.media.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.spi.MediaHandlerConfig;

@ExtendWith(MockitoExtension.class)
class ImageQualityPercentageTest {

  @Mock
  MediaHandlerConfig mediaHandlerConfig;

  @BeforeEach
  void setUp() {
    when(mediaHandlerConfig.getDefaultImageQualityPercentage()).thenReturn(0.75d);
  }

  @Test
  void testGet() {
    assertEquals(0.75d, ImageQualityPercentage.get(new MediaArgs(), mediaHandlerConfig));
    assertEquals(0.9d, ImageQualityPercentage.get(new MediaArgs().imageQualityPercentage(0.9d), mediaHandlerConfig));
  }

  @Test
  void testGetAsInteger() {
    assertEquals(75, ImageQualityPercentage.getAsInteger(new MediaArgs(), mediaHandlerConfig));
    assertEquals(90, ImageQualityPercentage.getAsInteger(new MediaArgs().imageQualityPercentage(0.9d), mediaHandlerConfig));
  }

}
