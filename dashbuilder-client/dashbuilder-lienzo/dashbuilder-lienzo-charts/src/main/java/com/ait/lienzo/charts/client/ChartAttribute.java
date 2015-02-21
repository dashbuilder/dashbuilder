/*
   Copyright (c) 2014,2015 Ahome' Innovation Technologies. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ait.lienzo.charts.client;

import com.ait.lienzo.charts.client.i18n.MessageConstants;
import com.ait.lienzo.client.core.Attribute;
import com.ait.lienzo.client.core.AttributeType;

public class ChartAttribute extends Attribute
{
    protected static final MessageConstants CHART_MESSAGES =   MessageConstants.MESSAGES;

    public final static ChartAttribute DIRECTION = new ChartAttribute("direction", CHART_MESSAGES.chartDirectionLabel(), CHART_MESSAGES.chartDirectionDescription(), ChartAttributeType.DIRECTION);
    public final static ChartAttribute ORIENTATION = new ChartAttribute("orientation", CHART_MESSAGES.chartOrientationLabel(), CHART_MESSAGES.chartOrientationDescription(), ChartAttributeType.ORIENTATION);
    public final static ChartAttribute SHOW_TITLE = new ChartAttribute("showTitle", CHART_MESSAGES.showTitleLabel(), CHART_MESSAGES.showTitleDescription(), AttributeType.BOOLEAN_TYPE);
    public final static ChartAttribute RESIZABLE = new ChartAttribute("resizable", CHART_MESSAGES.resizableLabel(), CHART_MESSAGES.resizableDescription(), AttributeType.BOOLEAN_TYPE);
    public final static ChartAttribute ANIMATED = new ChartAttribute("animated", CHART_MESSAGES.animatedLabel(), CHART_MESSAGES.animatedDescription(), AttributeType.BOOLEAN_TYPE);
    public final static ChartAttribute ALIGN = new ChartAttribute("align", CHART_MESSAGES.chartAlignLabel(), CHART_MESSAGES.chartAlignDescription(), ChartAttributeType.ALIGN);
    public final static ChartAttribute MARGIN_LEFT = new ChartAttribute("marginLeft", CHART_MESSAGES.marginLeftLabel(), CHART_MESSAGES.marginLeftDescription(), AttributeType.NUMBER_TYPE);
    public final static ChartAttribute MARGIN_RIGHT = new ChartAttribute("marginRight", CHART_MESSAGES.marginRightLabel(), CHART_MESSAGES.marginRightDescription(), AttributeType.NUMBER_TYPE);
    public final static ChartAttribute MARGIN_TOP = new ChartAttribute("marginTop", CHART_MESSAGES.marginTopLabel(), CHART_MESSAGES.marginTopDescription(), AttributeType.NUMBER_TYPE);
    public final static ChartAttribute MARGIN_BOTTOM = new ChartAttribute("marginBottom", CHART_MESSAGES.marginBottomLabel(), CHART_MESSAGES.marginBottomDescription(), AttributeType.NUMBER_TYPE);
    public final static ChartAttribute XY_CHART_DATA = new ChartAttribute("xyChartData", CHART_MESSAGES.xyDataLabel(), CHART_MESSAGES.xyDataDescription(), ChartAttributeType.XY_CHART_DATA_TYPE);
    public final static ChartAttribute PIE_CHART_DATA = new ChartAttribute("pieChartData", CHART_MESSAGES.pieDataLabel(), CHART_MESSAGES.pieDataDescription(), ChartAttributeType.PIE_CHART_DATA_TYPE);

    /** AXIS **/
    public final static ChartAttribute CATEGORIES_AXIS = new ChartAttribute("categoriesAxis", CHART_MESSAGES.categoriesAxisLabel(), CHART_MESSAGES.categoriesAxisDescription(), ChartAttributeType.AXIS_TYPE);
    public final static ChartAttribute VALUES_AXIS = new ChartAttribute("valuesAxis", CHART_MESSAGES.valuesAxisLabel(), CHART_MESSAGES.valuesAxisDescription(), ChartAttributeType.AXIS_TYPE);
    public final static ChartAttribute SHOW_CATEGORIES_AXIS_TITLE = new ChartAttribute("showCategoriesAxisTitle", CHART_MESSAGES.showCategoriesAxisTitleLabel(), CHART_MESSAGES.showCategoriesAxisTitleDescription(), AttributeType.BOOLEAN_TYPE);
    public final static ChartAttribute SHOW_VALUES_AXIS_TITLE = new ChartAttribute("showValuesAxisTitle", CHART_MESSAGES.showValuesAxisTitleLabel(), CHART_MESSAGES.showValuesAxisTitleDescription(), AttributeType.BOOLEAN_TYPE);
    public final static ChartAttribute CATEGORIES_AXIS_LABELS_POSITION = new ChartAttribute("categoriesAxisLabelsPosition", CHART_MESSAGES.categoriesAxisLabelsPositionLabel(), CHART_MESSAGES.categoriesAxisLabelsPositionDescription(), ChartAttributeType.LABELS_POSITION);
    public final static ChartAttribute VALUES_AXIS_LABELS_POSITION = new ChartAttribute("valuesAxisLabelsPosition", CHART_MESSAGES.valuesAxisLabelsPositionLabel(), CHART_MESSAGES.valuesAxisLabelsPositionDescription(), ChartAttributeType.LABELS_POSITION);
    
    /** LEGEND **/
    public final static ChartAttribute LEGEND_POSITION = new ChartAttribute("legendPosition", CHART_MESSAGES.legendPositionLabel(), CHART_MESSAGES.legendPositionDescription(), ChartAttributeType.LEGEND_POSITION);
    public final static ChartAttribute LEGEND_ALIGN = new ChartAttribute("legendAlign", CHART_MESSAGES.legendAlignLabel(), CHART_MESSAGES.legendAlignDescription(), ChartAttributeType.LEGEND_ALIGN);
    
    protected ChartAttribute(String property, String label, String description, AttributeType type)
    {
        super(property, label, description, type);
    }
}
