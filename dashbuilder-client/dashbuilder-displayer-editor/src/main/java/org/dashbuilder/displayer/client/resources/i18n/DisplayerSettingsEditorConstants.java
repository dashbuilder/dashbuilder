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

    String common_showTitle();

    String common_title();

    String common_title_placeholder();

    String common_renderer();

    String common_columns();

    String common_columns_placeholder();

    String chart_width();

    String chart_height();

    String chart_topMargin();

    String chart_bottomMargin();

    String chart_leftMargin();

    String chart_rightMargin();

    String chart_legendShow();

    String chart_legendPosition();

    String xaxis_showLabels();

    String xaxis_angle();

    String xaxis_title();

    String xaxis_title_placeholder();
}
