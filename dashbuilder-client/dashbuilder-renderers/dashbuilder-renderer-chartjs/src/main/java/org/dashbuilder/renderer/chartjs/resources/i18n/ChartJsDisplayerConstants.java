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

package org.dashbuilder.renderer.chartjs.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface ChartJsDisplayerConstants extends Messages {

    public static final ChartJsDisplayerConstants INSTANCE = GWT.create( ChartJsDisplayerConstants.class );

    String common_Categories();

    String common_Series();

    String common_Values();

    String common_Value();

    String common_Locations();

    String common_Rows();

    String common_Columns();

    String common_noData();

    String chartjsDisplayer_initalizing();

    String chartjsDisplayer_resetAnchor();

    String chartjsDisplayer_error();

    String chartjsDisplayer_error_settings_unset();

    String chartjsDisplayer_error_handler_unset();

    String chartjsDisplayer_error_dataset_notfound();

    String chartjsCategoriesDisplayer_color_blue();

    String chartjsCategoriesDisplayer_color_red();

    String chartjsCategoriesDisplayer_color_orange();

    String chartjsCategoriesDisplayer_color_brown();

    String chartjsCategoriesDisplayer_color_coral();

    String chartjsCategoriesDisplayer_color_aqua();

    String chartjsCategoriesDisplayer_color_fuchsia();

    String chartjsCategoriesDisplayer_color_gold();

    String chartjsCategoriesDisplayer_color_green();

    String chartjsCategoriesDisplayer_color_grey();

    String chartjsCategoriesDisplayer_color_lime();

    String chartjsCategoriesDisplayer_color_magenta();

    String chartjsCategoriesDisplayer_color_pink();

    String chartjsCategoriesDisplayer_color_silver();

    String chartjsCategoriesDisplayer_color_yellow();

}
