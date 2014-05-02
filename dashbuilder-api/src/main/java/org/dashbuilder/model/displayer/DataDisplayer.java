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
package org.dashbuilder.model.displayer;

import java.util.List;

public interface DataDisplayer {

    String getTitle();
    void setTitle(String title);
    boolean isTitleVisible();
    void setTitleVisible(boolean visible);
    DataDisplayerType getType();
    void setType(DataDisplayerType type);
    DataDisplayerRenderer getRenderer();
    void setRenderer(DataDisplayerRenderer renderer);
    List<DataDisplayerColumn> getColumnList();

/*
    String getForegroundColor();
    String getBackgroundColor();
    int getWidth();
    int getHeight();
    String getGraphicAlign();
    boolean isLegendVisible();
    boolean isRoundToIntegerEnabled();
    boolean isTitleVisible();
    String getLegendAnchor();

    // Sort criteria must belong to data set retrieval settings
    int getSortCriteria();
    int getSortOrder();

    int getNumberOfRowsPerPage();

    */
/** The display angle for the X-axis labels. *//*

    int getXAxisLabelsAngle();

    */
/** Display area below line *//*

    boolean isAreaVisible();

*/
}
