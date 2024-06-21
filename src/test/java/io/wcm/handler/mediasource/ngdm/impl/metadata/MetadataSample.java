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
package io.wcm.handler.mediasource.ngdm.impl.metadata;

import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_ASSET_ID;

/**
 * Next Gen Dynamic Media Metadata samples.
 */
public final class MetadataSample {

  public static final String METADATA_JSON_IMAGE = "{"
      + "  \"assetId\": \"" + SAMPLE_ASSET_ID + "\","
      + "  \"repositoryMetadata\": {"
      + "    \"repo:name\": \"test.jpg\","
      + "    \"dc:format\": \"image/jpeg\","
      + "    \"smartcrops\": {"
      + "      \"Landscape\": {"
      + "        \"height\": \"180\","
      + "        \"left\": \"0.0\","
      + "        \"manualCrop\": \"true\","
      + "        \"normalizedHeight\": \"0.84375\","
      + "        \"normalizedWidth\": \"1.0\","
      + "        \"width\": \"320\","
      + "        \"top\": \"0.5774\""
      + "      },"
      + "      \"Portrait\": {"
      + "        \"height\": \"200\","
      + "        \"left\": \"0.16792180740265983\","
      + "        \"manualCrop\": \"false\","
      + "        \"normalizedHeight\": \"0.9980170652565797\","
      + "        \"normalizedWidth\": \"0.3326723533333333\","
      + "        \"width\": \"100\","
      + "        \"top\": \"0.0\""
      + "      },"
      + "      \"Invalid\": {"
      + "        \"height\": \"200\","
      + "        \"left\": \"-0.16792180740265983\","
      + "        \"manualCrop\": \"false\","
      + "        \"normalizedHeight\": \"-0.9980170652565797\","
      + "        \"normalizedWidth\": \"-0.6666399615446242\","
      + "        \"width\": \"100\","
      + "        \"top\": \"0.0\""
      + "      }"
      + "    }"
      + "  },"
      + "  \"assetMetadata\": {"
      + "    \"dam:assetStatus\": \"approved\","
      + "    \"dc:description\": \"Test Description\","
      + "    \"dc:title\": \"Test Image\","
      + "    \"tiff:ImageLength\": 800,"
      + "    \"tiff:ImageWidth\": 1200"
      + "  }"
      + "}";

  public static final String METADATA_JSON_PDF = "{"
      + "  \"assetId\": \"" + SAMPLE_ASSET_ID + "\","
      + "  \"repositoryMetadata\": {"
      + "    \"repo:name\": \"test.pdf\","
      + "    \"dc:format\": \"application/pdf\""
      + "  },"
      + "  \"assetMetadata\": {"
      + "    \"dam:assetStatus\": \"approved\","
      + "    \"dc:description\": \"Test Description\","
      + "    \"dc:title\": \"Test Document\""
      + "  }"
      + "}";

  private MetadataSample() {
    // constants only
  }

}
