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

  const NextGenDynamicMedia = function (config) {
    const self = this;

    if (config.fileupload) {
      self._fileupload = config.fileupload;
      self._$fileupload = $(config.fileupload);
    }
    self._pathfield = config.pathfield;
    self._$pathfield = $(config.pathfield);
    self._assetSelectedCallback = config.assetSelectedCallback;
    self._filterImagesOnly = config.filterImagesOnly;

    self._ngdmConfig = self._$pathfield.data("wcmio-nextgendynamicmedia-config");
    if (!self._ngdmConfig) {
      // NGDM not enabled
      return;
    }

    const pickRemoteButtonOnClick = () => {
      self.pickRemoteAsset(assetReference => {
        if (assetReference != self._$pathfield.val()) {
          self._$pathfield.val(assetReference);
          if (self._assetSelectedCallback) {
            self._assetSelectedCallback(assetReference);
          }
        }
      });
    };
    self._addPickRemoteButton(pickRemoteButtonOnClick);
    self._updateRemotePickLink(pickRemoteButtonOnClick);
  };

  /**
   * Opens NGDM asset selector to pick a remote asset.
   * @param assetReferenceCallback called when asset is picked with the asset reference as parameter
   */
  NextGenDynamicMedia.prototype.pickRemoteAsset = function(assetReferenceCallback) {
    const self = this;

    self._prepareAssetSelectorDialog();
    const assetSelectorProps = self._prepareAssetSelectorProps(assetReferenceCallback);
    PureJSSelectors.renderAssetSelectorWithAuthFlow(
      self._assetSelectorDialogContainer.get(0),
      assetSelectorProps,
      () => self._assetSelectorDialog.show()
    );
  }

  /**
   * Add additional button to pathfield to pick remote assets.
   * @param onclickHandler Method that is called when button is clicked
   */
  NextGenDynamicMedia.prototype._addPickRemoteButton = function(onclickHandler) {
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
    $(buttonWrapper).insertAfter(self._$pathfield.find(".coral-InputGroup-button"));
  }

  /**
   * Prepares a Coral Dialog in DOM to render the asset selector.
   * If the markup already exists, it is reused.
   * Sets variables self._assetSelectorDialog and self._assetSelectorDialogContainer as a result.
   */
  NextGenDynamicMedia.prototype._prepareAssetSelectorDialog = function() {
    const self = this;

    self._assetSelectorDialog = $("#wcmio-handler-media-ngdm-assetselector");
    if (self._assetSelectorDialog.length === 0) {
      self._assetSelectorDialog = new Coral.Dialog().set({
        id: "wcmio-handler-media-ngdm-assetselector",
        content: {
          innerHTML: '<div class="wcmio-handler-media-ngdm-assetselector-dialogbody"></div>'
        },
        fullscreen: false
      });
      document.body.appendChild(self._assetSelectorDialog);
    }
    self._assetSelectorDialogContainer = $(self._assetSelectorDialog).find(".wcmio-handler-media-ngdm-assetselector-dialogbody");
  }

  /**
   * Prepare configuration for asset selector.
   * @param assetReferenceCallback called when asset is picked with the asset reference as parameter
   */
  NextGenDynamicMedia.prototype._prepareAssetSelectorProps = function(assetReferenceCallback) {
    const self = this;

    const assetSelectorProps = {
      //repositoryId: self._ngdmConfig.repositoryId,
      aemTierType: ['delivery'],
      apiKey: self._ngdmConfig.apiKey,
      env: self._ngdmConfig.env,
      handleSelection: (selection) => {
        const selectedAsset = selection[0];
        const assetReference = self._toAssetReference(selectedAsset);
        if (assetReference) {
          assetReferenceCallback(assetReference);
        }
      },
      onClose: () => {
        const $underlay = $("._coral-Underlay");
        $underlay.removeClass("is-open");
        self._assetSelectorDialog.remove();
      },
      hideTreeNav: true,
      acvConfig: {
        selectionType: "single",
      }
    };

    if (self._filterImagesOnly) {
      assetSelectorProps.filterSchema = [
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
      ];
    }

    return assetSelectorProps;
  }

  /**
   * Converts select asset object to asset reference string.
   */
  NextGenDynamicMedia.prototype._toAssetReference = function(selectedAsset) {
    const { name, "repo:assetId": assetId } = selectedAsset;
    if (name && assetId) {
      return `/${assetId}/${name}`;
    }
    return undefined;
  }

  /**
   * Replace click handler for "Pick/Remote" link - apply same feature as the browse remote button in the path field.
   * @param onclickHandler Method that is called when button is clicked
   */
  NextGenDynamicMedia.prototype._updateRemotePickLink = function (onclickHandler) {
    const self = this;
    if (self._$fileupload) {
      // it would be more correct to look for the .cq-FileUpload-picker-polaris in scope of self._$fileupload,
      // but the click tricker is registered this way in /libs/cq/gui/components/authoring/dialog/fileupload/clientlibs/fileupload/js/fileupload-polaris.js
      // so we have to do it the same way.
      $(document).off("click", ".cq-FileUpload-picker-polaris").on("click", ".cq-FileUpload-picker-polaris", onclickHandler);
    }
  };

  ns.NextGenDynamicMedia = NextGenDynamicMedia;

}(Granite.$, wcmio.handler.media, jQuery(document), document, this));
