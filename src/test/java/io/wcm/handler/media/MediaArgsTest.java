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

import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_2COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_3COL;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import io.wcm.handler.media.MediaArgs.ImageSizes;
import io.wcm.handler.media.MediaArgs.MediaFormatOption;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaArgs.WidthOption;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.markup.DragDropSupport;
import io.wcm.handler.media.markup.IPERatioCustomize;
import io.wcm.handler.mediasource.dam.AemRenditionType;
import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.wcm.commons.contenttype.FileExtension;

class MediaArgsTest {

  @Test
  void testGetMediaFormats() {
    MediaArgs mediaArgs;

    mediaArgs = new MediaArgs(EDITORIAL_1COL);
    assertArrayEquals(new MediaFormat[] {
        EDITORIAL_1COL
    }, mediaArgs.getMediaFormats());

    mediaArgs = new MediaArgs("editorial_1col");
    assertArrayEquals(new String[] {
        "editorial_1col"
    }, mediaArgs.getMediaFormatNames());

    mediaArgs = new MediaArgs(EDITORIAL_1COL, EDITORIAL_2COL);
    assertArrayEquals(new MediaFormat[] {
        EDITORIAL_1COL, EDITORIAL_2COL
    }, mediaArgs.getMediaFormats());

    mediaArgs = new MediaArgs("editorial_1col", "editorial_2col");
    assertArrayEquals(new String[] {
        "editorial_1col", "editorial_2col"
    }, mediaArgs.getMediaFormatNames());

    assertNull(new MediaArgs().mediaFormat((MediaFormat)null).getMediaFormats());
    assertNull(new MediaArgs().mediaFormatName((String)null).getMediaFormatNames());
  }

  @Test
  void testGetMediaFormatsMandatory() {
    MediaArgs mediaArgs;

    mediaArgs = new MediaArgs().mandatoryMediaFormats(EDITORIAL_1COL, EDITORIAL_2COL);
    assertArrayEquals(new MediaFormat[] {
        EDITORIAL_1COL, EDITORIAL_2COL
    }, mediaArgs.getMediaFormats());

    mediaArgs = new MediaArgs().mandatoryMediaFormatNames("editorial_1col", "editorial_2col");
    assertArrayEquals(new String[] {
        "editorial_1col", "editorial_2col"
    }, mediaArgs.getMediaFormatNames());
  }

  @Test
  void testGetUrlMode() {
    assertEquals(UrlModes.FULL_URL, new MediaArgs().urlMode(UrlModes.FULL_URL).getUrlMode());
  }

  @Test
  void testFixedDimension() {
    MediaArgs mediaArgs = new MediaArgs().fixedDimension(100, 50);
    assertEquals(100, mediaArgs.getFixedWidth());
    assertEquals(50, mediaArgs.getFixedHeight());

    mediaArgs.fixedWidth(200);
    mediaArgs.fixedHeight(100);
    assertEquals(200, mediaArgs.getFixedWidth());
    assertEquals(100, mediaArgs.getFixedHeight());
  }

  @Test
  void testGetFileExtensions() {
    assertArrayEquals(new String[] {
        "gif"
    }, new MediaArgs().fileExtension("gif").getFileExtensions());
    assertArrayEquals(new String[] {
        "gif", "jpg"
    }, new MediaArgs().fileExtensions("gif", "jpg").getFileExtensions());

    assertNull(new MediaArgs().fileExtension(null).getFileExtensions());
  }

  @Test
  void testGetProperties() {
    Map<String, Object> props = Map.of("prop1", "value1");

    MediaArgs mediaArgs = new MediaArgs()
    .property("prop3", "value3")
    .properties(props)
    .property("prop2", "value2");

    assertEquals(3, mediaArgs.getProperties().size());
    assertEquals("value1", mediaArgs.getProperties().get("prop1", String.class));
    assertEquals("value2", mediaArgs.getProperties().get("prop2", String.class));
    assertEquals("value3", mediaArgs.getProperties().get("prop3", String.class));
  }

  @Test
  void testDragDropSupport() {
    MediaArgs mediaArgs = new MediaArgs();
    assertEquals(DragDropSupport.AUTO, mediaArgs.getDragDropSupport());

    mediaArgs.dragDropSupport(DragDropSupport.ALWAYS);
    assertEquals(DragDropSupport.ALWAYS, mediaArgs.getDragDropSupport());
  }

  @Test
  void testInvalidForceOutputFileExtensions() {
    MediaArgs mediaArgs = new MediaArgs();
    assertThrows(IllegalArgumentException.class, () -> {
      mediaArgs.enforceOutputFileExtension(FileExtension.SVG);
    });
  }

  @Test
  @SuppressWarnings("null")
  void testImageSizesAndWidthOptions() {
    MediaArgs mediaArgs = new MediaArgs();
    mediaArgs.imageSizes(new ImageSizes("size1", 10, 20, 30));
    assertNotNull(mediaArgs.getImageSizes());
    assertNotNull(mediaArgs.getImageSizes().getWidthOptions());
    assertEquals("size1", mediaArgs.getImageSizes().getSizes());
    assertEquals(3, mediaArgs.getImageSizes().getWidthOptions().length);
    assertNull(mediaArgs.getImageSizes().getWidthOptions()[0].getDensity());

    mediaArgs.imageSizes(new ImageSizes("",
            new WidthOption(5, "1x"),
            new WidthOption(15, "2x", false)));
    assertNotNull(mediaArgs.getImageSizes());
    assertNotNull(mediaArgs.getImageSizes().getWidthOptions());
    assertEquals("", mediaArgs.getImageSizes().getSizes());
    assertEquals(2, mediaArgs.getImageSizes().getWidthOptions().length);
    assertEquals("1x", mediaArgs.getImageSizes().getWidthOptions()[0].getDensity());
    assertEquals("", mediaArgs.getImageSizes().getWidthOptions()[0].getDensityDescriptor());
    assertTrue(mediaArgs.getImageSizes().getWidthOptions()[0].isMandatory());
    assertFalse(mediaArgs.getImageSizes().getWidthOptions()[1].isMandatory());
    assertEquals("[sizes=,widthOptions=5:1x,15:2x?]", mediaArgs.getImageSizes().toString());
    assertEquals("15:2x?", mediaArgs.getImageSizes().getWidthOptions()[1].toString());
  }

  @Test
  @SuppressWarnings("null")
  void testPictureSourcesWidthOptions() {
    MediaArgs mediaArgs = new MediaArgs();
    mediaArgs.pictureSources(
            new PictureSource(EDITORIAL_1COL).widths(10, 20, 30),
            new PictureSource(EDITORIAL_2COL).widthOptions(
                    new WidthOption(5),
                    new WidthOption(15, "2x", false)));

    assertNotNull(mediaArgs.getPictureSources());
    assertEquals(2, mediaArgs.getPictureSources().length);

    PictureSource source1 = mediaArgs.getPictureSources()[0];
    assertNotNull(source1.getWidthOptions());
    assertEquals(10L, source1.getWidthOptions()[0].getWidth());
    assertNull(source1.getWidthOptions()[0].getDensity());
    assertEquals("", source1.getWidthOptions()[0].getDensityDescriptor());
    assertTrue(source1.getWidthOptions()[0].isMandatory());
    assertEquals("[mediaFormat=Editorial Standard 1 Column (215x102px; 215:102; gif,jpg,png),widthOptions=10,20,30]", source1.toString());

    PictureSource source2 = mediaArgs.getPictureSources()[1];
    assertNotNull(source2.getWidthOptions());
    assertEquals(15L, source2.getWidthOptions()[1].getWidth());
    assertEquals("2x", source2.getWidthOptions()[1].getDensityDescriptor());
    assertFalse(source2.getWidthOptions()[1].isMandatory());
    assertEquals("[mediaFormat=Editorial Standard 2 Columns (450x213px; 75:35.5; gif,jpg,png),widthOptions=5,15:2x?]", source2.toString());
  }

  @Test
  @SuppressWarnings({
      "deprecation",
      "java:S5961" // ignore number of asserts
  })
  void testClone() {
    MediaFormatOption[] mediaFormatOptions = new MediaFormatOption[] {
        new MediaFormatOption(EDITORIAL_1COL, true),
        new MediaFormatOption(EDITORIAL_2COL, false)
    };
    String[] fileExtensions = new String[] {
        "ext1",
        "ext2"
    };
    Map<String,Object> props = ImmutableValueMap.of("prop1", "value1", "prop2", "value2");

    ImageSizes imageSizes = new ImageSizes("sizes1", new long[] { 1, 2, 3 });
    PictureSource[] pictureSourceSets = new PictureSource[] {
        new PictureSource(EDITORIAL_1COL).media("media1").widths(1,2,3),
        new PictureSource(EDITORIAL_2COL).widths(4),
        new PictureSource(EDITORIAL_3COL).widthOptions(
            new WidthOption(5, "1x", true),
            new WidthOption(6, "2x", false))
    };

    MediaArgs mediaArgs = new MediaArgs();
    mediaArgs.mediaFormatOptions(mediaFormatOptions);
    mediaArgs.fileExtensions(fileExtensions);
    mediaArgs.enforceOutputFileExtension(FileExtension.PNG);
    mediaArgs.urlMode(UrlModes.FULL_URL_FORCENONSECURE);
    mediaArgs.fixedWidth(10);
    mediaArgs.fixedHeight(20);
    mediaArgs.download(true);
    mediaArgs.contentDispositionAttachment(true);
    mediaArgs.altText("altText");
    mediaArgs.forceAltValueFromAsset(true);
    mediaArgs.decorative(true);
    mediaArgs.dummyImage(true);
    mediaArgs.dummyImageUrl("/dummy/url");
    mediaArgs.includeAssetAemRenditions(Set.of(AemRenditionType.THUMBNAIL_RENDITION, AemRenditionType.WEB_RENDITION));
    mediaArgs.includeAssetThumbnails(true);
    mediaArgs.includeAssetWebRenditions(true);
    mediaArgs.imageSizes(imageSizes);
    mediaArgs.pictureSources(pictureSourceSets);
    mediaArgs.imageQualityPercentage(0.5d);
    mediaArgs.dragDropSupport(DragDropSupport.NEVER);
    mediaArgs.ipeRatioCustomize(IPERatioCustomize.NEVER);
    mediaArgs.dynamicMediaDisabled(true);
    mediaArgs.webOptimizedImageDeliveryDisabled(true);
    mediaArgs.properties(props);

    MediaArgs clone = mediaArgs.clone();
    assertNotSame(mediaArgs, clone);
    assertNotSame(mediaArgs.getMediaFormatOptions(), clone.getMediaFormatOptions());
    assertNotSame(mediaArgs.getMediaFormats(), clone.getMediaFormats());
    assertNotSame(mediaArgs.getFileExtensions(), clone.getFileExtensions());
    assertNotSame(mediaArgs.getPictureSources(), clone.getPictureSources());
    assertNotSame(mediaArgs.getProperties(), clone.getProperties());

    assertArrayEquals(mediaArgs.getMediaFormatOptions(), clone.getMediaFormatOptions());
    assertArrayEquals(mediaArgs.getMediaFormats(), clone.getMediaFormats());
    assertArrayEquals(mediaArgs.getMediaFormatNames(), clone.getMediaFormatNames());
    assertArrayEquals(mediaArgs.getFileExtensions(), clone.getFileExtensions());
    assertEquals(mediaArgs.getEnforceOutputFileExtension(), clone.getEnforceOutputFileExtension());
    assertEquals(mediaArgs.getUrlMode(), clone.getUrlMode());
    assertEquals(mediaArgs.getFixedWidth(), clone.getFixedWidth());
    assertEquals(mediaArgs.getFixedHeight(), clone.getFixedHeight());
    assertEquals(mediaArgs.isDownload(), clone.isDownload());
    assertEquals(mediaArgs.isContentDispositionAttachment(), clone.isContentDispositionAttachment());
    assertEquals(mediaArgs.getAltText(), clone.getAltText());
    assertEquals(mediaArgs.isForceAltValueFromAsset(), clone.isForceAltValueFromAsset());
    assertEquals(mediaArgs.isDecorative(), clone.isDecorative());
    assertEquals(mediaArgs.isDummyImage(), clone.isDummyImage());
    assertEquals(mediaArgs.getDummyImageUrl(), clone.getDummyImageUrl());
    assertEquals(mediaArgs.getIncludeAssetAemRenditions(), clone.getIncludeAssetAemRenditions());
    assertEquals(mediaArgs.isIncludeAssetThumbnails(), clone.isIncludeAssetThumbnails());
    assertEquals(mediaArgs.isIncludeAssetWebRenditions(), clone.isIncludeAssetWebRenditions());
    assertEquals(mediaArgs.getImageSizes(), clone.getImageSizes());
    assertArrayEquals(mediaArgs.getPictureSources(), clone.getPictureSources());
    assertEquals(mediaArgs.getImageQualityPercentage(), clone.getImageQualityPercentage());
    assertEquals(mediaArgs.getDragDropSupport(), clone.getDragDropSupport());
    assertEquals(IPERatioCustomize.NEVER, clone.getIPERatioCustomize());
    assertEquals(mediaArgs.isDynamicMediaDisabled(), clone.isDynamicMediaDisabled());
    assertEquals(mediaArgs.isWebOptimizedImageDeliveryDisabled(), clone.isWebOptimizedImageDeliveryDisabled());
    assertEquals(ImmutableValueMap.copyOf(mediaArgs.getProperties()), ImmutableValueMap.copyOf(clone.getProperties()));
  }

  @Test
  void testEquals() {
    MediaArgs mediaArgs1 = new MediaArgs().mediaFormat(EDITORIAL_1COL).urlMode(UrlModes.FULL_URL).altText("abc");
    MediaArgs mediaArgs2 = new MediaArgs().mediaFormat(EDITORIAL_1COL).urlMode(UrlModes.FULL_URL).altText("abc");
    MediaArgs mediaArgs3 = new MediaArgs().mediaFormat(EDITORIAL_2COL).urlMode(UrlModes.FULL_URL).altText("abc");

    assertEquals(mediaArgs1, mediaArgs2);
    assertEquals(mediaArgs2, mediaArgs1);
    assertNotEquals(mediaArgs1, mediaArgs3);
    assertNotEquals(mediaArgs2, mediaArgs3);
  }

  @Test
  void testToString() {
    MediaArgs mediaArgs = new MediaArgs().altText("abc");
    assertTrue(StringUtils.contains(mediaArgs.toString(), "abc"));
  }

}
