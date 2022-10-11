/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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

import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO2;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO_SQUARE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaBuilder;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest.MediaInclude;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.dam.impl.metadata.AssetSynchonizationService;
import io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataListenerService;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class MediaIncludeTest {

  private final AemContext context = AppAemContext.newAemContext();

  private MediaHandler mediaHandler;
  private Asset asset;
  private Asset asset2;
  private Asset asset3;

  @BeforeEach
  void setUp() {
    // register RenditionMetadataListenerService to generate rendition metadata
    context.registerInjectActivateService(new AssetSynchonizationService());
    context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "threadPoolSize", 0,
        "allowedRunMode", new String[0]);

    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    // prepare assets with web rendition
    asset = context.create().asset("/content/dam/test.jpg", 400, 200, ContentType.JPEG);
    context.create().assetRenditionWebEnabled(asset, 400, 200);
    asset2 = context.create().asset("/content/dam/test2.jpg", 300, 300, ContentType.JPEG);
    context.create().assetRenditionWebEnabled(asset2, 300, 300);
    asset3 = context.create().asset("/content/dam/test3.jpg", 300, 500, ContentType.JPEG);
    context.create().assetRenditionWebEnabled(asset3, 300, 500);
  }

  @Test
  void testResponsiveImageWithInclude() {
    Resource resource = context.create().resource("/content/test",
        "sling:resourceType", "app1/components/comp1",
        MediaNameConstants.PN_MEDIA_REF, asset.getPath(),
        MediaNameConstants.PN_MEDIA_CROP, new CropDimension(10, 10, 160, 100).getCropString(),
        "ref2", asset2.getPath(),
        "crop2", new CropDimension(0, 0, 200, 150).getCropString(),
        "ref3", asset3.getPath());

    MediaBuilder media2Builder = mediaHandler.get(resource)
        .refProperty("ref2")
        .cropProperty("crop2");
    MediaBuilder media3Builder = mediaHandler.get(resource)
        .refProperty("ref3")
        .autoCrop(true);

    Media media = mediaHandler.get(resource)
        .pictureSource(new PictureSource(RATIO).media("media1").widths(80))
        .pictureSource(new PictureSource(RATIO2).media("media2").widths(100))
        .pictureSource(new PictureSource(RATIO_SQUARE).widths(75))
        .include(new MediaInclude(RATIO2, media2Builder))
        .include(new MediaInclude(RATIO_SQUARE, media3Builder))
        .build();
    assertTrue(media.isValid());
    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());
    assertEquals(3, renditions.size());

    Rendition rendition1 = renditions.get(0);
    assertEquals(80, rendition1.getWidth());
    assertEquals(50, rendition1.getHeight());
    assertEquals("/content/dam/test.jpg/_jcr_content/renditions/original.image_file.80.50.10,10,170,110.file/test.jpg", rendition1.getUrl());

    Rendition rendition2 = renditions.get(1);
    assertEquals(100, rendition2.getWidth());
    assertEquals(75, rendition2.getHeight());
    assertEquals("/content/dam/test2.jpg/_jcr_content/renditions/original.image_file.100.75.0,0,200,150.file/test2.jpg", rendition2.getUrl());

    Rendition rendition3 = renditions.get(2);
    assertEquals(75, rendition3.getWidth());
    assertEquals(75, rendition3.getHeight());
    assertEquals("/content/dam/test3.jpg/_jcr_content/renditions/original.image_file.75.75.0,100,300,400.file/test3.jpg", rendition3.getUrl());
  }

}
