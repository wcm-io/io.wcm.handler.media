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
package io.wcm.handler.media.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.commons.jcr.JcrConstants;

import io.wcm.sling.commons.resource.ImmutableValueMap;

@ExtendWith(MockitoExtension.class)
class JcrBinaryTest {

  private static final String MIMETYPE_GIF = "image/gif";

  @Mock
  private Resource resource;
  @Mock
  private Resource subResource;

  @Test
  void testIsNtFileResource_Resource() {
    when(resource.getResourceType()).thenReturn(JcrConstants.NT_RESOURCE);
    assertFalse(JcrBinary.isNtFile(resource));
    assertTrue(JcrBinary.isNtResource(resource));
    assertTrue(JcrBinary.isNtFileOrResource(resource));
  }

  @Test
  void testIsNtFileResource_File() {
    when(resource.getResourceType()).thenReturn(JcrConstants.NT_FILE);
    assertTrue(JcrBinary.isNtFile(resource));
    assertFalse(JcrBinary.isNtResource(resource));
    assertTrue(JcrBinary.isNtFileOrResource(resource));
  }

  @Test
  @SuppressWarnings("null")
  void testIsNtFileResource_Other() {
    when(resource.getResourceType()).thenReturn(null);
    assertFalse(JcrBinary.isNtFile(resource));
    assertFalse(JcrBinary.isNtResource(resource));
    assertFalse(JcrBinary.isNtFileOrResource(resource));
  }

  @Test
  void testGetMimeType_Resource() {
    when(resource.getResourceType()).thenReturn(JcrConstants.NT_RESOURCE);
    when(resource.getValueMap()).thenReturn(ImmutableValueMap.of(
        JcrConstants.JCR_MIMETYPE, MIMETYPE_GIF));

    assertEquals(MIMETYPE_GIF, JcrBinary.getMimeType(resource));
  }

  @Test
  void testGetMimeType_Resource_NoMimeType() {
    when(resource.getResourceType()).thenReturn(JcrConstants.NT_RESOURCE);
    when(resource.getValueMap()).thenReturn(ValueMap.EMPTY);

    assertNull(JcrBinary.getMimeType(resource));
  }

  @Test
  void testGetMimeType_File() {
    when(resource.getResourceType()).thenReturn(JcrConstants.NT_FILE);
    when(resource.getChild(JcrConstants.JCR_CONTENT)).thenReturn(subResource);
    when(subResource.getResourceType()).thenReturn(JcrConstants.NT_RESOURCE);
    when(subResource.getValueMap()).thenReturn(ImmutableValueMap.of(
        JcrConstants.JCR_MIMETYPE, MIMETYPE_GIF));

    assertEquals(MIMETYPE_GIF, JcrBinary.getMimeType(resource));
  }

  @Test
  void testGetMimeType_File_NoContent() {
    when(resource.getResourceType()).thenReturn(JcrConstants.NT_FILE);
    when(resource.getChild(JcrConstants.JCR_CONTENT)).thenReturn(null);

    assertNull(JcrBinary.getMimeType(resource));
  }

  @Test
  void testGetMimeType_Other() {
    when(resource.getResourceType()).thenReturn("otherType");

    assertNull(JcrBinary.getMimeType(resource));
  }

}
