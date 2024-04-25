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

/**
 * Constants for {@link MediaFileServlet} and {@link ImageFileServlet}.
 */
public final class MediaFileServletConstants {

  /**
   * Selector for forcing a "save-as" dialog in the browser
   */
  public static final String SELECTOR_DOWNLOAD = "download_attachment";

  /**
   * Content disposition header
   */
  public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

  /**
   * Content disposition header
   */
  public static final String HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy";

  /**
   * Selector
   */
  public static final String SELECTOR = "media_file";

  /**
   * Extension
   */
  public static final String EXTENSION = "file";

  private MediaFileServletConstants() {
    // constants only
  }

}
