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
package io.wcm.handler.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.handler.commons.dom.Div;
import io.wcm.handler.media.spi.MediaSource;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class MediaTest {

  @Mock
  private MediaSource mediaSource;
  private MediaRequest mediaRequest;

  private Media underTest;

  @BeforeEach
  void setUp() {
    mediaRequest = new MediaRequest("/media/ref", new MediaArgs());
    underTest = new Media(mediaSource, mediaRequest);
  }


  @Test
  void testMediaSourceRequest() {
    assertSame(mediaSource, underTest.getMediaSource());
    assertSame(mediaRequest, underTest.getMediaRequest());

    MediaRequest mediaRequest2 = new MediaRequest("/media/ref2", new MediaArgs());
    underTest.setMediaRequest(mediaRequest2);
    assertSame(mediaRequest2, underTest.getMediaRequest());
  }

  @Test
  @SuppressWarnings("deprecation")
  void testElement() {
    Div div = new Div();
    div.setText("test");

    underTest.setElement(div);
    assertSame(div, underTest.getElement());
    assertEquals("<div>test</div>", underTest.getMarkup());
  }

  @Test
  void testElementBuilder() {
    Div div = new Div();
    div.setText("test");

    underTest.setElementBuilder(m -> div.setTitle("title1"));
    assertSame(div, underTest.getElement());
    assertEquals("<div title=\"title1\">test</div>", underTest.getMarkup());
  }

  @Test
  void testUrlAndValid() {
    underTest.setUrl("/my/url");

    assertEquals("/my/url", underTest.getUrl());
  }

  @Test
  void testAsset() {
    Asset asset = mock(Asset.class);
    underTest.setAsset(asset);
    assertSame(asset, underTest.getAsset());
  }


  @Test
  void testRenditions() {
    assertNull(underTest.getRendition());
    assertTrue(underTest.getRenditions().isEmpty());

    Rendition rendition1 = mock(Rendition.class);
    Rendition rendition2 = mock(Rendition.class);
    Collection<Rendition> renditions = List.of(rendition1, rendition2);
    underTest.setRenditions(renditions);

    assertSame(rendition1, underTest.getRendition());
    assertEquals(renditions, underTest.getRenditions());
  }

  @Test
  void testCropDimension() {
    CropDimension dimension = new CropDimension(1, 2, 3, 4);
    underTest.setCropDimension(dimension);
    assertSame(dimension, underTest.getCropDimension());
  }

  @Test
  void testMediaInvalidReason() {
    assertTrue(underTest.isValid());
    underTest.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_INVALID);
    assertEquals(MediaInvalidReason.MEDIA_REFERENCE_INVALID, underTest.getMediaInvalidReason());
    assertFalse(underTest.isValid());
  }

  @Test
  void testToString() {
    assertTrue(StringUtils.contains(underTest.toString(), "/media/ref"));
  }

}
