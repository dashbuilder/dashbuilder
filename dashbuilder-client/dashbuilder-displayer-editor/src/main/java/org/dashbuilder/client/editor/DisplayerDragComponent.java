/**
 * Copyright (C) 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.client.editor;

import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.PerspectiveCoordinator;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.dashbuilder.displayer.client.widgets.DisplayerEditor;
import org.dashbuilder.displayer.client.widgets.DisplayerEditorPopup;
import org.dashbuilder.displayer.client.widgets.DisplayerView;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.ext.layout.editor.client.components.HasModalConfiguration;
import org.uberfire.ext.layout.editor.client.components.ModalConfigurationContext;
import org.uberfire.ext.layout.editor.client.components.RenderingContext;
import org.uberfire.ext.plugin.client.perspective.editor.api.PerspectiveEditorDragComponent;

@Dependent
public class DisplayerDragComponent implements PerspectiveEditorDragComponent, HasModalConfiguration {

    @Inject
    DisplayerEditorPopup editor;

    @Inject
    DisplayerSettingsJSONMarshaller marshaller;

    @Inject
    PlaceManager placeManager;

    @Inject
    PerspectiveCoordinator perspectiveCoordinator;

    @Override
    public IsWidget getDragWidget() {
        TextBox textBox = GWT.create(TextBox.class);
        textBox.setPlaceholder("Displayer Component");
        textBox.setReadOnly(true);
        return textBox;
    }

    @Override
    public IsWidget getPreviewWidget(final RenderingContext ctx) {
        return getShowWidget(ctx);
    }

    @Override
    public IsWidget getShowWidget(final RenderingContext ctx) {
        Map<String, String> properties = ctx.getComponent().getProperties();
        String json = properties.get("json");
        if (json == null) return null;

        final DisplayerSettings settings = marshaller.fromJsonString(json);
        final DisplayerView displayerView = new DisplayerView(settings);

        displayerView.addAttachHandler(new AttachEvent.Handler() {
            public void onAttachOrDetach(AttachEvent attachEvent) {
                if (attachEvent.isAttached()) {
                    int containerWidth = ctx.getContainer().getOffsetWidth() - 40;
                    adjustSize(settings, containerWidth);
                    Displayer displayer = displayerView.draw();
                    perspectiveCoordinator.addDisplayer(displayer);
                }
            }
        });
        return displayerView;
    }

    @Override
    public Modal getConfigurationModal(final ModalConfigurationContext ctx) {
        Map<String, String> properties = ctx.getComponentProperties();
        String json = properties.get("json");
        DisplayerSettings settings = json != null ? marshaller.fromJsonString(json) : null;

        editor.init(settings, new DisplayerEditor.Listener() {

            public void onClose(DisplayerEditor editor) {
                ctx.configurationCancelled();
            }

            public void onSave(DisplayerEditor editor) {
                String json = marshaller.toJsonString(editor.getDisplayerSettings());
                ctx.setComponentProperty("json", json);
                ctx.configurationFinished();
            }
        });
        return editor;
    }

    protected void adjustSize(DisplayerSettings settings, int containerWidth) {
        int displayerWidth = settings.getChartWidth();
        int tableWidth = settings.getTableWidth();
        if (containerWidth > 0 &&  displayerWidth > containerWidth) {
            int ratio = containerWidth * 100 / displayerWidth;
            settings.setChartWidth(containerWidth);
            settings.setChartHeight(settings.getChartHeight() * ratio / 100);
        }
        if (tableWidth == 0 || tableWidth > containerWidth) {
            settings.setTableWidth(containerWidth - 20);
        }
    }
}
