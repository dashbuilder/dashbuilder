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
package org.dashbuilder.displayer.client.widgets;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.RendererLibrary;
import org.dashbuilder.displayer.client.RendererManager;
import org.dashbuilder.displayer.client.resources.i18n.CommonConstants;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.TabContent;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.TabPanel;
import org.gwtbootstrap3.client.ui.constants.Toggle;

public class RendererSelector extends Composite {

    private static final String RENDERER_KEY = "renderer";

    interface RendererSelectorBinder extends UiBinder<Widget, RendererSelector> {

    }

    private static final RendererSelectorBinder uiBinder = GWT.create( RendererSelectorBinder.class );

    @UiField
    Panel mainPanel;

    @UiField
    Panel listPanel;

    @UiField
    Panel radioButtonsPanel;

    @UiField
    TabPanel tabPanel;

    @UiField
    TabContent tabContent;

    @UiField
    NavTabs navTabs;

    @UiField
    ListBox listBox;

    public RendererSelector( DisplayerSettings displayerSettings,
                             SelectorType selectorType,
                             final RendererSelectorEventHandler handler ) {

        // Init the widget with the view binding.
        initWidget( uiBinder.createAndBindUi( this ) );

        // Add the event handler.
        if ( handler != null ) {
            addHandler( handler, RendererSelectorEvent.TYPE );
        }

        RendererManager rendererManager = RendererManager.get();
        RendererLibrary rendererLibrary = rendererManager.getRendererForDisplayer( displayerSettings );
        List<RendererLibrary> renderers = rendererManager.getRenderersForType( displayerSettings.getType(), displayerSettings.getSubtype() );
        if ( renderers != null && renderers.size() > 1 ) {

            // Build the selector.
            switch ( selectorType ) {
                case LIST:
                    buildDropdown( renderers, rendererLibrary );
                    break;
                case RADIO:
                    buildRadioList( renderers, rendererLibrary );
                    break;
                case TAB:
                    buildTabList( renderers, rendererLibrary );
                    break;
            }

        }
        // If there is only one renderer in the list, do not show the selector.
        else {
            listPanel.setVisible( false );
            radioButtonsPanel.setVisible( false );
            mainPanel.setVisible( false );
        }
    }

    public String getTitle() {
        return CommonConstants.INSTANCE.renderer_selector_title();
    }

    private void buildDropdown( List<RendererLibrary> renderers,
                                RendererLibrary currentLib ) {

        listBox.clear();
        listPanel.setVisible( true );
        radioButtonsPanel.setVisible( false );
        tabPanel.setVisible( false );

        // Add listbox contents.
        int index = 0;
        int selectedIndex = 0;
        for ( RendererLibrary rendererLib : renderers ) {
            if ( currentLib != null && rendererLib.equals( currentLib ) ) {
                selectedIndex = index;
            }
            listBox.addItem( rendererLib.getName() );
            index++;
        }
        listBox.setSelectedIndex( selectedIndex );

        // The click event handler.
        listBox.addChangeHandler( new ChangeHandler() {
            public void onChange( ChangeEvent event ) {
                int index = listBox.getSelectedIndex();
                String value = listBox.getValue( index );
                RendererLibrary lib = RendererManager.get().getRendererByName( value );
                fireEvent( new RendererSelectorEvent( lib.getUUID() ) );
            }
        } );

    }

    private void buildRadioList( List<RendererLibrary> renderers,
                                 RendererLibrary currentLib ) {

        radioButtonsPanel.setVisible( true );
        listPanel.setVisible( false );
        tabPanel.setVisible( false );

        for ( RendererLibrary rendererLib : renderers ) {
            final RadioButton rb = new RadioButton( RENDERER_KEY, rendererLib.getName() );
            rb.setValue( rendererLib.equals( currentLib ) );
            rb.addClickHandler( new ClickHandler() {
                public void onClick( ClickEvent event ) {
                    String value = rb.getText();
                    RendererLibrary lib = RendererManager.get().getRendererByName( value );
                    fireEvent( new RendererSelectorEvent( lib.getUUID() ) );
                }
            } );
            radioButtonsPanel.add( rb );
            if ( currentLib != null && rendererLib.equals( currentLib ) ) {
                rb.setValue( true );
            }
        }
    }

    private void buildTabList( List<RendererLibrary> renderers,
                               RendererLibrary currentLib ) {

        radioButtonsPanel.setVisible( false );
        listPanel.setVisible( false );
        tabContent.clear();
        navTabs.clear();
        tabPanel.setVisible( true );

        for ( RendererLibrary rendererLib : renderers ) {
            final TabPane pane = new TabPane();
            final TabListItem tabListItem = new TabListItem();

            tabListItem.setDataTargetWidget( pane );
            tabListItem.setText( rendererLib.getName() );
            tabListItem.addClickHandler( new ClickHandler() {
                public void onClick( ClickEvent event ) {
                    final String value = tabListItem.getText().trim();
                    final RendererLibrary lib = RendererManager.get().getRendererByName( value );
                    fireEvent( new RendererSelectorEvent( lib.getUUID() ) );
                }
            } );

            tabContent.add( pane );
            navTabs.add( tabListItem );

            if ( currentLib != null && rendererLib.equals( currentLib ) ) {
                tabListItem.setActive( true );
                tabListItem.showTab();
            }
        }
    }

    @Override
    public void setWidth( String width ) {
        super.setWidth( width );
        mainPanel.setWidth( width );
        listPanel.setWidth( width );
        radioButtonsPanel.setWidth( width );
        tabPanel.setWidth( width );
    }

    @Override
    public void setHeight( String height ) {
        super.setHeight( height );
        mainPanel.setHeight( height );
        listPanel.setHeight( height );
        radioButtonsPanel.setHeight( height );
        tabPanel.setHeight( height );
    }

    /**
     * The callback then a renderer is selected.
     */
    public static class RendererSelectorEvent extends GwtEvent<RendererSelectorEventHandler> {

        public static Type<RendererSelectorEventHandler> TYPE = new Type<RendererSelectorEventHandler>();

        private String renderer;

        public RendererSelectorEvent( String renderer ) {
            this.renderer = renderer;
        }

        @Override
        public Type<RendererSelectorEventHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch( RendererSelectorEventHandler handler ) {
            handler.onRendererSelected( this );
        }

        public String getRenderer() {
            return renderer;
        }
    }

    public interface RendererSelectorEventHandler extends EventHandler {

        void onRendererSelected( RendererSelectorEvent event );
    }

    public enum SelectorType {
        LIST, RADIO, TAB;
    }
}