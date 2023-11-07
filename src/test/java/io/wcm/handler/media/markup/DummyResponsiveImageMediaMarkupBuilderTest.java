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
package io.wcm.handler.media.markup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.wcm.api.WCMMode;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.impl.DummyImageServlet;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.media.testcontext.DummyMediaFormats;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * Test DummyResponsiveImageMediaMarkupBuilder
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "deprecation", "null" })
class DummyResponsiveImageMediaMarkupBuilderTest {

  private final AemContext context = AppAemContext.newAemContext();

  @Mock
  private MediaSource mediaSource;
  @Mock
  private Asset asset;

  @Mock
  private Rendition rendition;

  @Mock
  private Resource resource;

  @Test
  void testAccepts_DISABLED() {
    WCMMode.DISABLED.toRequest(context.request());
    MediaMarkupBuilder underTest = AdaptTo.notNull(context.request(), DummyResponsiveImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    Media media = new Media(mediaSource, mediaRequest);
    assertFalse(underTest.accepts(media));

    mediaRequest.getMediaArgs().mandatoryMediaFormats(DummyMediaFormats.RESPONSIVE_32_9_L1);
    assertFalse(underTest.accepts(media));

    mediaRequest.getMediaArgs().mandatoryMediaFormats(DummyMediaFormats.RESPONSIVE_32_9_L1, DummyMediaFormats.RESPONSIVE_32_9_M1);

    media.setRenditions(List.of(rendition));
    assertFalse(underTest.accepts(media));
    media.setRenditions(null);

    assertFalse(underTest.accepts(media));

  }

  @Test
  void testAccepts_EDIT() {
    WCMMode.EDIT.toRequest(context.request());
    MediaMarkupBuilder underTest = AdaptTo.notNull(context.request(), DummyResponsiveImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    Media media = new Media(mediaSource, mediaRequest);
    assertFalse(underTest.accepts(media));

    mediaRequest.getMediaArgs().mandatoryMediaFormats(DummyMediaFormats.RESPONSIVE_32_9_L1);
    assertFalse(underTest.accepts(media));

    mediaRequest.getMediaArgs().mandatoryMediaFormats(DummyMediaFormats.RESPONSIVE_32_9_L1, DummyMediaFormats.RESPONSIVE_32_9_M1);

    assertTrue(underTest.accepts(media));

    media.setRenditions(List.of(rendition));
    assertFalse(underTest.accepts(media));

    media.setRenditions(null);
    mediaRequest.getMediaArgs().dummyImage(false);
    assertFalse(underTest.accepts(media));

  }

  @Test
  void testAccepts_PREVIEW() {
    WCMMode.PREVIEW.toRequest(context.request());
    MediaMarkupBuilder underTest = AdaptTo.notNull(context.request(), DummyResponsiveImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy", new MediaArgs());
    Media media = new Media(mediaSource, mediaRequest);
    assertFalse(underTest.accepts(media));

    mediaRequest.getMediaArgs().mandatoryMediaFormats(DummyMediaFormats.RESPONSIVE_32_9_L1);
    assertFalse(underTest.accepts(media));

    mediaRequest.getMediaArgs().mandatoryMediaFormats(DummyMediaFormats.RESPONSIVE_32_9_L1, DummyMediaFormats.RESPONSIVE_32_9_M1);

    assertTrue(underTest.accepts(media));

    media.setRenditions(List.of(rendition));
    assertFalse(underTest.accepts(media));

    media.setRenditions(null);
    mediaRequest.getMediaArgs().dummyImage(false);
    assertFalse(underTest.accepts(media));
  }

  @Test
  void testBuild() throws JSONException {

    WCMMode.PREVIEW.toRequest(context.request());
    MediaMarkupBuilder underTest = AdaptTo.notNull(context.request(), DummyResponsiveImageMediaMarkupBuilder.class);

    MediaRequest mediaRequest = new MediaRequest("/media/dummy",
        new MediaArgs().mandatoryMediaFormats(DummyMediaFormats.RESPONSIVE_32_9_L1, DummyMediaFormats.RESPONSIVE_32_9_M1));
    Media media = new Media(mediaSource, mediaRequest);

    HtmlElement image = underTest.build(media);
    assertTrue(StringUtils.equals(image.getAttributeValue("class"), MediaNameConstants.CSS_DUMMYIMAGE));
    JSONArray sources = new JSONArray(image.getAttributeValue("data-resp-src"));
    assertNotNull(sources);
    assertEquals(2, sources.length());

    assertEquals("L1", sources.getJSONObject(0).get(MediaNameConstants.PROP_BREAKPOINT));
    assertEquals(DummyImageServlet.PATH + ".suffix.png/height=540/mf=Responsive~2032~3A9/width=1920.png",
        sources.getJSONObject(0).get("src"));

    assertEquals("M1", sources.getJSONObject(1).get(MediaNameConstants.PROP_BREAKPOINT));
    assertEquals(DummyImageServlet.PATH + ".suffix.png/height=360/mf=Responsive~2032~3A9/width=1281.png",
        sources.getJSONObject(1).get("src"));
    assertNull(image.getAttributeValue("alt"), "alt");

  }

  @Test
  void testIsValidMedia() {
    MediaMarkupBuilder builder = AdaptTo.notNull(context.request(), DummyImageMediaMarkupBuilder.class);

    assertFalse(builder.isValidMedia(null));
    assertFalse(builder.isValidMedia(new Image()));
    assertFalse(builder.isValidMedia(new Image("/any/path.gif")));
    assertFalse(builder.isValidMedia(new Image().setData("resp-src", "[{'mg': 'test', 'src':'/dummy/img.png'}]")));
  }

}
