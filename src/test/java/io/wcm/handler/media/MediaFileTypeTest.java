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
package io.wcm.handler.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.wcm.wcm.commons.contenttype.ContentType;

class MediaFileTypeTest {

  @Test
  void testIsImage() {
    assertTrue(MediaFileType.isImage("gif"));
    assertTrue(MediaFileType.isImage("JPG"));
    assertTrue(MediaFileType.isImage("jpeg"));
    assertTrue(MediaFileType.isImage("tif"));
    assertTrue(MediaFileType.isImage("TIFF"));
    assertFalse(MediaFileType.isImage("pdf"));
    assertFalse(MediaFileType.isImage(null));
  }

  @Test
  void testGetImageFileExtensions() {
    assertEquals(Set.of("jpg", "jpeg", "gif", "png", "svg", "tif", "tiff"), MediaFileType.getImageFileExtensions());
  }

  @Test
  void testGetImageContentTypes() {
    assertEquals(Set.of(ContentType.JPEG, ContentType.GIF, ContentType.PNG, ContentType.SVG, ContentType.TIFF),
        MediaFileType.getImageContentTypes());
  }

  @Test
  void testIsBrowserImage() {
    assertTrue(MediaFileType.isBrowserImage("GIF"));
    assertTrue(MediaFileType.isBrowserImage("jpg"));
    assertTrue(MediaFileType.isBrowserImage("jpeg"));
    assertTrue(MediaFileType.isBrowserImage("SVG"));
    assertFalse(MediaFileType.isBrowserImage("tif"));
    assertFalse(MediaFileType.isBrowserImage("pdf"));
    assertFalse(MediaFileType.isBrowserImage(null));
  }

  @Test
  void testGetBrowserImageFileExtensions() {
    assertEquals(Set.of("jpg", "jpeg", "gif", "png", "svg"), MediaFileType.getBrowserImageFileExtensions());
  }

  @Test
  void testGetBrowserImageContentTypes() {
    assertEquals(Set.of(ContentType.JPEG, ContentType.GIF, ContentType.PNG, ContentType.SVG),
        MediaFileType.getBrowserImageContentTypes());
  }

  @Test
  void testIsVectorImage() {
    assertTrue(MediaFileType.isVectorImage("svg"));
    assertFalse(MediaFileType.isVectorImage("jpg"));
    assertFalse(MediaFileType.isVectorImage("pdf"));
    assertFalse(MediaFileType.isVectorImage(null));
  }

  @Test
  void testGetVectorImageFileExtensions() {
    assertEquals(Set.of("svg"), MediaFileType.getVectorImageFileExtensions());
  }

  @Test
  void testGetVectorImageContentTypes() {
    assertEquals(Set.of(ContentType.SVG), MediaFileType.getVectorImageContentTypes());
  }

  @Test
  void testGetByContentType() {
    assertEquals(MediaFileType.JPEG, MediaFileType.getByContentType("image/jpeg"));
    assertEquals(MediaFileType.JPEG, MediaFileType.getByContentType("IMAGE/JPEG"));
    assertEquals(MediaFileType.GIF, MediaFileType.getByContentType("image/gif"));
    assertNull(MediaFileType.getByContentType("application/octet-stream"));
    assertNull(MediaFileType.getByContentType(""));
    assertNull(MediaFileType.getByContentType(null));
  }

  @Test
  void testGetExtension() {
    assertEquals("jpg", MediaFileType.JPEG.getExtension());
    assertEquals("tif", MediaFileType.TIFF.getExtension());
    assertEquals("m3u8", MediaFileType.M3U8.getExtension());
    assertEquals("mpd", MediaFileType.MPD.getExtension());
  }

  @Test
  void testGetByFileExtensions() {
    assertEquals(MediaFileType.JPEG, MediaFileType.getByFileExtensions("jpg"));
    assertEquals(MediaFileType.JPEG, MediaFileType.getByFileExtensions("JPG"));
    assertEquals(MediaFileType.JPEG, MediaFileType.getByFileExtensions("jpeg"));
    assertEquals(MediaFileType.GIF, MediaFileType.getByFileExtensions("gif"));
    assertNull(MediaFileType.getByFileExtensions("txt"));
    assertNull(MediaFileType.getByFileExtensions(""));
    assertNull(MediaFileType.getByFileExtensions(null));
  }

}
