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
package io.wcm.handler.mediasource.ngdm.impl;

/**
 * Sample reference.
 */
public final class NextGenDynamicMediaReferenceSample {

  public static final String SAMPLE_UUID = "12345678-abcd-abcd-abcd-abcd12345678";
  public static final String SAMPLE_ASSET_ID = "urn:aaid:aem:" + SAMPLE_UUID;
  public static final String SAMPLE_FILENAME = "my-image.jpg";
  public static final String SAMPLE_REFERENCE = "/" + SAMPLE_ASSET_ID + "/" + SAMPLE_FILENAME;

  private NextGenDynamicMediaReferenceSample() {
    // constants only
  }

}
