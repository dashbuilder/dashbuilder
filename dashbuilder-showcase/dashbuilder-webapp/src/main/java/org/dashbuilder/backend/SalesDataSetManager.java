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
package org.dashbuilder.backend;

import java.util.Date;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dashbuilder.backend.command.CommandEvent;
import org.dashbuilder.shared.sales.SalesConstants;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetManager;
import org.uberfire.commons.services.cdi.Startup;

/**
 * Generates a random data set containing sales opportunity records.
 */
@ApplicationScoped
@Startup
public class SalesDataSetManager {

    @Inject SalesDataSetGenerator salesDataSetGenerator;
    @Inject DataSetManager dataSetManager;

    @PostConstruct
    private void init() {
        // Generate the data set to be used by the Showcase Gallery and by the Sales sample dashboards.
        Date currentDate = new Date();
        DataSet salesDataSet = salesDataSetGenerator.generateDataSet(SalesConstants.SALES_OPPS, 30, currentDate.getYear()-1, currentDate.getYear()+3);
        dataSetManager.registerDataSet(salesDataSet);
    }

    /**
     * Listen to commands issued from the operating system shell.
     */
    private void onCommandReceived(@Observes CommandEvent event) {
        String[] command = event.getCommand().split("\\s+");
        if (command.length < 2 || !command[0].trim().toLowerCase().equals("sales")) {
            return;
        }
        String op =  command[1].trim().toLowerCase();
        if ("refresh".equals(op)) {
            init();
        }
    }
}
