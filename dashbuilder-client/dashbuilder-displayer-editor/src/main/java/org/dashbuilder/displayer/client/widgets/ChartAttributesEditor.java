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
import com.github.gwtbootstrap.client.ui.ListBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.Position;
import org.dashbuilder.displayer.client.resources.i18n.DisplayerSettingsEditorConstants;

public class ChartAttributesEditor extends Composite {

    interface ChartAttributesEditorUIBinder extends UiBinder<Widget, ChartAttributesEditor> {}
    private static final ChartAttributesEditorUIBinder uiBinder = GWT.create( ChartAttributesEditorUIBinder.class );

    @UiField
    IntegerBox chartWidthIntegerBox;

    @UiField
    IntegerBox chartHeightIntegerBox;

    @UiField
    IntegerBox chartTopMarginIntegerBox;

    @UiField
    IntegerBox chartBottomMarginIntegerBox;

    @UiField
    IntegerBox chartLeftMarginIntegerBox;

    @UiField
    IntegerBox chartRightMarginIntegerBox;

    @UiField
    CheckBox chartShowLegendCheckBox;

    @UiField
    ListBox chartLegendPositionListBox;

    public ChartAttributesEditor() {
        // Init the editor from the UI Binder template
        initWidget( uiBinder.createAndBindUi( this ) );
        initPositionList();
    }

    public void addChartWidthChangeHandler( ValueChangeHandler<Integer> valueChangeHandler ) {
        chartWidthIntegerBox.addValueChangeHandler( valueChangeHandler );
    }

    public void addChartHeightChangeHandler( ValueChangeHandler<Integer> valueChangeHandler ) {
        chartHeightIntegerBox.addValueChangeHandler( valueChangeHandler );
    }

    public void addChartTopMarginChangeHandler( ValueChangeHandler<Integer> valueChangeHandler ) {
        chartTopMarginIntegerBox.addValueChangeHandler( valueChangeHandler );
    }

    public void addChartBottomMarginChangeHandler( ValueChangeHandler<Integer> valueChangeHandler ) {
        chartBottomMarginIntegerBox.addValueChangeHandler( valueChangeHandler );
    }

    public void addChartLeftMarginChangeHandler( ValueChangeHandler<Integer> valueChangeHandler ) {
        chartLeftMarginIntegerBox.addValueChangeHandler( valueChangeHandler );
    }

    public void addChartRightMarginChangeHandler( ValueChangeHandler<Integer> valueChangeHandler ) {
        chartRightMarginIntegerBox.addValueChangeHandler( valueChangeHandler );
    }

    public void addShowLegendChangeHandler( ValueChangeHandler<Boolean> valueChangeHandler ) {
        chartShowLegendCheckBox.addValueChangeHandler( valueChangeHandler );
    }

    public void addLegendPositionChangeHandler( ChangeHandler changeHandler ) {
        chartLegendPositionListBox.addChangeHandler( changeHandler );
    }

    public void setChartWidth( int _chartWidth ) {
        chartWidthIntegerBox.setValue( _chartWidth );
    }

    public void setChartHeight( int _chartHeight ) {
        chartHeightIntegerBox.setValue( _chartHeight );
    }

    public void setChartTopMargin( int _chartTopMargin ) {
        chartTopMarginIntegerBox.setValue( _chartTopMargin );
    }

    public void setChartBottomMargin( int _chartBottomMargin ) {
        chartBottomMarginIntegerBox.setValue( _chartBottomMargin );
    }

    public void setChartLeftMargin( int _chartLeftMargin ) {
        chartLeftMarginIntegerBox.setValue( _chartLeftMargin );
    }

    public void setChartRightMargin( int _chartRightMargin ) {
        chartRightMarginIntegerBox.setValue( _chartRightMargin );
    }

    public void setChartShowLegend( boolean showLegend ) {
        chartShowLegendCheckBox.setValue( showLegend );
    }

    public void setChartLegendPosition( Position legendPosition ) {
        chartLegendPositionListBox.setSelectedValue( legendPosition.toString() );
    }

    protected void initPositionList() {
        chartLegendPositionListBox.clear();
        for ( Position position : Position.values()) {
            String positionKey = position.toString();
            String positionLabel = DisplayerSettingsEditorConstants.INSTANCE.getString( positionKey );
            chartLegendPositionListBox.addItem( positionLabel, positionKey );
        }
    }
}
