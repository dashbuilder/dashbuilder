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
package org.dashbuilder.client.samples.sales;

import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DataDisplayerBuilder;
import org.dashbuilder.model.displayer.DataDisplayerType;

import static org.dashbuilder.model.displayer.DataDisplayerType.*;

/**
 * A set of data displayer definitions built on top of the the Sales Opportunities sample data set.
 */
public class SalesOppsDisplayers {

    public static DataDisplayer pipelineStatus(DataDisplayerType type) {
        return new DataDisplayerBuilder()
                .title("Pipeline status")
                .type(type)
                .column("Pipeline")
                .column("Number of opps")
                .build();
    }

    public static DataDisplayer expectedPipeline(DataDisplayerType type)  {
        return new DataDisplayerBuilder()
            .title("Expected Pipeline")
            .type(type)
            .column("Closing date")
            .column("Expected amount")
            .build();
    }

    public static DataDisplayer byStatus(DataDisplayerType type) {
        return new DataDisplayerBuilder()
                .title("By Status")
                .type(type)
                .column("Status")
                .column("Total amount")
                .build();
    }

    public static DataDisplayer bySalesPerson(DataDisplayerType type) {
        return new DataDisplayerBuilder()
                .title("By Sales Person")
                .type(type)
                .column("Sales person")
                .column("Total amount")
                .build();
    }

    public static DataDisplayer byProduct(DataDisplayerType type) {
        return new DataDisplayerBuilder()
                .title("By Product")
                .type(type)
                .column("Product")
                .column("Total amount")
                .build();
    }

    public static DataDisplayer byCountry(DataDisplayerType type) {
        return new DataDisplayerBuilder()
                .title("By Country")
                .type(type)
                .column("Country")
                .column("Total amount")
                .build();
    }

    public static DataDisplayer byCountryMinMaxAvg(DataDisplayerType type) {
        return byCountryMinMaxAvg(type, 600, 400);
    }

    public static DataDisplayer byCountryMinMaxAvg(DataDisplayerType type, int width, int height) {
        return new DataDisplayerBuilder()
                .title("By Country (min/max/avg)")
                .type(type).width(width).height(height)
                .column("Country")
                .column("Min", "Min")
                .column("Max", "Max")
                .column("Average", "Avg")
                .build();
    }

    public static DataDisplayer byProbability(DataDisplayerType type) {
        return new DataDisplayerBuilder()
                .title("By Probability")
                .type(type)
                .column("Probability")
                .column("Total amount")
                .build();
    }

    public static DataDisplayer countrySummaryTable() {
        return new DataDisplayerBuilder()
                .title("Country Summary")
                .type(TABLE)
                .build();
    }

    public static DataDisplayer opportunitiesListing() {
        return new DataDisplayerBuilder()
                .title("List of Opportunities")
                .type(TABLE)
                .build();
    }
}
