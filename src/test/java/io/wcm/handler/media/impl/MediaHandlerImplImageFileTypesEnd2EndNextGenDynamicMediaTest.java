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

import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_ASSET_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;

import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReference;
import io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * Executes the same "end-to-end" as {@link MediaHandlerImplImageFileTypesEnd2EndTest}, but
 * with rendering via web-optimized image delivery.
 */
@ExtendWith(AemContextExtension.class)
@SuppressWarnings("java:S2699") // all tests have assertions
class MediaHandlerImplImageFileTypesEnd2EndNextGenDynamicMediaTest extends MediaHandlerImplImageFileTypesEnd2EndTest {

  @Override
  void setUp() {
    MockNextGenDynamicMediaConfig nextGenDynamicMediaConfig = context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class);
    nextGenDynamicMediaConfig.setEnabled(true);
    nextGenDynamicMediaConfig.setRepositoryId("repo1");
    super.setUp();
  }

  @Override
  @Test
  void testAsset_JPEG_Original() {
    Asset asset = createNextGenDynamicMediaReferenceAsAsset("sample.jpg");
    buildAssertMedia(asset, 100, 50,
        "https://repo1/adobe/dynamicmedia/deliver/" + SAMPLE_ASSET_ID + "/sample.jpg?preferwebp=true&quality=85",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_JPEG_Rescale() {
    Asset asset = createNextGenDynamicMediaReferenceAsAsset("sample.jpg");
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://repo1/adobe/dynamicmedia/deliver/" + SAMPLE_ASSET_ID + "/sample.jpg?crop=80%3A40%2Csmart&preferwebp=true&quality=85&width=80",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_JPEG_AutoCrop() {
    Asset asset = createNextGenDynamicMediaReferenceAsAsset("sample.jpg");
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://repo1/adobe/dynamicmedia/deliver/" + SAMPLE_ASSET_ID + "/sample.jpg?crop=1%3A1%2Csmart&preferwebp=true&quality=85",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_JPEG_AutoCrop_ImageQuality() {
    Asset asset = createNextGenDynamicMediaReferenceAsAsset("sample.jpg");
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://repo1/adobe/dynamicmedia/deliver/" + SAMPLE_ASSET_ID + "/sample.jpg?crop=1%3A1%2Csmart&preferwebp=true&quality=60",
        ContentType.JPEG, 0.6d);
  }

  @Override
  @Test
  void testAsset_GIF_Original() {
    Asset asset = createNextGenDynamicMediaReferenceAsAsset("sample.gif");
    buildAssertMedia(asset, 100, 50,
        "https://repo1/adobe/dynamicmedia/deliver/" + SAMPLE_ASSET_ID + "/sample.gif?preferwebp=true&quality=85",
        ContentType.GIF);
  }

  @Override
  @Test
  void testAsset_GIF_Rescale() {
    Asset asset = createNextGenDynamicMediaReferenceAsAsset("sample.gif");
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://repo1/adobe/dynamicmedia/deliver/" + SAMPLE_ASSET_ID + "/sample.gif?crop=80%3A40%2Csmart&preferwebp=true&quality=85&width=80",
        ContentType.GIF);
  }

  @Override
  @Test
  void testAsset_GIF_AutoCrop() {
    Asset asset = createNextGenDynamicMediaReferenceAsAsset("sample.gif");
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://repo1/adobe/dynamicmedia/deliver/" + SAMPLE_ASSET_ID + "/sample.gif?crop=1%3A1%2Csmart&preferwebp=true&quality=85",
        ContentType.GIF);
  }

  @Override
  @Test
  void testAsset_PNG_Original() {
    Asset asset = createNextGenDynamicMediaReferenceAsAsset("sample.png");
    buildAssertMedia(asset, 100, 50,
        "https://repo1/adobe/dynamicmedia/deliver/" + SAMPLE_ASSET_ID + "/sample.png?preferwebp=true&quality=85",
        ContentType.PNG);
  }

  @Override
  @Test
  void testAsset_PNG_Rescale() {
    Asset asset = createNextGenDynamicMediaReferenceAsAsset("sample.png");
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://repo1/adobe/dynamicmedia/deliver/" + SAMPLE_ASSET_ID + "/sample.png?crop=80%3A40%2Csmart&preferwebp=true&quality=85&width=80",
        ContentType.PNG);
  }

  @Override
  @Test
  void testAsset_PNG_AutoCrop() {
    Asset asset = createNextGenDynamicMediaReferenceAsAsset("sample.png");
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://repo1/adobe/dynamicmedia/deliver/" + SAMPLE_ASSET_ID + "/sample.png?crop=1%3A1%2Csmart&preferwebp=true&quality=85",
        ContentType.PNG);
  }

  @Override
  @Test
  void testAsset_TIFF_Original() {
    Asset asset = createNextGenDynamicMediaReferenceAsAsset("sample.tif");
    buildAssertMedia(asset, 100, 50,
        "https://repo1/adobe/dynamicmedia/deliver/" + SAMPLE_ASSET_ID + "/sample.jpg?preferwebp=true&quality=85",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_TIFF_Rescale() {
    Asset asset = createNextGenDynamicMediaReferenceAsAsset("sample.tif");
    buildAssertMedia_Rescale(asset, 80, 40,
        "https://repo1/adobe/dynamicmedia/deliver/" + SAMPLE_ASSET_ID + "/sample.jpg?crop=80%3A40%2Csmart&preferwebp=true&quality=85&width=80",
        ContentType.JPEG);
  }

  @Override
  @Test
  void testAsset_TIFF_AutoCrop() {
    Asset asset = createNextGenDynamicMediaReferenceAsAsset("sample.tif");
    buildAssertMedia_AutoCrop(asset, 50, 50,
        "https://repo1/adobe/dynamicmedia/deliver/" + SAMPLE_ASSET_ID + "/sample.jpg?crop=1%3A1%2Csmart&preferwebp=true&quality=85",
        ContentType.JPEG);
  }

  @SuppressWarnings("null")
  Asset createNextGenDynamicMediaReferenceAsAsset(String fileName) {
    String reference = new NextGenDynamicMediaReference(SAMPLE_ASSET_ID, fileName).toReference();
    Asset asset = mock(Asset.class);
    when(asset.getPath()).thenReturn(reference);
    when(asset.getOriginal()).thenReturn(mock(Rendition.class));
    return asset;
  }

}
