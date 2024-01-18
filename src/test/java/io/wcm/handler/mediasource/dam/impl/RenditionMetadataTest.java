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
package io.wcm.handler.mediasource.dam.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.image.Layer;

import io.wcm.handler.media.Media;
import io.wcm.handler.mediasource.dam.AbstractDamTest;

/**
 * Tests the {@link RenditionMetadata}, especially the compareTo method
 */
@SuppressWarnings("null")
class RenditionMetadataTest extends AbstractDamTest {

  private RenditionMetadata originalRendition;
  private RenditionMetadata originalRenditionCopy;
  private RenditionMetadata smallestRendition;
  private RenditionMetadata biggestRendition;

  @BeforeEach
  void setUp() throws Exception {
    Media media = mediaHandler().get(MEDIAITEM_PATH_STANDARD).build();
    Asset asset = media.getAsset().adaptTo(Asset.class);

    originalRendition = new RenditionMetadata(asset.getRendition("original"));
    assertNotNull(originalRendition);

    originalRenditionCopy = new RenditionMetadata(asset.getRendition("cq5dam.thumbnail.215.102.jpg"));
    assertNotNull(originalRenditionCopy);

    smallestRendition = new RenditionMetadata(asset.getRendition("cq5dam.web.450.213.jpg"));
    assertNotNull(smallestRendition);

    biggestRendition = new RenditionMetadata(asset.getRendition("cq5dam.web.960.455.jpg"));
    assertNotNull(biggestRendition);
  }

  /**
   * Two original renditions
   */
  @Test
  void testCompareOriginalRenditionToItself() {
    assertEquals(0, originalRendition.compareTo(originalRendition), "original rendition is not equal to itself");
  }

  /**
   * Small and big rendition
   */
  @Test
  void testCompareSmallToBigRendition() {
    assertEquals(-1, smallestRendition.compareTo(biggestRendition), "smaller rendition is not smaller");
  }

  /**
   * Big and small rendition
   */
  @Test
  void testCompareBigToSmallRendition() {
    assertEquals(1, biggestRendition.compareTo(smallestRendition), "bigger rendition is not bigger");
  }

  /**
   * Two equal renditions
   */
  @Test
  void testCompareTwoEqualRenditions() {
    assertEquals(0, biggestRendition.compareTo(biggestRendition), "two equal renditions are not equal");
  }

  /**
   * Original rendition and a rendition, with the same dimension as original rendition
   */
  @Test
  void testCompareOriginalRenditionToEqualRendition() {
    assertEquals(-1, originalRendition.compareTo(originalRenditionCopy), "original rendition is not preferred over the equal rendition");
    assertEquals(1, originalRenditionCopy.compareTo(originalRendition), "original rendition is not preferred over the equal rendition");
  }

  @Test
  void testMatchesExact() {
    assertTrue(smallestRendition.matches(450, 213));
    assertFalse(smallestRendition.matches(500, 500));
    assertFalse(smallestRendition.matches(450, 500));
    assertFalse(smallestRendition.matches(500, 213));
    assertTrue(smallestRendition.matches(450, 0));
    assertTrue(smallestRendition.matches(0, 213));
    assertTrue(smallestRendition.matches(0, 0));
  }

  @Test
  void testMatchesSpec() {
    assertTrue(smallestRendition.matches(450, 213, 450, 213, 0, 0d));
    assertTrue(smallestRendition.matches(200, 100, 500, 300, 0, 0d));
    assertTrue(smallestRendition.matches(0, 213, 0, 213, 0, 0d));
    assertTrue(smallestRendition.matches(450, 0, 450, 0, 0, 0d));
    assertTrue(smallestRendition.matches(0, 0, 450, 213, 0, 0d));
    assertTrue(smallestRendition.matches(450, 213, 0, 0, 0, 0d));

    assertFalse(smallestRendition.matches(500, 0, 0, 0, 0, 0d));
    assertFalse(smallestRendition.matches(0, 500, 0, 0, 0, 0d));
    assertFalse(smallestRendition.matches(0, 0, 100, 0, 0, 0d));
    assertFalse(smallestRendition.matches(0, 0, 0, 100, 0, 0d));

    assertTrue(smallestRendition.matches(0, 0, 0, 0, 200, 0d));
    assertTrue(smallestRendition.matches(0, 0, 0, 0, 400, 0d));
    assertFalse(smallestRendition.matches(0, 0, 0, 0, 600, 0d));

    assertTrue(smallestRendition.matches(0, 0, 0, 0, 0, 2.11d));
    assertFalse(smallestRendition.matches(0, 0, 0, 0, 0, 2.2d));
  }

  @Test
  void testGetLayer() throws PersistenceException {
    loadImageBinary_originalRenditionCopy();
    Layer layer = originalRenditionCopy.getLayer();
    assertEquals(215, layer.getWidth());
    assertEquals(102, layer.getHeight());
  }

  @Test
  void testGetInputStream() throws Exception {
    loadImageBinary_originalRenditionCopy();
    InputStream is = originalRenditionCopy.getInputStream();
    assertNotNull(is);
    is.close();
  }

  @Test
  void testAdaptTo() throws Exception {
    loadImageBinary_originalRenditionCopy();
    String path = MEDIAITEM_PATH_STANDARD + "/jcr:content/renditions/cq5dam.thumbnail.215.102.jpg";

    Rendition damRendition = originalRenditionCopy.adaptTo(Rendition.class);
    assertEquals(path, damRendition.getPath());

    Resource resource = originalRenditionCopy.adaptTo(Resource.class);
    assertEquals(path, resource.getPath());

    Layer layer = originalRenditionCopy.adaptTo(Layer.class);
    assertEquals(215, layer.getWidth());
    assertEquals(102, layer.getHeight());

    InputStream is = originalRenditionCopy.adaptTo(InputStream.class);
    assertNotNull(is);
    is.close();
  }

  @Test
  void testEquals() {
    assertEquals(smallestRendition,smallestRendition);
    assertNotEquals(smallestRendition, biggestRendition);
  }

  private void loadImageBinary_originalRenditionCopy() throws PersistenceException {
    String path = MEDIAITEM_PATH_STANDARD + "/jcr:content/renditions/cq5dam.thumbnail.215.102.jpg";
    context.resourceResolver().delete(context.resourceResolver().getResource(path));
    context.load().binaryFile("/sample_image_215x102.jpg", path);
  }

}
