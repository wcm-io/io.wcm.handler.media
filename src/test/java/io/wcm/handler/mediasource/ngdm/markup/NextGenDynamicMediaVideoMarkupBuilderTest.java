/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2026 wcm.io
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
package io.wcm.handler.mediasource.ngdm.markup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Source;
import io.wcm.handler.commons.dom.Span;
import io.wcm.handler.commons.dom.Video;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.testcontext.DummyMediaFormats;
import io.wcm.sling.commons.adapter.AdaptTo;

class NextGenDynamicMediaVideoMarkupBuilderTest extends AbstractNextGenDynamicMediaMarkupBuilderTest {

  @Test
  void testAccepts() {
    NextGenDynamicMediaVideoMarkupBuilder underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaVideoMarkupBuilder.class);

    Media media = mediaHandler.get(resource).build();
    assertTrue(underTest.accepts(media));

    media.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_INVALID);
    assertFalse(underTest.accepts(media));
  }

  @Test
  void testBuild() {
    NextGenDynamicMediaVideoMarkupBuilder underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaVideoMarkupBuilder.class);

    Media media = mediaHandler.get(resource).build();
    HtmlElement element = underTest.build(media);

    assertTrue(element instanceof Video);
    Video video = (Video)element;
    assertEquals(30, video.getWidth());
    assertEquals(30, video.getHeight());
    assertEquals("controls", video.getAttributeValue("controls"));
    assertEquals(expectedBaseUrl + "/as/video.jpg", video.getAttributeValue("poster"));

    Source source = (Source)video.getChild("source");
    assertNotNull(source);
    assertEquals("application/vnd.apple.mpegurl", source.getType());
    assertEquals(expectedBaseUrl + "/manifest.m3u8", source.getSrc());
  }

  @Test
  void testBuildWithMediaFormat() {
    NextGenDynamicMediaVideoMarkupBuilder underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaVideoMarkupBuilder.class);

    Media media = mediaHandler.get(resource)
        .mediaFormat(DummyMediaFormats.EDITORIAL_1COL)
        .build();
    HtmlElement element = underTest.build(media);

    assertTrue(element instanceof Video);
    Video video = (Video)element;
    assertEquals(DummyMediaFormats.EDITORIAL_1COL.getWidth(), video.getWidth());
    assertEquals(DummyMediaFormats.EDITORIAL_1COL.getHeight(), video.getHeight());
  }

  @Test
  void testIsValidMedia() {
    NextGenDynamicMediaVideoMarkupBuilder underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaVideoMarkupBuilder.class);
    assertTrue(underTest.isValidMedia(new Video()));
    assertFalse(underTest.isValidMedia(new Span()));
  }

}
