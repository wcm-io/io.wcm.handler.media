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
package io.wcm.handler.mediasource.ngdm.impl.metadata;

import static com.day.cq.dam.api.DamConstants.ASSET_STATUS_APPROVED;
import static com.day.cq.dam.api.DamConstants.DC_DESCRIPTION;
import static com.day.cq.dam.api.DamConstants.DC_TITLE;
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_IMAGE;
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_IMAGE_FULL;
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_PDF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.wcm.handler.media.Dimension;
import io.wcm.wcm.commons.contenttype.ContentType;

class NextGenDynamicMediaMetadataTest {

  @Test
  void testEmptyJson() throws JsonProcessingException {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson("{}");
    assertEquals(ContentType.OCTET_STREAM, metadata.getMimeType());
    assertNull(metadata.getDimension());
    assertNull(metadata.getAssetStatus());
    assertTrue(metadata.getSmartCrops().isEmpty());
    assertFalse(metadata.isValid());
    assertEquals("NextGenDynamicMediaMetadata[]", metadata.toString());
  }

  @Test
  void testSampleJson_Image() throws JsonProcessingException {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(METADATA_JSON_IMAGE);
    assertEquals(ContentType.JPEG, metadata.getMimeType());
    Dimension dimension = metadata.getDimension();
    assertNotNull(dimension);
    assertEquals(1200, dimension.getWidth());
    assertEquals(800, dimension.getHeight());
    assertEquals(ASSET_STATUS_APPROVED, metadata.getAssetStatus());
    assertEquals(2, metadata.getSmartCrops().size());
    assertTrue(metadata.isValid());
    assertEquals("NextGenDynamicMediaMetadata[mimeType=image/jpeg,dimension=[width=1200,height=800],assetStatus=approved,"
        + "properties={dam:assetStatus=approved, tiff:ImageLength=800, tiff:ImageWidth=1200},"
        + "smartCrops=[[cropDimension=[left=0,top=462,width=1200,height=675],name=Landscape,ratio=1.7777777777777777], "
        + "[cropDimension=[left=202,top=0,width=399,height=798],name=Portrait,ratio=0.5]]]", metadata.toString());
  }

  @Test
  void testSampleJson_Image_Full() throws JsonProcessingException {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(METADATA_JSON_IMAGE_FULL);
    assertEquals(ContentType.JPEG, metadata.getMimeType());
    Dimension dimension = metadata.getDimension();
    assertNotNull(dimension);
    assertEquals(1500, dimension.getWidth());
    assertEquals(900, dimension.getHeight());
    assertEquals(ASSET_STATUS_APPROVED, metadata.getAssetStatus());
    assertEquals("Test Image", metadata.getProperties().get(DC_TITLE, String.class));
    assertEquals("Test Description", metadata.getProperties().get(DC_DESCRIPTION, String.class));
    assertEquals(0, metadata.getSmartCrops().size());
    assertTrue(metadata.isValid());
    assertEquals("NextGenDynamicMediaMetadata[mimeType=image/jpeg,dimension=[width=1500,height=900],assetStatus=approved,"
        + "properties={dam:assetStatus=approved, dc:description=Test Description, dc:title=Test Image, tiff:ImageLength=900, tiff:ImageWidth=1500}]",
        metadata.toString());
  }

  @Test
  void testSampleJson_PDF() throws JsonProcessingException {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(METADATA_JSON_PDF);
    assertEquals(ContentType.PDF, metadata.getMimeType());
    assertNull(metadata.getDimension());
    assertEquals(ASSET_STATUS_APPROVED, metadata.getAssetStatus());
    assertTrue(metadata.getSmartCrops().isEmpty());
    assertTrue(metadata.isValid());
    assertEquals("NextGenDynamicMediaMetadata[mimeType=application/pdf,assetStatus=approved,properties={dam:assetStatus=approved}]", metadata.toString());
  }

  @Test
  void testInvalidJson() {
    assertThrows(JsonProcessingException.class, () -> NextGenDynamicMediaMetadata.fromJson("no json"));
  }

}
