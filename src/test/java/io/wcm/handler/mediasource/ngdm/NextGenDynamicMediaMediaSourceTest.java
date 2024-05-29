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
package io.wcm.handler.mediasource.ngdm;

import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_REFERENCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.EditConfig;
import com.day.cq.wcm.api.components.EditContext;

import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigServiceImpl;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class NextGenDynamicMediaMediaSourceTest {

  private final AemContext context = AppAemContext.newAemContext();

  @Test
  void testGetId() {
    NextGenDynamicMediaMediaSource underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaMediaSource.class);
    assertEquals(NextGenDynamicMediaMediaSource.ID, underTest.getId());
  }

  @Test
  void testAccepts_withoutNextGenDynamicMediaConfig() {
    NextGenDynamicMediaMediaSource underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaMediaSource.class);

    assertFalse(underTest.accepts(SAMPLE_REFERENCE));
    assertFalse(underTest.accepts("/content/dam/sample.jpg"));
    assertFalse(underTest.accepts("invalid"));
    assertFalse(underTest.accepts(""));
    assertFalse(underTest.accepts((String)null));
  }

  @Test
  void testAccepts_withNextGenDynamicMediaConfigDisabled() {
    registerMockNextGenDynamicMediaConfig(false, true);
    NextGenDynamicMediaMediaSource underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaMediaSource.class);

    assertFalse(underTest.accepts(SAMPLE_REFERENCE));
    assertFalse(underTest.accepts("/content/dam/sample.jpg"));
    assertFalse(underTest.accepts("invalid"));
    assertFalse(underTest.accepts(""));
    assertFalse(underTest.accepts((String)null));
  }

  @Test
  void testAccepts_withNextGenDynamicMediaConfigEnabled() {
    registerMockNextGenDynamicMediaConfig(true, true);
    NextGenDynamicMediaMediaSource underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaMediaSource.class);

    assertTrue(underTest.accepts(SAMPLE_REFERENCE));
    assertTrue(underTest.accepts("/content/dam/sample.jpg"));
    assertFalse(underTest.accepts("invalid"));
    assertFalse(underTest.accepts(""));
    assertFalse(underTest.accepts((String)null));
  }

  @Test
  void testAccepts_withNextGenDynamicMediaConfigEnabled_NoLocalAssets() {
    registerMockNextGenDynamicMediaConfig(true, false);
    NextGenDynamicMediaMediaSource underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaMediaSource.class);

    assertTrue(underTest.accepts(SAMPLE_REFERENCE));
    assertFalse(underTest.accepts("/content/dam/sample.jpg"));
    assertFalse(underTest.accepts("invalid"));
    assertFalse(underTest.accepts(""));
    assertFalse(underTest.accepts((String)null));
  }

  @Test
  @SuppressWarnings("null")
  void testEnableMediaDrop() {
    NextGenDynamicMediaMediaSource underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaMediaSource.class);
    MediaRequest mediaRequest = new MediaRequest(context.currentResource(), new MediaArgs());

    Image img = new Image();
    underTest.enableMediaDrop(img, mediaRequest);
    assertNull(img.getCssClass());
  }

  @Test
  @SuppressWarnings("null")
  void testEnableMediaDrop_Authoring() {
    // simulate component context
    ComponentContext wcmComponentContext = mock(ComponentContext.class);
    context.request().setAttribute(ComponentContext.CONTEXT_ATTR_NAME, wcmComponentContext);
    when(wcmComponentContext.getResource()).thenReturn(context.currentResource());
    when(wcmComponentContext.getEditContext()).thenReturn(mock(EditContext.class));
    when(wcmComponentContext.getEditContext().getEditConfig()).thenReturn(mock(EditConfig.class));
    WCMMode.EDIT.toRequest(context.request());

    NextGenDynamicMediaMediaSource underTest = AdaptTo.notNull(context.request(), NextGenDynamicMediaMediaSource.class);
    MediaRequest mediaRequest = new MediaRequest(context.currentResource(), new MediaArgs());

    Image img = new Image();
    underTest.enableMediaDrop(img, mediaRequest);
    assertEquals("cq-dd-image", img.getCssClass());
  }

  void registerMockNextGenDynamicMediaConfig(boolean enabled, boolean localAssets) {
    MockNextGenDynamicMediaConfig nextGenDynamicMediaConfig = context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class);
    nextGenDynamicMediaConfig.setEnabled(enabled);
    nextGenDynamicMediaConfig.setRepositoryId("repo1");
    context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class,
        "localAssets", localAssets);
  }
}
