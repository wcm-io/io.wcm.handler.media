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
package io.wcm.handler.mediasource.dam.markup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.PrefixRenditionPicker;
import com.day.cq.dam.video.VideoConstants;
import com.day.cq.dam.video.VideoProfile;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Video;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.markup.MediaMarkupBuilderUtil;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.handler.url.UrlHandler;

/**
 * Default implementation of {@link MediaMarkupBuilder} for DAM video assets.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ConsumerType
public class DamVideoMediaMarkupBuilder implements MediaMarkupBuilder {

  private static final String H264_PROFILE = "format_aac";
  private static final String OGG_PROFILE = "format_ogg";
  private static final String LEGACY_H264_PROFILE = "hq"; // for AEM 6.3
  private static final String LEGACY_OGG_PROFILE = "firefoxhq"; // for AEM 6.3
  private static final List<String> VIDEO_PROFILE_NAMES = List.of(H264_PROFILE, OGG_PROFILE,
      LEGACY_H264_PROFILE, LEGACY_OGG_PROFILE);

  private static final Logger log = LoggerFactory.getLogger(DamVideoMediaMarkupBuilder.class);

  @SlingObject
  private ResourceResolver resourceResolver;
  @Self
  private UrlHandler urlHandler;
  @OSGiService
  private ConfigurationResourceResolver configurationResourceResolver;

  @Override
  public final boolean accepts(@NotNull Media media) {
    if (!media.isValid()) {
      return false;
    }
    Asset asset = getDamAsset(media);
    if (asset != null) {
      return asset.getRendition(new PrefixRenditionPicker(VideoConstants.RENDITION_PREFIX)) != null;
    }
    else {
      return false;
    }
  }

  /**
   * Return video profile names stored below /etc/dam/video supported by this markup builder.
   * @return Video profile names
   */
  protected List<String> getVideoProfileNames() {
    return VIDEO_PROFILE_NAMES;
  }

  /**
   * Return video profiles supported by this markup builder.
   * @return Video profiles
   */
  protected List<VideoProfile> getVideoProfiles() {
    List<VideoProfile> profiles = new ArrayList<>();
    for (String profileName : getVideoProfileNames()) {
      VideoProfile profile = getVideoProfile(profileName);
      if (profile != null) {
        profiles.add(profile);
      }
      else {
        log.debug("DAM video profile with name '{}' does not exist.", profileName);
      }
    }
    return profiles;
  }

  private VideoProfile getVideoProfile(String profileName) {
    return VideoProfile.get(resourceResolver, configurationResourceResolver, profileName);
  }

  /**
   * @param media Media metadata
   * @return DAM asset or null
   */
  protected @Nullable Asset getDamAsset(Media media) {
    io.wcm.handler.media.Asset asset = media.getAsset();
    if (asset != null) {
      return asset.adaptTo(Asset.class);
    }
    return null;
  }

  @Override
  public final HtmlElement build(@NotNull Media media) {
    return getVideoPlayerElement(media);
  }

  /**
   * Build HTML5 video player element
   * @param media Media metadata
   * @return Media element
   */
  protected Video getVideoPlayerElement(@NotNull Media media) {
    Dimension dimension = MediaMarkupBuilderUtil.getMediaformatDimension(media);

    Video video = new Video();
    video.setWidth(dimension.getWidth());
    video.setHeight(dimension.getHeight());
    video.setControls(true);

    // add video sources for each video profile
    addSources(video, media);

    return video;
  }

  /**
   * Add sources for HTML5 video player
   * @param video Video
   * @param media Media metadata
   */
  protected void addSources(Video video, Media media) {
    Asset asset = getDamAsset(media);
    if (asset == null) {
      return;
    }

    for (VideoProfile profile : getVideoProfiles()) {
      com.day.cq.dam.api.Rendition rendition = profile.getRendition(asset);
      if (rendition != null) {
        video.createSource()
        .setType(profile.getHtmlType())
        .setSrc(urlHandler.get(rendition.getPath()).buildExternalResourceUrl(rendition.adaptTo(Resource.class)));
      }
    }
  }

  /**
   * Get additional parameters to be set as &lt;param&gt; elements on html object element for flash player.
   * @param media Media metadata
   * @param dimension Dimension
   * @return Set of key/value pairs
   */
  protected Map<String, String> getAdditionalFlashPlayerParameters(Media media, Dimension dimension) {
    Map<String, String> parameters = new HashMap<>();

    parameters.put("allowFullScreen", "true");
    parameters.put("wmode", "opaque");

    return parameters;
  }

  /**
   * Get additional parameters to be set as flashvars parameter on html object element for flash player.
   * @param media Media metadata
   * @param dimension Dimension
   * @return Set of key/value pairs
   */
  protected Map<String, String> getAdditionalFlashPlayerFlashVars(Media media, Dimension dimension) {
    Map<String, String> flashvars = new HashMap<>();

    flashvars.put("autoPlay", "false");
    flashvars.put("loop", "false");

    return flashvars;
  }

  @Override
  public final boolean isValidMedia(@NotNull HtmlElement element) {
    return (element instanceof Video);
  }

}
