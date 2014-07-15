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
import com.github.gwtbootstrap.client.ui.IntegerBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class XAxisChartAttributesEditor extends Composite {

    interface XAxisChartAttributesEditorUIBinder extends UiBinder<Widget, XAxisChartAttributesEditor> {}
    private static final XAxisChartAttributesEditorUIBinder uiBinder = GWT.create( XAxisChartAttributesEditorUIBinder.class );

    @UiField
    CheckBox xaxisShowLabelsCheckBox;

    @UiField
    IntegerBox xaxisAngleIntegerBox;

    @UiField
    TextBox xaxisTitleTextBox;

    public XAxisChartAttributesEditor() {
        // Init the editor from the UI Binder template
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    public void addXAxisShowLabelsChangeHandler( ValueChangeHandler<Boolean> valueChangeHandler ) {
        xaxisShowLabelsCheckBox.addValueChangeHandler( valueChangeHandler );
    }

    public void addXAxisAngleChangeHandler( ValueChangeHandler<Integer> valueChangeHandler ) {
        xaxisAngleIntegerBox.addValueChangeHandler( valueChangeHandler );
    }

    public void addXAxisTitleChangeHandler( ValueChangeHandler<String> valueChangeHandler ) {
        xaxisTitleTextBox.addValueChangeHandler( valueChangeHandler );
    }

    public void setXaxisShowLabels( Boolean showLabels ) {
        xaxisShowLabelsCheckBox.setValue( showLabels );
    }

    public void setXaxisLabelsAngle( Integer angle ) {
        xaxisAngleIntegerBox.setValue( angle );
    }

    public void setXaxisTitle( String title ) {
        xaxisTitleTextBox.setText( title );
    }
}
