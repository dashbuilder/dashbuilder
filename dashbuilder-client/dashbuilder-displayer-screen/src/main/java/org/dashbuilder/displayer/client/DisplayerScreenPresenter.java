/**
 * Copyright (C) 2014 JBoss Inc
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.displayer.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.common.client.error.ClientRuntimeError;
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
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PerspectiveManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.PanelManager;
import org.uberfire.client.workbench.events.ChangeTitleWidgetEvent;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.model.menu.impl.BaseMenuCustom;

@WorkbenchScreen(identifier = "DisplayerScreen")
@Dependent
public class DisplayerScreenPresenter {

    @Inject
    private DataSetClientServices dataSetClientServices;

    private DisplayerView displayerView;
    private DisplayerEditorPopup displayerEditor;
    private PerspectiveCoordinator perspectiveCoordinator;
    private PerspectiveManager perspectiveManager;
    private PanelManager panelManager;
    private DisplayerSettingsJSONMarshaller jsonMarshaller;
    private DisplayerSettings displayerSettings;
    private PlaceManager placeManager;
    private UUIDGenerator uuidGenerator;

    private PlaceRequest placeRequest;
    private Menus menu = null;
    private boolean editEnabled = false;
    private boolean cloneEnabled = false;
    private boolean csvExportAllowed = false;
    private boolean excelExportAllowed = false;

    private ButtonGroup menuActionsButton;

    // TODO allow configuration of this through a custom system property?
    private static final int MAX_EXPORT_LIMIT = 100000;

    @Inject
    private Event<ChangeTitleWidgetEvent> changeTitleEvent;

    @Inject
    public DisplayerScreenPresenter(UUIDGenerator uuidGenerator,
            PerspectiveManager perspectiveManager,
            PlaceManager placeManager,
            DisplayerView displayerView,
            PanelManager panelManager,
            DisplayerEditorPopup displayerEditor,
            PerspectiveCoordinator perspectiveCoordinator,
            DisplayerSettingsJSONMarshaller jsonMarshaller) {

        this.uuidGenerator = uuidGenerator;
        this.placeManager = placeManager;
        this.perspectiveManager = perspectiveManager;
        this.displayerView = displayerView;
        this.panelManager = panelManager;
        this.perspectiveCoordinator = perspectiveCoordinator;
        this.jsonMarshaller = jsonMarshaller;
        this.displayerEditor =  displayerEditor;
        this.menuActionsButton = getMenuActionsButton();
    }

    @OnStartup
    public void onStartup( final PlaceRequest placeRequest ) {
        this.placeRequest = placeRequest;
        String json = placeRequest.getParameter( "json", "" );
        if ( !StringUtils.isBlank( json ) ) {
            this.displayerSettings = jsonMarshaller.fromJsonString( json );
        }
        if ( displayerSettings == null ) {
            throw new IllegalArgumentException( Constants.INSTANCE.displayer_presenter_displayer_notfound() );
        }

        // Check if display renderer selector component.
        Boolean showRendererSelector = Boolean.parseBoolean(placeRequest.getParameter("showRendererSelector","false"));
        displayerView.setIsShowRendererSelector(showRendererSelector);

        // Draw the Displayer.
        if ( StringUtils.isBlank( displayerSettings.getUUID() ) ) {
            displayerSettings.setUUID( uuidGenerator.newUuid() );
        }
        displayerView.setDisplayerSettings( displayerSettings );
        Displayer displayer = displayerView.draw();

        // Register the Displayer into the coordinator.
        perspectiveCoordinator.addDisplayer( displayer );

        // Check edit mode
        String edit = placeRequest.getParameter( "edit", "false" );
        String clone = placeRequest.getParameter( "clone", "false" );
        editEnabled = Boolean.parseBoolean( edit );
        cloneEnabled = Boolean.parseBoolean( clone );
        csvExportAllowed = displayerSettings.isCSVExportAllowed();
        excelExportAllowed = displayerSettings.isExcelExportAllowed();
        this.menu = makeMenuBar();
        adjustMenuActions(this.displayerSettings);
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
    public Menus getMenu() {
        return menu;
    }

    private Menus makeMenuBar() {
        return MenuFactory
                .newTopLevelCustomMenu( new MenuFactory.CustomMenuBuilder() {
                    @Override
                    public void push( MenuFactory.CustomMenuBuilder element ) {
                    }

                    @Override
                    public MenuItem build() {
                        return new BaseMenuCustom<IsWidget>() {
                            @Override
                            public IsWidget build() {
                                return menuActionsButton;
                            }

                            @Override
                            public boolean isEnabled() {
                                return editEnabled || cloneEnabled || csvExportAllowed || excelExportAllowed;
                            }

                            @Override
                            public void setEnabled( boolean enabled ) {
                            }

                            @Override
                            public Collection<String> getRoles() {
                                return null;
                            }

                            @Override
                            public String getSignatureId() {
                                return null;
                            }

                        };
                    }
                } ).endMenu()
                .build();
    }

    private Command getEditCommand() {
        return new Command() {
            public void execute() {
                perspectiveCoordinator.editOn();

                final String currentTitle = displayerSettings.getTitle();
                displayerEditor.init(displayerSettings.cloneInstance(), new DisplayerEditor.Listener() {

                    public void onClose( DisplayerEditor editor ) {
                        perspectiveCoordinator.editOff();
                    }

                    public void onSave( final DisplayerEditor editor ) {
                        perspectiveCoordinator.editOff();

                        DisplayerSettings newSettings = editor.getDisplayerSettings();

                        if (!displayerSettings.equals(newSettings)) {

                            String newTitle = newSettings.getTitle();
                            if (!currentTitle.equals(newTitle)) {
                                changeTitleEvent.fire(new ChangeTitleWidgetEvent(placeRequest, newSettings.getTitle()));
                            }

                            PanelDefinition panelDefinition = panelManager.getPanelForPlace(placeRequest);
                            placeManager.goTo(createPlaceRequest(newSettings), panelDefinition);
                            placeManager.closePlace(placeRequest);
                            perspectiveManager.savePerspectiveState(new Command() {
                                public void execute() {
                                }
                            });
                        }

                    }
                } );
            }
        };
    }

    private Command getCloneCommand() {
        return new Command() {
            public void execute() {
                perspectiveCoordinator.editOn();

                DisplayerSettings clonedSettings = displayerSettings.cloneInstance();
                clonedSettings.setUUID( uuidGenerator.newUuid() );
                clonedSettings.setTitle( "Copy of " + clonedSettings.getTitle() );

                displayerEditor.init(clonedSettings, new DisplayerEditor.Listener() {

                    public void onClose( DisplayerEditor editor ) {
                        perspectiveCoordinator.editOff();
                    }

                    public void onSave( final DisplayerEditor editor ) {
                        perspectiveCoordinator.editOff();
                        PanelDefinition panelDefinition = panelManager.getPanelForPlace( placeRequest );
                        placeManager.goTo( createPlaceRequest( editor.getDisplayerSettings() ), panelDefinition );
                        perspectiveManager.savePerspectiveState( new Command() {
                            public void execute() {
                            }
                        } );
                    }
                } );
            }
        };
    }

    private Command getExportCsvCommand() {
        return new Command() {
            public void execute() {
                try {
                    // Get all the data set rows with a maximum of 10000
                    DataSetLookup currentLookup = getConstrainedDataSetLookup(displayerView.getDisplayer().getDataSetHandler().getCurrentDataSetLookup());
                    dataSetClientServices.exportDataSetCSV(currentLookup, new DataSetExportReadyCallback() {
                        @Override
                        public void exportReady(Path exportFilePath) {
                            final String u = DataSetClientServices.get().getDownloadFileUrl(exportFilePath);
                            Window.open(u,
                                        "downloading",
                                        "resizable=no,scrollbars=yes,status=no");
                        }
                        @Override
                        public void onError(ClientRuntimeError error) {
                            displayerView.error(error);
                        }
                    });
                } catch (Exception e) {
                    displayerView.error(new ClientRuntimeError(e));
                }
            }
        };
    }

    private Command getExportExcelCommand() {
        return new Command() {
            public void execute() {
                try {
                    // Get all the data set rows with a maximum of 10000
                    DataSetLookup currentLookup = getConstrainedDataSetLookup(displayerView.getDisplayer().getDataSetHandler().getCurrentDataSetLookup());
                    dataSetClientServices.exportDataSetExcel(currentLookup, new DataSetExportReadyCallback() {
                        @Override
                        public void exportReady(Path exportFilePath) {
                            final String u = DataSetClientServices.get().getDownloadFileUrl(exportFilePath);
                            Window.open(u,
                                        "downloading",
                                        "resizable=no,scrollbars=yes,status=no");
                        }
                        @Override
                        public void onError(ClientRuntimeError error) {
                            displayerView.error(error);
                        }
                    });
                } catch (Exception e) {
                    displayerView.error(new ClientRuntimeError(e));
                }
            }
        };
    }

    private DataSetLookup getConstrainedDataSetLookup( DataSetLookup dataSetLookup ) {
        DataSetLookup _dataSetLookup = dataSetLookup.cloneInstance();
        if ( dataSetLookup.getNumberOfRows() > 0 ) {
            // TODO: ask the user ....
            DataSetMetadata metadata = DataSetClientServices.get().getMetadata( dataSetLookup.getDataSetUUID() );
            if ( metadata.getNumberOfRows() > MAX_EXPORT_LIMIT ) {
                Window.alert( Constants.INSTANCE.displayer_presenter_export_large_dataset() );
            }
            _dataSetLookup.setRowOffset( 0 );
            _dataSetLookup.setNumberOfRows( MAX_EXPORT_LIMIT );
        }
        return _dataSetLookup;
    }

    protected void removeDisplayer() {
        Displayer displayer = displayerView.getDisplayer();
        perspectiveCoordinator.removeDisplayer( displayer );
        displayer.close();
    }

    private PlaceRequest createPlaceRequest( DisplayerSettings displayerSettings ) {
        String json = jsonMarshaller.toJsonString( displayerSettings );
        Map<String, String> params = new HashMap<String, String>();
        params.put( "json", json );
        params.put( "edit", "true" );
        params.put( "clone", "true" );
        return new DefaultPlaceRequest( "DisplayerScreen", params );
    }

    private void adjustMenuActions( DisplayerSettings displayerSettings ) {
        final ComplexPanel menu = (ComplexPanel) menuActionsButton.getWidget( 1 );
        menu.getWidget( 2 ).setVisible( displayerSettings.isCSVExportAllowed() );
        menu.getWidget( 3 ).setVisible( displayerSettings.isExcelExportAllowed() );
    }

    private ButtonGroup getMenuActionsButton() {
        return new ButtonGroup() {{
            add( new Button( Constants.INSTANCE.menu_button_actions() ) {{
                setSize( ButtonSize.EXTRA_SMALL );
                addStyleName( Pull.RIGHT.getCssName() );
                setDataToggle(Toggle.DROPDOWN);
            }} );
            add( new DropDownMenu() {{
                add( new AnchorListItem( Constants.INSTANCE.menu_edit() ) {{
                    addClickHandler( new ClickHandler() {
                        @Override
                        public void onClick( ClickEvent clickEvent ) {
                            getEditCommand().execute();
                        }
                    } );
                }} );
                add( new AnchorListItem( Constants.INSTANCE.menu_clone() ) {{
                    addClickHandler( new ClickHandler() {
                        @Override
                        public void onClick( ClickEvent clickEvent ) {
                            getCloneCommand().execute();
                        }
                    } );
                }} );
                add( new AnchorListItem( Constants.INSTANCE.menu_export_csv() ) {{
                    addClickHandler( new ClickHandler() {
                        @Override
                        public void onClick( ClickEvent clickEvent ) {
                            getExportCsvCommand().execute();
                        }
                    } );
                }} );
                add( new AnchorListItem( Constants.INSTANCE.menu_export_excel() ) {{
                    addClickHandler( new ClickHandler() {
                        @Override
                        public void onClick( ClickEvent clickEvent ) {
                            getExportExcelCommand().execute();
                        }
                    } );
                }} );
            }} );
        }};
    }
}
