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

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.jdom2.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Image;
import io.wcm.handler.commons.dom.Picture;
import io.wcm.handler.commons.dom.Source;
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaArgs.ImageSizes;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaArgs.WidthOption;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.Ratio;

/**
 * Basic implementation of {@link io.wcm.handler.media.spi.MediaMarkupBuilder} for images.
 * <p>
 * If image sizes or picture sources are set on the media handler this markup builder also
 * generates markup for responsive images using <code>img</code> with <code>sizes</code> and <code>srcset</code>
 * attributes or <code>picture</code> with <code>source</code> elements.
 * </p>
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ConsumerType
public class SimpleImageMediaMarkupBuilder extends AbstractImageMediaMarkupBuilder {

  @Override
  public final boolean accepts(@NotNull Media media) {
    // accept if rendition is an image rendition, and resolving was successful
    return media.isValid()
        && media.getRendition() != null
        && media.getRendition().isBrowserImage();
  }

  @Override
  public final HtmlElement<?> build(@NotNull Media media) {

    // render media element for rendition
    HtmlElement<?> mediaElement = getMediaElement(media);

    // further processing in edit or preview mode
    applyWcmMarkup(mediaElement, media);

    return mediaElement;
  }

  /**
   * Create <code>img</code> or <code>picture</code> media element.
   * @param media Media metadata
   * @return Media element with properties or null if media metadata is invalid
   */
  protected @Nullable HtmlElement<?> getMediaElement(@NotNull Media media) {
    PictureSource[] pictureSources = media.getMediaRequest().getMediaArgs().getPictureSources();
    if (pictureSources != null && pictureSources.length > 0) {
      return getPictureElement(media);
    }
    else {
      return getImageElement(media);
    }
  }

  /**
   * Create an <code>img</code> element that displays the given rendition image.
   * @param media Media metadata
   * @return <code>img</code> element with properties or null if media metadata is invalid
   */
  protected @Nullable HtmlElement<?> getPictureElement(@NotNull Media media) {
    PictureSource[] pictureSources = media.getMediaRequest().getMediaArgs().getPictureSources();

    Picture picture = new Picture();

    // add source elements (only if matching renditions found)
    boolean foundAnySource = false;
    for (PictureSource pictureSource : pictureSources) {
      Source source = new Source();
      if (pictureSource.getMedia() != null) {
        source.setMedia(pictureSource.getMedia());
      }
      String srcSet = getSrcSetRenditions(media, pictureSource.getMediaFormat(), pictureSource.getWidthOptions());
      if (srcSet != null) {
        source.setSrcSet(srcSet);
        picture.add(source);
        foundAnySource = true;
      }
    }

    // add image element
    HtmlElement<?> image = getImageElement(media);
    if (image == null) {
      return null;
    }

    if (foundAnySource) {
      picture.add(image);
      return picture;
    }
    else {
      return image;
    }
  }

  /**
   * Create an <code>img</code> element that displays the given rendition image.
   * @param media Media metadata
   * @return <code>img</code> element with properties or null if media metadata is invalid
   */
  protected @Nullable HtmlElement<?> getImageElement(@NotNull Media media) {
    Image img = null;

    MediaArgs mediaArgs = media.getMediaRequest().getMediaArgs();
    Asset asset = media.getAsset();
    Rendition rendition = media.getRendition();

    String url = null;
    if (rendition != null) {
      url = rendition.getUrl();
    }

    if (url != null) {
      img = new Image(url);

      // Alternative text
      String altText = null;
      if (asset != null) {
        altText = asset.getAltText();
      }
      if (StringUtils.isNotEmpty(altText)) {
        img.setAlt(altText);
      }

      // set width/height
      if (rendition != null && mediaArgs.getImageSizes() == null && mediaArgs.getPictureSources() == null) {
        long height = rendition.getHeight();
        if (height > 0) {
          img.setHeight(height);
        }
        long width = rendition.getWidth();
        if (width > 0) {
          img.setWidth(width);
        }
      }

      // set image sizes/srcset
      ImageSizes imageSizes = mediaArgs.getImageSizes();
      if (imageSizes != null) {
        MediaFormat primaryMediaFormat = getFirstMediaFormat(media);
        if (primaryMediaFormat != null) {
          String srcSet = getSrcSetRenditions(media, primaryMediaFormat, imageSizes.getWidthOptions());
          if (srcSet != null) {
            img.setSrcSet(srcSet);
            img.setSizes(imageSizes.getSizes());
          }
        }
      }

    }

    // set additional attributes
    setAdditionalAttributes(img, media);

    return img;
  }

  /**
   * Generate srcset list from the resolved renditions for the ratio of the given media formats and the given widths.
   * Widths that have no match are ignored.
   * @param media Media
   * @param mediaFormat Media format
   * @param widths widths
   * @return srcset String or null if no matching renditions found
   */
  protected @Nullable String getSrcSetRenditions(@NotNull Media media, @NotNull MediaFormat mediaFormat,
      @NotNull WidthOption @NotNull... widths) {
    return getSrcSetRenditions(media, mediaFormat, Arrays.stream(widths)
        .mapToLong(WidthOption::getWidth)
        .toArray());
  }

  /**
   * Generate srcset list from the resolved renditions for the ratio of the given media formats and the given widths.
   * Widths that have no match are ignored.
   * @param media Media
   * @param mediaFormat Media format
   * @param widths widths
   * @return srcset String or null if no matching renditions found
   */
  protected @Nullable String getSrcSetRenditions(@NotNull Media media, @NotNull MediaFormat mediaFormat,
      long @NotNull... widths) {
    StringBuilder srcset = new StringBuilder();

    for (long width : widths) {
      Optional<String> url = media.getRenditions().stream()
          .filter(rendition -> (Ratio.matches(rendition.getRatio(), mediaFormat.getRatio())
              || Ratio.matches(mediaFormat.getRatio(), 0d))
              && rendition.getWidth() == width)
          .map(rendition -> rendition.getUrl())
          .findFirst();
      if (url.isPresent()) {
        if (srcset.length() > 0) {
          srcset.append(", ");
        }
        srcset.append(url.get()).append(" ").append(Long.toString(width)).append("w");
      }
    }

    if (srcset.length() > 0) {
      return srcset.toString();
    }
    else {
      return null;
    }
  }

  /**
   * Get first media format from the media formats of the media args that has a ratio set.
   * @param media Media
   * @return Media format or null if none found
   */
  protected final @Nullable MediaFormat getFirstMediaFormatWithRatio(@NotNull Media media) {
    MediaFormat[] mediaFormats = media.getMediaRequest().getMediaArgs().getMediaFormats();
    if (mediaFormats != null) {
      for (MediaFormat mediaFormat : mediaFormats) {
        if (mediaFormat.hasRatio()) {
          return mediaFormat;
        }
      }
    }
    return null;
  }

  /**
   * Get first media format from the media formats of the media args that has a ratio set.
   * @param media Media
   * @return Media format or null if none found
   */
  protected final @Nullable MediaFormat getFirstMediaFormat(@NotNull Media media) {
    MediaFormat[] mediaFormats = media.getMediaRequest().getMediaArgs().getMediaFormats();
    if (mediaFormats != null) {
      for (MediaFormat mediaFormat : mediaFormats) {
        return mediaFormat;
      }
    }
    return null;
  }

  @Override
  public final boolean isValidMedia(@NotNull HtmlElement<?> element) {
    if (element instanceof Image) {
      Image img = (Image)element;
      return StringUtils.isNotEmpty(img.getSrc())
          && !StringUtils.contains(img.getCssClass(), MediaNameConstants.CSS_DUMMYIMAGE);
    }
    if (element instanceof Picture) {
      Element imgChild = element.getChild("img");
      if (imgChild instanceof Image) {
        Image img = (Image)imgChild;
        return StringUtils.isNotEmpty(img.getSrc())
            && !StringUtils.contains(element.getCssClass(), MediaNameConstants.CSS_DUMMYIMAGE);
      }
    }
    return false;
  }

}
