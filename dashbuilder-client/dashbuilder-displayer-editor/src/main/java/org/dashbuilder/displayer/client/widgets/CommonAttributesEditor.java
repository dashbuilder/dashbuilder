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
package org.dashbuilder.displayer.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ListBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.displayer.DataDisplayer;
import org.dashbuilder.displayer.DataDisplayerColumn;
import org.dashbuilder.displayer.client.AbstractDisplayerEditor;
import org.dashbuilder.displayer.impl.DataDisplayerColumnImpl;

public class CommonAttributesEditor extends AbstractDisplayerEditor {

    interface CommonEditorAttributesUIBinder extends UiBinder<Widget, CommonAttributesEditor> {}
    private static final CommonEditorAttributesUIBinder uiBinder = GWT.create( CommonEditorAttributesUIBinder.class );

    @UiField
    CheckBox showTitleCheckbox;

    @UiField
    TextBox titleTextBox;

    // TODO
    @UiField
    ListBox rendererListBox;

    // TODO falta una implementación un poco más elaborada que permita poner ids + aliases de columnas
    @UiField
    TextBox columnsTextBox;

    public CommonAttributesEditor() {
        // Init the editor from the UI Binder template
        initWidget( uiBinder.createAndBindUi( this ) );

//        rendererListBox.addChangeHandler( new ChangeHandler() {
//            @Override
//            public void onChange(ChangeEvent event ) {
//                handleRendererChanged( event );
//            }
//        } );
    }

    @Override
    public void setDataDisplayer( DataDisplayer dataDisplayer ) {
        super.setDataDisplayer( dataDisplayer );
        init();
    }

    protected void init() {
        showTitleCheckbox.setValue( dataDisplayer.isTitleVisible() );
        titleTextBox.setText( dataDisplayer.getTitle() );
        columnsTextBox.setText( formatColumns( dataDisplayer.getColumnList() ) );
    }

    @UiHandler( "showTitleCheckbox" )
    void handleShowTitleCheckboxClicked( ClickEvent e ) {
        dataDisplayer.setTitleVisible( showTitleCheckbox.getValue() );
        if ( showTitleCheckbox.getValue() ) titleTextBox.setEnabled( true );
        else titleTextBox.setEnabled( false );
        notifyChanges();
    }

    @UiHandler( "titleTextBox" )
    void handleTitleChanged( final ValueChangeEvent<String> event ) {
        if ( titleTextBox.getValue() != null ) {
            dataDisplayer.setTitle( titleTextBox.getValue() );
        }
        notifyChanges();
    }

//    void handleRendererChanged( ChangeEvent event ) {
//        notifyChanges();
//    }

    @UiHandler( "columnsTextBox" )
    void handleColumnsChanged( final ValueChangeEvent<String> event ) {
        dataDisplayer.getColumnList().clear();
        dataDisplayer.getColumnList().addAll( parseColumns( columnsTextBox.getText() ) );
        notifyChanges();
    }

    private List<DataDisplayerColumn> parseColumns( String columns ) {
        if ( columns.length() > 0) {
            String[] sa = columns.split( "," );
            List<DataDisplayerColumn> l = new ArrayList<DataDisplayerColumn>( sa.length );
            for ( int i = 0; i < sa.length; i++ ) {
                DataDisplayerColumnImpl ddci = new DataDisplayerColumnImpl();
                String[] idAlias = sa[i].trim().split( ":" );
                if ( idAlias.length == 2 ) {
                    if ( StringUtils.isBlank( idAlias[0] ) && StringUtils.isBlank( idAlias[1] ) )
                        throw new IllegalArgumentException( "You must specify at least a column alias." );

                    if ( !StringUtils.isBlank( idAlias[1] ) ) {
                        ddci.setDisplayName( idAlias[ 1 ].trim() );
                    } else ddci.setDisplayName( idAlias[0].trim() );

                    if ( !StringUtils.isBlank( idAlias[0] ) ) ddci.setColumnId( idAlias[0].trim() );

                } else {
                    if ( !StringUtils.isBlank( idAlias[0] ) ) ddci.setDisplayName( idAlias[0].trim() );
                    else throw new IllegalArgumentException( "You must specify at least a column alias." );
                }
                l.add( ddci );
            }
            return l;
        }
        return new ArrayList<DataDisplayerColumn>();
    }

    private String formatColumns( List<DataDisplayerColumn> columns ) {
        StringBuilder sb = new StringBuilder( "" );
        if ( columns != null ) {
            for ( int i = 0; i < columns.size(); i++ ) {
                String columnId = columns.get( i ).getColumnId();
                if ( !StringUtils.isBlank( columnId ) ) {
                    sb.append( columnId ).append( ":" );
                }
                sb.append( columns.get( i ).getDisplayName() );
                if ( i != columns.size() -1 ) sb.append( "," );
            }
        }
        return sb.toString();
    }
}
