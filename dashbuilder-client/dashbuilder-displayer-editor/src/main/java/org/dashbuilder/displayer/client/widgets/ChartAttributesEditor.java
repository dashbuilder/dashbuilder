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

import com.github.gwtbootstrap.client.ui.IntegerBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ChartAttributesEditor extends Composite {

    interface ChartAttributesEditorUIBinder extends UiBinder<Widget, ChartAttributesEditor> {}
    private static final ChartAttributesEditorUIBinder uiBinder = GWT.create( ChartAttributesEditorUIBinder.class );

    @UiField
    IntegerBox chartWidth;

    @UiField
    IntegerBox chartHeight;

    @UiField
    IntegerBox chartTopMargin;

    @UiField
    IntegerBox chartBottomMargin;

    @UiField
    IntegerBox chartLeftMargin;

    @UiField
    IntegerBox chartRightMargin;

    public ChartAttributesEditor() {
        // Init the editor from the UI Binder template
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    public void addChartWidthChangeHandler( ValueChangeHandler<Integer> valueChangeHandler ) {
        chartWidth.addValueChangeHandler( valueChangeHandler );
    }

    public void addChartHeightChangeHandler( ValueChangeHandler<Integer> valueChangeHandler ) {
        chartHeight.addValueChangeHandler( valueChangeHandler );
    }

    public void addChartTopMarginChangeHandler( ValueChangeHandler<Integer> valueChangeHandler ) {
        chartTopMargin.addValueChangeHandler( valueChangeHandler );
    }

    public void addChartBottomMarginChangeHandler( ValueChangeHandler<Integer> valueChangeHandler ) {
        chartBottomMargin.addValueChangeHandler( valueChangeHandler );
    }

    public void addChartLeftMarginChangeHandler( ValueChangeHandler<Integer> valueChangeHandler ) {
        chartLeftMargin.addValueChangeHandler( valueChangeHandler );
    }

    public void addChartRightMarginChangeHandler( ValueChangeHandler<Integer> valueChangeHandler ) {
        chartRightMargin.addValueChangeHandler( valueChangeHandler );
    }

    public void setChartWidth( int _chartWidth ) {
        chartWidth.setValue( _chartWidth );
    }

    public void setChartHeight( int _chartHeight ) {
        chartHeight.setValue( _chartHeight );
    }

    public void setChartTopMargin( int _chartTopMargin ) {
        chartTopMargin.setValue( _chartTopMargin );
    }

    public void setChartBottomMargin( int _chartBottomMargin ) {
        chartBottomMargin.setValue( _chartBottomMargin );
    }

    public void setChartLeftMargin( int _chartLeftMargin ) {
        chartLeftMargin.setValue( _chartLeftMargin );
    }

    public void setChartRightMargin( int _chartRightMargin ) {
        chartRightMargin.setValue( _chartRightMargin );
    }
}
