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

/**
 * The top level interface for a data displayer. A DataDisplayer contains the information necessary to visualize data
 * in a specific way.
 */
public interface DataDisplayer {

    /**
     * @return The caption that will be given to the specific data visualization.
     */
    String getTitle();

    /**
     * Set the caption that will be given to the specific data visualization.
     * @param title The caption.
     */
    void setTitle(String title);

    /**
     * @return True if the caption will be visible, false if not.
     */
    boolean isTitleVisible();

    /**
     * Set whether the caption should be visible or not.
     * @param visible True if the caption is to be visible, false if not.
     */
    void setTitleVisible(boolean visible);

    /**
     * @return The type of this DataDisplayer.
     * @see org.dashbuilder.model.displayer.DataDisplayerType
     */
    DataDisplayerType getType();

    /**
     * @return The identifier of the renderer for this displayer
     */
    String getRenderer();

    /**
     * Set the renderer that will be used for visualizing this DataDisplayer.
     * @param renderer The identifier of the renderer.
     */
    void setRenderer(String renderer);

    /**
     * @return A List of DataDisplayerColumns that were configured for this DataDisplayer,
     * or an empty list if none were configured. In the latter case, The DataSet will be introspected in order to
     * visualize the data it contains.
     * @see org.dashbuilder.model.displayer.DataDisplayerColumn
     */
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
