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
package io.wcm.handler.media.format.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.Constants;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatBuilder;
import io.wcm.handler.media.format.MediaFormatProviderManager;
import io.wcm.handler.media.spi.MediaFormatProvider;
import io.wcm.sling.commons.caservice.impl.ContextAwareServiceResolverImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaFormatProviderManagerImplTest {

  private static final MediaFormat MF11 = MediaFormatBuilder.create("mf11").description("desc-from-1").build();
  private static final MediaFormat MF12 = MediaFormatBuilder.create("mf12").description("desc-from-1").build();
  private static final SortedSet<MediaFormat> MEDIAFORMATS_1 = new TreeSet<>(Set.of(MF11, MF12));

  private static final MediaFormat MF11_FROM2 = MediaFormatBuilder.create("mf11").description("desc-from-2").build();
  private static final MediaFormat MF21 = MediaFormatBuilder.create("mf21").description("desc-from-2").build();
  private static final SortedSet<MediaFormat> MEDIAFORMATS_2 = new TreeSet<>(Set.of(MF11_FROM2, MF21));

  private final AemContext context = new AemContext();

  @Mock
  private MediaFormatProvider provider1;
  @Mock
  private MediaFormatProvider provider2;

  private Resource resource;
  private MediaFormatProviderManager underTest;

  @BeforeEach
  void setUp() {
    resource = context.create().resource("/content/test");

    context.registerInjectActivateService(new ContextAwareServiceResolverImpl());

    when(provider1.getMediaFormats()).thenReturn(MEDIAFORMATS_1);
    when(provider2.getMediaFormats()).thenReturn(MEDIAFORMATS_2);

    context.registerService(MediaFormatProvider.class, provider1,
        Constants.SERVICE_RANKING, 200);
    context.registerService(MediaFormatProvider.class, provider2,
        Constants.SERVICE_RANKING, 100);

    underTest = context.registerInjectActivateService(new MediaFormatProviderManagerImpl());
  }

  @Test
  void testWithResource() {
    SortedSet<MediaFormat> result = underTest.getMediaFormats(resource);
    assertEquals(new TreeSet<>(Set.of(MF11, MF12, MF21)), result);

    MediaFormat first = result.iterator().next();
    assertEquals("mf11", first.getName());
    // make sure when multiplie providers define formats with the same name the one with the highest ranking wins
    assertEquals("desc-from-1", first.getDescription());
  }

  @Test
  void testNullResource() {
    assertEquals(Collections.emptySet(), underTest.getMediaFormats(null));
  }

  @Test
  void testGetAllMediaFormats() {
    SortedMap<String, SortedSet<MediaFormat>> allMediaFormats = underTest.getAllMediaFormats();

    assertEquals(1, allMediaFormats.size());

    Map.Entry<String, SortedSet<MediaFormat>> entry = allMediaFormats.entrySet().iterator().next();
    assertEquals("mock-bundle", entry.getKey());

    SortedSet<MediaFormat> mediaFormats = entry.getValue();
    assertEquals(new TreeSet<>(Set.of(MF11, MF12, MF21)), mediaFormats);
  }

}
