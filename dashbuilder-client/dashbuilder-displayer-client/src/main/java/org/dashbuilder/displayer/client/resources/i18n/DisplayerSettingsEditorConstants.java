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
package org.dashbuilder.displayer.client.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface DisplayerSettingsEditorConstants extends ConstantsWithLookup {

    public static final DisplayerSettingsEditorConstants INSTANCE = GWT.create( DisplayerSettingsEditorConstants.class );

    // Position enum literals

    String POSITION_BOTTOM();

    String POSITION_TOP();

    String POSITION_LEFT();

    String POSITION_RIGHT();

    String POSITION_IN();


    // Common settings editor literals

    String settingsEditor_caption();

    String settingsJsonSource_caption();

    String common_showTitle();

    String common_title();

    String common_title_placeholder();

    String common_renderer();

    String common_columns();

    String common_columns_placeholder();

    String refresh_interval();

    String refresh_stale_data();

    String chart_width();

    String chart_height();

    String chart_topMargin();

    String chart_bottomMargin();

    String chart_leftMargin();

    String chart_rightMargin();

    String chart_legendShow();

    String chart_legendPosition();

    String chart_3d();

    String table_pageSize();

    String table_width();

    String table_sortEnabled();

    String table_sortColumn();

    String table_sortColumn_placeholder();

    String table_sortOrder();

    String table_ascSortOrder();

    String table_descSortOrder();

    String xaxis_showLabels();

    String xaxis_angle();

    String xaxis_title();

    String xaxis_title_placeholder();

    String yaxis_showLabels();

    String yaxis_angle();

    String yaxis_title();

    String yaxis_title_placeholder();

    String meter_start();

    String meter_warning();

    String meter_critical();

    String meter_end();

    String barchart_orientation();

    String barchart_horizontal();

    String barchart_vertical();

    String filter_enabled();

    String filter_self();

    String filter_listening();

    String filter_notifications();
}
