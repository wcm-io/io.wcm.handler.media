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
    assertFalse(metadata.isValid());
    assertEquals("[dimension=<null>,mimeType=<null>]", metadata.toString());
  }

  @Test
  void testSampleJson_Image() throws JsonProcessingException {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(METADATA_JSON_IMAGE);
    assertEquals(ContentType.JPEG, metadata.getMimeType());
    Dimension dimension = metadata.getDimension();
    assertNotNull(dimension);
    assertEquals(1200, dimension.getWidth());
    assertEquals(800, dimension.getHeight());
    assertTrue(metadata.isValid());
    assertEquals("[dimension=[width=1200,height=800],mimeType=image/jpeg]", metadata.toString());
  }

  @Test
  void testSampleJson_PDF() throws JsonProcessingException {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(METADATA_JSON_PDF);
    assertEquals(ContentType.PDF, metadata.getMimeType());
    assertNull(metadata.getDimension());
    assertTrue(metadata.isValid());
    assertEquals("[dimension=<null>,mimeType=application/pdf]", metadata.toString());
  }

  @Test
  void testInvalidJson() {
    assertThrows(JsonProcessingException.class, () -> NextGenDynamicMediaMetadata.fromJson("no json"));
  }

  @Test
  void testFromAsset() {
    Asset asset = context.create().asset("/content/dam/sample.jpg", 100, 50, ContentType.JPEG);
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromAsset(asset);

    assertEquals(ContentType.JPEG, metadata.getMimeType());
    Dimension dimension = metadata.getDimension();
    assertNotNull(dimension);
    assertEquals(100, dimension.getWidth());
    assertEquals(50, dimension.getHeight());
    assertTrue(metadata.isValid());
  }

}
