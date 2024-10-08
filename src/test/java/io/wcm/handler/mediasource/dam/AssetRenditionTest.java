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
package io.wcm.handler.mediasource.dam;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.dam.api.DamConstants.METADATA_FOLDER;
import static com.day.cq.dam.api.DamConstants.TIFF_IMAGELENGTH;
import static com.day.cq.dam.api.DamConstants.TIFF_IMAGEWIDTH;
import static io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataNameConstants.NN_RENDITIONS_METADATA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.Rendition;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.dam.impl.metadata.AssetSynchonizationService;
import io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataListenerService;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class AssetRenditionTest {

  private final AemContext context = AppAemContext.newAemContext();

  private Asset asset;
  private Rendition original;
  private Rendition rendition;

  @BeforeEach
  void setUp() {
    // register RenditionMetadataListenerService to generate rendition metadata
    context.registerInjectActivateService(new AssetSynchonizationService());
    context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "threadPoolSize", 0,
        "allowedRunMode", new String[0]);

    asset = context.create().asset("/content/dam/asset1.jpg", 16, 9, ContentType.JPEG);
    original = asset.getOriginal();
    rendition = context.create().assetRendition(asset, "rendition1.png", 10, 5, ContentType.PNG);
  }

  @Test
  void testIsOriginal() {
    assertTrue(AssetRendition.isOriginal(original));
    assertFalse(AssetRendition.isOriginal(rendition));
  }

  @Test
  void testGetFilename() {
    assertEquals("asset1.jpg", AssetRendition.getFilename(original));
    assertEquals("rendition1.png", AssetRendition.getFilename(rendition));
  }

  @Test
  void testGetDimension() {
    assertEquals(new Dimension(16, 9), AssetRendition.getDimension(original));
    assertEquals(new Dimension(10, 5), AssetRendition.getDimension(rendition));
  }

  @Test
  void testGetDimension_WithAemRenditionMetadata() {
    // add rendition metadata as provided by AEMaaCS asset compute (has higher priority)
    context.create().resource(rendition, JCR_CONTENT + "/" + METADATA_FOLDER,
        TIFF_IMAGEWIDTH, 100, TIFF_IMAGELENGTH, 50);

    assertEquals(new Dimension(16, 9), AssetRendition.getDimension(original));
    assertEquals(new Dimension(100, 50), AssetRendition.getDimension(rendition));
  }

  @Test
  @SuppressWarnings("null")
  void testGetDimension_SVG() {
    Asset svgAsset = context.create().asset("/content/dam/sample.svg", "/filetype/sample.svg", ContentType.SVG);
    Rendition svgImageRendition = context.create().assetRendition(svgAsset, "sample.png", "/filetype/sample.png", ContentType.PNG);

    // remove asset metadata for width/height as they are not created by AEM for SVG, too (as of AEM 6.5)
    Resource metadataResource = AdaptTo.notNull(svgAsset, Resource.class).getChild(JCR_CONTENT + "/" + DamConstants.METADATA_FOLDER);
    ModifiableValueMap metadataProps = AdaptTo.notNull(metadataResource, ModifiableValueMap.class);
    metadataProps.remove(DamConstants.TIFF_IMAGEWIDTH);
    metadataProps.remove(DamConstants.TIFF_IMAGELENGTH);

    assertEquals(new Dimension(100, 50), AssetRendition.getDimension(svgAsset.getOriginal()));
    assertEquals(new Dimension(100, 50), AssetRendition.getDimension(svgImageRendition));
  }

  @Test
  void testGetDimensionWithoutRenditionsMetadata() throws PersistenceException {
    // remove renditions metadata generated by DamRenditionMetadataService
    Resource renditionsMetadata = AdaptTo.notNull(asset, Resource.class).getChild(JCR_CONTENT + "/" + NN_RENDITIONS_METADATA);
    if (renditionsMetadata != null) {
      context.resourceResolver().delete(renditionsMetadata);
    }

    assertEquals(new Dimension(16, 9), AssetRendition.getDimension(original, true));
    assertEquals(new Dimension(10, 5), AssetRendition.getDimension(rendition, true));
  }

  @Test
  void testGetDimensionWithoutRenditionsMetadata_WithAemRenditionMetadata() throws PersistenceException {
    // remove renditions metadata generated by DamRenditionMetadataService
    Resource renditionsMetadata = AdaptTo.notNull(asset, Resource.class).getChild(JCR_CONTENT + "/" + NN_RENDITIONS_METADATA);
    if (renditionsMetadata != null) {
      context.resourceResolver().delete(renditionsMetadata);
    }

    // add rendition metadata as provided by AEMaaCS asset compute
    context.create().resource(rendition, JCR_CONTENT + "/" + METADATA_FOLDER,
        TIFF_IMAGEWIDTH, 100, TIFF_IMAGELENGTH, 50);

    assertEquals(new Dimension(16, 9), AssetRendition.getDimension(original, true));
    assertEquals(new Dimension(100, 50), AssetRendition.getDimension(rendition, true));
  }

  @Test
  void testIsThumbnailRendition() {
    assertTrue(AssetRendition.isThumbnailRendition(renditionByName("cq5dam.thumbnail.10.10.png")));
    assertFalse(AssetRendition.isThumbnailRendition(renditionByName("cq5dam.web.100.100.jpg")));
  }

  @Test
  void testIsWebRendition() {
    assertFalse(AssetRendition.isWebRendition(renditionByName("cq5dam.thumbnail.10.10.png")));
    assertTrue(AssetRendition.isWebRendition(renditionByName("cq5dam.web.100.100.jpg")));
  }

  @SuppressWarnings("null")
  private static Rendition renditionByName(String name) {
    Rendition rendition = mock(Rendition.class);
    when(rendition.getName()).thenReturn(name);
    return rendition;
  }

}
