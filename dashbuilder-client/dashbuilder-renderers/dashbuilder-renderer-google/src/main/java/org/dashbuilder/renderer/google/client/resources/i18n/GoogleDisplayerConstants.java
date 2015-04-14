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

package org.dashbuilder.renderer.google.client.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface GoogleDisplayerConstants extends Messages {

    public static final GoogleDisplayerConstants INSTANCE = GWT.create( GoogleDisplayerConstants.class );

    String common_Categories();

    String common_Series();

    String common_Values();

    String common_Value();

    String common_Locations();

    String common_Rows();

    String common_Columns();

    String common_noData();

    String googleDisplayer_initalizing();

    String googleDisplayer_resetAnchor();

    String googleDisplayer_error();

    String googleDisplayer_error_settings_unset();

    String googleDisplayer_error_handler_unset();

    String googleDisplayer_error_dataset_notfound();

    String googleBubbleDisplayer_XAxis();

    String googleBubbleDisplayer_YAxis();

    String googleBubbleDisplayer_BubbleColor();

    String googleBubbleDisplayer_BubbleSize();

    String googleTableDisplayer_gotoFirstPage();

    String googleTableDisplayer_gotoPreviousPage();

    String googleTableDisplayer_gotoNextPage();

    String googleTableDisplayer_gotoLastPage();

    String googleTableDisplayer_pages( String leftMostPageNumber, String rightMostPageNumber, String totalPages);

    String googleTableDisplayer_rows( String from, String to, String totalRows);

    String googleCategoriesDisplayer_color_blue();

    String googleCategoriesDisplayer_color_red();

    String googleCategoriesDisplayer_color_orange();

    String googleCategoriesDisplayer_color_brown();

    String googleCategoriesDisplayer_color_coral();

    String googleCategoriesDisplayer_color_aqua();

    String googleCategoriesDisplayer_color_fuchsia();

    String googleCategoriesDisplayer_color_gold();

    String googleCategoriesDisplayer_color_green();

    String googleCategoriesDisplayer_color_grey();

    String googleCategoriesDisplayer_color_lime();

    String googleCategoriesDisplayer_color_magenta();

    String googleCategoriesDisplayer_color_pink();

    String googleCategoriesDisplayer_color_silver();

    String googleCategoriesDisplayer_color_yellow();

}
