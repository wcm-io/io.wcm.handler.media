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

  let ngdmConfig;
  let assetSelectorDialog;
  let assetSelectorDialogContainer;

  var NextGenDynamicMedia = function (config) {
    const self = this;
    self._pathfield = config.pathfield;
    self._$pathfield = $(config.pathfield);
    
    ngdmConfig = self._$pathfield.data("wcmio-nextgendynamicmedia-config");
    if (!ngdmConfig) {
      // NGDM not enabled
      return;
    }

    addPickRemoteButton(self._$pathfield, () => {
      self.pickRemoteAsset(assetReference => {
        if (assetReference != self._$pathfield.val()) {
          self._$pathfield.val(assetReference);
        }
      });
    });
  };

  /**
   * Opens NGDM asset selector to pick a remote asset.
   * @param assetReferenceCallback called when asset is picked with the asset reference as parameter
   */
  NextGenDynamicMedia.prototype.pickRemoteAsset = function(assetReferenceCallback) {
    showAssetSelectorDialog(assetReferenceCallback);
  }

  /**
   * Add additional button to pathfield to pick remote assets.
   * @param onclickHandler Method that is called when button is clicked
   */
  function addPickRemoteButton($pathfield, onclickHandler) {
    const self = this;

    const label = Granite.I18n.get("Remote");
    const pickRemoteButton = new Coral.Button().set({
      icon: "popOut",
      title: label,
      type: "button"
    });
    pickRemoteButton.setAttribute("aria-label", label);
    $(pickRemoteButton).on("click", onclickHandler);

    const buttonWrapper = document.createElement("span");
    buttonWrapper.classList.add("coral-InputGroup-button");
    buttonWrapper.appendChild(pickRemoteButton);

    // add new button (wrapper) after existing one to pick local assets
    $(buttonWrapper).insertAfter($pathfield.find(".coral-InputGroup-button"));
  }

  /**
   * Show asset selector dialog wrapped in Coral dialog.
   * @param assetReferenceCallback called when asset is picked with the asset reference as parameter
   */
  function showAssetSelectorDialog(assetReferenceCallback) {
    prepareAssetSelectorDialog();
    const assetSelectorProps = {
      repositoryId: ngdmConfig.repositoryId,
      apiKey: ngdmConfig.apiKey,
      env: ngdmConfig.env,
      handleSelection: (selection) => {
        const selectedAsset = selection[0];
        const assetReference = toAssetReference(selectedAsset);
        if (assetReference) {
          assetReferenceCallback(assetReference);
        }
      },
      onClose: () => {
        const $underlay = $("._coral-Underlay");
        $underlay.removeClass("is-open");
        assetSelectorDialog.remove();
      },
      hideTreeNav: false,
      acvConfig: {
        selectionType: "single",
      },
      filterSchema: [
        {
          header: "File Type",
          groupKey: "TopGroup",
          fields: [
            {
              element: "checkbox",
              name: "type",
              defaultValue: ["image/*"],
              readOnly: true,
              options: [
                {
                  label: "Images",
                  value: "image/*"
                }
              ]
            }
          ]
        },
        {
          fields: [
            {
              element: "checkbox",
              name: "type",
              options: [
                {
                  label: "JPG",
                  value: "image/jpeg"
                },
                {
                  label: "PNG",
                  value: "image/png"
                },
                {
                  label: "TIFF",
                  value: "image/tiff"
                },
                {
                  label: "GIF",
                  value: "image/gif"
                },
                {
                  label: "SVG",
                  value: "image/svg+xml"
                }
              ],
              columns: 2,
            }
          ],
          header: "Mime Types",
          groupKey: "MimeTypeGroup"
        }
      ]
    };

    PureJSSelectors.renderAssetSelectorWithAuthFlow(
      assetSelectorDialogContainer.get(0),
      assetSelectorProps,
      () => assetSelectorDialog.show()
    );
  }

  /**
   * Prepares a Coral Dialog in DOM to render the asset selector.
   * If the markup already exists, it is reused.
   * Sets variables assetSelectorDialog and assetSelectorDialogContainer as a result.
   */
  function prepareAssetSelectorDialog() {
    assetSelectorDialog = $("#wcmio-handler-media-ngdm-assetselector");
    if (assetSelectorDialog.length === 0) {
      assetSelectorDialog = new Coral.Dialog().set({
        id: "wcmio-handler-media-ngdm-assetselector",
        content: {
          innerHTML: '<div class="wcmio-handler-media-ngdm-assetselector-dialogbody"></div>'
        },
        fullscreen: false
      });
      document.body.appendChild(assetSelectorDialog);
    }
    assetSelectorDialogContainer = $(assetSelectorDialog).find(".wcmio-handler-media-ngdm-assetselector-dialogbody");
  }

  /**
   * Converts select asset object to asset reference string.
   */
  function toAssetReference(selectedAsset) {
    const { name, "repo:assetId": assetId } = selectedAsset;
    if (name && assetId) {
      return `/${assetId}/${name}`;
    }
    return undefined;
  }

  ns.NextGenDynamicMedia = NextGenDynamicMedia;

}(Granite.$, wcmio.handler.media, jQuery(document), document, this));
