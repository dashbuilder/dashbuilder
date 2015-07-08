/*
 * Copyright 2015 JBoss Inc
 *
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
 */
package org.dashbuilder.displayer.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.displayer.DisplayerSubType;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.RendererLibrary;
import org.dashbuilder.displayer.client.RendererManager;
import org.dashbuilder.displayer.client.resources.i18n.DisplayerTypeLiterals;
import org.dashbuilder.displayer.client.resources.images.DisplayerImagesResources;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.constants.ImageType;

public class DisplayerSubtypeSelector extends Composite {

    public interface SubTypeChangeListener {
        void displayerSubtypeChanged(DisplayerSubType displayerSubType);
    }

    private SubTypeChangeListener listener;
    private List<DisplayerSubTypeImageWidget> imageWidgetList;
    RendererManager rendererManager;

    private FlexTable subtypes;
    private VerticalPanel subtypePanel;

    public DisplayerSubtypeSelector(SubTypeChangeListener subTypeChangeListener) {
        listener = subTypeChangeListener;
        rendererManager = RendererManager.get();
        imageWidgetList = new ArrayList<DisplayerSubTypeImageWidget>(5);

        subtypes = new FlexTable();
        subtypePanel = new VerticalPanel();
        subtypePanel.add(subtypes);
        initWidget(subtypePanel);
    }

    public void select(String renderer, DisplayerType type, DisplayerSubType selectedSubType) {
        subtypes.removeAllRows();
        imageWidgetList.clear();

        RendererLibrary rendererLibrary = null;
        if (!StringUtils.isBlank(renderer)) rendererLibrary = rendererManager.getRendererByUUID(renderer);
        else rendererLibrary = rendererManager.getRendererForType(type);

        if (rendererLibrary != null) {
            List<DisplayerSubType> supportedSubTypes = rendererLibrary.getSupportedSubtypes(type);
            if (supportedSubTypes != null && supportedSubTypes.size() > 0) {
                for (int i = 0; i < supportedSubTypes.size(); i++) {
                    final DisplayerSubType subtype = supportedSubTypes.get(i);

                    // Double check the renderer library for invalid subtypes for this type
                    if (!type.getSubTypes().contains(subtype)) {
                        throw new RuntimeException("Wrong subtype (" + subtype + ") indicated for type " + type + " by renderer library " + rendererLibrary.getUUID());
                    }

                    String resourcePrefix = type.toString() + "_" + subtype.toString();
                    ImageResource selectedIR = (ImageResource) DisplayerImagesResources.INSTANCE.getResource(resourcePrefix + DisplayerImagesResources.SELECTED_SUFFIX);
                    ImageResource unselectedIR = (ImageResource) DisplayerImagesResources.INSTANCE.getResource(resourcePrefix + DisplayerImagesResources.UNSELECTED_SUFFIX);
                    String tooltip = DisplayerTypeLiterals.INSTANCE.getString(resourcePrefix + "_tt");

                    boolean initiallySelected = selectedSubType != null? subtype == selectedSubType : i == 0;
                    final DisplayerSubTypeImageWidget dstiw = new DisplayerSubTypeImageWidget(  selectedIR,
                                                                                                unselectedIR,
                                                                                                tooltip,
                                                                                                initiallySelected);
                    imageWidgetList.add(dstiw);

                    if (initiallySelected) listener.displayerSubtypeChanged(subtype);

                    dstiw.setSelectClickHandler(
                            new ClickHandler() {
                                @Override
                                public void onClick(ClickEvent event) {
                                    if (!dstiw.isSelected) select(dstiw);
                                    if (listener != null) listener.displayerSubtypeChanged(subtype);
                                }
                            });

                    subtypes.setWidget(i, 0, dstiw);
                }
            } else {
                // Show a default image for those chart types that don't have any subtypes
                ImageResource selectedIR = (ImageResource)DisplayerImagesResources.INSTANCE.getResource(type.toString() + DisplayerImagesResources.DEFAULT_SUFFIX );
                String tooltip = DisplayerTypeLiterals.INSTANCE.getString(type.toString() + DisplayerImagesResources.DEFAULT_SUFFIX + "_tt");

                DisplayerSubTypeImageWidget dstiw = new DisplayerSubTypeImageWidget(  selectedIR,
                        null,
                        tooltip,
                        true);

                subtypes.setWidget(0, 0, dstiw);
            }
        }
    }

    private void select(DisplayerSubTypeImageWidget dstiw) {
        for (DisplayerSubTypeImageWidget imageWidget : imageWidgetList) {
            if (imageWidget == dstiw) imageWidget.select();
            else imageWidget.unselect();
        }
    }

    public class DisplayerSubTypeImageWidget extends Composite {

        private FlexTable container = new FlexTable();

        private boolean isSelected = false;
        private Image selected;
        private Image unselected;

        public DisplayerSubTypeImageWidget( ImageResource selectedImage,
                                            ImageResource unselectedImage,
                                            String tooltip,
                                            boolean initiallySelected) {

            initWidget(container);

            isSelected = initiallySelected;

            if (selectedImage != null) {
                selected = new Image(selectedImage);
                selected.setType(ImageType.THUMBNAIL);
                selected.setTitle(tooltip);
                selected.setVisible(isSelected);
                selected.addStyleName("selDispSubtype"); //for selenium
                container.setWidget(0, 0, selected);
            }

            if (unselectedImage != null) {
                unselected = new Image(unselectedImage);
                unselected.setType(ImageType.THUMBNAIL);
                unselected.setTitle(tooltip);
                unselected.setVisible(!isSelected);
                container.setWidget(0, 1, unselected);
            }
        }

        public HandlerRegistration setSelectClickHandler(ClickHandler selectedClickHandler) {
            return unselected != null ? unselected.addClickHandler(selectedClickHandler) : null;
        }

        public void select() {
            isSelected = true;
            selected.setVisible(true);
            unselected.setVisible(false);
        }

        public void unselect() {
            isSelected = false;
            selected.setVisible(false);
            unselected.setVisible(true);
        }
    }
}
