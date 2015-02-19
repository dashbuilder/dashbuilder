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

package com.ait.lienzo.charts.client.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

/**
 * An I18 based interface used for Lienzo Chart Constants.
 */
public interface MessageConstants extends Constants
{
    public static final MessageConstants MESSAGES = GWT.create(MessageConstants.class);

    @DefaultStringValue("Chart alignment")
    public String chartAlignLabel();

    @DefaultStringValue("Chart alignment")
    public String chartAlignDescription();

    @DefaultStringValue("Direction")
    public String chartDirectionLabel();

    @DefaultStringValue("Direction")
    public String chartDirectionDescription();

    @DefaultStringValue("Orientation")
    public String chartOrientationLabel();

    @DefaultStringValue("Orientation")
    public String chartOrientationDescription();
    
    @DefaultStringValue("Legend position")
    public String legendPositionLabel();

    @DefaultStringValue("Legend position")
    public String legendPositionDescription();

    @DefaultStringValue("Legend alignment")
    public String legendAlignLabel();

    @DefaultStringValue("Legend alignment")
    public String legendAlignDescription();
    
    @DefaultStringValue("Categories Axis")
    public String categoriesAxisLabel();

    @DefaultStringValue("Categories Axis")
    public String categoriesAxisDescription();

    @DefaultStringValue("Values Axis")
    public String valuesAxisLabel();

    @DefaultStringValue("Values Axis")
    public String valuesAxisDescription();

    @DefaultStringValue("Show Categories Axis title")
    public String showCategoriesAxisTitleLabel();

    @DefaultStringValue("Show Categories Axis title")
    public String showCategoriesAxisTitleDescription();

    @DefaultStringValue("Show Values Axis title")
    public String showValuesAxisTitleLabel();

    @DefaultStringValue("Show Values Axis title")
    public String showValuesAxisTitleDescription();

    @DefaultStringValue("XY Chart Data")
    public String xyDataLabel();

    @DefaultStringValue("XY Chart Data")
    public String xyDataDescription();

    @DefaultStringValue("Pie Chart Data")
    public String pieDataLabel();

    @DefaultStringValue("Pie Chart Data")
    public String pieDataDescription();

    @DefaultStringValue("Show title")
    public String showTitleLabel();

    @DefaultStringValue("Show title")
    public String showTitleDescription();

    @DefaultStringValue("Can be resized")
    public String resizableLabel();

    @DefaultStringValue("Can be resized")
    public String resizableDescription();
    
}
