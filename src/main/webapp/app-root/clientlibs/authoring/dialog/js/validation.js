/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
/*
 * Registers Coral UI 3 validators.
 */
;(function(document, Granite, $, undefined) {
  "use strict";

  // Predefined patterns
  var pattern = {
    /*
     * Matches for strings like:
     * 100,200
     *  500 , 600 , 700
     * 100?, 200, 300?
     * 100:1x, 200:1.5x, 300:2x?
     */
    responsiveWidths: /^\s*\d+(:\d+(\.\d+)?x)?\??\s*(,\s*\d+(:\d+(\.\d+)?x)?\??\s*)*$/
  };

  var foundationValidator = $(window).adaptTo("foundation-registry");

  var getValue = function(el) {
    return $(el).val();
  };

  // predefined "responsiveWidths" pattern validator
  foundationValidator.register('foundation.validation.validator', {
    selector: '[data-foundation-validation="wcmio.handler.media.responsiveWidths"]',
    validate: function(el) {
      var value = getValue(el);
      var valid = value.length === 0 || pattern.responsiveWidths.test(value);
      if (!valid) {
        return Granite.I18n.get("Invalid widths list.");
      }
    }
  });

  // validates valid media format for asset picker
  foundationValidator.register('foundation.validation.validator', {
    selector: '[data-foundation-validation="wcmio.handler.media.mediaFormat"]',
    validate: function(el) {
      var nextSibling = el.nextSibling;
      while (nextSibling) {
        var tagName = nextSibling.tagName?.toLowerCase();
        if (tagName === 'coral-alert') {
          return nextSibling.textContent;
        }
        nextSibling = nextSibling.nextSibling;
      }
    }
  });

})(document, Granite, Granite.$);
