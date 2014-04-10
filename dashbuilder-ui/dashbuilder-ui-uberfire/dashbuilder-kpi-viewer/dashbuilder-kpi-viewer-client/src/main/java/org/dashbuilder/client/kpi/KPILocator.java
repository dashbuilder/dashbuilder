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
package org.dashbuilder.client.kpi;

import java.util.List;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.group.Domain;
import org.dashbuilder.model.dataset.group.DomainStrategy;
import org.dashbuilder.model.dataset.group.DomainType;
import org.dashbuilder.model.dataset.group.Range;
import org.dashbuilder.model.dataset.group.DataSetGroup;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.client.js.JsDataDisplayer;
import org.dashbuilder.client.js.JsObjectHelper;
import org.dashbuilder.model.kpi.*;
import org.dashbuilder.model.kpi.impl.KPIImpl;

@ApplicationScoped
public class KPILocator {

    public static final String[] SAMPLE_DATASETS = {
        " {\n" +
        "  \"columns\": [\n" +
        "      {\n" +
        "          \"id\": \"department\",\n" +
        "          \"type\": \"label\",\n" +
        "          \"name\": \"Department\",\n" +
        "          \"values\": [\"Sales\", \"Engineering\", \"Administration\", \"HR\"]\n" +
        "      },\n" +
        "      {\n" +
        "          \"id\": \"amount\",\n" +
        "          \"type\": \"label\",\n" +
        "          \"name\": \"Total expenses amount\",\n" +
        "          \"values\": [\"10300.45\", \"9000.00\", \"3022.44\", \"22223.56\"]\n" +
        "      }\n" +
        "  ]\n" +
        " }",
        " {\n" +
        "  \"columns\": [\n" +
        "      {\n" +
        "          \"id\": \"country\",\n" +
        "          \"type\": \"label\",\n" +
        "          \"name\": \"\",\n" +
        "          \"values\": [\"Spain\", \"USA\", \"Brazil\", \"UK\", \"Canada\", \"Portugal\", \"Australia\", \"France\"]\n" +
        "      },\n" +
        "      {\n" +
        "          \"id\": \"salesrate\",\n" +
        "          \"type\": \"label\",\n" +
        "          \"name\": \"Sales rate\",\n" +
        "          \"values\": [\"103\", \"34\", \"45\", \"23\", \"22\", \"212\", \"12\", \"34\"]\n" +
        "      }\n" +
        "  ]\n" +
        " }"};

    public static final String[] SAMPLE_DISPLAYERS = {
        "{\n" +
        "     \"title\": \"Expenses per department\",\n" +
        "     \"type\": \"piechart\",\n" +
        "     \"renderer\": \"google\",\n" +
        "     \"xAxis\": {\"columnId\": \"department\", \"displayName\": \"Department\"},\n" +
        "     \"yAxes\": [{\"columnId\": \"amount\", \"displayName\": \"Total amount\"}]\n" +
        " }",
        "{\n" +
        "     \"title\": \"Sales rate by country\",\n" +
        "     \"type\": \"barchart\",\n" +
        "     \"renderer\": \"google\",\n" +
        "     \"xAxis\": {\"columnId\": \"department\", \"displayName\": \"Country\"},\n" +
        "     \"yAxes\": [{\"columnId\": \"amount\", \"displayName\": \"Sales rate\"}]\n" +
        " }"};

    public static final String DISPLAYER1 =
            "{\n" +
            "     \"title\": \"Oppportunities amount per stage\",\n" +
            "     \"type\": \"piechart\",\n" +
            "     \"renderer\": \"google\",\n" +
            "     \"xAxis\": {\"columnId\": \"pipeline\", \"displayName\": \"Pipeline\"},\n" +
            "     \"yAxes\": [{\"columnId\": \"total\", \"displayName\": \"Total amount\"}]\n" +
            " }";

private List<KPI> kpiList = new ArrayList<KPI>();

    @PostConstruct
    public void init() {
        for (int i = 0; i < SAMPLE_DISPLAYERS.length; i++) {
            // Parse the JSON
            JsDataDisplayer jsDisplayer = JsDataDisplayer.fromJson(SAMPLE_DISPLAYERS[i]);
            DataDisplayer displayer = JsObjectHelper.createDataDisplayer(jsDisplayer);

            // Create the KPI
            KPIImpl kpi = new KPIImpl();
            kpi.setUUID("sample" + i);
            kpi.setDataSetLookup(new DataSetLookup("test-sample"));
            kpi.setDataDisplayer(displayer);
            kpiList.add(kpi);
        }

        DomainStrategy strategy = new DomainStrategy();
        strategy.setDomainType(DomainType.FIXED);

        DataSetGroup group = new DataSetGroup();
        Domain domain = new Domain("pipeline", strategy);
        Range range = new Range("amount", "total", "Total amount", "sum");
        group.addDomains(domain);
        group.addRanges(range);
        DataSetLookup lookup = new DataSetLookup("dataset-sales-opportunities", group);

        JsDataDisplayer jsDisplayer = JsDataDisplayer.fromJson(DISPLAYER1);
        DataDisplayer displayer = JsObjectHelper.createDataDisplayer(jsDisplayer);

        // Create the KPI
        KPIImpl kpi = new KPIImpl();
        kpi.setUUID("test-group");
        kpi.setDataSetLookup(lookup);
        kpi.setDataDisplayer(displayer);
        kpiList.add(kpi);
    }

    public KPI getKPI(String uid) {
        for (KPI kpi : kpiList) {
            if (kpi.getUUID().equals(uid)) return kpi;
        }
        // Return the first sample by default.
        return kpiList.get(0);
    }
}
