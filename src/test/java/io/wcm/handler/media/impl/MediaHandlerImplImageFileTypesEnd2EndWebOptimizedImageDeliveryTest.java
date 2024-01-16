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

import static io.wcm.testing.mock.aem.dam.MockAssetDelivery.getAssetId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;

import io.wcm.testing.mock.aem.dam.MockAssetDelivery;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * Executes the same "end-to-end" as {@link MediaHandlerImplImageFileTypesEnd2EndTest}, but
 * with rendering via web-optimized image delivery.
 */
@ExtendWith(AemContextExtension.class)
@SuppressWarnings("java:S2699") // all tests have assertions
class MediaHandlerImplImageFileTypesEnd2EndWebOptimizedImageDeliveryTest extends MediaHandlerImplImageFileTypesEnd2EndTest {

  @Override
  void setUp() {
    context.registerInjectActivateService(MockAssetDelivery.class);
    super.setUp();
  }

  @Override
  @Test
  void testAsset_JPEG_Original() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia(asset, 100, 50,
        "/asset/delivery/" + getAssetId(asset) + "/sample.jpg?preferwebp=true",
        ContentType.JPEG);
  }

  @Test
  void testAsset_JPEG_Original_WebOptimizedImageDeliveryDisabled() {
    this.webOptimizedImageDeliveryDisabled = true;
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia(asset, 100, 50,
        "/content/dam/sample.jpg/_jcr_content/renditions/original./sample.jpg",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_JPEG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia_Rescale(asset, 80, 40,
        "/asset/delivery/" + getAssetId(asset) + "/sample.jpg?preferwebp=true&width=80",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_JPEG_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "/asset/delivery/" + getAssetId(asset) + "/sample.jpg?c=25%2C0%2C50%2C50&preferwebp=true&width=50",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_JPEG_CropWithExplicitRendition() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    context.create().assetRendition(asset, "square.jpg", 50, 50, ContentType.JPEG);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "/asset/delivery/" + getAssetId(asset) + "/sample.jpg?c=25%2C0%2C50%2C50&preferwebp=true&width=50",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_GIF_Original() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    buildAssertMedia(asset, 100, 50,
        "/asset/delivery/" + getAssetId(asset) + "/sample.gif?preferwebp=true",
        ContentType.GIF);
  }

  @Override
  @Test
  void testAsset_GIF_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    buildAssertMedia_Rescale(asset, 80, 40,
        "/asset/delivery/" + getAssetId(asset) + "/sample.gif?preferwebp=true&width=80",
        ContentType.GIF);
  }

  @Override
  @Test
  void testAsset_GIF_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.gif", ContentType.GIF);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "/asset/delivery/" + getAssetId(asset) + "/sample.gif?c=25%2C0%2C50%2C50&preferwebp=true&width=50",
        ContentType.GIF);
  }

  @Override
  @Test
  void testAsset_PNG_Original() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    buildAssertMedia(asset, 100, 50,
        "/asset/delivery/" + getAssetId(asset) + "/sample.png?preferwebp=true",
        ContentType.PNG);
  }

  @Override
  @Test
  void testAsset_PNG_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    buildAssertMedia_Rescale(asset, 80, 40,
        "/asset/delivery/" + getAssetId(asset) + "/sample.png?preferwebp=true&width=80",
        ContentType.PNG);
  }

  @Override
  @Test
  void testAsset_PNG_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.png", ContentType.PNG);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "/asset/delivery/" + getAssetId(asset) + "/sample.png?c=25%2C0%2C50%2C50&preferwebp=true&width=50",
        ContentType.PNG);
  }

  @Override
  @Test
  void testAsset_TIFF_Original() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia(asset, 100, 50,
        "/asset/delivery/" + getAssetId(asset) + "/sample.jpg?preferwebp=true",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_TIFF_Rescale() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia_Rescale(asset, 80, 40,
        "/asset/delivery/" + getAssetId(asset) + "/sample.jpg?preferwebp=true&width=80",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_TIFF_AutoCrop() {
    Asset asset = createSampleAsset("/filetype/sample.tif", ContentType.TIFF);
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "/asset/delivery/" + getAssetId(asset) + "/sample.jpg?c=25%2C0%2C50%2C50&preferwebp=true&width=50",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_SVG_Rescale() {
    // TODO: Auto-generated method stub
    super.testAsset_SVG_Rescale();
  }

}
