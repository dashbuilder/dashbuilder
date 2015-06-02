/**
 * Copyright (C) 2014 JBoss Inc
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
package org.dashbuilder.displayer.client;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonGroup;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.perspective.editor.PerspectiveEditor;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOffEvent;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOnEvent;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetExportReadyCallback;
import org.dashbuilder.dataset.uuid.UUIDGenerator;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.json.DisplayerSettingsJSONMarshaller;
import org.dashbuilder.displayer.client.resources.i18n.Constants;
import org.dashbuilder.displayer.client.widgets.DisplayerEditor;
import org.dashbuilder.displayer.client.widgets.DisplayerEditorPopup;
import org.dashbuilder.displayer.client.widgets.DisplayerView;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.model.menu.impl.BaseMenuCustom;

@WorkbenchScreen(identifier = "DisplayerScreen")
@Dependent
public class DisplayerScreenPresenter {

    protected DataSetClientServices dataSetClientServices;
    protected PerspectiveEditor perspectiveEditor;
    protected DisplayerView displayerView;
    protected DisplayerEditorPopup displayerEditor;
    protected PerspectiveCoordinator perspectiveCoordinator;
    protected DisplayerSettingsJSONMarshaller jsonMarshaller;
    protected DisplayerSettings displayerSettings;
    protected UUIDGenerator uuidGenerator;
    protected PlaceRequest placeRequest;

    protected ButtonGroup editButtonGroup;
    protected ButtonGroup cloneButtonGroup;
    protected ButtonGroup csvExportButtonGroup;
    protected ButtonGroup xlsExportButtonGroup;

    // TODO allow configuration of this through a custom system property?
    protected static final int MAX_EXPORT_LIMIT = 100000;

    @Inject
    public DisplayerScreenPresenter(
            DataSetClientServices dataSetClientServices,
            PerspectiveEditor perspectiveEditor,
            UUIDGenerator uuidGenerator,
            DisplayerView displayerView,
            PerspectiveCoordinator perspectiveCoordinator,
            DisplayerSettingsJSONMarshaller jsonMarshaller) {

        this.dataSetClientServices = dataSetClientServices;
        this.perspectiveEditor = perspectiveEditor;
        this.uuidGenerator = uuidGenerator;
        this.displayerView = displayerView;
        this.perspectiveCoordinator = perspectiveCoordinator;
        this.jsonMarshaller = jsonMarshaller;
        this.displayerEditor =  new DisplayerEditorPopup();
    }

    @OnStartup
    public void onStartup(final PlaceRequest placeRequest) {
        this.placeRequest = placeRequest;
        String json = placeRequest.getParameter("json", "");
        if (!StringUtils.isBlank(json)) this.displayerSettings = jsonMarshaller.fromJsonString(json);
        if (displayerSettings == null) throw new IllegalArgumentException(Constants.INSTANCE.displayer_presenter_displayer_notfound());

        // Check if display renderer selector component.
        Boolean showRendererSelector = Boolean.parseBoolean(placeRequest.getParameter("showRendererSelector","false"));
        displayerView.setIsShowRendererSelector(showRendererSelector);

        // Draw the Displayer.
        if (StringUtils.isBlank(displayerSettings.getUUID())) displayerSettings.setUUID(uuidGenerator.newUuid());
        displayerView.setDisplayerSettings(displayerSettings);
        Displayer displayer = displayerView.draw();

        // Register the Displayer into the coordinator.
        perspectiveCoordinator.addDisplayer(displayer);

        buildMenuActionsButton();
    }

    @OnClose
    public void onClose() {
        this.removeDisplayer();
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return displayerSettings.getTitle();
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return displayerView;
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return MenuFactory
            .newTopLevelCustomMenu(newMenuItemBuilder(editButtonGroup)).endMenu()
            .newTopLevelCustomMenu(newMenuItemBuilder(cloneButtonGroup)).endMenu()
            .newTopLevelCustomMenu(newMenuItemBuilder(csvExportButtonGroup)).endMenu()
            .newTopLevelCustomMenu(newMenuItemBuilder(xlsExportButtonGroup)).endMenu()
            .build();
    }

    @WorkbenchMenu
    public MenuFactory.CustomMenuBuilder newMenuItemBuilder(final Widget widget) {
        return new MenuFactory.CustomMenuBuilder() {
            public void push(MenuFactory.CustomMenuBuilder element) {
                throw new UnsupportedOperationException("Not implemented.");
            }

            public MenuItem build() {
                return new BaseMenuCustom<IsWidget>() {
                    public IsWidget build() {
                        return widget;
                    }
                };
            }
        };
    }

    protected void buildMenuActionsButton() {
        Button editButton = new Button(new ClickHandler() {
            public void onClick(ClickEvent event) {
                getEditCommand().execute();
            }
        });
        editButton.setTitle(Constants.INSTANCE.menu_edit());
        editButton.setIcon(IconType.EDIT);
        editButton.setSize(ButtonSize.MINI);
        editButtonGroup = new ButtonGroup();
        editButtonGroup.add(editButton);

        Button cloneButton = new Button(new ClickHandler() {
            public void onClick(ClickEvent event) {
                getCloneCommand().execute();
            }
        });
        cloneButton.setTitle(Constants.INSTANCE.menu_clone());
        cloneButton.setIcon(IconType.COPY);
        cloneButton.setSize(ButtonSize.MINI);
        cloneButtonGroup = new ButtonGroup();
        cloneButtonGroup.add(cloneButton);

        Button csvExportButton = new Button(new ClickHandler() {
            public void onClick(ClickEvent event) {
                getExportCsvCommand().execute();
            }
        });
        csvExportButton.setTitle(Constants.INSTANCE.menu_export_csv());
        csvExportButton.setIcon(IconType.FILE);
        csvExportButton.setSize(ButtonSize.MINI);
        csvExportButtonGroup = new ButtonGroup();
        csvExportButtonGroup.add(csvExportButton);

        Button xlsExportButton = new Button(new ClickHandler() {
            public void onClick(ClickEvent event) {
                getExportExcelCommand().execute();
            }
        });
        xlsExportButton.setTitle(Constants.INSTANCE.menu_export_excel());
        xlsExportButton.setIcon(IconType.TH_LIST);
        xlsExportButton.setSize(ButtonSize.MINI);
        xlsExportButtonGroup = new ButtonGroup();
        xlsExportButtonGroup.add(xlsExportButton);

        updateMenuVisibleFlags();
    }

    protected void updateMenuVisibleFlags() {
        editButtonGroup.setVisible(perspectiveEditor.isEditOn());
        cloneButtonGroup.setVisible(perspectiveEditor.isEditOn());
        csvExportButtonGroup.setVisible(displayerSettings.isCSVExportAllowed());
        xlsExportButtonGroup.setVisible(displayerSettings.isExcelExportAllowed());
    }

    protected void onPerspectiveEditOn(@Observes PerspectiveEditOnEvent event) {
        updateMenuVisibleFlags();
    }

    protected void onPerspectiveEditOff(@Observes PerspectiveEditOffEvent event) {
        updateMenuVisibleFlags();
    }

    protected Command getEditCommand() {
        return new Command() {
            public void execute() {
                final String currentTitle = displayerSettings.getTitle();
                DisplayerEditorPopup displayerEditor =  new DisplayerEditorPopup();
                displayerEditor.init(displayerSettings, new DisplayerEditor.Listener() {

                    public void onClose(DisplayerEditor editor) {
                    }

                    public void onSave(final DisplayerEditor editor) {
                        updateDisplayer(editor.getDisplayerSettings());

                        String newTitle = editor.getDisplayerSettings().getTitle();
                        if (!currentTitle.equals(newTitle)) {
                            perspectiveEditor.changePlaceTitle(placeRequest, editor.getDisplayerSettings().getTitle());
                        }
                        perspectiveEditor.updatePlace(placeRequest, createPlaceRequest(editor.getDisplayerSettings()));
                        perspectiveEditor.saveCurrentPerspective();
                    }
                });
            }
        };
    }

    protected Command getCloneCommand() {
        return new Command() {
            public void execute() {
                DisplayerSettings clonedSettings = displayerSettings.cloneInstance();
                clonedSettings.setUUID(uuidGenerator.newUuid());
                clonedSettings.setTitle("Copy of " + clonedSettings.getTitle());

                DisplayerEditorPopup displayerEditor = new DisplayerEditorPopup();
                displayerEditor.init(clonedSettings, new DisplayerEditor.Listener() {

                    public void onClose(DisplayerEditor editor) {
                    }

                    public void onSave(final DisplayerEditor editor) {
                        perspectiveEditor.updatePlace(placeRequest, createPlaceRequest(editor.getDisplayerSettings()));
                        perspectiveEditor.saveCurrentPerspective();
                    }
                });
            }
        };
    }

    protected Command getExportCsvCommand() {
        return new Command() {
            public void execute() {
                // Get all the data set rows with a maximum of 10000
                DataSetLookup currentLookup = getConstrainedDataSetLookup(displayerView.getDisplayer().getDataSetHandler().getCurrentDataSetLookup());

                try {
                    dataSetClientServices.exportDataSetCSV(currentLookup, new DataSetExportReadyCallback() {
                        @Override
                        public void exportReady(String exportFilePath) {
                            final String s = DataSetClientServices.get().getExportServletUrl();
                            final String u = DataSetClientServices.get().getDownloadFileUrl(s, exportFilePath);
                            Window.open(u,
                                            "downloading",
                                            "resizable=no,scrollbars=yes,status=no");
                        }
                    });
                } catch ( Exception e ) {
                    displayerView.error("Export to CSV failed", e);
                }
            }
        };
    }

    protected Command getExportExcelCommand() {
        return new Command() {
            public void execute() {
                // Get all the data set rows with a maximum of 10000
                DataSetLookup currentLookup = getConstrainedDataSetLookup(displayerView.getDisplayer().getDataSetHandler().getCurrentDataSetLookup());

                try {
                    dataSetClientServices.exportDataSetExcel(currentLookup, new DataSetExportReadyCallback() {
                        @Override
                        public void exportReady(String exportFilePath) {
                            final String s = DataSetClientServices.get().getExportServletUrl();
                            final String u = DataSetClientServices.get().getDownloadFileUrl(s, exportFilePath);
                            Window.open(u,
                                    "downloading",
                                    "resizable=no,scrollbars=yes,status=no");
                        }
                    });
                } catch (Exception e) {
                    displayerView.error("Export to Excel failed", e);
                }
            }
        };
    }

    protected DataSetLookup getConstrainedDataSetLookup(DataSetLookup dataSetLookup) {
        DataSetLookup _dataSetLookup = dataSetLookup.cloneInstance();
        if (dataSetLookup.getNumberOfRows() > 0) {
            // TODO: ask the user ....
            DataSetMetadata metadata = DataSetClientServices.get().getMetadata(dataSetLookup.getDataSetUUID());
            if (metadata.getNumberOfRows() > MAX_EXPORT_LIMIT) {
                Window.alert(Constants.INSTANCE.displayer_presenter_export_large_dataset());
            }
            _dataSetLookup.setRowOffset(0);
            _dataSetLookup.setNumberOfRows(MAX_EXPORT_LIMIT);
        }
        return _dataSetLookup;
    }

    protected void updateDisplayer(DisplayerSettings settings) {
        this.removeDisplayer();

        this.displayerSettings = settings;
        this.displayerView.setDisplayerSettings(settings);

        Displayer newDisplayer = this.displayerView.draw();
        this.perspectiveCoordinator.addDisplayer(newDisplayer);
    }

    protected void removeDisplayer() {
        Displayer displayer = displayerView.getDisplayer();
        perspectiveCoordinator.removeDisplayer( displayer );
        displayer.close();
    }

    protected PlaceRequest createPlaceRequest(DisplayerSettings displayerSettings) {
        String json = jsonMarshaller.toJsonString(displayerSettings);
        Map<String,String> params = new HashMap<String, String>();
        params.put("json", json);
        return new DefaultPlaceRequest("DisplayerScreen", params);
    }
}
