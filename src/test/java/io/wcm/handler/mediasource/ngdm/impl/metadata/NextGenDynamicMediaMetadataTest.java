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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.wcm.handler.media.Dimension;

class NextGenDynamicMediaMetadataTest {

  static final String SAMPLE_JSON_IMAGE = "{"
      + "  \"assetId\": \"urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678\","
      + "  \"repositoryMetadata\": {"
      + "    \"repo:name\": \"test.jpg\","
      + "    \"dc:format\": \"image/jpeg\""
      + "  },"
      + "  \"assetMetadata\": {"
      + "    \"dam:assetStatus\": \"approved\","
      + "    \"dc:description\": \"Test Description\","
      + "    \"dc:title\": \"Test Image\","
      + "    \"tiff:ImageLength\": 800,"
      + "    \"tiff:ImageWidth\": 1200"
      + "  }"
      + "}";

  static final String SAMPLE_JSON_PDF = "{"
      + "  \"assetId\": \"urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678\","
      + "  \"repositoryMetadata\": {"
      + "    \"repo:name\": \"test.pdf\","
      + "    \"dc:format\": \"application/pdf\""
      + "  },"
      + "  \"assetMetadata\": {"
      + "    \"dam:assetStatus\": \"approved\","
      + "    \"dc:description\": \"Test Description\","
      + "    \"dc:title\": \"Test Document\""
      + "  }"
      + "}";

  @Test
  void testEmptyJson() throws JsonProcessingException {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson("{}");
    assertNull(metadata.getMimeType());
    assertNull(metadata.getDimension());
    assertFalse(metadata.isValid());
    assertEquals("[dimension=<null>,mimeType=<null>]", metadata.toString());
  }

  @Test
  void testSampleJson_Image() throws JsonProcessingException {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(SAMPLE_JSON_IMAGE);
    assertEquals("image/jpeg", metadata.getMimeType());
    Dimension dimension = metadata.getDimension();
    assertNotNull(dimension);
    assertEquals(1200, dimension.getWidth());
    assertEquals(800, dimension.getHeight());
    assertTrue(metadata.isValid());
    assertEquals("[dimension=[width=1200,height=800],mimeType=image/jpeg]", metadata.toString());
  }

  @Test
  void testSampleJson_PDF() throws JsonProcessingException {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(SAMPLE_JSON_PDF);
    assertEquals("application/pdf", metadata.getMimeType());
    assertNull(metadata.getDimension());
    assertTrue(metadata.isValid());
    assertEquals("[dimension=<null>,mimeType=application/pdf]", metadata.toString());
  }

  @Test
  void testInvalidJson() {
    assertThrows(JsonProcessingException.class, () -> NextGenDynamicMediaMetadata.fromJson("no json"));
  }

}
