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
package io.wcm.handler.mediasource.dam;

import static io.wcm.handler.mediasource.dam.AemRenditionType.OTHER_RENDITION;
import static io.wcm.handler.mediasource.dam.AemRenditionType.THUMBNAIL_RENDITION;
import static io.wcm.handler.mediasource.dam.AemRenditionType.VIDEO_RENDITION;
import static io.wcm.handler.mediasource.dam.AemRenditionType.WEB_RENDITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AemRenditionTypeTest {

  @Test
  void testThumbnailRendition() {
    assertTrue(THUMBNAIL_RENDITION.matches("cq5dam.thumbnail.10.10.png"));
    assertFalse(THUMBNAIL_RENDITION.matches("cq5dam.web.100.100.jpg"));
    assertFalse(THUMBNAIL_RENDITION.matches("cq5dam.video.hq.m4v"));
    assertFalse(THUMBNAIL_RENDITION.matches("cq5dam.zoom.50.50.jpg"));
    assertFalse(THUMBNAIL_RENDITION.matches("cqdam.text.txt"));
    assertFalse(THUMBNAIL_RENDITION.matches("othername.gif"));
  }

  @Test
  void testWebRendition() {
    assertFalse(WEB_RENDITION.matches("cq5dam.thumbnail.10.10.png"));
    assertTrue(WEB_RENDITION.matches("cq5dam.web.100.100.jpg"));
    assertFalse(WEB_RENDITION.matches("cq5dam.video.hq.m4v"));
    assertFalse(WEB_RENDITION.matches("cq5dam.zoom.50.50.jpg"));
    assertFalse(WEB_RENDITION.matches("cqdam.text.txt"));
    assertFalse(WEB_RENDITION.matches("othername.gif"));
  }

  @Test
  void testVideoRendition() {
    assertFalse(VIDEO_RENDITION.matches("cq5dam.thumbnail.10.10.png"));
    assertFalse(VIDEO_RENDITION.matches("cq5dam.web.100.100.jpg"));
    assertTrue(VIDEO_RENDITION.matches("cq5dam.video.hq.m4v"));
    assertFalse(VIDEO_RENDITION.matches("cq5dam.zoom.50.50.jpg"));
    assertFalse(VIDEO_RENDITION.matches("cqdam.text.txt"));
    assertFalse(VIDEO_RENDITION.matches("othername.gif"));
  }

  @Test
  void testAemGeneratedRendition() {
    assertTrue(OTHER_RENDITION.matches("cq5dam.thumbnail.10.10.png"));
    assertTrue(OTHER_RENDITION.matches("cq5dam.web.100.100.jpg"));
    assertTrue(OTHER_RENDITION.matches("cq5dam.video.hq.m4v"));
    assertTrue(OTHER_RENDITION.matches("cq5dam.zoom.50.50.jpg"));
    assertTrue(OTHER_RENDITION.matches("cqdam.text.txt"));
    assertFalse(OTHER_RENDITION.matches("othername.gif"));
  }

  @Test
  void testOf() {
    assertEquals(THUMBNAIL_RENDITION, AemRenditionType.forRendition("cq5dam.thumbnail.10.10.png"));
    assertEquals(WEB_RENDITION, AemRenditionType.forRendition("cq5dam.web.100.100.jpg"));
    assertEquals(VIDEO_RENDITION, AemRenditionType.forRendition("cq5dam.video.hq.m4v"));
    assertEquals(OTHER_RENDITION, AemRenditionType.forRendition("cq5dam.zoom.50.50.jpg"));
    assertEquals(OTHER_RENDITION, AemRenditionType.forRendition("cqdam.text.txt"));
    assertNull(AemRenditionType.forRendition("othername.gif"));
  }

}
