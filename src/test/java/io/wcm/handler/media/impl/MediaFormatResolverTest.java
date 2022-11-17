/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import static io.wcm.handler.media.impl.MediaFormatResolver.MEDIAFORMAT_NAME_SEPARATOR;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_2COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.IMAGE_UNCONSTRAINED;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO_16_10;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO_4_3;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaArgs.ImageSizes;
import io.wcm.handler.media.MediaArgs.MediaFormatOption;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaArgs.WidthOption;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatHandler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaFormatResolverTest {

  @Mock
  private MediaFormatHandler mediaFormatHandler;

  private MediaFormatResolver underTest;

  @BeforeEach
  void setUp() {
    when(mediaFormatHandler.getMediaFormat(EDITORIAL_1COL.getName())).thenReturn(EDITORIAL_1COL);
    when(mediaFormatHandler.getMediaFormat(EDITORIAL_2COL.getName())).thenReturn(EDITORIAL_2COL);
    when(mediaFormatHandler.getMediaFormat(RATIO_16_10.getName())).thenReturn(RATIO_16_10);
    when(mediaFormatHandler.getMediaFormat(RATIO_4_3.getName())).thenReturn(RATIO_4_3);

    underTest = new MediaFormatResolver(mediaFormatHandler);
  }

  @Test
  void testMediaFormats() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormats(EDITORIAL_1COL, EDITORIAL_2COL);

    assertTrue(underTest.resolve(mediaArgs));

    assertArrayEquals(new MediaFormat[] { EDITORIAL_1COL, EDITORIAL_2COL }, mediaArgs.getMediaFormats());
  }

  @Test
  void testMediaFormatNamesAllValid() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormatNames(EDITORIAL_1COL.getName(), EDITORIAL_2COL.getName());

    assertTrue(underTest.resolve(mediaArgs));

    assertArrayEquals(new MediaFormat[] { EDITORIAL_1COL, EDITORIAL_2COL }, mediaArgs.getMediaFormats());
  }

  @Test
  void testMediaFormatNamesSomeInvalid() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormatNames(EDITORIAL_1COL.getName(), "invalid", EDITORIAL_2COL.getName());

    assertFalse(underTest.resolve(mediaArgs));

    assertArrayEquals(new MediaFormat[] { EDITORIAL_1COL, null, EDITORIAL_2COL }, mediaArgs.getMediaFormats());
  }

  @Test
  void testMediaFormatNamesAllInvalid() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormatNames("invalid1", "invalid2");

    assertFalse(underTest.resolve(mediaArgs));

    assertArrayEquals(new MediaFormat[] { null, null }, mediaArgs.getMediaFormats());
  }

  @Test
  void testImageSizes() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(RATIO_16_10)
        .imageSizes(new ImageSizes("size1", 10, 20));

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormatOption[] mediaFormatOptions = mediaArgs.getMediaFormatOptions();
    assertEquals(3, mediaFormatOptions.length);
    assertEquals(RATIO_16_10, mediaFormatOptions[0].getMediaFormat());
    assertResponsiveMediaFormat(RATIO_16_10, 10, true, mediaFormatOptions[1]);
    assertResponsiveMediaFormat(RATIO_16_10, 20, true, mediaFormatOptions[2]);
  }

  @Test
  void testImageSizes_MixedMandatory() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(RATIO_16_10)
        .imageSizes(new ImageSizes("size1", new WidthOption(10, true), new WidthOption(20, false)));

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormatOption[] mediaFormatOptions = mediaArgs.getMediaFormatOptions();
    assertEquals(3, mediaFormatOptions.length);
    assertEquals(RATIO_16_10, mediaFormatOptions[0].getMediaFormat());
    assertResponsiveMediaFormat(RATIO_16_10, 10, true, mediaFormatOptions[1]);
    assertResponsiveMediaFormat(RATIO_16_10, 20, false, mediaFormatOptions[2]);
  }

  @Test
  void testImageSizes_MultipleMediaFormats() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormats(RATIO_16_10, RATIO_4_3)
        .imageSizes(new ImageSizes("size1", 10, 20));

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormatOption[] mediaFormatOptions = mediaArgs.getMediaFormatOptions();
    assertEquals(6, mediaFormatOptions.length);
    assertEquals(RATIO_16_10, mediaFormatOptions[0].getMediaFormat());
    assertEquals(RATIO_4_3, mediaFormatOptions[1].getMediaFormat());
    assertResponsiveMediaFormat(RATIO_16_10, 10, true, mediaFormatOptions[2]);
    assertResponsiveMediaFormat(RATIO_16_10, 20, true, mediaFormatOptions[3]);
    assertResponsiveMediaFormat(RATIO_4_3, 10, true, mediaFormatOptions[4]);
    assertResponsiveMediaFormat(RATIO_4_3, 20, true, mediaFormatOptions[5]);
  }

  @Test
  void testImageSizes_MultipleMediaFormats_MixedMandatory() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormats(RATIO_16_10, RATIO_4_3)
        .imageSizes(new ImageSizes("size1", new WidthOption(10, true), new WidthOption(20, false)));

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormatOption[] mediaFormatOptions = mediaArgs.getMediaFormatOptions();
    assertEquals(6, mediaFormatOptions.length);
    assertEquals(RATIO_16_10, mediaFormatOptions[0].getMediaFormat());
    assertEquals(RATIO_4_3, mediaFormatOptions[1].getMediaFormat());
    assertResponsiveMediaFormat(RATIO_16_10, 10, true, mediaFormatOptions[2]);
    assertResponsiveMediaFormat(RATIO_16_10, 20, false, mediaFormatOptions[3]);
    assertResponsiveMediaFormat(RATIO_4_3, 10, true, mediaFormatOptions[4]);
    assertResponsiveMediaFormat(RATIO_4_3, 20, false, mediaFormatOptions[5]);
  }

  @Test
  void testImageSizes_NoRatioMediaFormat() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(IMAGE_UNCONSTRAINED)
        .imageSizes(new ImageSizes("size1", 10, 20));

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormatOption[] mediaFormatOptions = mediaArgs.getMediaFormatOptions();
    assertEquals(3, mediaFormatOptions.length);
    assertEquals(IMAGE_UNCONSTRAINED, mediaFormatOptions[0].getMediaFormat());
    assertResponsiveMediaFormat(IMAGE_UNCONSTRAINED, 10, true, mediaFormatOptions[1]);
    assertResponsiveMediaFormat(IMAGE_UNCONSTRAINED, 20, true, mediaFormatOptions[2]);
  }

  @Test
  void testImageSizes_NoMediaFormat() {
    MediaArgs mediaArgs = new MediaArgs()
        .imageSizes(new ImageSizes("sizes", 10, 20));

    assertFalse(underTest.resolve(mediaArgs));
  }

  @Test
  void testPictureSources_DifferentRatio() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(RATIO_16_10)
        .pictureSources(new PictureSource[] {
            new PictureSource(RATIO_16_10).media("media1").widths(20, 30),
            new PictureSource(RATIO_4_3).widths(10, 20)
        });

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormatOption[] mediaFormatOptions = mediaArgs.getMediaFormatOptions();
    assertEquals(5, mediaFormatOptions.length);
    assertEquals(RATIO_16_10, mediaFormatOptions[0].getMediaFormat());
    assertResponsiveMediaFormat(RATIO_16_10, 20, true, mediaFormatOptions[1]);
    assertResponsiveMediaFormat(RATIO_16_10, 30, true, mediaFormatOptions[2]);
    assertResponsiveMediaFormat(RATIO_4_3, 10, true, mediaFormatOptions[3]);
    assertResponsiveMediaFormat(RATIO_4_3, 20, true, mediaFormatOptions[4]);
  }

  @Test
  void testPictureSources_DifferentRatio_MediaFormatNames() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(RATIO_16_10)
        .pictureSources(new PictureSource[] {
            new PictureSource(RATIO_16_10.getName()).media("media1").widths(20, 30),
            new PictureSource(RATIO_4_3.getName()).widths(10, 20)
        });

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormatOption[] mediaFormatOptions = mediaArgs.getMediaFormatOptions();
    assertEquals(5, mediaFormatOptions.length);
    assertEquals(RATIO_16_10, mediaFormatOptions[0].getMediaFormat());
    assertResponsiveMediaFormat(RATIO_16_10, 20, true, mediaFormatOptions[1]);
    assertResponsiveMediaFormat(RATIO_16_10, 30, true, mediaFormatOptions[2]);
    assertResponsiveMediaFormat(RATIO_4_3, 10, true, mediaFormatOptions[3]);
    assertResponsiveMediaFormat(RATIO_4_3, 20, true, mediaFormatOptions[4]);
  }

  @Test
  void testPictureSources_DifferentRatio_MixedMandatory() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(RATIO_16_10)
        .pictureSources(new PictureSource[] {
            new PictureSource(RATIO_16_10).media("media1").widthOptions(new WidthOption(20, true), new WidthOption(30, false)),
            new PictureSource(RATIO_4_3).widthOptions(new WidthOption(10, false), new WidthOption(20, true))
        });

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormatOption[] mediaFormatOptions = mediaArgs.getMediaFormatOptions();
    assertEquals(5, mediaFormatOptions.length);
    assertEquals(RATIO_16_10, mediaFormatOptions[0].getMediaFormat());
    assertResponsiveMediaFormat(RATIO_16_10, 20, true, mediaFormatOptions[1]);
    assertResponsiveMediaFormat(RATIO_16_10, 30, false, mediaFormatOptions[2]);
    assertResponsiveMediaFormat(RATIO_4_3, 10, false, mediaFormatOptions[3]);
    assertResponsiveMediaFormat(RATIO_4_3, 20, true, mediaFormatOptions[4]);
  }

  @Test
  void testPictureSources_SameRatio() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(RATIO_16_10)
        .pictureSources(new PictureSource[] {
            new PictureSource(RATIO_16_10).media("media1").widths(20, 30),
            new PictureSource(RATIO_16_10).widths(10, 20)
        });

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormatOption[] mediaFormatOptions = mediaArgs.getMediaFormatOptions();
    assertEquals(4, mediaFormatOptions.length);
    assertEquals(RATIO_16_10, mediaFormatOptions[0].getMediaFormat());
    assertResponsiveMediaFormat(RATIO_16_10, 20, true, mediaFormatOptions[1]);
    assertResponsiveMediaFormat(RATIO_16_10, 30, true, mediaFormatOptions[2]);
    assertResponsiveMediaFormat(RATIO_16_10, 10, true, mediaFormatOptions[3]);
  }

  @Test
  void testPictureSources_NoWidths() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(RATIO_16_10)
        .pictureSources(new PictureSource[] {
            new PictureSource(RATIO_16_10).media("media1")
        });

    assertTrue(underTest.resolve(mediaArgs));

    MediaFormatOption[] mediaFormatOptions = mediaArgs.getMediaFormatOptions();
    assertEquals(1, mediaFormatOptions.length);
    assertEquals(RATIO_16_10, mediaFormatOptions[0].getMediaFormat());
  }

  @Test
  void testPictureSources_InvalidMediaFormatName() {
    MediaArgs mediaArgs = new MediaArgs()
        .mediaFormat(RATIO_16_10)
        .pictureSources(new PictureSource[] {
            new PictureSource("invalid-format-name").media("media1")
        });

    assertFalse(underTest.resolve(mediaArgs));
  }

  @SuppressWarnings("null")
  private void assertResponsiveMediaFormat(MediaFormat baseMediaFormat, long width, boolean mandatory,
      MediaFormatOption actualMediaFormatOption) {
    MediaFormat actualMediaFormat = actualMediaFormatOption.getMediaFormat();
    assertNotNull(actualMediaFormat);
    assertEquals(baseMediaFormat.getName() + MEDIAFORMAT_NAME_SEPARATOR + width, actualMediaFormat.getName());
    assertEquals(width, actualMediaFormat.getWidth());
    assertEquals(baseMediaFormat.getRatio(), actualMediaFormat.getRatio(), 0.001);
    assertEquals(mandatory, actualMediaFormatOption.isMandatory());
  }

}
