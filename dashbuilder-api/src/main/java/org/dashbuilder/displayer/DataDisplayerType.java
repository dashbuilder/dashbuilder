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

/**
 * An enumeration for the different types of DataDisplayer.
 */
public enum DataDisplayerType {

    /**
     * A Bar Chart DataDisplayer.
     */
    BARCHART,

    /**
     * A Pie Chart DataDisplayer.
     */
    PIECHART,

    /**
     * An Area Chart DataDisplayer.
     */
    AREACHART,

    /**
     * A Line Chart DataDisplayer.
     */
    LINECHART,

    /**
     * A Bubble Chart DataDisplayer.
     */
    BUBBLECHART,

    /**
     * A Meter Chart DataDisplayer.
     */
    METERCHART,

    /**
     * A Table DataDisplayer.
     */
    TABLE,

    /**
     * A Map DataDisplayer.
     */
    MAP;

    public static DataDisplayerType getByName(String str) {
        return valueOf(str.toUpperCase());
    }
}
