package org.dashbuilder.kpi.client;

import java.util.List;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.client.js.JsDataDisplayer;
import org.dashbuilder.client.js.JsDataSet;
import org.dashbuilder.client.kpi.*;

@ApplicationScoped
public class KPILocator {

    public static final String[] SAMPLE_DATASETS = {
        " {\n" +
        "  \"columns\": [\n" +
        "      {\n" +
        "          \"id\": \"department\",\n" +
        "          \"type\": \"label\",\n" +
        "          \"name\": \"Department\",\n" +
        "          \"values\": {\"sales\", \"engineering\", \"administration\", \"HR\"}\n" +
        "      },\n" +
        "      {\n" +
        "          \"id\": \"amount\",\n" +
        "          \"type\": \"label\",\n" +
        "          \"name\": \"Total expenses amount\",\n" +
        "          \"values\": {\"10300.45\", \"9000.00\", \"3022.44\", \"22223.56\"}\n" +
        "      }\n" +
        "  ]\n" +
        " }",
        " {\n" +
        "  \"columns\": [\n" +
        "      {\n" +
        "          \"id\": \"country\",\n" +
        "          \"type\": \"label\",\n" +
        "          \"name\": \"\",\n" +
        "          \"values\": {\"spain\", \"usa\", \"brazil\", \"uk\", \"canada\", \"portugal\", \"australia\", \"france\"}\n" +
        "      },\n" +
        "      {\n" +
        "          \"id\": \"salesrate\",\n" +
        "          \"type\": \"label\",\n" +
        "          \"name\": \"Sales rate\",\n" +
        "          \"values\": {\"103\", \"34\", \"45\", \"23\", \"22\", \"212\", \"12\", \"34\"}\n" +
        "      }\n" +
        "  ]\n" +
        " }"};

    public static final String[] SAMPLE_DISPLAYERS = {
        "{\n" +
        "     \"title\": \"Expenses amount per department\",\n" +
        "     \"type\": \"piechart\",\n" +
        "     \"renderer\": \"google\",\n" +
        "     \"xAxis\": {\"columnId:\", \"department\", \"displayName\": \"Department\"}\n" +
        "     \"yAxes\": [{\"columnId:\", \"amount\", \"displayName\": \"Total amount\"}]\n" +
        " }",
        "{\n" +
        "     \"title\": \"Expenses amount per department\",\n" +
        "     \"type\": \"piechart\",\n" +
        "     \"renderer\": \"google\",\n" +
        "     \"xAxis\": {\"columnId:\", \"country\", \"displayName\": \"Country\"}\n" +
        "     \"yAxes\": [{\"columnId:\", \"salesrate\", \"displayName\": \"Sales rate\"}]\n" +
        " }"};

    private List<KPI> kpiList = new ArrayList<KPI>();

    @PostConstruct
    public void init() {
        for (int i = 0; i < SAMPLE_DATASETS.length; i++) {
            JsDataSet jsDataSet = JsDataSet.fromJson(SAMPLE_DATASETS[i]);
            JsDataDisplayer jsDisplayer = JsDataDisplayer.fromJson(SAMPLE_DISPLAYERS[i]);
            KPIImpl kpi = new KPIImpl();
            kpi.setUID("sample" + i);
            kpi.setDataSet(jsDataSet);
            kpi.setDataDisplayer(jsDisplayer);
            kpiList.add(kpi);
        }
    }

    public KPI getKPI(String uid) {
        for (KPI kpi : kpiList) {
            if (kpi.getUID().equals(uid)) return kpi;
        }
        // Return the first sample by default.
        return kpiList.get(0);
    }
}
