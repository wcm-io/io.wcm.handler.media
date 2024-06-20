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

import static io.wcm.handler.mediasource.ngdm.impl.MediaArgsDimension.getFirstMediaFormat;
import static io.wcm.handler.mediasource.ngdm.impl.MediaArgsDimension.getRequestedDimension;
import static io.wcm.handler.mediasource.ngdm.impl.MediaArgsDimension.getRequestedRatio;
import static io.wcm.handler.mediasource.ngdm.impl.MediaArgsDimension.getRequestedRatioAsWidthHeight;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.drew.lang.annotations.NotNull;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatBuilder;
import io.wcm.handler.media.format.Ratio;

class MediaArgsDimensionTest {

  @Test
  void testGetRequestedDimension() {
    assertEquals(dimension(100, 50), getRequestedDimension(new MediaArgs().fixedDimension(100, 50)));
    assertEquals(new Dimension(160, 90), getRequestedDimension(mediaFormat(mf -> mf.width(160).height(90))));
    assertEquals(new Dimension(160, 90), getRequestedDimension(mediaFormat(mf -> mf.minWidth(160).minHeight(90))));
    assertEquals(new Dimension(0, 0), getRequestedDimension(new MediaArgs()));
  }

  @Test
  void testGetRequestedRatio() {
    assertEquals(2d / 1d, getRequestedRatio(new MediaArgs().fixedDimension(100, 50)));
    assertEquals(16d / 9d, getRequestedRatio(mediaFormat(mf -> mf.width(160).height(90))));
    assertEquals(2d, getRequestedRatio(mediaFormat(mf -> mf.ratio(2d))));
    assertEquals(2d / 1.5d, getRequestedRatio(mediaFormat(mf -> mf.ratio(2d, 1.5d))));
    assertEquals(16d / 9d, getRequestedRatio(mediaFormat(mf -> mf.ratio(16L, 9L))));
    assertEquals(0d, getRequestedRatio(new MediaArgs()));
  }

  @Test
  @SuppressWarnings("null")
  void testGetRequestedRatioAsWidthHeight() {
    assertEquals(2d / 1d, Ratio.get(getRequestedRatioAsWidthHeight(new MediaArgs().fixedDimension(100, 50))), 0.0001d);
    assertEquals(16d / 9d, Ratio.get(getRequestedRatioAsWidthHeight(mediaFormat(mf -> mf.width(160).height(90)))), 0.0001d);
    assertEquals(2d, Ratio.get(getRequestedRatioAsWidthHeight(mediaFormat(mf -> mf.ratio(2d)))), 0.0001d);
    assertEquals(2d / 1.5d, Ratio.get(getRequestedRatioAsWidthHeight(mediaFormat(mf -> mf.ratio(2d, 1.5d)))), 0.0001d);
    assertEquals(16d / 9d, Ratio.get(getRequestedRatioAsWidthHeight(mediaFormat(mf -> mf.ratio(16L, 9L)))), 0.0001d);
    assertNull(getRequestedRatioAsWidthHeight(new MediaArgs()));
  }

  @Test
  @SuppressWarnings("null")
  void testGetRequestedRatioAsWidthHeight_Extrapolation() {
    Dimension dimension = getRequestedRatioAsWidthHeight(mediaFormat(mf -> mf.ratio(16.125d, 9.2d)));
    assertEquals(16125, dimension.getWidth());
    assertEquals(9200, dimension.getHeight());
  }

  @Test
  @SuppressWarnings("null")
  void testGetRequestedRatioAsWidthHeight_ExtrapolationNotRequired() {
    Dimension dimension = getRequestedRatioAsWidthHeight(mediaFormat(mf -> mf.ratio(16.0000001d, 9.00000005d)));
    assertEquals(16, dimension.getWidth());
    assertEquals(9, dimension.getHeight());
  }

  @Test
  void testGetFirstMediaFormat() {
    MediaFormat mf1 = MediaFormatBuilder.create("mf1").build();
    MediaFormat mf2 = MediaFormatBuilder.create("mf1").build();

    assertEquals(mf1, getFirstMediaFormat(new MediaArgs().mediaFormat(mf1)));
    assertEquals(mf2, getFirstMediaFormat(new MediaArgs().mediaFormats(mf2, mf1)));
    assertNull(getFirstMediaFormat(new MediaArgs()));
  }

  private static @NotNull Dimension dimension(long width, long height) {
    return new Dimension(width, height);
  }

  private static @NotNull MediaArgs mediaFormat(Consumer<MediaFormatBuilder> consumer) {
    MediaFormatBuilder builder = MediaFormatBuilder.create("test");
    consumer.accept(builder);
    MediaFormat mediaFormat = builder.build();
    return new MediaArgs().mediaFormat(mediaFormat);
  }

}
