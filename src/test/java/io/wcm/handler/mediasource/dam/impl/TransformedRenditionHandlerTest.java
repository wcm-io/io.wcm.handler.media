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

import static org.junit.Assert.assertEquals;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.service.event.EventHandler;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamEvent;
import com.day.cq.dam.api.Rendition;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * Tests for {@link TransformedRenditionHandler}
 */
public class TransformedRenditionHandlerTest {

  @Rule
  public AemContext context = AppAemContext.newAemContext();

  private Asset asset;
  private Rendition webRendition;
  private CropDimension cropDimension;

  @Before
  public void setUp() throws Exception {

    // register DamRenditionMetadataService which is only active on author run mode
    context.runMode("author");
    EventHandler eventHandler = context.registerInjectActivateService(new DamRenditionMetadataService());

    asset = context.create().asset("/content/dam/cropTest.jpg", 400, 300, ContentType.JPEG);

    // generate web-enabled rendition
    webRendition = context.create().assetRendition(asset, "cq5dam.web.200.150.jpg", 200, 150, ContentType.JPEG);
    eventHandler.handleEvent(DamEvent.renditionUpdated(asset.getPath(), "admin", webRendition.getPath()).toEvent());

    cropDimension = new CropDimension(20, 10, 100, 30);
  }

  @Test
  public void testCroppingWithoutRotation() {
    TransformedRenditionHandler underTest = new TransformedRenditionHandler(asset, cropDimension, null);
    assertEquals(1, underTest.getAvailableRenditions(new MediaArgs()).size());
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/cropTest.jpg/jcr:content/renditions/original.image_file.200.60.40,20,240,80.file/cropTest.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  @SuppressWarnings("null")
  public void testCroppingWithoutWebRendition() throws PersistenceException {
    // delete web rendition
    context.resourceResolver().delete(webRendition.adaptTo(Resource.class));

    TransformedRenditionHandler underTest = new TransformedRenditionHandler(asset, cropDimension, null);
    assertEquals(1, underTest.getAvailableRenditions(new MediaArgs()).size());
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/cropTest.jpg/jcr:content/renditions/original.image_file.100.30.20,10,120,40.file/cropTest.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testRotation90() {
    TransformedRenditionHandler underTest = new TransformedRenditionHandler(asset, null, 90);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/cropTest.jpg/jcr:content/renditions/original.image_file.300.400.-.90.file/cropTest.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testRotation180() {
    TransformedRenditionHandler underTest = new TransformedRenditionHandler(asset, null, 180);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/cropTest.jpg/jcr:content/renditions/original.image_file.400.300.-.180.file/cropTest.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testRotation270() {
    TransformedRenditionHandler underTest = new TransformedRenditionHandler(asset, null, 270);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/cropTest.jpg/jcr:content/renditions/original.image_file.300.400.-.270.file/cropTest.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testRotationInvalid() {
    TransformedRenditionHandler underTest = new TransformedRenditionHandler(asset, null, 45);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/cropTest.jpg/jcr:content/renditions/cq5dam.web.200.150.jpg./cq5dam.web.200.150.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testRotation90Cropping() {
    TransformedRenditionHandler underTest = new TransformedRenditionHandler(asset, cropDimension, 90);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/cropTest.jpg/jcr:content/renditions/original.image_file.60.200.40,20,240,80.90.file/cropTest.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testRotation180Cropping() {
    TransformedRenditionHandler underTest = new TransformedRenditionHandler(asset, cropDimension, 180);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/cropTest.jpg/jcr:content/renditions/original.image_file.200.60.40,20,240,80.180.file/cropTest.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testRotation270Cropping() {
    TransformedRenditionHandler underTest = new TransformedRenditionHandler(asset, cropDimension, 270);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/cropTest.jpg/jcr:content/renditions/original.image_file.60.200.40,20,240,80.270.file/cropTest.jpg",
        firstRendition.getMediaPath(false));
  }

  @Test
  public void testRotationInvalidCropping() {
    TransformedRenditionHandler underTest = new TransformedRenditionHandler(asset, cropDimension, 45);
    RenditionMetadata firstRendition = underTest.getAvailableRenditions(new MediaArgs()).iterator().next();
    assertEquals("/content/dam/cropTest.jpg/jcr:content/renditions/original.image_file.200.60.40,20,240,80.file/cropTest.jpg",
        firstRendition.getMediaPath(false));
  }

}
