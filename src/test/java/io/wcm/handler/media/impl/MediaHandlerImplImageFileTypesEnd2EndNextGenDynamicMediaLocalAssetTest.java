/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2024 wcm.io
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigServiceImpl;
import io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * This is an "end-to-end" test handling image files with different content types
 * from classpath, handles them with and without cropping using media handler
 * and renders the result using the ImageFileServlet.
 */
@ExtendWith(AemContextExtension.class)
@SuppressWarnings("java:S2699") // all tests have assertions
class MediaHandlerImplImageFileTypesEnd2EndNextGenDynamicMediaLocalAssetTest extends MediaHandlerImplImageFileTypesEnd2EndTest {

  @BeforeEach
  @Override
  void setUp() {
    MockNextGenDynamicMediaConfig nextGenDynamicMediaConfig = context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class);
    nextGenDynamicMediaConfig.setEnabled(true);
    nextGenDynamicMediaConfig.setRepositoryId("repo1");
    context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class,
        "enabledLocalAssets", "true",
        "localAssetsRepositoryId", "localrepo1");
    super.setUp();
  }

  @Override
  @Test
  void testAsset_JPEG_Original() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia(asset, 100, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.jpg?preferwebp=true&quality=85",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_JPEG_Original_ContentDisposition() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia_ContentDisposition(asset, 100, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.jpg?preferwebp=true&quality=85",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_JPEG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.jpg?crop=80%3A40%2Csmart&preferwebp=true&quality=85&width=80",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_JPEG_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.jpg?crop=1%3A1%2Csmart&preferwebp=true&quality=85",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_JPEG_AutoCrop_ImageQuality() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.jpg?crop=1%3A1%2Csmart&preferwebp=true&quality=60",
        ContentType.JPEG, 0.6d);
  }

  @Override
  @Test
  void testAsset_JPEG_CropWithExplicitRendition() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    context.create().assetRendition(asset, "square.jpg", 50, 50, ContentType.JPEG);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.jpg?crop=1%3A1%2Csmart&preferwebp=true&quality=85",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_GIF_Original() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    buildAssertMedia(asset, 100, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.gif?preferwebp=true&quality=85",
        ContentType.GIF);
  }

  @Override
  @Test
  void testAsset_GIF_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.gif?crop=80%3A40%2Csmart&preferwebp=true&quality=85&width=80",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_GIF_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.gif?crop=1%3A1%2Csmart&preferwebp=true&quality=85",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_PNG_Original() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    buildAssertMedia(asset, 100, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.png?preferwebp=true&quality=85",
        ContentType.PNG);
  }

  @Override
  @Test
  void testAsset_PNG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.png?crop=80%3A40%2Csmart&preferwebp=true&quality=85&width=80",
        ContentType.PNG);
  }

  @Override
  @Test
  void testAsset_PNG_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.png?crop=1%3A1%2Csmart&preferwebp=true&quality=85",
        ContentType.PNG);
  }

  @Override
  @Test
  void testAsset_TIFF_Original() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia(asset, 100, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.jpg?preferwebp=true&quality=85",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_TIFF_Original_ContentDisposition() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia_ContentDisposition(asset, 100, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.jpg?preferwebp=true&quality=85",
        ContentType.TIFF);
  }

  @Override
  @Test
  void testAsset_TIFF_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.jpg?crop=80%3A40%2Csmart&preferwebp=true&quality=85&width=80",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_TIFF_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/sample.jpg?crop=1%3A1%2Csmart&preferwebp=true&quality=85",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_SVG_Original() {
    Asset asset = createSampleAsset("/filetype/sample.svg", ContentType.SVG);
    buildAssertMedia(asset, 100, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/original/as/sample.svg",
        ContentType.SVG);
  }

  @Override
  @Test
  void testAsset_SVG_Original_ContentDisposition() {
    Asset asset = createSampleAsset("/filetype/sample.svg", ContentType.SVG);
    buildAssertMedia_ContentDisposition(asset, 100, 50,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/original/as/sample.svg?attachment=true",
        ContentType.SVG);
  }

  @Override
  @Test
  void testAsset_SVG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.svg", ContentType.SVG);
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://localrepo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/original/as/sample.svg",
        ContentType.SVG);
  }

  @Override
  @Test
  @Disabled("Not supported with NGDM")
  void testAsset_SVG_AutoCrop() {
    // disabled
  }

}
