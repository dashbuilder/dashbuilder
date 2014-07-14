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

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ListBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class CommonAttributesEditor extends Composite {

    interface CommonAttributesEditorUIBinder extends UiBinder<Widget, CommonAttributesEditor> {}
    private static final CommonAttributesEditorUIBinder uiBinder = GWT.create( CommonAttributesEditorUIBinder.class );

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
    }

    public void addShowTitleChangeHandler( ValueChangeHandler<Boolean> valueChangeHandler ) {
        showTitleCheckbox.addValueChangeHandler( valueChangeHandler );
    }

    public void addTitleChangeHandler( ValueChangeHandler<String> valueChangeHandler ) {
        titleTextBox.addValueChangeHandler( valueChangeHandler );
    }

    public void addColumnsChangeHandler( ValueChangeHandler<String> valueChangeHandler ) {
        columnsTextBox.addValueChangeHandler( valueChangeHandler );
    }

    public void setIsTitleVisible( Boolean isTitleVisible ) {
        showTitleCheckbox.setValue( isTitleVisible );
    }

    public void setTitle( String title ) {
        titleTextBox.setText( title );
    }

    public void setColumns( String columns ) {
        columnsTextBox.setText( columns );
    }

    @UiHandler( "showTitleCheckbox" )
    void handleShowTitleCheckboxClicked( ClickEvent e ) {
        if ( showTitleCheckbox.getValue() ) titleTextBox.setEnabled( true );
        else titleTextBox.setEnabled( false );
    }
}
