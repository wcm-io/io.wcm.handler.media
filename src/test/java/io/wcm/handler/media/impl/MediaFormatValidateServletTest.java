/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import static io.wcm.handler.media.impl.MediaFormatValidateServlet.MEDIA_INVALID_REASON_I18N_PREFIX;
import static io.wcm.handler.media.impl.MediaFormatValidateServlet.RP_MEDIA_FORMATS;
import static io.wcm.handler.media.impl.MediaFormatValidateServlet.RP_MEDIA_FORMATS_MANDATORY;
import static io.wcm.handler.media.impl.MediaFormatValidateServlet.RP_MEDIA_REF;
import static io.wcm.handler.media.testcontext.AppAemContext.ROOTPATH_CONTENT;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.HOME_STAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Constants;
import org.skyscreamer.jsonassert.JSONAssert;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.spi.MediaProcessor;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.media.testcontext.DummyMediaHandlerConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class MediaFormatValidateServletTest {

  private final AemContext context = AppAemContext.newAemContext();

  private MediaFormatValidateServlet underTest;

  @BeforeEach
  void setUp() {
    underTest = context.registerInjectActivateService(new MediaFormatValidateServlet());
    context.currentPage(context.create().page(ROOTPATH_CONTENT));
  }

  @Test
  @SuppressWarnings("null")
  void testValid() throws Exception {
    Asset asset = context.create().asset("/content/dam/sample.jpg",
        EDITORIAL_1COL.getWidth(),
        EDITORIAL_1COL.getHeight(),
        ContentType.JPEG);

    context.request().setParameterMap(Map.of(
        RP_MEDIA_FORMATS, EDITORIAL_1COL.getName(),
        RP_MEDIA_REF, asset.getPath()));
    underTest.service(context.request(), context.response());

    assertResponse("{'valid':true}");
  }

  @Test
  void testValid_MultipleFormats() throws Exception {
    Asset asset = context.create().asset("/content/dam/sample.jpg",
        EDITORIAL_1COL.getWidth(),
        EDITORIAL_1COL.getHeight(),
        ContentType.JPEG);

    context.request().setParameterMap(Map.of(
        RP_MEDIA_FORMATS, HOME_STAGE.getName() + "," + EDITORIAL_1COL.getName(),
        RP_MEDIA_FORMATS_MANDATORY, HOME_STAGE.getName() + "," + EDITORIAL_1COL.getName(),
        RP_MEDIA_REF, asset.getPath()));
    underTest.service(context.request(), context.response());

    assertResponse("{'valid':false,"
        + "'reason':'" + MEDIA_INVALID_REASON_I18N_PREFIX + MediaInvalidReason.NOT_ENOUGH_MATCHING_RENDITIONS.name() + "',"
        + "'reasonTitle':'io.wcm.handler.media.assetInvalid'}");
  }

  @Test
  @SuppressWarnings("null")
  void testValid_MultipleFormats_Optional() throws Exception {
    Asset asset = context.create().asset("/content/dam/sample.jpg",
        EDITORIAL_1COL.getWidth(),
        EDITORIAL_1COL.getHeight(),
        ContentType.JPEG);

    context.request().setParameterMap(Map.of(
        RP_MEDIA_FORMATS, HOME_STAGE.getName() + "," + EDITORIAL_1COL.getName(),
        RP_MEDIA_FORMATS_MANDATORY, EDITORIAL_1COL.getName(),
        RP_MEDIA_REF, asset.getPath()));
    underTest.service(context.request(), context.response());

    assertResponse("{'valid':true}");
  }

  @Test
  @SuppressWarnings("null")
  void testInvalid() throws Exception {
    Asset asset = context.create().asset("/content/dam/sample.jpg",
        10,
        10,
        ContentType.JPEG);

    context.request().setParameterMap(Map.of(
        RP_MEDIA_FORMATS, EDITORIAL_1COL.getName(),
        RP_MEDIA_REF, asset.getPath()));
    underTest.service(context.request(), context.response());

    assertResponse("{'valid':false,"
        + "'reason':'" + MEDIA_INVALID_REASON_I18N_PREFIX + MediaInvalidReason.NO_MATCHING_RENDITION.name() + "',"
        + "'reasonTitle':'io.wcm.handler.media.assetInvalid'}");
  }

  @Test
  @SuppressWarnings("null")
  void testInvalid_CustomMessageKey() throws Exception {
    context.registerService(MediaHandlerConfig.class, new DummyMediaHandlerConfig() {
      @Override
      public @NotNull List<Class<? extends MediaProcessor>> getPreProcessors() {
        return List.of(AllInvalidMediaPreProcessor.class);
      }
    }, Constants.SERVICE_RANKING, 1000);

    Asset asset = context.create().asset("/content/dam/sample.jpg",
        EDITORIAL_1COL.getWidth(),
        EDITORIAL_1COL.getHeight(),
        ContentType.JPEG);

    context.request().setParameterMap(Map.of(
        RP_MEDIA_FORMATS, EDITORIAL_1COL.getName(),
        RP_MEDIA_REF, asset.getPath()));
    underTest.service(context.request(), context.response());

    assertResponse("{'valid':false,"
        + "'reason':'custom-invalid',"
        + "'reasonTitle':'io.wcm.handler.media.assetInvalid'}");
  }

  private void assertResponse(String expectedJson) throws JSONException {
    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Model(adaptables = { SlingHttpServletRequest.class, Resource.class })
  public static class AllInvalidMediaPreProcessor implements MediaProcessor {
    @Override
    public @NotNull Media process(@NotNull Media media) {
      // mark all assets as invalid with custom message
      media.setMediaInvalidReason(MediaInvalidReason.CUSTOM);
      media.setMediaInvalidReasonCustomMessage("custom-invalid");
      return media;
    }
  }

}
