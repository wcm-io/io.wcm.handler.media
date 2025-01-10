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
package io.wcm.handler.media.format;

import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.sling.api.resource.Resource;

import io.wcm.handler.media.spi.MediaFormatProvider;

/**
 * Collects all media format definitions provided by applications via {@link MediaFormatProvider} interface.
 *
 * <p>
 * This interface is implemented by an OSGi services.
 * It should be considered INTERNAL and not be used by applications.
 * </p>
 */
public interface MediaFormatProviderManager {

  /**
   * Get all media format definitions for application.
   * @param contextResource Context resource to get media formats for
   * @return Media format definitions
   */
  SortedSet<MediaFormat> getMediaFormats(Resource contextResource);

  /**
   * Get all media format definitions deployed in the system, grouped by bundle which exposes them.
   * @return Map with bundle name as key, set of media formats as values.
   */
  SortedMap<String, SortedSet<MediaFormat>> getAllMediaFormats();

}
