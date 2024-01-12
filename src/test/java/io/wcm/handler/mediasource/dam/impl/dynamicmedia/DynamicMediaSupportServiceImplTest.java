/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.handler.mediasource.dam.impl.dynamicmedia;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.url.SiteConfig;
import io.wcm.handler.url.UrlMode;
import io.wcm.handler.url.UrlModes;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.testing.mock.wcmio.caconfig.MockCAConfig;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class DynamicMediaSupportServiceImplTest {

  private final AemContext context = AppAemContext.newAemContext();

  private Asset asset;

  @BeforeEach
  void setUp() {
    asset = context.create().asset("/content/dam/dummy.jpg", 10, 10, ContentType.JPEG,
        Scene7Constants.PN_S7_FILE, "DummyFolder/dummy");
  }

  @Test
  void testDefaultConfig() {
    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl());
    assertTrue(underTest.isDynamicMediaEnabled());
    assertTrue(underTest.isValidateSmartCropRenditionSizes());
    assertEquals(new Dimension(2000, 2000), underTest.getImageSizeLimit());
    assertEquals("https://dummy.scene7.com", underTest.getDynamicMediaServerUrl(asset, null, context.request()));
    assertTrue(underTest.isDynamicMediaCapabilityEnabled(true));
    assertFalse(underTest.isDynamicMediaCapabilityEnabled(false));
  }

  @Test
  void testConfigDisabled() {
    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl(),
        "enabled", false);
    assertFalse(underTest.isDynamicMediaEnabled());
  }

  @Test
  void testConfigDMCapability_OFF() {
    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl(),
        "dmCapabilityDetection", "OFF");
    assertFalse(underTest.isDynamicMediaCapabilityEnabled(true));
    assertFalse(underTest.isDynamicMediaCapabilityEnabled(false));
  }

  @Test
  void testConfigDMCapability_ON() {
    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl(),
        "dmCapabilityDetection", "ON");
    assertTrue(underTest.isDynamicMediaCapabilityEnabled(true));
    assertTrue(underTest.isDynamicMediaCapabilityEnabled(false));
  }

  @Test
  void testAuthorPreviewMode() {
    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl(),
        "authorPreviewMode", true,
        "validateSmartCropRenditionSizes", false,
        "imageSizeLimitWidth", 4000,
        "imageSizeLimitHeight", 3000);
    assertTrue(underTest.isDynamicMediaEnabled());
    assertFalse(underTest.isValidateSmartCropRenditionSizes());
    assertEquals(new Dimension(4000, 3000), underTest.getImageSizeLimit());
    assertEquals("https://author.dummysite.org", underTest.getDynamicMediaServerUrl(asset, null, context.request()));
  }

  @Test
  void testAuthorPreviewMode_SiteConfig() {
    MockCAConfig.contextPathStrategyAbsoluteParent(context, 1);
    MockContextAwareConfig.writeConfiguration(context, AppAemContext.ROOTPATH_CONTENT, SiteConfig.class,
        "siteUrlAuthor", "https://author-dm");

    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl(),
        "authorPreviewMode", true);
    assertTrue(underTest.isDynamicMediaEnabled());
    assertEquals(new Dimension(2000, 2000), underTest.getImageSizeLimit());
    assertEquals("https://author-dm", underTest.getDynamicMediaServerUrl(asset, null, context.request()));
  }

  /**
   * Ensure Site URL auto-detection with &lt;auto&gt; placeholder works.
   */
  @Test
  void testAuthorPreviewMode_SiteConfig_AutoDetection() {
    context.request().setServerName("servername-dm");
    context.request().setServerPort(8443);
    context.request().setScheme("https");

    MockCAConfig.contextPathStrategyAbsoluteParent(context, 1);
    MockContextAwareConfig.writeConfiguration(context, AppAemContext.ROOTPATH_CONTENT, SiteConfig.class,
        "siteUrlAuthor", "<auto>https://author-dm");

    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl(),
        "authorPreviewMode", true);
    assertTrue(underTest.isDynamicMediaEnabled());
    assertEquals(new Dimension(2000, 2000), underTest.getImageSizeLimit());
    assertEquals("https://servername-dm:8443", underTest.getDynamicMediaServerUrl(asset, null, context.request()));
  }

  /**
   * Ensure Site URL auto-detection with &lt;auto&gt; placeholder works with using the fallback URL
   * when media handler is used outside request context (adapted from resource).
   */
  @Test
  @SuppressWarnings("null")
  void testAuthorPreviewMode_SiteConfig_AutoDetection_Fallback() {
    context.request().setServerName("servername-dm");
    context.request().setServerPort(8443);
    context.request().setScheme("https");

    MockCAConfig.contextPathStrategyAbsoluteParent(context, 1);
    MockContextAwareConfig.writeConfiguration(context, AppAemContext.ROOTPATH_CONTENT, SiteConfig.class,
        "siteUrlAuthor", "<auto>https://author-dm-fallback");

    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl(),
        "authorPreviewMode", true);
    assertTrue(underTest.isDynamicMediaEnabled());
    assertEquals(new Dimension(2000, 2000), underTest.getImageSizeLimit());
    assertEquals("https://author-dm-fallback", underTest.getDynamicMediaServerUrl(asset, null, context.currentResource()));
  }

  /**
   * Test getting image profile for asset with profile association in one of the parent folders.
   */
  @Test
  void testGetImageProfileForAsset_MultipleFolders() {
    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl());
    Resource profile1 = context.create().resource("/conf/global/settings/dam/adminui-extension/imageprofile/profile1");

    Resource folder1 = context.create().resource("/content/dam/folder1");
    context.create().resource(folder1, JCR_CONTENT, DamConstants.IMAGE_PROFILE, profile1.getPath());

    Resource folder2 = context.create().resource(folder1, "folder2");
    context.create().resource(folder2, JCR_CONTENT);

    Resource folder3 = context.create().resource(folder2, "folder3");
    context.create().resource(folder3, JCR_CONTENT);

    Asset testAsset = context.create().asset(folder3.getPath() + "/test.jpg", 50, 30, ContentType.JPEG,
        Scene7Constants.PN_S7_FILE, "DummyFolder/folder1/folder2/folder3/test");

    ImageProfile imageProfile = underTest.getImageProfileForAsset(testAsset);

    assertNotNull(imageProfile);
  }

  /**
   * Test getting image profile for asset with profile association in one of the parent folders,
   * and at least one of the parent folders is "incomplete" (no jcr:content node), e.g. because the
   * folder itself was never published.
   */
  @Test
  void testGetImageProfileForAsset_MultipleFolders_IncompleteFolders() {
    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl());
    Resource profile1 = context.create().resource("/conf/global/settings/dam/adminui-extension/imageprofile/profile1");

    Resource folder1 = context.create().resource("/content/dam/folder1");
    context.create().resource(folder1, JCR_CONTENT, DamConstants.IMAGE_PROFILE, profile1.getPath());

    Resource folder2 = context.create().resource(folder1, "folder2");
    // deliberately create no jcr:content node for "folder2"

    Resource folder3 = context.create().resource(folder2, "folder3");
    // deliberately create no jcr:content node for "folder3"

    Asset testAsset = context.create().asset(folder3.getPath() + "/test.jpg", 50, 30, ContentType.JPEG,
        Scene7Constants.PN_S7_FILE, "DummyFolder/folder1/folder2/folder3/test");

    ImageProfile imageProfile = underTest.getImageProfileForAsset(testAsset);

    assertNotNull(imageProfile);
  }

  @ParameterizedTest
  @MethodSource("forcePublicshUrlModes")
  void testAuthorPreviewMode_SiteConfig_ForcePublish(UrlMode urlMode) {
    MockCAConfig.contextPathStrategyAbsoluteParent(context, 1);
    MockContextAwareConfig.writeConfiguration(context, "/content/dam", SiteConfig.class,
        "siteUrlAuthor", "https://author");

    DynamicMediaSupportService underTest = context.registerInjectActivateService(new DynamicMediaSupportServiceImpl(),
        "authorPreviewMode", true);
    assertTrue(underTest.isDynamicMediaEnabled());
    assertEquals(new Dimension(2000, 2000), underTest.getImageSizeLimit());
    assertEquals("https://dummy.scene7.com", underTest.getDynamicMediaServerUrl(asset, urlMode, context.request()));
  }

  private static Stream<Arguments> forcePublicshUrlModes() {
    return Stream.of(
        Arguments.of(UrlModes.FULL_URL_PUBLISH),
        Arguments.of(UrlModes.FULL_URL_PUBLISH_FORCENONSECURE),
        Arguments.of(UrlModes.FULL_URL_PUBLISH_FORCESECURE),
        Arguments.of(UrlModes.FULL_URL_PUBLISH_PROTOCOLRELATIVE));
  }

}
