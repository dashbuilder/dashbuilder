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
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsColumn;
import org.dashbuilder.displayer.Position;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.DisplayerHelper;
import org.dashbuilder.displayer.client.RendererLibLocator;
import org.dashbuilder.displayer.client.resources.i18n.DisplayerSettingsEditorConstants;
import org.kie.uberfire.properties.editor.client.PropertyEditorWidget;
import org.kie.uberfire.properties.editor.model.PropertyEditorCategory;
import org.kie.uberfire.properties.editor.model.PropertyEditorChangeEvent;
import org.kie.uberfire.properties.editor.model.PropertyEditorEvent;
import org.kie.uberfire.properties.editor.model.PropertyEditorFieldInfo;
import org.kie.uberfire.properties.editor.model.PropertyEditorType;
import org.kie.uberfire.properties.editor.model.validators.PropertyFieldValidator;

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
    PropertyEditorWidget propertyEditor;

    protected Listener listener;
    protected Displayer displayer;
    protected DisplayerSettings displayerSettings;
    protected DisplayerConstraints displayerContraints;
    private Set<DisplayerAttributeDef> supportedAttributes;

    public static final String PROPERTY_EDITOR_ID = "displayerSettingsEditor";

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

        propertyEditor.setFilterPanelVisible(false);
        propertyEditor.handle(new PropertyEditorEvent(PROPERTY_EDITOR_ID, getPropertyEditorCategories()));
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

        if (isSupported(TITLE_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Title");
            categories.add(category);

            if (isSupported(TITLE)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.common_title(),
                        displayerSettings.getTitle(),
                        PropertyEditorType.TEXT)
                        .withKey(TITLE.getFullId()));
            }
            if (isSupported(TITLE_VISIBLE)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.common_showTitle(),
                        Boolean.toString(displayerSettings.isTitleVisible()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(TITLE_VISIBLE.getFullId()));
            }
        }

        if (isSupported(RENDERER)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Renderer");
            categories.add(category);

            List<String> optionList = new ArrayList<String>();
            for (String option : RendererLibLocator.get().getAvailableRenderers(displayerSettings.getType())) {
                optionList.add(option);
            }
            if (optionList.size() > 1) {
                String renderer = displayerSettings.getRenderer();
                if (renderer == null) {
                    renderer = RendererLibLocator.get().getDefaultRenderer(displayerSettings.getType());
                }
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.common_renderer(),
                        renderer,
                        PropertyEditorType.COMBO).withComboValues(optionList)
                        .withKey(RENDERER.getFullId()));
            }
        }


        if (isSupported(CHART_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Chart");
            categories.add(category);

            if (isSupported(CHART_WIDTH)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_width(),
                        Integer.toString(displayerSettings.getChartWidth()),
                        PropertyEditorType.TEXT)
                        .withKey(CHART_WIDTH.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(CHART_HEIGHT)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_height(),
                        Integer.toString(displayerSettings.getChartHeight()),
                        PropertyEditorType.TEXT)
                        .withKey(CHART_HEIGHT.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(CHART_3D)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_3d(),
                        Boolean.toString(displayerSettings.isChart3D()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(CHART_3D.getFullId()));
            }
        }
        if (isSupported(CHART_MARGIN_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Margins");
            categories.add(category);

            if (isSupported(CHART_MARGIN_TOP)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_topMargin(),
                        Integer.toString(displayerSettings.getChartMarginTop()),
                        PropertyEditorType.TEXT)
                        .withKey(CHART_MARGIN_TOP.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(CHART_MARGIN_BOTTOM)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_bottomMargin(),
                        Integer.toString(displayerSettings.getChartMarginBottom()),
                        PropertyEditorType.TEXT)
                        .withKey(CHART_MARGIN_BOTTOM.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(CHART_MARGIN_LEFT)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_leftMargin(),
                        Integer.toString(displayerSettings.getChartMarginLeft()),
                        PropertyEditorType.TEXT)
                        .withKey(CHART_MARGIN_LEFT.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(CHART_MARGIN_RIGHT)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_rightMargin(),
                        Integer.toString(displayerSettings.getChartMarginRight()),
                        PropertyEditorType.TEXT)
                        .withKey(CHART_MARGIN_RIGHT.getFullId())
                        .withValidators(new LongValidator()));
            }
        }
        if (isSupported(CHART_LEGEND_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Legend");
            categories.add(category);

            if (isSupported(CHART_SHOWLEGEND)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_legendShow(),
                        Boolean.toString(displayerSettings.isChartShowLegend()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(CHART_SHOWLEGEND.getFullId()));
            }
            if (isSupported(CHART_LEGENDPOSITION)) {
                List<String> optionList = new ArrayList<String>();
                for (Position position : Position.values()) {
                    String positionKey = position.toString();
                    String positionLabel = DisplayerSettingsEditorConstants.INSTANCE.getString("POSITION_" + positionKey );
                    optionList.add(positionLabel);
                }
                if (optionList.size() > 1) {
                    String positionKey = displayerSettings.getChartLegendPosition().toString();
                    String positionLabel = DisplayerSettingsEditorConstants.INSTANCE.getString("POSITION_" + positionKey);
                    category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_legendPosition(),
                            positionLabel,
                            PropertyEditorType.COMBO).withComboValues(optionList)
                            .withKey(CHART_LEGENDPOSITION.getFullId()));
                }
            }
        }
        if (isSupported(XAXIS_GROUP) || isSupported(YAXIS_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Axis");
            categories.add(category);

            if (isSupported(XAXIS_SHOWLABELS)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.xaxis_showLabels(),
                        Boolean.toString(displayerSettings.isXAxisShowLabels()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(XAXIS_SHOWLABELS.getFullId()));
            }
            if (isSupported(XAXIS_TITLE)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.xaxis_title(),
                        displayerSettings.getXAxisTitle(),
                        PropertyEditorType.TEXT).withKey(XAXIS_TITLE.getFullId()));
            }
            if (isSupported(YAXIS_SHOWLABELS)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.yaxis_showLabels(),
                        Boolean.toString(displayerSettings.isYAxisShowLabels()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(YAXIS_SHOWLABELS.getFullId()));
            }
            if (isSupported(YAXIS_TITLE)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.yaxis_title(),
                        displayerSettings.getYAxisTitle(),
                        PropertyEditorType.TEXT).withKey(YAXIS_TITLE.getFullId()));
            }
        }
        if (isSupported(BARCHART_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Bar");
            categories.add(category);

            if (isSupported(BARCHART_HORIZONTAL)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.barchart_horizontal(),
                        Boolean.toString(displayerSettings.isBarchartHorizontal()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(BARCHART_HORIZONTAL.getFullId()));
            }
        }
        if (isSupported(TABLE_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Table");
            categories.add(category);

            if (isSupported(TABLE_PAGESIZE)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.table_pageSize(),
                        Integer.toString(displayerSettings.getTablePageSize()),
                        PropertyEditorType.TEXT)
                        .withKey(TABLE_PAGESIZE.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(TABLE_WIDTH)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.table_width(),
                        Integer.toString(displayerSettings.getTableWidth()),
                        PropertyEditorType.TEXT)
                        .withKey(TABLE_WIDTH.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (isSupported(TABLE_SORTENABLED)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.table_sortEnabled(),
                        Boolean.toString(displayerSettings.isTableSortEnabled()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(TABLE_SORTENABLED.getFullId()));
            }
            if (isSupported(TABLE_SORTCOLUMNID)) {
                final List<String> optionList = new ArrayList<String>();
                try {
                    displayer.getDataSetHandler().lookupDataSet(new DataSetReadyCallback() {
                        public void callback(DataSet dataSet) {
                            List<DataColumn> dsColumns = dataSet.getColumns();
                            for (DataColumn column : dsColumns) {
                                optionList.add(column.getId());
                            }
                        }
                        public void notFound() {
                            optionList.add("ERROR: Data set not found");
                        }
                    });
                } catch (Exception e) {
                    optionList.add("ERROR: " + e.toString());
                }

                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.table_sortColumn(),
                        displayerSettings.getTableDefaultSortColumnId(),
                        PropertyEditorType.COMBO).withComboValues(optionList)
                        .withKey(TABLE_SORTCOLUMNID.getFullId()));
            }
            if (isSupported(TABLE_SORTORDER)) {
                List<String> optionList = new ArrayList<String>();
                optionList.add(SortOrder.ASCENDING.toString());
                optionList.add(SortOrder.DESCENDING.toString());
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.table_sortOrder(),
                        displayerSettings.getTableDefaultSortOrder().toString(),
                        PropertyEditorType.COMBO).withComboValues(optionList)
                        .withKey(TABLE_SORTORDER.getFullId()));
            }
        }
        if (isSupported(METER_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Meter");
            categories.add(category);

            if (isSupported(METER_START)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.meter_start(),
                        Long.toString(displayerSettings.getMeterStart()),
                        PropertyEditorType.TEXT)
                        .withKey(METER_START.getFullId())
                        .withValidators(new MeterValidator(displayerSettings, 0)));
            }
            if (isSupported(METER_WARNING)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.meter_warning(),
                        Long.toString(displayerSettings.getMeterWarning()),
                        PropertyEditorType.TEXT)
                        .withKey(METER_WARNING.getFullId())
                        .withValidators(new MeterValidator(displayerSettings, 0)));
            }
            if (isSupported(METER_CRITICAL)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.meter_critical(),
                        Long.toString(displayerSettings.getMeterCritical()),
                        PropertyEditorType.TEXT)
                        .withKey(METER_CRITICAL.getFullId())
                        .withValidators(new MeterValidator(displayerSettings, 0)));
            }
            if (isSupported(METER_END)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.meter_end(),
                        Long.toString(displayerSettings.getMeterEnd()),
                        PropertyEditorType.TEXT)
                        .withKey(METER_END.getFullId())
                        .withValidators(new MeterValidator(displayerSettings, 0)));
            }

        }
        if (isSupported(FILTER_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Filter");
            categories.add(category);

            if (isSupported(FILTER_ENABLED)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.filter_enabled(),
                        Boolean.toString(displayerSettings.isFilterEnabled()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(FILTER_ENABLED.getFullId()));
            }
            if (isSupported(FILTER_SELFAPPLY_ENABLED)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.filter_self(),
                        Boolean.toString(displayerSettings.isFilterSelfApplyEnabled()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(FILTER_SELFAPPLY_ENABLED.getFullId()));
            }
            if (isSupported(FILTER_LISTENING_ENABLED)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.filter_listening(),
                        Boolean.toString(displayerSettings.isFilterListeningEnabled()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(FILTER_LISTENING_ENABLED.getFullId()));
            }
            if (isSupported(FILTER_NOTIFICATION_ENABLED)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.filter_notifications(),
                        Boolean.toString(displayerSettings.isFilterNotificationEnabled()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(FILTER_NOTIFICATION_ENABLED.getFullId()));
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
            displayerSettings.setDisplayerSetting(attrKey, attrValue);

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
            return "Value must be an integer number.";
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
            return "Value must be a double.";
        }
    }

    /**
     * Property Editor Validator for meter intervals
     */
    public class MeterValidator extends LongValidator {

        private DisplayerSettings displayerSettings;
        private int level;

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
            return Long.MAX_VALUE;
        }

        private String getLevelDescr(int level) {
            switch (level) {
                case 0: return DisplayerSettingsEditorConstants.INSTANCE.meter_start();
                case 1: return DisplayerSettingsEditorConstants.INSTANCE.meter_warning();
                case 2: return DisplayerSettingsEditorConstants.INSTANCE.meter_critical();
                case 3: return DisplayerSettingsEditorConstants.INSTANCE.meter_end();
            }
            return "Unknown";
        }

        @Override
        public boolean validate( Object value ) {
            if (!super.validate(value)) {
                return false;
            }
            long thisLevel = Long.parseLong(value.toString());
            long upperLevel = getLevelValue(level+1);
            return thisLevel <= upperLevel;
        }

        @Override
        public String getValidatorErrorMessage() {
            return "Must be lower than the " + getLevelDescr(level+1) + " value.";
        }
    }
}
