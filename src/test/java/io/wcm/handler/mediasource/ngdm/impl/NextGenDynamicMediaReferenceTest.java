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
package io.wcm.handler.mediasource.ngdm.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NextGenDynamicMediaReferenceTest {

  private static final String SAMPLE_ASSET_ID = "urn:aaid:aem:18412592-06f9-4d97-afac-a9387237c243";
  private static final String SAMPLE_FILENAME = "my-image.jpg";
  private static final String SAMPLE_REFERENCE = "/" + SAMPLE_ASSET_ID + "/" + SAMPLE_FILENAME;

  @Test
  void testFromReference() {
    NextGenDynamicMediaReference underTest = NextGenDynamicMediaReference.fromReference(SAMPLE_REFERENCE);
    assertNotNull(underTest);
    assertEquals(SAMPLE_ASSET_ID, underTest.getAssetId());
    assertEquals(SAMPLE_FILENAME, underTest.getFileName());
    assertEquals(SAMPLE_REFERENCE, underTest.toReference());
    assertEquals(SAMPLE_REFERENCE, underTest.toString());
  }

  @Test
  void testFromReference_Invalid() {
    assertNull(NextGenDynamicMediaReference.fromReference("wurstbrot"));
    assertNull(NextGenDynamicMediaReference.fromReference("/wurst/brot"));
    assertNull(NextGenDynamicMediaReference.fromReference(""));
  }

  @Test
  void testIsReference() {
    assertTrue(NextGenDynamicMediaReference.isReference(SAMPLE_REFERENCE));
    assertFalse(NextGenDynamicMediaReference.isReference("wurstbrot"));
    assertFalse(NextGenDynamicMediaReference.isReference("/wurst/brot"));
    assertFalse(NextGenDynamicMediaReference.isReference(""));
    assertFalse(NextGenDynamicMediaReference.isReference(null));
  }

  @Test
  void testNewInstance_IllegalArgument() {
    assertThrows(IllegalArgumentException.class,
        () -> new NextGenDynamicMediaReference("wurstbrot", SAMPLE_FILENAME));
  }

}
