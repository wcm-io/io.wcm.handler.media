/*-
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
;(function ($, ns, channel, document, window, undefined) {
  "use strict";

  var NextGenDynamicMedia = function (config) {
    var self = this;
    self._pathfield = config.pathfield;
    self._$pathfield = $(config.pathfield);
    self._config = self._$pathfield.data("wcmio-nextgendynamicmedia-config");

    if (!self._config) {
      // NGDM not enabled
      return;
    }

    self._addPickRemoteButton();
  };

  /**
   * Add additional button to pathfield to pick remote assets.
   */
  NextGenDynamicMedia.prototype._addPickRemoteButton = function () {
    var self = this;

    var label = Granite.I18n.get("Remote");
    var pickRemoteButton = new Coral.Button().set({
      icon: "popOut",
      title: label,
      type: "button"
    });
    pickRemoteButton.setAttribute("aria-label", label);
    $(pickRemoteButton).on("click", function() {
      self.pickRemoteAsset();
    });

    var buttonWrapper = document.createElement("span");
    buttonWrapper.classList.add("coral-InputGroup-button");
    buttonWrapper.appendChild(pickRemoteButton);

    // add new button (wrapper) after existing one to pick local assets
    $(buttonWrapper).insertAfter(self._$pathfield.find(".coral-InputGroup-button"));
  }

  /**
   * Opens NGDM asset selector to pick a remote asset.
   */
  NextGenDynamicMedia.prototype.pickRemoteAsset = function () {
    var self = this;

    alert(`pick remote asset: ${JSON.stringify(self._config)}`);
  }

  ns.NextGenDynamicMedia = NextGenDynamicMedia;

}(Granite.$, wcmio.handler.media, jQuery(document), document, this));
