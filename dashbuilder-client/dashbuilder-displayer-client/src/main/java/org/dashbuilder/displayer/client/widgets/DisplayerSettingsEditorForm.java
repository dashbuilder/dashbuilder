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

import java.util.Set;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.IntegerBox;
import com.github.gwtbootstrap.client.ui.ListBox;
import com.github.gwtbootstrap.client.ui.LongBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.SpacerWidget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerEditorConfig;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsColumn;
import org.dashbuilder.displayer.Position;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.displayer.client.RendererLibLocator;
import org.dashbuilder.displayer.client.resources.i18n.DisplayerSettingsEditorConstants;
import org.dashbuilder.displayer.impl.DisplayerSettingsColumnImpl;

public class DisplayerSettingsEditorForm extends Composite {

    public interface Presenter {
        void displayerSettingsChanged(DisplayerSettings settings);
    }

    interface SettingsEditorUIBinder extends UiBinder<Widget, DisplayerSettingsEditorForm> {}
    private static final SettingsEditorUIBinder uiBinder = GWT.create( SettingsEditorUIBinder.class );

    private String tableDefaultSortColumnId = null;
    private SortOrder tableDefaultSortOrder = null;

    private Label showTitleCheckboxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.common_showTitle() );
    private CheckBox showTitleCheckbox = new CheckBox();

    private Label titleTextBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.common_title() );
    private TextBox titleTextBox = new TextBox();

    private Label rendererListBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.common_renderer() );
    private ListBox rendererListBox = new ListBox( false );

    private Label columnsTextBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.common_columns() );
    private TextBox columnsTextBox = new TextBox();

    private Label chartWidthIntegerBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.chart_width() );
    private IntegerBox chartWidthIntegerBox = new IntegerBox();

    private Label chartHeightIntegerBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.chart_height() );
    private IntegerBox chartHeightIntegerBox = new IntegerBox();

    private Label chartTopMarginIntegerBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.chart_topMargin() );
    private IntegerBox chartTopMarginIntegerBox = new IntegerBox();

    private Label chartBottomMarginIntegerBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.chart_bottomMargin() );
    private IntegerBox chartBottomMarginIntegerBox = new IntegerBox();

    private Label chartLeftMarginIntegerBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.chart_leftMargin() );
    private IntegerBox chartLeftMarginIntegerBox = new IntegerBox();

    private Label chartRightMarginIntegerBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.chart_rightMargin() );
    private IntegerBox chartRightMarginIntegerBox = new IntegerBox();

    private Label chartShowLegendCheckBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.chart_legendShow() );
    private CheckBox chartShowLegendCheckBox = new CheckBox();

    private Label chartLegendPositionListBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.chart_legendPosition() );
    private ListBox chartLegendPositionListBox = new ListBox( false );

    private Label chart3DCheckboxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.chart_3d() );
    private CheckBox chart3DCheckbox = new CheckBox();

    private Label tablePageSizeIntegerBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.table_pageSize() );
    private IntegerBox tablePageSizeIntegerBox = new IntegerBox();

    private Label tableWidthIntegerBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.table_width() );
    private IntegerBox tableWidthIntegerBox = new IntegerBox();

    private Label tableSortEnabledCheckBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.table_sortEnabled() );
    private CheckBox tableSortEnabledCheckBox = new CheckBox();

    private Label tableDefaultSortColumnTextBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.table_sortColumn() );
    private TextBox tableDefaultSortColumnTextBox = new TextBox();

    private Label tableSortOrderLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.table_sortOrder() );
    private Label tableSortAscRadioLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.table_ascSortOrder() );
    private RadioButton tableSortAscRadio = new RadioButton("tableSort");
    private Label tableSortDescRadioLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.table_descSortOrder() );
    private RadioButton tableSortDescRadio = new RadioButton("tableSort");

    private Label xaxisShowLabelsCheckBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.xaxis_showLabels() );
    private CheckBox xaxisShowLabelsCheckBox = new CheckBox();

//    private Label xaxisAngleIntegerBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.xaxis_angle() );
//    private IntegerBox xaxisAngleIntegerBox = new IntegerBox();

    private Label xaxisTitleTextBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.xaxis_title() );
    private TextBox xaxisTitleTextBox = new TextBox();

    private Label yaxisShowLabelsCheckBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.yaxis_showLabels() );
    private CheckBox yaxisShowLabelsCheckBox = new CheckBox();

//    private Label yaxisAngleIntegerBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.yaxis_angle() );
//    private IntegerBox yaxisAngleIntegerBox = new IntegerBox();

    private Label yaxisTitleTextBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.yaxis_title() );
    private TextBox yaxisTitleTextBox = new TextBox();

    private Label meterStartLongBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.meter_start() );
    private LongBox meterStartLongBox = new LongBox();

    private Label meterWarningLongBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.meter_warning() );
    private LongBox meterWarningLongBox = new LongBox();

    private Label meterCriticalLongBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.meter_critical() );
    private LongBox meterCriticalLongBox = new LongBox();

    private Label meterEndLongBoxLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.meter_end() );
    private LongBox meterEndLongBox = new LongBox();

    private Label barChartOrientationLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.barchart_orientation() );
    private Label barChartHorizontalRadioLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.barchart_horizontal() );
    private RadioButton barChartHorizontalRadio = new RadioButton("barOrientation");
    private Label barChartVerticalRadioLabel = new Label( DisplayerSettingsEditorConstants.INSTANCE.barchart_vertical() );
    private RadioButton barChartVerticalRadio = new RadioButton("barOrientation");

    @UiField
    FlexTable editorSettingsTable;

    protected Presenter presenter;
    protected DisplayerSettings displayerSettings;
    protected DisplayerEditorConfig displayerEditorConfig;
    private Set<DisplayerAttributeDef> supportedAttributes;

    public DisplayerSettingsEditorForm() {

        // Init the editor from the UI Binder template
        initWidget( uiBinder.createAndBindUi( this ) );

//        showTitleCheckbox.setValue( true );
        showTitleCheckbox.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                boolean showTitle = showTitleCheckbox.getValue();
                titleTextBox.setEnabled( showTitle );
                displayerSettings.setTitleVisible( showTitle );
                notifyChanges();
            }
        } );

        titleTextBox.setPlaceholder( DisplayerSettingsEditorConstants.INSTANCE.common_title_placeholder() );
        titleTextBox.addValueChangeHandler( new ValueChangeHandler<String>() {
            @Override
            public void onValueChange( ValueChangeEvent<String> event ) {
                String title = event.getValue();
                if ( title != null ) {
                    displayerSettings.setTitle( title );
                    notifyChanges();
                }
            }
        } );

        rendererListBox.addChangeHandler( new ChangeHandler() {
            @Override
            public void onChange( ChangeEvent event ) {
                String selectedRenderer = ( ( ListBox ) event.getSource() ).getValue();
                displayerSettings.setRenderer( selectedRenderer );
                notifyChanges();
            }
        } );

        columnsTextBox.setPlaceholder( DisplayerSettingsEditorConstants.INSTANCE.common_columns_placeholder() );
        columnsTextBox.addValueChangeHandler( new ValueChangeHandler<String>() {
            @Override
            public void onValueChange( ValueChangeEvent<String> event ) {
                displayerSettings.setColumns( parseColumns( event.getValue() ) );
                notifyChanges();
            }
        } );

        chartWidthIntegerBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
        chartWidthIntegerBox.addValueChangeHandler( new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange( ValueChangeEvent<Integer> event ) {
                int width = displayerSettings.getChartWidth();
                if ( event.getValue() != null ) width = event.getValue();
                displayerSettings.setChartWidth( width );
                notifyChanges();
            }
        } );

        chartHeightIntegerBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
        chartHeightIntegerBox.addValueChangeHandler( new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange( ValueChangeEvent<Integer> event ) {
                int height = displayerSettings.getChartHeight();
                if ( event.getValue() != null ) height = event.getValue();
                displayerSettings.setChartHeight( height );
                notifyChanges();
            }
        } );

        chartTopMarginIntegerBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
        chartTopMarginIntegerBox.addValueChangeHandler( new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange( ValueChangeEvent<Integer> event ) {
                int topMargin = displayerSettings.getChartMarginTop();
                if ( event.getValue() != null ) topMargin = event.getValue();
                displayerSettings.setChartMarginTop( topMargin );
                notifyChanges();
            }
        } );

        chartBottomMarginIntegerBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
        chartBottomMarginIntegerBox.addValueChangeHandler( new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange( ValueChangeEvent<Integer> event ) {
                int bottomMargin = displayerSettings.getChartMarginBottom();
                if ( event.getValue() != null ) bottomMargin = event.getValue();
                displayerSettings.setChartMarginBottom( bottomMargin );
                notifyChanges();
            }
        } );

        chartLeftMarginIntegerBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
        chartLeftMarginIntegerBox.addValueChangeHandler( new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange( ValueChangeEvent<Integer> event ) {
                int leftMargin = displayerSettings.getChartMarginLeft();
                if ( event.getValue() != null ) leftMargin = event.getValue();
                displayerSettings.setChartMarginLeft( leftMargin );
                notifyChanges();
            }
        } );

        chartRightMarginIntegerBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
        chartRightMarginIntegerBox.addValueChangeHandler( new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange( ValueChangeEvent<Integer> event ) {
                int rightMargin = displayerSettings.getChartMarginRight();
                if ( event.getValue() != null ) rightMargin = event.getValue();
                displayerSettings.setChartMarginRight( rightMargin );
                notifyChanges();
            }
        } );

        chartShowLegendCheckBox.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                boolean showLegend = chartShowLegendCheckBox.getValue();
                chartLegendPositionListBox.setEnabled( showLegend );
                displayerSettings.setChartShowLegend( showLegend );
                notifyChanges();
            }
        } );

        initPositionList();
        chartLegendPositionListBox.addChangeHandler( new ChangeHandler() {
            @Override
            public void onChange( ChangeEvent event ) {
                // TODO try to uncouple the changehandler implementation from the underlying widget, in this case the listbox
                String selectedPosition = ( ( ListBox ) event.getSource() ).getValue();
                displayerSettings.setChartLegendPosition( Position.valueOf( selectedPosition ) );
                notifyChanges();
            }
        } );

        chart3DCheckbox.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                boolean _3d = chart3DCheckbox.getValue();
                displayerSettings.setChart3D( _3d );
                notifyChanges();
            }
        } );

        tablePageSizeIntegerBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
        tablePageSizeIntegerBox.addValueChangeHandler( new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange( ValueChangeEvent<Integer> event ) {
                int tablePageSize = displayerSettings.getTablePageSize();
                if ( event.getValue() != null ) tablePageSize = event.getValue();
                displayerSettings.setTablePageSize( tablePageSize );
                notifyChanges();
            }
        } );

        tableWidthIntegerBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
        tableWidthIntegerBox.addValueChangeHandler( new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange( ValueChangeEvent<Integer> event ) {
                int tableWidth = displayerSettings.getTableWidth();
                if ( event.getValue() != null ) tableWidth = event.getValue();
                displayerSettings.setTableWidth( tableWidth );
                notifyChanges();
            }
        } );

        tableSortEnabledCheckBox.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                boolean tableSortEnabled = tableSortEnabledCheckBox.getValue();
                tableDefaultSortColumnTextBox.setEnabled( tableSortEnabled );
                tableSortAscRadio.setEnabled( tableSortEnabled );
                tableSortDescRadio.setEnabled( tableSortEnabled );
                // reset values if table sort were disabled after possibly having changed the default sort column id or sort order.
                if ( !tableSortEnabled ) {
                    displayerSettings.setTableDefaultSortColumnId( tableDefaultSortColumnId );
                    displayerSettings.setTableDefaultSortOrder( tableDefaultSortOrder );
                }
                displayerSettings.setTableSortEnabled( tableSortEnabled );
                notifyChanges();
            }
        } );

        tableDefaultSortColumnTextBox.setPlaceholder( DisplayerSettingsEditorConstants.INSTANCE.table_sortColumn_placeholder() );
        tableDefaultSortColumnTextBox.addValueChangeHandler( new ValueChangeHandler<String>() {
            @Override
            public void onValueChange( ValueChangeEvent<String> event ) {
                String columnId = event.getValue();
                if ( !StringUtils.isBlank( columnId ) ) {
                    displayerSettings.setTableDefaultSortColumnId( columnId );
                    notifyChanges();
                }
            }
        } );

        tableSortAscRadio.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                boolean asc = tableSortAscRadio.getValue();
                displayerSettings.setTableDefaultSortOrder( asc ? SortOrder.ASCENDING : SortOrder.DESCENDING );
                notifyChanges();
            }
        } );

        tableSortDescRadio.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                boolean asc = !tableSortDescRadio.getValue();
                displayerSettings.setTableDefaultSortOrder( asc ? SortOrder.ASCENDING : SortOrder.DESCENDING );
                notifyChanges();
            }
        } );

        xaxisShowLabelsCheckBox.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                boolean showXAxisLabels = xaxisShowLabelsCheckBox.getValue();
                xaxisTitleTextBox.setEnabled( showXAxisLabels );
                displayerSettings.setXAxisShowLabels( showXAxisLabels );
                notifyChanges();
            }
        } );

        xaxisTitleTextBox.setPlaceholder( DisplayerSettingsEditorConstants.INSTANCE.xaxis_title_placeholder() );
        xaxisTitleTextBox.addValueChangeHandler( new ValueChangeHandler<String>() {
            @Override
            public void onValueChange( ValueChangeEvent<String> event ) {
                String title = event.getValue();
                if ( title != null ) {
                    displayerSettings.setXAxisTitle( title );
                    notifyChanges();
                }
            }
        } );

//        xaxisAngleIntegerBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
//        xaxisAngleIntegerBox.addValueChangeHandler( new ValueChangeHandler<Integer>() {
//            @Override
//            public void onValueChange( ValueChangeEvent<Integer> event ) {
//                int axisAngle = displayerSettings.getXAxisLabelsAngle();
//                if ( event.getValue() != null ) axisAngle = event.getValue();
//                displayerSettings.setXAxisLabelsAngle( axisAngle );
//                notifyChanges();
//            }
//        } );

        yaxisShowLabelsCheckBox.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                boolean showYAxisLabels = yaxisShowLabelsCheckBox.getValue();
                yaxisTitleTextBox.setEnabled( showYAxisLabels );
                displayerSettings.setYAxisShowLabels( showYAxisLabels );
                notifyChanges();
            }
        } );

        yaxisTitleTextBox.setPlaceholder( DisplayerSettingsEditorConstants.INSTANCE.yaxis_title_placeholder() );
        yaxisTitleTextBox.addValueChangeHandler( new ValueChangeHandler<String>() {
            @Override
            public void onValueChange( ValueChangeEvent<String> event ) {
                String title = event.getValue();
                if ( title != null ) {
                    displayerSettings.setYAxisTitle( title );
                    notifyChanges();
                }
            }
        } );

//        yaxisAngleIntegerBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
//        yaxisAngleIntegerBox.addValueChangeHandler( new ValueChangeHandler<Integer>() {
//            @Override
//            public void onValueChange( ValueChangeEvent<Integer> event ) {
//                int axisAngle = displayerSettings.getYAxisLabelsAngle();
//                if ( event.getValue() != null ) axisAngle = event.getValue();
//                displayerSettings.setYAxisLabelsAngle( axisAngle );
//                notifyChanges();
//            }
//        } );

        meterStartLongBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
        meterStartLongBox.addValueChangeHandler( new ValueChangeHandler<Long>() {
            @Override
            public void onValueChange( ValueChangeEvent<Long> event ) {
                long meterStart = displayerSettings.getMeterStart();
                if ( event.getValue() != null ) meterStart = event.getValue();
                displayerSettings.setMeterStart( meterStart );
                notifyChanges();
            }
        } );

        meterWarningLongBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
        meterWarningLongBox.addValueChangeHandler( new ValueChangeHandler<Long>() {
            @Override
            public void onValueChange( ValueChangeEvent<Long> event ) {
                long meterWarning = displayerSettings.getMeterWarning();
                if ( event.getValue() != null ) meterWarning = event.getValue();
                displayerSettings.setMeterWarning( meterWarning );
                notifyChanges();
            }
        } );

        meterCriticalLongBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
        meterCriticalLongBox.addValueChangeHandler( new ValueChangeHandler<Long>() {
            @Override
            public void onValueChange( ValueChangeEvent<Long> event ) {
                long meterCritical = displayerSettings.getMeterCritical();
                if ( event.getValue() != null ) meterCritical = event.getValue();
                displayerSettings.setMeterCritical( meterCritical );
                notifyChanges();
            }
        } );

        meterEndLongBox.setAlignment( ValueBoxBase.TextAlignment.RIGHT );
        meterEndLongBox.addValueChangeHandler( new ValueChangeHandler<Long>() {
            @Override
            public void onValueChange( ValueChangeEvent<Long> event ) {
                long meterEnd = displayerSettings.getMeterEnd();
                if ( event.getValue() != null ) meterEnd = event.getValue();
                displayerSettings.setMeterEnd( meterEnd );
                notifyChanges();
            }
        } );

        barChartHorizontalRadio.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                boolean horizontal = barChartHorizontalRadio.getValue();
                displayerSettings.setBarchartHorizontal( horizontal );
                notifyChanges();
            }
        } );

        barChartVerticalRadio.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                boolean vertical = barChartVerticalRadio.getValue();
                displayerSettings.setBarchartHorizontal( !vertical );
                notifyChanges();
            }
        } );
    }

    public DisplayerSettings getDisplayerSettings() {
        return displayerSettings;
    }

    public void init(DisplayerSettings displayerSettings, Presenter presenter) {
        this.displayerSettings = displayerSettings.cloneInstance();
        this.presenter = presenter;

        Displayer displayer = DisplayerHelper.lookupDisplayer(displayerSettings);
        this.displayerEditorConfig = displayer.getDisplayerEditorConfig();
        this.supportedAttributes = displayerEditorConfig.getSupportedAttributes();

        initRendererList();

        editorSettingsTable.clear();

        int rowCounter = 0;
        if ( supportedAttributes.contains( DisplayerAttributeDef.TITLE_VISIBLE ) ) {
            showTitleCheckbox.setValue( displayerSettings.isTitleVisible() );
            editorSettingsTable.setWidget( rowCounter, 0, showTitleCheckboxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, showTitleCheckbox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.TITLE ) ) {
            titleTextBox.setText( displayerSettings.getTitle() );
            editorSettingsTable.setWidget( rowCounter, 0, titleTextBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, titleTextBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.RENDERER ) ) {
            rendererListBox.setSelectedValue( displayerSettings.getRenderer() );
            editorSettingsTable.setWidget( rowCounter, 0, rendererListBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, rendererListBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.COLUMNS ) ) {
            columnsTextBox.setText( formatColumns( displayerSettings.getColumns() ) );
            editorSettingsTable.setWidget( rowCounter, 0, columnsTextBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, columnsTextBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.CHART_WIDTH ) ) {
            chartWidthIntegerBox.setValue( displayerSettings.getChartWidth() );
            editorSettingsTable.setWidget( rowCounter, 0, chartWidthIntegerBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, chartWidthIntegerBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.CHART_HEIGHT ) ) {
            chartHeightIntegerBox.setValue( displayerSettings.getChartHeight() );
            editorSettingsTable.setWidget( rowCounter, 0, chartHeightIntegerBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, chartHeightIntegerBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.CHART_MARGIN_TOP ) ) {
            chartTopMarginIntegerBox.setValue( displayerSettings.getChartMarginTop() );
            editorSettingsTable.setWidget( rowCounter, 0, chartTopMarginIntegerBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, chartTopMarginIntegerBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.CHART_MARGIN_BOTTOM ) ) {
            chartBottomMarginIntegerBox.setValue( displayerSettings.getChartMarginBottom() );
            editorSettingsTable.setWidget( rowCounter, 0, chartBottomMarginIntegerBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, chartBottomMarginIntegerBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.CHART_MARGIN_LEFT ) ) {
            chartLeftMarginIntegerBox.setValue( displayerSettings.getChartMarginLeft() );
            editorSettingsTable.setWidget( rowCounter, 0, chartLeftMarginIntegerBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, chartLeftMarginIntegerBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.CHART_MARGIN_RIGHT ) ) {
            chartRightMarginIntegerBox.setValue( displayerSettings.getChartMarginRight() );
            editorSettingsTable.setWidget( rowCounter, 0, chartRightMarginIntegerBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, chartRightMarginIntegerBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.CHART_SHOWLEGEND ) ) {
            chartShowLegendCheckBox.setValue( displayerSettings.isChartShowLegend() );
            editorSettingsTable.setWidget( rowCounter, 0, chartShowLegendCheckBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, chartShowLegendCheckBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.CHART_LEGENDPOSITION ) ) {
            chartLegendPositionListBox.setSelectedValue( displayerSettings.getChartLegendPosition().toString() );
            editorSettingsTable.setWidget( rowCounter, 0, chartLegendPositionListBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, chartLegendPositionListBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.CHART_3D ) ) {
            chart3DCheckbox.setValue( displayerSettings.isChart3D() );
            editorSettingsTable.setWidget( rowCounter, 0, chart3DCheckboxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, chart3DCheckbox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.TABLE_PAGESIZE ) ) {
            tablePageSizeIntegerBox.setValue( displayerSettings.getTablePageSize() );
            editorSettingsTable.setWidget( rowCounter, 0, tablePageSizeIntegerBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, tablePageSizeIntegerBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.TABLE_WIDTH ) ) {
            tableWidthIntegerBox.setValue( displayerSettings.getTableWidth() );
            editorSettingsTable.setWidget( rowCounter, 0, tableWidthIntegerBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, tableWidthIntegerBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.TABLE_SORTENABLED ) ) {
            tableSortEnabledCheckBox.setValue( displayerSettings.isTableSortEnabled() );
            editorSettingsTable.setWidget( rowCounter, 0, tableSortEnabledCheckBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, tableSortEnabledCheckBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.TABLE_SORTCOLUMNID ) ) {
            tableDefaultSortColumnId = displayerSettings.getTableDefaultSortColumnId();

            tableDefaultSortColumnTextBox.setText( displayerSettings.getTableDefaultSortColumnId() );
            editorSettingsTable.setWidget( rowCounter, 0, tableDefaultSortColumnTextBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, tableDefaultSortColumnTextBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.TABLE_SORTORDER ) ) {
            tableDefaultSortOrder = displayerSettings.getTableDefaultSortOrder();

            boolean ascending = SortOrder.ASCENDING.equals( tableDefaultSortOrder );

            tableSortAscRadio.setValue( ascending );
            tableSortDescRadio.setValue( !ascending );

            FlexTable barRadios = new FlexTable();
            barRadios.setWidget( 0, 0, tableSortAscRadioLabel );
            barRadios.setWidget( 0, 1, SpacerWidget.SINGLE );
            barRadios.setWidget( 0, 2, tableSortAscRadio );
            barRadios.setWidget( 1, 0, tableSortDescRadioLabel );
            barRadios.setWidget( 1, 1, SpacerWidget.SINGLE );
            barRadios.setWidget( 1, 2, tableSortDescRadio );

            editorSettingsTable.setWidget( rowCounter, 0, tableSortOrderLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, barRadios );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.XAXIS_SHOWLABELS ) ) {
            xaxisShowLabelsCheckBox.setValue( displayerSettings.isXAxisShowLabels() );
            editorSettingsTable.setWidget( rowCounter, 0, xaxisShowLabelsCheckBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, xaxisShowLabelsCheckBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.XAXIS_TITLE ) ) {
            xaxisTitleTextBox.setText( displayerSettings.getXAxisTitle() );
            editorSettingsTable.setWidget( rowCounter, 0, xaxisTitleTextBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, xaxisTitleTextBox );
        }

//        if ( supportedAttributes.contains( DisplayerAttributeDef.XAXIS_LABELSANGLE ) ) {
//            xaxisAngleIntegerBox.setValue( displayerSettings.getXAxisLabelsAngle() );
//            editorSettingsTable.setWidget( rowCounter, 0, xaxisAngleIntegerBoxLabel );
//            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
//            editorSettingsTable.setWidget( rowCounter++, 2, xaxisAngleIntegerBox );
//        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.YAXIS_SHOWLABELS ) ) {
            yaxisShowLabelsCheckBox.setValue( displayerSettings.isYAxisShowLabels() );
            editorSettingsTable.setWidget( rowCounter, 0, yaxisShowLabelsCheckBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, yaxisShowLabelsCheckBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.YAXIS_TITLE ) ) {
            yaxisTitleTextBox.setText( displayerSettings.getYAxisTitle() );
            editorSettingsTable.setWidget( rowCounter, 0, yaxisTitleTextBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, yaxisTitleTextBox );
        }

//        if ( supportedAttributes.contains( DisplayerAttributeDef.YAXIS_LABELSANGLE ) ) {
//            yaxisAngleIntegerBox.setValue( displayerSettings.getYAxisLabelsAngle() );
//            editorSettingsTable.setWidget( rowCounter, 0, yaxisAngleIntegerBoxLabel );
//            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
//            editorSettingsTable.setWidget( rowCounter++, 2, yaxisAngleIntegerBox );
//        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.METER_START ) ) {
            meterStartLongBox.setValue( displayerSettings.getMeterStart() );
            editorSettingsTable.setWidget( rowCounter, 0, meterStartLongBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, meterStartLongBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.METER_WARNING ) ) {
            meterWarningLongBox.setValue( displayerSettings.getMeterWarning() );
            editorSettingsTable.setWidget( rowCounter, 0, meterWarningLongBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, meterWarningLongBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.METER_CRITICAL ) ) {
            meterCriticalLongBox.setValue( displayerSettings.getMeterCritical() );
            editorSettingsTable.setWidget( rowCounter, 0, meterCriticalLongBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, meterCriticalLongBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.METER_END ) ) {
            meterEndLongBox.setValue( displayerSettings.getMeterEnd() );
            editorSettingsTable.setWidget( rowCounter, 0, meterEndLongBoxLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, meterEndLongBox );
        }

        if ( supportedAttributes.contains( DisplayerAttributeDef.BARCHART_HORIZONTAL ) ) {
            barChartHorizontalRadio.setValue( displayerSettings.isBarchartHorizontal() );
            barChartVerticalRadio.setValue( !displayerSettings.isBarchartHorizontal() );

            FlexTable barRadios = new FlexTable();
            barRadios.setWidget( 0, 0, barChartHorizontalRadioLabel );
            barRadios.setWidget( 0, 1, SpacerWidget.SINGLE );
            barRadios.setWidget( 0, 2, barChartHorizontalRadio );
            barRadios.setWidget( 1, 0, barChartVerticalRadioLabel );
            barRadios.setWidget( 1, 1, SpacerWidget.SINGLE );
            barRadios.setWidget( 1, 2, barChartVerticalRadio );

            editorSettingsTable.setWidget( rowCounter, 0, barChartOrientationLabel );
            editorSettingsTable.setWidget( rowCounter, 1, SpacerWidget.SINGLE );
            editorSettingsTable.setWidget( rowCounter++, 2, barRadios );
        }
    }

    protected void notifyChanges() {
        if (presenter != null) {
            presenter.displayerSettingsChanged(displayerSettings);
        }
    }

    private void initPositionList() {
        chartLegendPositionListBox.clear();
        for ( Position position : Position.values()) {
            String positionKey = position.toString();
            String positionLabel = DisplayerSettingsEditorConstants.INSTANCE.getString("POSITION_" + positionKey );
            chartLegendPositionListBox.addItem( positionLabel, positionKey );
        }
    }

    private void initRendererList() {
        rendererListBox.clear();
        for ( String renderer : RendererLibLocator.get().getAvailableRenderersByDisplayerType( this.displayerSettings.getType() ) ) {
            rendererListBox.addItem( renderer, renderer );
        }
    }

    private DisplayerSettingsColumn[] parseColumns( String columns ) {
        if ( columns.length() > 0) {
            String[] sa = columns.split( "," );
            DisplayerSettingsColumn[] arr = new DisplayerSettingsColumn[ sa.length ];
            for ( int i = 0; i < sa.length; i++ ) {
                DisplayerSettingsColumnImpl dsci = new DisplayerSettingsColumnImpl();
                String[] idAlias = sa[i].trim().split( ":" );
                if ( idAlias.length == 2 ) {
                    if ( StringUtils.isBlank( idAlias[ 0 ] ) && StringUtils.isBlank( idAlias[1] ) )
                        throw new IllegalArgumentException( "You must specify at least a column alias." );

                    if ( !StringUtils.isBlank( idAlias[1] ) ) {
                        dsci.setDisplayName( idAlias[ 1 ].trim() );
                    } else dsci.setDisplayName( idAlias[0].trim() );

                    if ( !StringUtils.isBlank( idAlias[0] ) ) dsci.setColumnId( idAlias[0].trim() );

                } else {
                    if ( !StringUtils.isBlank( idAlias[0] ) ) dsci.setDisplayName( idAlias[0].trim() );
                    else throw new IllegalArgumentException( "You must specify at least a column alias." );
                }
                arr[i] = dsci;
            }
            return arr;
        }
        return new DisplayerSettingsColumn[]{};
    }

    private String formatColumns( DisplayerSettingsColumn[] columns ) {
        StringBuilder sb = new StringBuilder( "" );
        if ( columns != null ) {
            for ( int i = 0; i < columns.length; i++ ) {
                String columnId = columns[ i ].getColumnId();
                if ( !StringUtils.isBlank( columnId ) ) {
                    sb.append( columnId ).append( ":" );
                }
                sb.append( columns[ i ].getDisplayName() );
                if ( i != columns.length -1 ) sb.append( "," );
            }
        }
        return sb.toString();
    }
}
