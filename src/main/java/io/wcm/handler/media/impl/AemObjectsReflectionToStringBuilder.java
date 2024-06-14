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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.Page;

/**
 * Extends ReflectionToStringBuilder to provide custom handling for AEM-related objects
 * (Resource, ValueMap, Page) for a more compact log output.
 */
public class AemObjectsReflectionToStringBuilder extends ReflectionToStringBuilder {

  /**
   * @param object Object to output
   */
  public AemObjectsReflectionToStringBuilder(Object object) {
    super(object);
  }

  /**
   * @param object Object to output
   * @param style Style
   */
  public AemObjectsReflectionToStringBuilder(Object object, ToStringStyle style) {
    super(object, style);
  }

  @Override
  protected Object getValue(Field field) throws IllegalAccessException {
    if (Resource.class.isAssignableFrom(field.getType())) {
      Resource resource = (Resource)field.get(this.getObject());
      if (resource != null) {
        return resource.getPath();
      }
    }
    else if (ValueMap.class.isAssignableFrom(field.getType())) {
      ValueMap valueMap = (ValueMap)field.get(this.getObject());
      if (valueMap != null) {
        return filteredValueMap(valueMap);
      }
    }
    else if (Page.class.isAssignableFrom(field.getType())) {
      Page page = (Page)field.get(this.getObject());
      if (page != null) {
        return page.getPath();
      }
    }
    return super.getValue(field);
  }

  /**
   * Filter value map to exclude jcr:* properties and null values.
   * @param props Value map
   * @return Filtered value map, sorted by key
   */
  public static Map<String, Object> filteredValueMap(ValueMap props) {
    return props.entrySet().stream()
        .filter(entry -> !entry.getKey().startsWith("jcr:") && entry.getValue() != null)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o1, o2) -> o1, TreeMap::new));
  }

}
