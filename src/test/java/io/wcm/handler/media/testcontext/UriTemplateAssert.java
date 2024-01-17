/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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
package io.wcm.handler.media.testcontext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;

/**
 * Helper methods for asserting URI templates.
 */
public final class UriTemplateAssert {

  private UriTemplateAssert() {
    // static methods only
  }

  public static void assertUriTemplate(Media media, UriTemplateType type,
      long expectedMaxWith, long expectedMaxHeight, String expectedTemplate) {
    assertTrue(media.isValid(), "media valid");
    Asset asset = media.getAsset();
    assertNotNull(asset);
    UriTemplate uriTemplate = asset.getUriTemplate(type);
    assertEquals(type, uriTemplate.getType(), "uriTemplateType");
    assertEquals(expectedMaxWith, uriTemplate.getMaxWidth(), "maxWidth(" + type + ")");
    assertEquals(expectedMaxHeight, uriTemplate.getMaxHeight(), "maxHeight(" + type + ")");
    assertEquals(expectedTemplate, uriTemplate.getUriTemplate(), "uriTemplate(" + type + ")");
    assertEquals(expectedTemplate, uriTemplate.toString(), "toString(" + type + ")");
  }

  public static void assertUriTemplate(Rendition rendition, UriTemplateType type,
      long expectedMaxWith, long expectedMaxHeight, String expectedTemplate) {
    assertNotNull(rendition, "rendition valid");
    UriTemplate uriTemplate = rendition.getUriTemplate(type);
    assertEquals(type, uriTemplate.getType(), "uriTemplateType");
    assertEquals(expectedMaxWith, uriTemplate.getMaxWidth(), "maxWidth(" + type + ")");
    assertEquals(expectedMaxHeight, uriTemplate.getMaxHeight(), "maxHeight(" + type + ")");
    assertEquals(expectedTemplate, uriTemplate.getUriTemplate(), "uriTemplate(" + type + ")");
    assertEquals(expectedTemplate, uriTemplate.toString(), "toString(" + type + ")");
  }

}
