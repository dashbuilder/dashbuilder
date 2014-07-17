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
package org.dashbuilder.displayer;

import java.util.List;

/**
 * The top level interface for a data displayer. A DisplayerSettings contains the information necessary to visualize data
 * in a specific way.
 */
public interface DisplayerSettings {

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
     * @return The type of this DisplayerSettings.
     * @see DisplayerType
     */
    DisplayerType getType();

    /**
     * @return The identifier of the renderer for this displayer
     */
    String getRenderer();

    /**
     * Set the renderer that will be used for visualizing this DisplayerSettings.
     * @param renderer The identifier of the renderer.
     */
    void setRenderer(String renderer);

    /**
     * @return A List of DisplayerSettingsColumns that were configured for this DisplayerSettings,
     * or an empty list if none were configured. In the latter case, the DataSet will be introspected in order to
     * visualize the data it contains.
     * @see DisplayerSettingsColumn
     */
    List<DisplayerSettingsColumn> getColumnList();

    /**
     * Check if the ability to select & filter values (or range of values) is enabled for this displayer.
     */
    boolean isFilterEnabled();

    /**
     * Switch on/off the ability to generate filter requests within this displayer.
     */
    void setFilterEnabled(boolean selectionEnabled);

    /**
     * Check whether the displayer is sensitive to filter requests made on the displayer itself.
     */
    boolean isFilterSelfApplyEnabled();

    /**
     * Switch on/off applying filters on the displayer itself.
     */
    void setFilterSelfApplyEnabled(boolean selectionSelfApplyEnabled);

    /**
     * Check if the displayer notifies any filter made on it to other displayers. Usually, in a dashboard there
     * exists a unique coordinator which takes cares of propagate all the filter events among the other displayers.
     */
    boolean isFilterNotificationEnabled();

    /**
     * Switch on/off the filter event notification to other displayers.
     */
    void setFilterNotificationEnabled(boolean selectionNotificationEnabled);

    /**
     * Check if this displayer listen to filter events coming from other displayers.
     */
    boolean isFilterListeningEnabled();

    /**
     * Switch on/off the ability to listen to filter events coming from other displayers.
     */
    void setFilterListeningEnabled(boolean selectionListeningEnabled);
}
