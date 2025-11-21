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
package io.wcm.handler.media.spi;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ConsumerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.sling.commons.caservice.ContextAwareService;

/**
 * {@link MediaFormatProvider} OSGi services provide media formats for the media handler.
 * Applications can set service properties or bundle headers as defined in {@link ContextAwareService} to apply this
 * configuration only for resources that match the relevant resource paths.
 */
@ConsumerType
public abstract class MediaFormatProvider implements ContextAwareService {

  private final Set<MediaFormat> mediaFormats;

  private static final Logger log = LoggerFactory.getLogger(MediaFormatProvider.class);

  /**
   * Initialize provider with explicit set of media formats.
   * @param mediaFormats Set of media formats for parameter provider
   */
  protected MediaFormatProvider(Set<MediaFormat> mediaFormats) {
    this.mediaFormats = mediaFormats;
  }

  /**
   * Initialize provider from media format definitions in class fields.
   * @param type Type containing media format definitions as public static fields.
   */
  protected MediaFormatProvider(Class<?> type) {
    this(getMediaFormatsFromPublicFields(type));
  }

  /**
   * Get media formats defined by this provider.
   * @return Media formats that the application defines
   */
  public @NotNull Set<MediaFormat> getMediaFormats() {
    return mediaFormats;
  }

  /**
   * Get all media formats defined as public static fields in the given type.
   * @param type Type
   * @return Set of media formats
   */
  private static Set<MediaFormat> getMediaFormatsFromPublicFields(Class<?> type) {
    Set<MediaFormat> params = new HashSet<>();
    try {
      Field[] fields = type.getFields();
      for (Field field : fields) {
        if (field.getType().isAssignableFrom(MediaFormat.class)) {
          params.add((MediaFormat)field.get(null));
        }
      }
    }
    catch (IllegalArgumentException | IllegalAccessException ex) {
      log.warn("Unable to access fields of {}", type.getName(), ex);
    }
    return Collections.unmodifiableSet(params);
  }

  /**
   * @deprecated Prevent finalize attack (PMD CT_CONSTRUCTOR_THROW / SEI CERT Rule OBJ-11)
   */
  @Override
  @SuppressWarnings({ "PMD.EmptyFinalizer", "checkstyle:SuperFinalize", "checkstyle:NoFinalizerCheck", "java:S1113" })
  @Deprecated(since = "2.0.0")
  protected final void finalize() {
    // do nothing
  }

}
