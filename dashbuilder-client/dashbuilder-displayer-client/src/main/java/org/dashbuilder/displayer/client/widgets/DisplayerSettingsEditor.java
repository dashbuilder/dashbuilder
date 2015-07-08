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
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.ColumnSettings;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.Position;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.displayer.client.RendererLibrary;
import org.dashbuilder.displayer.client.RendererManager;
import org.dashbuilder.displayer.client.resources.i18n.CommonConstants;
import org.dashbuilder.displayer.client.resources.i18n.PositionLiterals;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.constants.LabelType;
import org.uberfire.ext.properties.editor.client.PropertyEditorWidget;
import org.uberfire.ext.properties.editor.model.PropertyEditorCategory;
import org.uberfire.ext.properties.editor.model.PropertyEditorChangeEvent;
import org.uberfire.ext.properties.editor.model.PropertyEditorEvent;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;
import org.uberfire.ext.properties.editor.model.PropertyEditorType;
import org.uberfire.ext.properties.editor.model.validators.PropertyFieldValidator;

import static org.dashbuilder.displayer.DisplayerAttributeDef.*;
import static org.dashbuilder.displayer.DisplayerAttributeGroupDef.*;

@ApplicationScoped
public class DisplayerSettingsEditor extends Composite {

    public interface Listener {
        void displayerSettingsChanged(DisplayerSettings settings);
    }

    interface Binder extends UiBinder<Widget, DisplayerSettingsEditor> {}
    private static final Binder uiBinder = GWT.create( Binder.class );

    @UiField
    Panel mainPanel;

    @UiField
    PropertyEditorWidget propertyEditor;

    protected Listener listener;
    protected Displayer displayer;
    protected DisplayerSettings displayerSettings;
    protected DisplayerConstraints displayerContraints;
    private Set<DisplayerAttributeDef> supportedAttributes;

    public static final String PROPERTY_EDITOR_ID = "displayerSettingsEditor";
    public static final String COLUMNS_PREFFIX = "columns.";

    public DisplayerSettingsEditor() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public DisplayerSettings getDisplayerSettings() {
        return displayerSettings;
    }

    public void init(DisplayerSettings displayerSettings, Listener listener) {
        this.displayerSettings = displayerSettings.cloneInstance();
        this.listener = listener;

        this.displayer = DisplayerHelper.lookupDisplayer(displayerSettings);
        this.displayerContraints = displayer.getDisplayerConstraints();
        this.supportedAttributes = displayerContraints.getSupportedAttributes();

        // propertyEditor.setFilterGroupVisible(false);
        try {
            displayer.getDataSetHandler().lookupDataSet(new DataSetReadyCallback() {
                public void callback(DataSet dataSet) {
                    propertyEditor.handle(new PropertyEditorEvent(PROPERTY_EDITOR_ID, getPropertyEditorCategories()));
                }
                public void notFound() {
                    mainPanel.add(new Label(LabelType.WARNING, CommonConstants.INSTANCE.error() + CommonConstants.INSTANCE.displayer_editor_dataset_notfound()));
                }

                @Override
                public boolean onError(final ClientRuntimeError error) {
                    mainPanel.add( new Label( LabelType.WARNING, CommonConstants.INSTANCE.error() + error.getMessage() ) );
                    return false;
                }
            });
        } catch (Exception e) {
            mainPanel.add(new Label(LabelType.WARNING, CommonConstants.INSTANCE.error() + e.toString()));
        }
    }

    protected boolean isSupported(DisplayerAttributeDef attributeDef) {
        return supportedAttributes.contains(attributeDef);
    }

    protected boolean isSupported(DisplayerAttributeGroupDef groupDef) {
        if (supportedAttributes.contains(groupDef)) {

            for (DisplayerAttributeDef attrDef : groupDef.getChildren()) {
                if (attrDef instanceof DisplayerAttributeGroupDef) {
                    continue;
                }
                if (supportedAttributes.contains(attrDef)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected List<PropertyEditorCategory> getPropertyEditorCategories() {
        final List<PropertyEditorCategory> categories = new ArrayList<PropertyEditorCategory>();

        if (isSupported(GENERAL_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory(CommonConstants.INSTANCE.common_group());
            categories.add(category);

            if (isSupported(TITLE)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.common_title(),
                        displayerSettings.getTitle(),
                        PropertyEditorType.TEXT)
                        .withKey(TITLE.getFullId()));
            }
            if (isSupported(TITLE_VISIBLE)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.common_showTitle(),
                        Boolean.toString(displayerSettings.isTitleVisible()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(TITLE_VISIBLE.getFullId()));
            }
            if (isSupported(ALLOW_EXPORT_CSV)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.common_allowCSV(),
                        Boolean.toString(displayerSettings.isCSVExportAllowed()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(ALLOW_EXPORT_CSV.getFullId()));
            }
            if (isSupported(ALLOW_EXPORT_EXCEL)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.common_allowExcel(),
                        Boolean.toString(displayerSettings.isExcelExportAllowed()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(ALLOW_EXPORT_EXCEL.getFullId()));
            }
        }

        if (isSupported(RENDERER)) {
            PropertyEditorCategory category = new PropertyEditorCategory( CommonConstants.INSTANCE.common_renderer());
            categories.add(category);

            List<String> optionList = new ArrayList<String>();
            for (RendererLibrary option : RendererManager.get().getRenderersForType(displayerSettings.getType())) {
                optionList.add(option.getUUID());
            }
            if (optionList.size() > 1) {
                RendererLibrary renderer = RendererManager.get().getRendererForDisplayer(displayerSettings);
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.common_renderer(),
                        renderer.getUUID(),
                        PropertyEditorType.COMBO).withComboValues(optionList)
                        .withKey(RENDERER.getFullId()));
            }
        }


        if (isSupported(CHART_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory( CommonConstants.INSTANCE.chart_group());
            categories.add(category);

            if (isSupported(CHART_WIDTH)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.chart_width(),
                        Integer.toString(displayerSettings.getChartWidth()),
                        PropertyEditorType.TEXT)
                        .withKey(CHART_WIDTH.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(CHART_HEIGHT)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.chart_height(),
                        Integer.toString(displayerSettings.getChartHeight()),
                        PropertyEditorType.TEXT)
                        .withKey(CHART_HEIGHT.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(CHART_BGCOLOR)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.chart_bgColor(),
                        displayerSettings.getChartBackgroundColor(),
                        PropertyEditorType.COLOR)
                        .withKey(CHART_BGCOLOR.getFullId()));
            }
            if (isSupported(CHART_3D)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.chart_3d(),
                        Boolean.toString(displayerSettings.isChart3D()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(CHART_3D.getFullId()));
            }
        }
        if (isSupported(CHART_MARGIN_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory( CommonConstants.INSTANCE.chart_marginGroup());
            categories.add(category);

            if (isSupported(CHART_MARGIN_TOP)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.chart_topMargin(),
                        Integer.toString(displayerSettings.getChartMarginTop()),
                        PropertyEditorType.TEXT)
                        .withKey(CHART_MARGIN_TOP.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(CHART_MARGIN_BOTTOM)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.chart_bottomMargin(),
                        Integer.toString(displayerSettings.getChartMarginBottom()),
                        PropertyEditorType.TEXT)
                        .withKey(CHART_MARGIN_BOTTOM.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(CHART_MARGIN_LEFT)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.chart_leftMargin(),
                        Integer.toString(displayerSettings.getChartMarginLeft()),
                        PropertyEditorType.TEXT)
                        .withKey(CHART_MARGIN_LEFT.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(CHART_MARGIN_RIGHT)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.chart_rightMargin(),
                        Integer.toString(displayerSettings.getChartMarginRight()),
                        PropertyEditorType.TEXT)
                        .withKey(CHART_MARGIN_RIGHT.getFullId())
                        .withValidators(new LongValidator()));
            }
        }
        if (isSupported(CHART_LEGEND_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory( CommonConstants.INSTANCE.chart_legendGroup());
            categories.add(category);

            if (isSupported(CHART_SHOWLEGEND)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.chart_legendShow(),
                        Boolean.toString(displayerSettings.isChartShowLegend()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(CHART_SHOWLEGEND.getFullId()));
            }
            if (isSupported(CHART_LEGENDPOSITION)) {
                List<String> optionList = new ArrayList<String>();
                for (Position position : Position.values()) {
                    String positionKey = position.toString();
                    String positionLabel = PositionLiterals.INSTANCE.getString("POSITION_" + positionKey );
                    optionList.add(positionLabel);
                }
                if (optionList.size() > 1) {
                    String positionKey = displayerSettings.getChartLegendPosition().toString();
                    String positionLabel = PositionLiterals.INSTANCE.getString("POSITION_" + positionKey);
                    category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.chart_legendPosition(),
                            positionLabel,
                            PropertyEditorType.COMBO).withComboValues(optionList)
                            .withKey(CHART_LEGENDPOSITION.getFullId()));
                }
            }
        }
        if (isSupported(XAXIS_GROUP) || isSupported(YAXIS_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory( CommonConstants.INSTANCE.axis_group());
            categories.add(category);

            if (isSupported(XAXIS_SHOWLABELS)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.xaxis_showLabels(),
                        Boolean.toString(displayerSettings.isXAxisShowLabels()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(XAXIS_SHOWLABELS.getFullId()));
            }
            if (isSupported(XAXIS_TITLE)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.xaxis_title(),
                        displayerSettings.getXAxisTitle(),
                        PropertyEditorType.TEXT).withKey(XAXIS_TITLE.getFullId()));
            }
            if (isSupported(YAXIS_SHOWLABELS)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.yaxis_showLabels(),
                        Boolean.toString(displayerSettings.isYAxisShowLabels()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(YAXIS_SHOWLABELS.getFullId()));
            }
            if (isSupported(YAXIS_TITLE)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.yaxis_title(),
                        displayerSettings.getYAxisTitle(),
                        PropertyEditorType.TEXT).withKey(YAXIS_TITLE.getFullId()));
            }
        }
        if (isSupported(TABLE_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory( CommonConstants.INSTANCE.table_group());
            categories.add(category);

            if (isSupported(TABLE_PAGESIZE)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.table_pageSize(),
                        Integer.toString(displayerSettings.getTablePageSize()),
                        PropertyEditorType.TEXT)
                        .withKey(TABLE_PAGESIZE.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(TABLE_WIDTH)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.table_width(),
                        Integer.toString(displayerSettings.getTableWidth()),
                        PropertyEditorType.TEXT)
                        .withKey(TABLE_WIDTH.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(TABLE_SORTENABLED)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.table_sortEnabled(),
                        Boolean.toString(displayerSettings.isTableSortEnabled()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(TABLE_SORTENABLED.getFullId()));
            }
            if (isSupported(TABLE_SORTCOLUMNID)) {
                final List<String> optionList = new ArrayList<String>();
                DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();
                List<DataColumn> dsColumns = dataSet.getColumns();
                optionList.add("");
                for (DataColumn column : dsColumns) optionList.add(column.getId());

                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.table_sortColumn(),
                        displayerSettings.getTableDefaultSortColumnId(),
                        PropertyEditorType.COMBO).withComboValues(optionList)
                        .withKey(TABLE_SORTCOLUMNID.getFullId()));
            }
            if (isSupported(TABLE_SORTORDER)) {
                List<String> optionList = new ArrayList<String>();
                optionList.add(SortOrder.ASCENDING.toString());
                optionList.add(SortOrder.DESCENDING.toString());
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.table_sortOrder(),
                        displayerSettings.getTableDefaultSortOrder().toString(),
                        PropertyEditorType.COMBO).withComboValues(optionList)
                        .withKey(TABLE_SORTORDER.getFullId()));
            }
        }
        if (isSupported(METER_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory( CommonConstants.INSTANCE.meter_group());
            categories.add(category);

            if (isSupported(METER_START)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.meter_start(),
                        Long.toString(displayerSettings.getMeterStart()),
                        PropertyEditorType.TEXT)
                        .withKey(METER_START.getFullId())
                        .withValidators(new MeterValidator(displayerSettings, 0)));
            }
            if (isSupported(METER_WARNING)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.meter_warning(),
                        Long.toString(displayerSettings.getMeterWarning()),
                        PropertyEditorType.TEXT)
                        .withKey(METER_WARNING.getFullId())
                        .withValidators(new MeterValidator(displayerSettings, 1)));
            }
            if (isSupported(METER_CRITICAL)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.meter_critical(),
                        Long.toString(displayerSettings.getMeterCritical()),
                        PropertyEditorType.TEXT)
                        .withKey(METER_CRITICAL.getFullId())
                        .withValidators(new MeterValidator(displayerSettings, 2)));
            }
            if (isSupported(METER_END)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.meter_end(),
                        Long.toString(displayerSettings.getMeterEnd()),
                        PropertyEditorType.TEXT)
                        .withKey(METER_END.getFullId())
                        .withValidators(new MeterValidator(displayerSettings, 3)));
            }

        }
        if (isSupported(FILTER_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory( CommonConstants.INSTANCE.filter_group());
            categories.add(category);

            if (isSupported(FILTER_ENABLED)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.filter_enabled(),
                        Boolean.toString(displayerSettings.isFilterEnabled()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(FILTER_ENABLED.getFullId()));
            }
            if (isSupported(FILTER_SELFAPPLY_ENABLED)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.filter_self(),
                        Boolean.toString(displayerSettings.isFilterSelfApplyEnabled()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(FILTER_SELFAPPLY_ENABLED.getFullId()));
            }
            if (isSupported(FILTER_LISTENING_ENABLED)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.filter_listening(),
                        Boolean.toString(displayerSettings.isFilterListeningEnabled()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(FILTER_LISTENING_ENABLED.getFullId()));
            }
            if (isSupported(FILTER_NOTIFICATION_ENABLED)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.filter_notifications(),
                        Boolean.toString(displayerSettings.isFilterNotificationEnabled()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(FILTER_NOTIFICATION_ENABLED.getFullId()));
            }
        }
        if (isSupported(REFRESH_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory( CommonConstants.INSTANCE.refresh_group());
            categories.add(category);

            if (isSupported(REFRESH_INTERVAL)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.refresh_interval(),
                        Integer.toString(displayerSettings.getRefreshInterval()),
                        PropertyEditorType.TEXT)
                        .withKey(REFRESH_INTERVAL.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(REFRESH_STALE_DATA)) {
                category.withField(new PropertyEditorFieldInfo( CommonConstants.INSTANCE.refresh_stale_data(),
                        Boolean.toString(displayerSettings.isRefreshStaleData()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(REFRESH_STALE_DATA.getFullId()));
            }
        }
        if (isSupported(COLUMNS_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory( CommonConstants.INSTANCE.common_columns());
            categories.add(category);

            DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();
            for (int i=0; i<dataSet.getColumns().size(); i++) {

                DataColumn dataColumn = dataSet.getColumnByIndex(i);
                ColumnSettings cs = displayerSettings.getColumnSettings(dataColumn);
                String fieldSuffix = COLUMNS_PREFFIX + cs.getColumnId() + ".";
                String expression = cs.getValueExpression();
                String pattern = cs.getValuePattern();

                category.withField(new PropertyEditorFieldInfo("\u25fe " + CommonConstants.INSTANCE.columns_name() + (i+1),
                        cs.getColumnName(),
                        PropertyEditorType.TEXT)
                        .withKey(fieldSuffix + "name"));

                if (expression != null) {
                    category.withField(new PropertyEditorFieldInfo("     " + CommonConstants.INSTANCE.columns_expression(),
                            expression,
                            PropertyEditorType.TEXT)
                            .withKey(fieldSuffix + "expression"));
                }
                if (pattern != null) {
                    category.withField(new PropertyEditorFieldInfo("     " + CommonConstants.INSTANCE.columns_pattern(),
                            pattern,
                            PropertyEditorType.TEXT)
                            .withKey(fieldSuffix + "pattern"));
                }
                /* Non-critical. Disable for the time being.
                if (isSupported(COLUMN_EMPTY)) {
                    String empty = cs.getEmptyTemplate();
                    category.withField(new PropertyEditorFieldInfo("     " + CommonConstants.INSTANCE.columns_emptyvalue(),
                            empty,
                            PropertyEditorType.TEXT)
                            .withKey(fieldSuffix + "empty"));
                }*/
            }
        }
        return categories;
    }

    /**
     * Capture & process the modification events sent by the property editor
     */
    public void onPropertyEditorChange(@Observes PropertyEditorChangeEvent event) {
        PropertyEditorFieldInfo property = event.getProperty();
        if (property.getEventId().equalsIgnoreCase(PROPERTY_EDITOR_ID)) {
            String attrKey = property.getKey();
            String attrValue = event.getNewValue();

            if (attrKey.startsWith(COLUMNS_PREFFIX)) {
                String[] strings = attrKey.split("\\.");
                if (strings.length == 3) {
                    String columnId = strings[1];
                    String setting = strings[2];
                    if ("name".equals(setting)) displayerSettings.setColumnName(columnId, attrValue);
                    else if ("empty".equals(setting)) displayerSettings.setColumnEmptyTemplate(columnId, attrValue);
                    else if ("pattern".equals(setting)) displayerSettings.setColumnValuePattern(columnId, attrValue);
                    else if ("expression".equals(setting)) displayerSettings.setColumnValueExpression(columnId, attrValue);
                }
            } else {
                displayerSettings.setDisplayerSetting(attrKey, attrValue);
            }

            if (listener != null) {
                listener.displayerSettingsChanged(displayerSettings);
            }
        }
    }

    /**
     * Property Editor Validator for integers
     */
    public class LongValidator implements PropertyFieldValidator {

        @Override
        public boolean validate( Object value ) {
            try {
                Long.parseLong( value.toString() );
                return true;
            } catch ( Exception e ) {
                return false;
            }
        }

        @Override
        public String getValidatorErrorMessage() {
            return CommonConstants.INSTANCE.settings_validation_integer();
        }
    }

    /**
     * Property Editor Validator for doubles
     */
    public class DoubleValidator implements PropertyFieldValidator {

        @Override
        public boolean validate( Object value ) {
            try {
                Double.parseDouble( value.toString() );
                return true;
            } catch ( Exception e ) {
                return false;
            }
        }

        @Override
        public String getValidatorErrorMessage() {
            return CommonConstants.INSTANCE.settings_validation_double();
        }
    }

    /**
     * Property Editor Validator for meter intervals
     */
    public class MeterValidator extends LongValidator {

        private DisplayerSettings displayerSettings;
        private int level;
        private boolean lowerOk = true;
        private boolean upperOk = true;

        public MeterValidator(DisplayerSettings displayerSettings, int level) {
            this.displayerSettings = displayerSettings;
            this.level = level;
        }

        private long getLevelValue(int level) {
            switch (level) {
                case 0: return displayerSettings.getMeterStart();
                case 1: return displayerSettings.getMeterWarning();
                case 2: return displayerSettings.getMeterCritical();
                case 3: return displayerSettings.getMeterEnd();
            }
            return level < 0 ? Long.MIN_VALUE : Long.MAX_VALUE;
        }

        private String getLevelDescr(int level) {
            switch (level) {
                case 0: return CommonConstants.INSTANCE.meter_start();
                case 1: return CommonConstants.INSTANCE.meter_warning();
                case 2: return CommonConstants.INSTANCE.meter_critical();
                case 3: return CommonConstants.INSTANCE.meter_end();
            }
            return CommonConstants.INSTANCE.settings_validation_meter_unknown();
        }

        @Override
        public boolean validate( Object value ) {
            if (!super.validate(value)) {
                return false;
            }
            long thisLevel = Long.parseLong(value.toString());
            long lowerLevel = getLevelValue(level-1);
            long upperLevel = getLevelValue(level + 1);
            lowerOk = thisLevel >= lowerLevel;
            upperOk = thisLevel <= upperLevel;
            return lowerOk && upperOk;
        }

        @Override
        public String getValidatorErrorMessage() {
            if (!lowerOk) return CommonConstants.INSTANCE.settings_validation_meter_higher(getLevelDescr(level-1));
            if (!upperOk) return CommonConstants.INSTANCE.settings_validation_meter_lower(getLevelDescr(level+1));
            return CommonConstants.INSTANCE.settings_validation_meter_invalid();
        }
    }
}
