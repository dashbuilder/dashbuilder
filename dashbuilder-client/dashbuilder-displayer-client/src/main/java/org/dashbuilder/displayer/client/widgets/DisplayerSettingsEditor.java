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

import javax.enterprise.event.Observes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.DisplayerSettings;
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

public class DisplayerSettingsEditor extends Composite {

    public interface Listener {
        void displayerSettingsChanged(DisplayerSettings settings);
    }

    interface Binder extends UiBinder<Widget, DisplayerSettingsEditor> {}
    private static final Binder uiBinder = GWT.create( Binder.class );

    @UiField
    PropertyEditorWidget propertyEditor;

    protected Listener listener;
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

        Displayer displayer = DisplayerHelper.lookupDisplayer(displayerSettings);
        this.displayerContraints = displayer.getDisplayerConstraints();
        this.supportedAttributes = displayerContraints.getSupportedAttributes();

        propertyEditor.setLastOpenAccordionGroupTitle("General");
        propertyEditor.handle(new PropertyEditorEvent(PROPERTY_EDITOR_ID, getPropertyEditorCategories()));
    }

    protected List<PropertyEditorCategory> getPropertyEditorCategories() {
        final List<PropertyEditorCategory> categories = new ArrayList<PropertyEditorCategory>();

        if (supportedAttributes.contains(DisplayerAttributeGroupDef.GENERAL_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("General");
            categories.add(category);

            if (supportedAttributes.contains(DisplayerAttributeDef.TITLE)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.common_title(),
                        displayerSettings.getTitle(),
                        PropertyEditorType.TEXT).withKey(DisplayerAttributeDef.TITLE.getFullId()));
            }
            if (supportedAttributes.contains(DisplayerAttributeDef.TITLE_VISIBLE)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.common_showTitle(),
                        Boolean.toString(displayerSettings.isTitleVisible()),
                        PropertyEditorType.BOOLEAN));
            }
            if (supportedAttributes.contains(DisplayerAttributeDef.RENDERER)) {
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
                            .withKey(DisplayerAttributeDef.CHART_LEGENDPOSITION.getFullId()));
                }
            }
        }
        if (supportedAttributes.contains(DisplayerAttributeGroupDef.CHART_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Chart");
            categories.add(category);

            if (supportedAttributes.contains(DisplayerAttributeDef.CHART_WIDTH)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_width(),
                        Integer.toString(displayerSettings.getChartWidth()),
                        PropertyEditorType.TEXT)
                        .withKey(DisplayerAttributeDef.CHART_WIDTH.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (supportedAttributes.contains(DisplayerAttributeDef.CHART_HEIGHT)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_height(),
                        Integer.toString(displayerSettings.getChartHeight()),
                        PropertyEditorType.TEXT)
                        .withKey(DisplayerAttributeDef.CHART_HEIGHT.getFullId())
                        .withValidators(new LongValidator()));
            }
            if (supportedAttributes.contains(DisplayerAttributeDef.CHART_3D)) {
                category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_3d(),
                        Boolean.toString(displayerSettings.isChart3D()),
                        PropertyEditorType.BOOLEAN)
                        .withKey(DisplayerAttributeDef.CHART_3D.getFullId()));
            }
            if (supportedAttributes.contains(DisplayerAttributeGroupDef.CHART_MARGIN_GROUP)) {

                if (supportedAttributes.contains(DisplayerAttributeDef.CHART_MARGIN_TOP)) {
                    category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_topMargin(),
                            Integer.toString(displayerSettings.getChartMarginTop()),
                            PropertyEditorType.TEXT)
                            .withKey(DisplayerAttributeDef.CHART_MARGIN_TOP.getFullId())
                            .withValidators(new LongValidator()));
                }
                if (supportedAttributes.contains(DisplayerAttributeDef.CHART_MARGIN_BOTTOM)) {
                    category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_bottomMargin(),
                            Integer.toString(displayerSettings.getChartMarginBottom()),
                            PropertyEditorType.TEXT)
                            .withKey(DisplayerAttributeDef.CHART_MARGIN_BOTTOM.getFullId())
                            .withValidators(new LongValidator()));
                }
                if (supportedAttributes.contains(DisplayerAttributeDef.CHART_MARGIN_LEFT)) {
                    category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_leftMargin(),
                            Integer.toString(displayerSettings.getChartMarginLeft()),
                            PropertyEditorType.TEXT)
                            .withKey(DisplayerAttributeDef.CHART_MARGIN_LEFT.getFullId())
                            .withValidators(new LongValidator()));
                }
                if (supportedAttributes.contains(DisplayerAttributeDef.CHART_MARGIN_RIGHT)) {
                    category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_rightMargin(),
                            Integer.toString(displayerSettings.getChartMarginRight()),
                            PropertyEditorType.TEXT)
                            .withKey(DisplayerAttributeDef.CHART_MARGIN_RIGHT.getFullId())
                            .withValidators(new LongValidator()));
                }
            }
            if (supportedAttributes.contains(DisplayerAttributeGroupDef.CHART_LEGEND_GROUP)) {
                if (supportedAttributes.contains(DisplayerAttributeDef.CHART_SHOWLEGEND)) {
                    category.withField(new PropertyEditorFieldInfo(DisplayerSettingsEditorConstants.INSTANCE.chart_legendShow(),
                            Boolean.toString(displayerSettings.isChartShowLegend()),
                            PropertyEditorType.BOOLEAN)
                            .withKey(DisplayerAttributeDef.CHART_SHOWLEGEND.getFullId()));
                }
                if (supportedAttributes.contains(DisplayerAttributeDef.CHART_LEGENDPOSITION)) {
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
                                .withKey(DisplayerAttributeDef.CHART_LEGENDPOSITION.getFullId()));
                    }
                }
            }
        }
        if (supportedAttributes.contains(DisplayerAttributeGroupDef.AXIS_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Axis");
            categories.add(category);
        }
        if (supportedAttributes.contains(DisplayerAttributeGroupDef.BARCHART_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Bar");
            categories.add(category);

        }
        if (supportedAttributes.contains(DisplayerAttributeGroupDef.METER_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Meter");
            categories.add(category);

        }
        if (supportedAttributes.contains(DisplayerAttributeGroupDef.TABLE_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Table");
            categories.add(category);

        }
        if (supportedAttributes.contains(DisplayerAttributeGroupDef.FILTER_GROUP)) {
            PropertyEditorCategory category = new PropertyEditorCategory("Filter");
            categories.add(category);

        }
        return categories;
    }

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
}
