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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetGenerator;
import org.dashbuilder.dataset.group.DateIntervalType;

/**
 * Generates performance metrics on a mock cluster.
 * <p>It stores metrics for the last 100 seconds</p>
 */
public class ClusterMetricsDataSetGenerator implements DataSetGenerator {

    DataSet dataSet = null;
    long timeFrameMillis = 100000;
    List<String> aliveNodes = new ArrayList<String>();
    List<String> cpuHighNodes = new ArrayList<String>();
    List<String> cpuLowNodes = new ArrayList<String>();
    List<String> memHighNodes = new ArrayList<String>();
    List<String> memLowNodes = new ArrayList<String>();

    public ClusterMetricsDataSetGenerator() {
        dataSet = DataSetFactory.newDataSetBuilder()
                .column("server", "Server", ColumnType.LABEL)
                .column("time", "Time", ColumnType.DATE)
                .column("cpu", "CPU %", ColumnType.NUMBER)
                .column("mem", "Mem (Gb)", ColumnType.NUMBER)
                .buildDataSet();
    }

    public synchronized DataSet buildDataSet(Map<String,String> params) {

        // Check if the data set is up to date.
        long now = System.currentTimeMillis();
        long last = dataSet.getRowCount() > 0 ? ((Date)dataSet.getValueAt(0, 1)).getTime() : -1;
        long diff = now-last;
        if (last != -1 && diff < 1000) {
            return dataSet;
        }

        if (!StringUtils.isBlank(params.get("timeFrame"))) {
            String p = params.get("timeFrame");
            long millis = DateIntervalType.getDurationInMillis(p);
            if (millis != -1) timeFrameMillis = millis;
        }
        if (!StringUtils.isBlank(params.get("aliveNodes"))) {
            aliveNodes = Arrays.asList(StringUtils.split(params.get("aliveNodes"), ","));
        }
        if (!StringUtils.isBlank(params.get("cpuHighNodes"))) {
            cpuHighNodes = Arrays.asList(StringUtils.split(params.get("cpuHighNodes"), ","));
        }
        if (!StringUtils.isBlank(params.get("cpuLowNodes"))) {
            cpuLowNodes = Arrays.asList(StringUtils.split(params.get("cpuLowNodes"), ","));
        }
        if (!StringUtils.isBlank(params.get("memHighNodes"))) {
            memHighNodes = Arrays.asList(StringUtils.split(params.get("memHighNodes"), ","));
        }
        if (!StringUtils.isBlank(params.get("memLowNodes"))) {
            memLowNodes = Arrays.asList(StringUtils.split(params.get("memLowNodes"), ","));
        }
        if (aliveNodes.isEmpty()) {
            return dataSet;
        }
        if (diff > timeFrameMillis) {
            diff = timeFrameMillis;
        }

        // Create a new data set containing the missing metrics since the last update.
        DataSet newDataSet = dataSet.cloneEmpty();
        long seconds = diff / 1000;
        for (long i = 0; i <seconds; i++) {
            long metricTime = now - i*1000;
            for (int j = 0; j < aliveNodes.size(); j++) {
                String node = aliveNodes.get(j);
                newDataSet.addValues(node, new Date(metricTime), cpu(node), mem(node));
            }
        }
        // Add the remain metric history
        boolean outOfBounds = false;
        Date threshold = new Date(now - timeFrameMillis);
        for (int i = 0; i < dataSet.getRowCount() && !outOfBounds; i++) {
            Date metricTime = (Date)dataSet.getValueAt(i, 1);
            if (metricTime.after(threshold)) {
                newDataSet.addValues(
                        dataSet.getValueAt(i, 0),
                        dataSet.getValueAt(i, 1),
                        dataSet.getValueAt(i, 2),
                        dataSet.getValueAt(i, 3));
            } else {
                outOfBounds = true;
            }
        }
        return dataSet = newDataSet;
    }

    public int cpu(String node) {
        double r = Math.random();
        if (cpuHighNodes.contains(node)) {
            return (int) (80 + 20*r);
        }
        if (cpuLowNodes.contains(node)) {
            return (int) (0 + 20*r);
        }
        return (int) (20 + 60*r);
    }

    public int mem(String node) {
        double r = Math.random();
        if (memHighNodes.contains(node)) {
            return  (int) (50 + 14*r);
        }
        if (memLowNodes.contains(node)) {
            return (int) (0 + 4*r);
        }
        return (int) (4 + 46*r);
    }

    public static void main(String[] args) throws Exception {
        ClusterMetricsDataSetGenerator g = new ClusterMetricsDataSetGenerator();
        Map<String,String> params = new HashMap<String, String>();
        params.put("aliveNodes", "server1");
        params.put("timeFrame", "10second");
        DataSet dataSet = g.buildDataSet(params);
        Thread.sleep(3000);
        dataSet = g.buildDataSet(params);
        Thread.sleep(1000);
    }
}
