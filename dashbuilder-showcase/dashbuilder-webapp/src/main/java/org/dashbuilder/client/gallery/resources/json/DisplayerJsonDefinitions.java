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
package org.dashbuilder.client.gallery.resources.json;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

public interface DisplayerJsonDefinitions extends Constants {

    public static final DisplayerJsonDefinitions INSTANCE = GWT.create( DisplayerJsonDefinitions.class );

    String barChart_horizontal();

    String barChart_vertical();

    String barChart_multiple();

    String pieChart_basic();

    String pieChart_drillDown();

    String lineChart_basic();

    String lineChart_multiple();

    String lineChart_multipleStatic();

    String areaChart_basic();

    String areaChart_fixedMonth();

    String areaChart_drillDown();

    String bubbleChart_basic();

    String tableReport_basic();

    String tableReport_filtered();

    String tableReport_grouped();

    String tableReport_default();

    String meterChart_basic();

    String meterChart_multiple();

    String meterChart_multipleStatic();

    String geoMap();

}
