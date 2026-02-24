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
import io.wcm.handler.commons.dom.Video;
import io.wcm.handler.media.Media;
import io.wcm.sling.commons.adapter.AdaptTo;

class NextGenDynamicMediaHostedPlayerMarkupBuilderTest extends AbstractNextGenDynamicMediaMarkupBuilderTest {

  @Test
  void testAccepts() {
    NextGenDynamicMediaHostedPlayerMarkupBuilder underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaHostedPlayerMarkupBuilder.class);

    Media media = mediaHandler.get(resource).hostedVideoPlayer(true).build();
    assertTrue(underTest.accepts(media));

    media = mediaHandler.get(resource).hostedVideoPlayer(false).build();
    assertFalse(underTest.accepts(media));
  }

  @Test
  void testBuild() {
    NextGenDynamicMediaHostedPlayerMarkupBuilder underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaHostedPlayerMarkupBuilder.class);

    Media media = mediaHandler.get(resource)
        .hostedVideoPlayer(true)
        .build();
    HtmlElement element = underTest.build(media);

    assertNotNull(element);
    assertEquals("iframe", element.getName());
    assertEquals(expectedBaseUrl + "/play", element.getAttributeValue("src"));
    assertEquals("30", element.getAttributeValue("width"));
    assertEquals("30", element.getAttributeValue("height"));
    assertEquals("true", element.getAttributeValue("allowfullscreen"));
    assertEquals("0", element.getAttributeValue("frameborder"));
  }

  @Test
  void testIsValidMedia() {
    NextGenDynamicMediaHostedPlayerMarkupBuilder underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaHostedPlayerMarkupBuilder.class);
    assertTrue(underTest.isValidMedia(new HtmlElement("iframe")));
    assertFalse(underTest.isValidMedia(new Video()));
  }

}
