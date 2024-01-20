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
package io.wcm.handler.media.imagemap.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.imagemap.ImageMapArea;
import io.wcm.handler.media.imagemap.ImageMapParser;
import io.wcm.handler.media.spi.ImageMapLinkResolver;

/**
 * Creates {@link ImageMapArea} from strings.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
}, adapters = ImageMapParser.class)
public class ImageMapParserImpl implements ImageMapParser {

  @SlingObject
  private Resource resource;

  @OSGiService(injectionStrategy = InjectionStrategy.OPTIONAL)
  @SuppressWarnings("java:S3740") // don't use generic here
  private ImageMapLinkResolver linkResolver;

  @Override
  @SuppressWarnings({
      "unchecked",
      "java:S3776", "java:S135" // ignore complexity
  })
  public @Nullable List<ImageMapArea> parseMap(@Nullable String mapString) {
    if (StringUtils.isBlank(mapString)) {
      return null;
    }

    List<ImageMapArea> areas = new ArrayList<>();
    // Parse the image map areas as defined at Image.PN_MAP
    String[] areaStrings = StringUtils.split(mapString, "][");
    for (String areaString : areaStrings) {
      int coordinatesEndIndex = areaString.indexOf(')');
      if (coordinatesEndIndex < 0) {
        continue;
      }
      String shapeAndCoords = StringUtils.substring(areaString, 0, coordinatesEndIndex + 1);
      String shape = StringUtils.substringBefore(shapeAndCoords, "(");
      String coordinates = StringUtils.substringBetween(shapeAndCoords, "(", ")");
      String remaining = StringUtils.substring(areaString, coordinatesEndIndex + 1);
      String[] remainingTokens = StringUtils.split(remaining, "|");
      if (StringUtils.isBlank(shape) || StringUtils.isBlank(coordinates)) {
        continue;
      }
      if (remainingTokens.length > 0) {
        String linkUrl = StringUtils.remove(remainingTokens[0], "\"");
        String linkWindowTarget = remainingTokens.length > 1 ? StringUtils.remove(remainingTokens[1], "\"") : "";
        String altText = remainingTokens.length > 2 ? StringUtils.remove(remainingTokens[2], "\"") : "";
        String relativeCoordinates = remainingTokens.length > 3 ? remainingTokens[3] : "";
        relativeCoordinates = StringUtils.substringBetween(relativeCoordinates, "(", ")");

        // resolve and validate via link handler
        Object link = null;
        if (linkResolver != null) {
          link = linkResolver.resolveLink(linkUrl, linkWindowTarget, resource);
          if (link != null) {
            linkUrl = linkResolver.getLinkUrl(link);
          }
        }

        if (linkUrl == null || StringUtils.isBlank(linkUrl)) {
          continue;
        }

        ImageMapArea area = new ImageMapAreaImpl(shape, coordinates,
            StringUtils.trimToNull(relativeCoordinates),
            link, linkUrl,
            StringUtils.trimToNull(linkWindowTarget), StringUtils.trimToNull(altText));

        areas.add(area);
      }
    }

    if (areas.isEmpty()) {
      return null;
    }
    else {
      return areas;
    }
  }

}
