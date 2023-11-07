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
package io.wcm.handler.media.format;

import static io.wcm.handler.media.format.MediaFormatBuilder.create;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

class MediaFormatTest {

  @Test
  void testRatioNone() {
    MediaFormat mf = create("mf1").build();
    assertEquals(0d, mf.getRatio(), 0.001d);
    assertNull(mf.getRatioDisplayString());
  }

  @Test
  void testRatio() {
    MediaFormat mf = create("mf1").ratio(1.25d).build();
    assertEquals(1.25d, mf.getRatio(), 0.001d);
    assertEquals("5:4", mf.getRatioDisplayString());
  }

  @Test
  void testRatioWidthHeight() {
    MediaFormat mf = create("mf1").ratio(16, 9).build();
    assertEquals(1.777d, mf.getRatio(), 0.001d);
    assertEquals("16:9", mf.getRatioDisplayString());
  }

  @Test
  void testRatioWidthHeightDouble() {
    MediaFormat mf = create("mf1").ratio(26.5, 5).build();
    assertEquals(5.3d, mf.getRatio(), 0.001d);
    assertEquals("26.5:5", mf.getRatioDisplayString());
  }

  @Test
  void testRatioFixedDimension() {
    MediaFormat mf = create("mf1").fixedDimension(100, 75).build();
    assertEquals(1.333d, mf.getRatio(), 0.001d);
    assertEquals("4:3", mf.getRatioDisplayString());
    assertEquals("mf1 (100x75px; 4:3)", mf.getCombinedTitle());
  }

  @Test
  void testIsImage() {
    MediaFormat mf1 = create("mf1").extensions("gif").build();
    MediaFormat mf2 = create("mf2").extensions("zip", "gif").build();
    MediaFormat mf3 = create("mf3").extensions("zip").build();

    assertTrue(mf1.isImage());
    assertTrue(mf2.isImage());
    assertFalse(mf3.isImage());
  }

  @Test
  void testFixedDimension() {
    MediaFormat mf1 = create("mf1").fixedDimension(100, 50).build();
    assertTrue(mf1.isFixedWidth());
    assertTrue(mf1.isFixedHeight());
    assertTrue(mf1.isFixedDimension());

    MediaFormat mf2 = create("mf2").width(100).build();
    assertTrue(mf2.isFixedWidth());
    assertFalse(mf2.isFixedHeight());
    assertFalse(mf2.isFixedDimension());

    MediaFormat mf3 = create("mf3").height(50).build();
    assertFalse(mf3.isFixedWidth());
    assertTrue(mf3.isFixedHeight());
    assertFalse(mf3.isFixedDimension());

    MediaFormat mf4 = create("mf4").width(50, 100).height(100, 150).build();
    assertFalse(mf4.isFixedWidth());
    assertFalse(mf4.isFixedHeight());
    assertFalse(mf4.isFixedDimension());
  }

  @Test
  void testGetEffectiveMinWidth() {
    MediaFormat mf1 = create("mf1").width(75).build();
    assertEquals(75, mf1.getEffectiveMinWidth());

    MediaFormat mf2 = create("mf2").width(50, 100).build();
    assertEquals(50, mf2.getEffectiveMinWidth());
  }

  @Test
  void testGetEffectiveMaxWidth() {
    MediaFormat mf1 = create("mf1").width(75).build();
    assertEquals(75, mf1.getEffectiveMaxWidth());

    MediaFormat mf2 = create("mf2").width(50, 100).build();
    assertEquals(100, mf2.getEffectiveMaxWidth());
  }

  @Test
  void testGetEffectiveMinHeight() {
    MediaFormat mf1 = create("mf1").height(75).build();
    assertEquals(75, mf1.getEffectiveMinHeight());

    MediaFormat mf2 = create("mf2").height(50, 100).build();
    assertEquals(50, mf2.getEffectiveMinHeight());
  }

  @Test
  void testGetEffectiveMaxHeight() {
    MediaFormat mf1 = create("mf1").height(75).build();
    assertEquals(75, mf1.getEffectiveMaxHeight());

    MediaFormat mf2 = create("mf2").height(50, 100).build();
    assertEquals(100, mf2.getEffectiveMaxHeight());
  }

  @Test
  void testGetMinDimension() {
    MediaFormat mf1 = create("mf1").build();
    assertNull(mf1.getMinDimension());

    MediaFormat mf2 = create("mf2").fixedDimension(100, 50).build();
    assertEquals(100, mf2.getMinDimension().getWidth());
    assertEquals(50, mf2.getMinDimension().getHeight());

    MediaFormat mf3 = create("mf3").width(50, 100).height(75, 200).build();
    assertEquals(50, mf3.getMinDimension().getWidth());
    assertEquals(75, mf3.getMinDimension().getHeight());

    MediaFormat mf4 = create("mf4").width(100).ratio(1.333d).build();
    assertEquals(100, mf4.getMinDimension().getWidth());
    assertEquals(75, mf4.getMinDimension().getHeight());

    MediaFormat mf5 = create("mf5").height(75).ratio(1.333d).build();
    assertEquals(100, mf5.getMinDimension().getWidth());
    assertEquals(75, mf5.getMinDimension().getHeight());
  }

  @Test
  void testGetCombinedTitle() {
    MediaFormat mf1 = create("mf1").build();
    assertEquals("mf1", mf1.getCombinedTitle());

    MediaFormat mf2 = create("mf2").label("MF2").fixedDimension(100, 50).build();
    assertEquals("MF2 (100x50px; 2:1)", mf2.getCombinedTitle());
    assertEquals("MF2 (100x50px; 2:1)", mf2.toString());

    MediaFormat mf3 = create("mf3").label("MF3").width(50, 100).height(75, 200).build();
    assertEquals("MF3 (50..100x75..200px)", mf3.getCombinedTitle());

    MediaFormat mf4 = create("mf4").fixedDimension(100, 50).extensions("gif", "jpg").build();
    assertEquals("mf4 (100x50px; 2:1; gif,jpg)", mf4.getCombinedTitle());

    MediaFormat mf5 = create("mf5").ratio(16, 9).extensions("gif", "jpg").build();
    assertEquals("mf5 (16:9; gif,jpg)", mf5.getCombinedTitle());

    MediaFormat mf6 = create("mf6").label("mf6 - 16:9").ratio(16, 9).extensions("gif", "jpg").build();
    assertEquals("mf6 - 16:9 (gif,jpg)", mf6.getCombinedTitle());

    MediaFormat mf7 = create("mf7").extensions("e1", "e2", "e3", "e4", "e5", "e6", "e7").build();
    assertEquals("mf7 (e1,e2,e3,e4,e5,e6...)", mf7.getCombinedTitle());

    MediaFormat mf8 = create("mf8").label("MF8").ratio(16, 9).width(100).build();
    assertEquals("MF8 (100x?px; 16:9)", mf8.getCombinedTitle());

    MediaFormat mf9 = create("mf9").label("MF9").ratio(16, 9).height(50).build();
    assertEquals("MF9 (?x50px; 16:9)", mf9.getCombinedTitle());

    MediaFormat mf10 = create("mf10").label("MF10").ratio(16, 9).minWidth(500).maxHeight(200).build();
    assertEquals("MF10 (500..x..200px; 16:9)", mf10.getCombinedTitle());

    MediaFormat mf11 = create("mf11").label("MF11").ratio(16, 9).maxWidth(500).minHeight(200).build();
    assertEquals("MF11 (..500x200..px; 16:9)", mf11.getCombinedTitle());
  }

  @Test
  void testSort() {
    SortedSet<MediaFormat> set = new TreeSet<>();
    set.add(create("mf1").build());
    set.add(create("mf3").build());
    set.add(create("mf2").build());

    List<MediaFormat> result = List.copyOf(set);
    assertEquals("mf1", result.get(0).getName());
    assertEquals("mf2", result.get(1).getName());
    assertEquals("mf3", result.get(2).getName());
  }

  @Test
  void testGuessHumanReadableRatioString() {
    assertEquals("16:9", create("mf").ratio(16d / 9d).build().getRatioDisplayString());
    assertEquals("4:3", create("mf").ratio(4d / 3d).build().getRatioDisplayString());
    assertEquals("3:4", create("mf").ratio(3d / 4d).build().getRatioDisplayString());
    assertEquals("26.5:5", create("mf").ratio(26.5d / 5d).build().getRatioDisplayString());
    assertEquals("26:5.5", create("mf").ratio(26d / 5.5d).build().getRatioDisplayString());
    assertEquals("17:5", create("mf").ratio(17d / 5d).build().getRatioDisplayString());
    assertEquals("5:1", create("mf").ratio(5d / 1d).build().getRatioDisplayString());
    assertEquals("1:1", create("mf").ratio(1d).build().getRatioDisplayString());
  }

}
