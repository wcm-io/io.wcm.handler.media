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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

class NextGenDynamicMediaMetadataTest {

  private static final String SAMPLE_JSON = "{"
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

  @Test
  void testEmptyJson() throws JsonProcessingException {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson("{}");
    assertEquals(0, metadata.getWidth());
    assertEquals(0, metadata.getHeight());
    assertNull(metadata.getMimeType());
    assertFalse(metadata.isValid());
    assertEquals("[height=0,mimeType=<null>,width=0]", metadata.toString());
  }

  @Test
  void testSampleJson() throws JsonProcessingException {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(SAMPLE_JSON);
    assertEquals(1200, metadata.getWidth());
    assertEquals(800, metadata.getHeight());
    assertEquals("image/jpeg", metadata.getMimeType());
    assertTrue(metadata.isValid());
    assertEquals("[height=800,mimeType=image/jpeg,width=1200]", metadata.toString());
  }

  @Test
  void testInvalidJson() {
    assertThrows(JsonProcessingException.class, () -> NextGenDynamicMediaMetadata.fromJson("no json"));
  }

}
