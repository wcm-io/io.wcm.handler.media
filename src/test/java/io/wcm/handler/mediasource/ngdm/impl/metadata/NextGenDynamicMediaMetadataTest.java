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
import static com.day.cq.dam.api.DamConstants.ASSET_STATUS_PENDING;
import static com.day.cq.dam.api.DamConstants.ASSET_STATUS_PROPERTY;
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_IMAGE;
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_PDF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class NextGenDynamicMediaMetadataTest {

  private final AemContext context = AppAemContext.newAemContext();

  @Test
  void testEmptyJson() throws JsonProcessingException {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson("{}");
    assertEquals(ContentType.OCTET_STREAM, metadata.getMimeType());
    assertNull(metadata.getDimension());
    assertNull(metadata.getAssetStatus());
    assertTrue(metadata.getSmartCrops().isEmpty());
    assertFalse(metadata.isValid());
    assertEquals("[assetStatus=<null>,dimension=<null>,mimeType=<null>,smartCrops=[]]", metadata.toString());
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
    assertEquals("[assetStatus=approved,dimension=[width=1200,height=800],mimeType=image/jpeg,smartCrops=["
        + "[left=0.0,name=Landscape,normalizedHeight=0.4226,normalizedWidth=1.0,top=0.5774], "
        + "[left=0.16792180740265983,name=Portrait,normalizedHeight=0.9980170652565797,normalizedWidth=0.6666399615446242,top=0.0]]"
        + "]", metadata.toString());
  }

  @Test
  void testSampleJson_PDF() throws JsonProcessingException {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(METADATA_JSON_PDF);
    assertEquals(ContentType.PDF, metadata.getMimeType());
    assertNull(metadata.getDimension());
    assertEquals(ASSET_STATUS_APPROVED, metadata.getAssetStatus());
    assertTrue(metadata.getSmartCrops().isEmpty());
    assertTrue(metadata.isValid());
    assertEquals("[assetStatus=approved,dimension=<null>,mimeType=application/pdf,smartCrops=[]]", metadata.toString());
  }

  @Test
  void testInvalidJson() {
    assertThrows(JsonProcessingException.class, () -> NextGenDynamicMediaMetadata.fromJson("no json"));
  }

  @Test
  void testFromAsset() {
    Asset asset = context.create().asset("/content/dam/sample.jpg", 100, 50, ContentType.JPEG,
        ASSET_STATUS_PROPERTY, ASSET_STATUS_PENDING);
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromAsset(asset);

    assertEquals(ContentType.JPEG, metadata.getMimeType());
    Dimension dimension = metadata.getDimension();
    assertNotNull(dimension);
    assertEquals(100, dimension.getWidth());
    assertEquals(50, dimension.getHeight());
    assertEquals(ASSET_STATUS_PENDING, metadata.getAssetStatus());
    assertTrue(metadata.getSmartCrops().isEmpty());
    assertTrue(metadata.isValid());
  }

}
