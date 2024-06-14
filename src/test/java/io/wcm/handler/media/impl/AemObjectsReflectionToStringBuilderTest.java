/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2024 wcm.io
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

import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class AemObjectsReflectionToStringBuilderTest {

  private final AemContext context = AppAemContext.newAemContext();

  @SuppressWarnings("null")
  private static final ValueMap VALUEMAP_SAMPLE = ImmutableValueMap.of(
      "prop1", "value1",
      JCR_CREATED, new Date(),
      JCR_PRIMARYTYPE, NT_UNSTRUCTURED,
      "prop2", 5,
      "prop3", null);

  @Test
  void testBuild() {
    ClassWithFields obj = new ClassWithFields();
    obj.prop1 = "value1";
    obj.resource = context.create().resource("/content/resource1",
        "prop2", "value2");
    obj.page = context.create().page("/content/page1");
    obj.props = VALUEMAP_SAMPLE;

    assertEquals("[page=/content/page1,prop1=value1,props={prop1=value1, prop2=5},resource=/content/resource1]", obj.toString());
  }

  @Test
  void testBuild_NullObjects() {
    ClassWithFields obj = new ClassWithFields();

    assertEquals("[page=<null>,prop1=<null>,props=<null>,resource=<null>]", obj.toString());
  }

  @Test
  void testFilteredValueMap() {
    Map<String, Object> filtered = AemObjectsReflectionToStringBuilder.filteredValueMap(VALUEMAP_SAMPLE);

    assertEquals(Map.of("prop1", "value1", "prop2", 5), filtered);
  }

  @SuppressWarnings("unused")
  @SuppressFBWarnings("URF_UNREAD_FIELD")
  private static final class ClassWithFields {
    String prop1;
    Resource resource;
    Page page;
    ValueMap props;

    @Override
    public String toString() {
      return new AemObjectsReflectionToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE).build();
    }
  }

}
