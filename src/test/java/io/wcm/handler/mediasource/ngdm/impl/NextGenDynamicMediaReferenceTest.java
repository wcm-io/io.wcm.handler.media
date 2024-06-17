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

import static com.day.cq.dam.api.DamConstants.ASSET_STATUS_APPROVED;
import static com.day.cq.dam.api.DamConstants.ASSET_STATUS_PROPERTY;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_ASSET_ID;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_FILENAME;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_REFERENCE;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class NextGenDynamicMediaReferenceTest {

  private final AemContext context = AppAemContext.newAemContext();

  @Test
  void testFromReference() {
    NextGenDynamicMediaReference underTest = NextGenDynamicMediaReference.fromReference(SAMPLE_REFERENCE);
    assertNotNull(underTest);
    assertEquals(SAMPLE_ASSET_ID, underTest.getAssetId());
    assertEquals(SAMPLE_FILENAME, underTest.getFileName());
    assertEquals(SAMPLE_REFERENCE, underTest.toReference());
    assertNull(underTest.getAsset());
    assertEquals(SAMPLE_REFERENCE, underTest.toString());
  }

  @Test
  void testFromReference_Invalid() {
    assertNull(NextGenDynamicMediaReference.fromReference("wurstbrot"));
    assertNull(NextGenDynamicMediaReference.fromReference("/wurst/brot"));
    assertNull(NextGenDynamicMediaReference.fromReference(""));
    assertNull(NextGenDynamicMediaReference.fromReference(null));
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

  @Test
  void testFromDamAssetReference_Invalid() {
    assertNull(NextGenDynamicMediaReference.fromDamAssetReference(null, context.resourceResolver()));
    assertNull(NextGenDynamicMediaReference.fromDamAssetReference("/invalid/path", context.resourceResolver()));
    assertNull(NextGenDynamicMediaReference.fromDamAssetReference(context.create().resource("/content/no-asset").getPath(),
        context.resourceResolver()));
  }

  @Test
  void testFromDamAssetReference_AssetWithoutUUID_Approved() {
    Asset asset = context.create().asset("/content/dam/asset1.jpg", 10, 10, ContentType.JPEG,
        ASSET_STATUS_PROPERTY, ASSET_STATUS_APPROVED);
    assertNull(NextGenDynamicMediaReference.fromDamAssetReference(asset.getPath(), context.resourceResolver()));
  }

  @Test
  void testFromDamAssetReference_AssetWithUUID_NotApproved() {
    Asset asset = context.create().asset("/content/dam/" + SAMPLE_FILENAME, 10, 10, ContentType.JPEG);
    assertNull(NextGenDynamicMediaReference.fromDamAssetReference(asset.getPath(), context.resourceResolver()));
  }

  @Test
  void testFromDamAssetReference_AssetWithUUID_Approved() {
    Asset asset = context.create().asset("/content/dam/" + SAMPLE_FILENAME, 10, 10, ContentType.JPEG,
        ASSET_STATUS_PROPERTY, ASSET_STATUS_APPROVED);
    ModifiableValueMap props = AdaptTo.notNull(asset, ModifiableValueMap.class);
    props.put(JcrConstants.JCR_UUID, SAMPLE_UUID);

    NextGenDynamicMediaReference underTest = NextGenDynamicMediaReference.fromDamAssetReference(asset.getPath(),
        context.resourceResolver());
    assertNotNull(underTest);
    assertEquals(SAMPLE_ASSET_ID, underTest.getAssetId());
    assertEquals(SAMPLE_FILENAME, underTest.getFileName());
    assertEquals(SAMPLE_REFERENCE, underTest.toReference());
    assertEquals(asset, underTest.getAsset());
    assertEquals(SAMPLE_REFERENCE, underTest.toString());
  }

}
