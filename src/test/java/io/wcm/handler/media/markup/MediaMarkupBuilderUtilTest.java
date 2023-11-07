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
package io.wcm.handler.media.markup;

import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.day.cq.commons.DiffService;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.EditConfig;
import com.day.cq.wcm.api.components.EditContext;
import com.day.cq.wcm.api.components.InplaceEditingConfig;

import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.handler.media.testcontext.DummyMediaHandlerConfig;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaMarkupBuilderUtilTest {

  private static final String VERSION_LABEL = "v1";

  @Mock
  private MediaSource mediaSource;
  @Mock
  private Resource resource;
  @Mock
  private ResourceResolver resolver;
  @Mock
  private PageManager pageManager;
  @Mock
  private Page page;
  @Mock
  private SlingHttpServletRequest request;
  @Mock
  private ComponentContext componentContext;
  @Mock
  private Resource componentResource;
  @Mock
  private EditContext editContext;
  @Mock
  private EditConfig editConfig;
  @Mock
  private InplaceEditingConfig inplaceEditingConfig;

  @BeforeEach
  @SuppressWarnings("null")
  void setUp() throws Exception {
    when(request.getResourceResolver()).thenReturn(resolver);
    when(resolver.adaptTo(PageManager.class)).thenReturn(pageManager);
    when(request.getResource()).thenReturn(resource);
    when(pageManager.getContainingPage(resource)).thenReturn(page);
    when(request.getParameter(DiffService.REQUEST_PARAM_DIFF_TO)).thenReturn(VERSION_LABEL);
    when(componentContext.getResource()).thenReturn(componentResource);
    when(componentResource.getResourceResolver()).thenReturn(resolver);
    when(componentContext.getEditContext()).thenReturn(editContext);
    when(editContext.getEditConfig()).thenReturn(editConfig);
    when(editConfig.getInplaceEditingConfig()).thenReturn(inplaceEditingConfig);
    when(inplaceEditingConfig.getEditorType()).thenReturn("image");
  }

  @Test
  void testAddDiffDecoration() {
    Image img = new Image("/dummy/image.gif");
    MediaMarkupBuilderUtil.addDiffDecoration(img, resource, MediaNameConstants.PN_MEDIA_REF, request,
        new DummyMediaHandlerConfig());
    assertEquals(MediaNameConstants.CSS_DIFF_ADDED, img.getCssClass());
  }

  @Test
  void testGetMediaformatDimension_fixedDimension() {
    MediaRequest mediaRequest = new MediaRequest("/dummy/image", new MediaArgs().fixedDimension(100, 50));
    Media media = new Media(mediaSource, mediaRequest);

    Dimension dimension = MediaMarkupBuilderUtil.getMediaformatDimension(media);
    assertEquals(100, dimension.getWidth());
    assertEquals(50, dimension.getHeight());
  }

  @Test
  void testGetMediaformatDimension_mediaFormat() {
    MediaRequest mediaRequest = new MediaRequest("/dummy/image", new MediaArgs(EDITORIAL_1COL));
    Media media = new Media(mediaSource, mediaRequest);

    Dimension dimension = MediaMarkupBuilderUtil.getMediaformatDimension(media);
    assertEquals(EDITORIAL_1COL.getWidth(), dimension.getWidth());
    assertEquals(EDITORIAL_1COL.getHeight(), dimension.getHeight());
  }

  @Test
  void testGetMediaformatDimension_noMatch() {
    MediaRequest mediaRequest = new MediaRequest("/dummy/image", new MediaArgs());
    Media media = new Media(mediaSource, mediaRequest);

    Dimension dimension = MediaMarkupBuilderUtil.getMediaformatDimension(media);
    assertEquals(MediaMarkupBuilder.DUMMY_MIN_DIMENSION, dimension.getWidth());
    assertEquals(MediaMarkupBuilder.DUMMY_MIN_DIMENSION, dimension.getHeight());
  }

  @Test
  void testCanApplyDragDropSupport_DragDropSupport_Never() {
    MediaRequest mediaRequest = new MediaRequest(resource, new MediaArgs().dragDropSupport(DragDropSupport.NEVER));
    assertFalse(MediaMarkupBuilderUtil.canApplyDragDropSupport(mediaRequest, componentContext));
  }

  @Test
  void testCanApplyDragDropSupport_DragDropSupport_Always() {
    MediaRequest mediaRequest = new MediaRequest(resource, new MediaArgs().dragDropSupport(DragDropSupport.ALWAYS));
    assertTrue(MediaMarkupBuilderUtil.canApplyDragDropSupport(mediaRequest, componentContext));
  }

  @Test
  void testCanApplyDragDropSupport_DragDropSupport_Auto() {
    // not allowed if not resource is specified
    MediaRequest mediaRequest = new MediaRequest("/content/dam/path", new MediaArgs().dragDropSupport(DragDropSupport.AUTO));
    assertFalse(MediaMarkupBuilderUtil.canApplyDragDropSupport(mediaRequest, componentContext));

    // not allowed if resource paths do not match
    mediaRequest = new MediaRequest(resource, new MediaArgs().dragDropSupport(DragDropSupport.AUTO));
    assertFalse(MediaMarkupBuilderUtil.canApplyDragDropSupport(mediaRequest, componentContext));

    // allowed if resource paths do match
    when(resource.getPath()).thenReturn("/content/resource/path");
    when(componentContext.getResource()).thenReturn(resource);
    mediaRequest = new MediaRequest(resource, new MediaArgs().dragDropSupport(DragDropSupport.AUTO));
    assertTrue(MediaMarkupBuilderUtil.canApplyDragDropSupport(mediaRequest, componentContext));
  }

  @Test
  void testCanSetCustomIPECropRatios_ALWAYS() {
    MediaRequest mediaRequest = new MediaRequest("/content/dam/path", new MediaArgs().ipeRatioCustomize(IPERatioCustomize.ALWAYS));
    assertTrue(MediaMarkupBuilderUtil.canSetCustomIPECropRatios(mediaRequest, componentContext));
  }

  @Test
  void testCanSetCustomIPECropRatios_ALWAYS_CustomEditorType_Mismatch() {
    when(inplaceEditingConfig.getEditorType()).thenReturn("custom");
    MediaRequest mediaRequest = new MediaRequest("/content/dam/path", new MediaArgs().ipeRatioCustomize(IPERatioCustomize.ALWAYS));
    assertFalse(MediaMarkupBuilderUtil.canSetCustomIPECropRatios(mediaRequest, componentContext));
  }

  @Test
  void testCanSetCustomIPECropRatios_ALWAYS_CustomEditorType_Match() {
    when(inplaceEditingConfig.getEditorType()).thenReturn("custom");
    MediaRequest mediaRequest = new MediaRequest("/content/dam/path", new MediaArgs().ipeRatioCustomize(IPERatioCustomize.ALWAYS));
    assertTrue(MediaMarkupBuilderUtil.canSetCustomIPECropRatios(mediaRequest, componentContext, Set.of("image", "custom")));
  }

  @Test
  void testCanSetCustomIPECropRatios_NEVER() {
    MediaRequest mediaRequest = new MediaRequest("/content/dam/path", new MediaArgs().ipeRatioCustomize(IPERatioCustomize.NEVER));
    assertFalse(MediaMarkupBuilderUtil.canSetCustomIPECropRatios(mediaRequest, componentContext));
  }

  @Test
  void testCanSetCustomIPECropRatios_AUTO() {
    MediaRequest mediaRequest = new MediaRequest("/content/dam/path", new MediaArgs().ipeRatioCustomize(IPERatioCustomize.AUTO));
    assertTrue(MediaMarkupBuilderUtil.canSetCustomIPECropRatios(mediaRequest, componentContext));
  }

  @Test
  void testCanSetCustomIPECropRatios_AUTO_IPEConfigPath() {
    MediaRequest mediaRequest = new MediaRequest("/content/dam/path", new MediaArgs().ipeRatioCustomize(IPERatioCustomize.AUTO));
    when(inplaceEditingConfig.getConfigPath()).thenReturn("/apps/components/comp1");
    assertTrue(MediaMarkupBuilderUtil.canSetCustomIPECropRatios(mediaRequest, componentContext));
  }

  @Test
  void testCanSetCustomIPECropRatios_AUTO_ExistingRatios() {
    MediaRequest mediaRequest = new MediaRequest("/content/dam/path", new MediaArgs().ipeRatioCustomize(IPERatioCustomize.AUTO));
    when(inplaceEditingConfig.getConfigPath()).thenReturn("/apps/components/comp1");
    when(resolver.getResource("/apps/components/comp1/plugins/crop/aspectRatios")).thenReturn(mock(Resource.class));
    assertFalse(MediaMarkupBuilderUtil.canSetCustomIPECropRatios(mediaRequest, componentContext));
  }

}
