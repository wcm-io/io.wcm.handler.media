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

import static io.wcm.handler.media.impl.MediaFileServletConstants.HEADER_CONTENT_DISPOSITION;
import static io.wcm.handler.media.impl.MediaFileServletConstants.HEADER_CONTENT_SECURITY_POLICY;
import static io.wcm.handler.media.impl.MediaFileServletConstants.SELECTOR_DOWNLOAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.InputStream;
import java.net.URI;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.external.ExternalizableInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class MediaFileServletTest {

  private static final long EXPECTED_CONTENT_LENGTH = 15471;
  private static final long EXPECTED_CONTENT_LENGTH_SVG = 718;

  private final AemContext context = AppAemContext.newAemContext();

  private MediaFileServlet underTest;

  @BeforeEach
  void setUp() {
    underTest = context.registerInjectActivateService(MediaFileServlet.class);
    context.currentResource(context.load().binaryFile("/sample_image_215x102.jpg", "/content/sample_image.jpg"));
  }

  @Test
  void testGet() throws Exception {
    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.JPEG, context.response().getContentType());
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getOutput().length);
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getContentLength());
    assertNull(context.response().getHeader(HEADER_CONTENT_DISPOSITION));
  }

  @Test
  void testGet_SVG() throws Exception {
    context.currentResource(context.load().binaryFile("/filetype/sample.svg", "/content/sample.svg"));

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.SVG, context.response().getContentType());
    assertEquals(EXPECTED_CONTENT_LENGTH_SVG, context.response().getOutput().length);
    assertEquals(EXPECTED_CONTENT_LENGTH_SVG, context.response().getContentLength());
    // forced content security policy for SVG to prevent stored XSS
    assertEquals("sandbox", context.response().getHeader(HEADER_CONTENT_SECURITY_POLICY));
  }

  @Test
  void testGet_SVG_DisableContentSecurityPolicy() throws Exception {
    // disable content security policy for SVG files
    underTest = context.registerInjectActivateService(MediaFileServlet.class,
        "svgContentSecurityPolicy", false);

    context.currentResource(context.load().binaryFile("/filetype/sample.svg", "/content/sample.svg"));

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.SVG, context.response().getContentType());
    assertEquals(EXPECTED_CONTENT_LENGTH_SVG, context.response().getOutput().length);
    assertEquals(EXPECTED_CONTENT_LENGTH_SVG, context.response().getContentLength());
    // no CSP
    assertNull(context.response().getHeader(HEADER_CONTENT_SECURITY_POLICY));
  }

  @Test
  void testGet_Download() throws Exception {
    context.requestPathInfo().setSelectorString(SELECTOR_DOWNLOAD);

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.DOWNLOAD, context.response().getContentType());
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getOutput().length);
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getContentLength());
    assertEquals("attachment;", context.response().getHeader(HEADER_CONTENT_DISPOSITION));
  }

  @Test
  void testGet_Download_Suffix() throws Exception {
    context.requestPathInfo().setSelectorString(SELECTOR_DOWNLOAD);
    context.requestPathInfo().setSuffix("sample_image.jpg");

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals(ContentType.DOWNLOAD, context.response().getContentType());
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getOutput().length);
    assertEquals(EXPECTED_CONTENT_LENGTH, context.response().getContentLength());
    assertEquals("attachment;filename=\"sample_image.jpg\"", context.response().getHeader(HEADER_CONTENT_DISPOSITION));
  }

  @Test
  @SuppressWarnings("null")
  void testGet_ExternalizableInputStream() throws Exception {
    // prepare ExternalizableInputStream
    final String EXTERNAL_IMAGE_URI = "https://example.com/sample_image.jpg";
    InputStream is = mock(InputStream.class, withSettings().extraInterfaces(ExternalizableInputStream.class));
    ExternalizableInputStream externalizableInputStream = (ExternalizableInputStream)is;
    when(externalizableInputStream.getURI()).thenReturn(new URI(EXTERNAL_IMAGE_URI));

    Resource resource = spy(context.currentResource());
    when(resource.adaptTo(InputStream.class)).thenReturn(is);
    context.currentResource(resource);

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, context.response().getStatus());
    assertEquals(EXTERNAL_IMAGE_URI, context.response().getHeader("Location"));
  }

  @Test
  void testGet_NoResource() throws Exception {
    context.currentResource((Resource)null);

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_NOT_FOUND, context.response().getStatus());
  }

  @Test
  void testGet_NoBinaryDataResource() throws Exception {
    context.currentResource(context.create().resource("/content/nobinarydata"));

    underTest.service(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_NOT_FOUND, context.response().getStatus());
  }

}
