/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.client.navigation.widget;

/**
 * The {@link NavWidget} implementations supporting this interface are able to display multiple nested levels for
 * a given root navigation group. For example, the {@link NavMenuBarWidget} or {@link NavTilesWidget} implementations.
 */
public interface HasMaxLevels {

    /**
     * The number of maximum levels to display.
     * @return A value lower than 1 means unlimited levels
     */
    int getMaxLevels();

    /**
     * Sets the number of maximum levels to display.
     * @param levels A value lower than 1 means unlimited levels
     */
    void setMaxLevels(int levels);
}
