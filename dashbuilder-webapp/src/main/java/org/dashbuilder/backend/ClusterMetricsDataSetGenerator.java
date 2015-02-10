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

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataset.*;
import org.dashbuilder.dataset.date.TimeAmount;
import org.dashbuilder.dataset.date.TimeFrame;

import java.util.*;

/**
 * Generates performance metrics on a mock cluster.
 * <p>It stores metrics for the last 100 seconds</p>
 */
public class ClusterMetricsDataSetGenerator implements DataSetGenerator {

    public static final String COLUMN_SERVER = "server";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_CPU0 = "cpu0";
    public static final String COLUMN_CPU1 = "cpu1";
    public static final String COLUMN_MEMORY_FREE = "mem_free";
    public static final String COLUMN_MEMORY_USED = "mem_used";
    public static final String COLUMN_NETWORK_TX = "tx";
    public static final String COLUMN_NETWORK_RX = "rx";
    public static final String COLUMN_PROCESSES_RUNNING = "p_running";
    public static final String COLUMN_PROCESSES_SLEEPING  = "p_sleeping";
    public static final String COLUMN_DISK_FREE = "disk_free";
    public static final String COLUMN_DISK_USED = "disk_used";
    
    
    DataSet dataSet = null;
    long timeFrameMillis = 100000;
    List<String> aliveNodes = new ArrayList<String>();
    List<String> overloadedNodes = new ArrayList<String>();

    public ClusterMetricsDataSetGenerator() {
        dataSet = DataSetFactory.newDataSetBuilder()
                .column(COLUMN_SERVER, "Server", ColumnType.LABEL)
                .column(COLUMN_TIMESTAMP, "Time", ColumnType.DATE)
                .column(COLUMN_CPU0, "CPU0 %", ColumnType.NUMBER)
                .column(COLUMN_CPU1, "CPU1 %", ColumnType.NUMBER)
                .column(COLUMN_MEMORY_FREE, "Mem (Gb)", ColumnType.NUMBER)
                .column(COLUMN_MEMORY_USED, "Mem (Gb)", ColumnType.NUMBER)
                .column(COLUMN_NETWORK_TX, "Upstream (Kbps)", ColumnType.NUMBER)
                .column(COLUMN_NETWORK_RX, "Downstream (Kbps)", ColumnType.NUMBER)
                .column(COLUMN_PROCESSES_RUNNING, "Running processes", ColumnType.NUMBER)
                .column(COLUMN_PROCESSES_SLEEPING, "Sleeping processes", ColumnType.NUMBER)
                .column(COLUMN_DISK_FREE, "Free disk space (Gb)", ColumnType.NUMBER)
                .column(COLUMN_DISK_USED, "Used disk space (Gb)", ColumnType.NUMBER)
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
            if (p != null && p.trim().length() > 0) {
                TimeAmount timeFrame = TimeAmount.parse(p);
                timeFrameMillis = timeFrame.toMillis();
            }
        }
        if (params.containsKey("aliveNodes")) {
            aliveNodes.clear();
            aliveNodes.addAll(Arrays.asList(StringUtils.split(params.get("aliveNodes"), ",")));
        }
        if (params.containsKey("overloadedNodes")) {
            overloadedNodes.clear();
            overloadedNodes.addAll(Arrays.asList(StringUtils.split(params.get("overloadedNodes"), ",")));
        }
        if (aliveNodes.isEmpty()) {
            return dataSet;
        }
        if (diff > timeFrameMillis) {
            diff = timeFrameMillis;
        }

        // Create a new data set containing the missing metrics since the last update.
        if (last == -1) last = now-timeFrameMillis;
        DataSet newDataSet = dataSet.cloneEmpty();
        long seconds = diff / 1000;
        Integer lastCpu0 = (dataSet.getRowCount() > 0 ? ((Double) dataSet.getValueAt(0, 2)).intValue() : null);
        Integer lastCpu1 = (dataSet.getRowCount() > 0 ? ((Double) dataSet.getValueAt(0, 3)).intValue() : null);
        Integer lastFreeMem = (dataSet.getRowCount() > 0 ? ((Double) dataSet.getValueAt(0, 4)).intValue() : null);
        Integer lastUsedMem = (dataSet.getRowCount() > 0 ? ((Double) dataSet.getValueAt(0, 5)).intValue() : null);
        Integer lastTx = (dataSet.getRowCount() > 0 ? ((Double) dataSet.getValueAt(0, 6)).intValue() : null);
        Integer lastRx = (dataSet.getRowCount() > 0 ? ((Double) dataSet.getValueAt(0, 7)).intValue() : null);
        Integer lastRunningProc = (dataSet.getRowCount() > 0 ? ((Double) dataSet.getValueAt(0, 8)).intValue() : null);
        Integer lastSleepingProc = (dataSet.getRowCount() > 0 ? ((Double) dataSet.getValueAt(0, 9)).intValue() : null);
        Integer lastFreeDisk = (dataSet.getRowCount() > 0 ? ((Double) dataSet.getValueAt(0, 10)).intValue() : null);
        Integer lastUsedDisk = (dataSet.getRowCount() > 0 ? ((Double) dataSet.getValueAt(0, 11)).intValue() : null);
        for (long i = 1; i <=seconds; i++) {
            long metricTime = last + i*1000;
            for (int j = 0; j < aliveNodes.size(); j++) {
                String node = aliveNodes.get(j);
                newDataSet.addValuesAt(0, node, new Date(metricTime), 
                        cpu(node, lastCpu0), cpu(node, lastCpu1),  
                        mem(node, lastFreeMem), mem(node, lastUsedMem), 
                        net(node, lastTx), net(node, lastRx),
                        proc(node, lastRunningProc), proc(node, lastSleepingProc),
                        disk(node, lastFreeDisk), disk(node, lastUsedDisk));
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
                        dataSet.getValueAt(i, 3),
                        dataSet.getValueAt(i, 4),
                        dataSet.getValueAt(i, 5),
                        dataSet.getValueAt(i, 6),
                        dataSet.getValueAt(i, 7),
                        dataSet.getValueAt(i, 8),
                        dataSet.getValueAt(i, 9),
                        dataSet.getValueAt(i, 10),
                        dataSet.getValueAt(i, 11));
            } else {
                outOfBounds = true;
            }
        }
        return dataSet = newDataSet;
    }

    /**
     * Network (kbps)
     * Overloaded values : from 3000 kbps (3,75 MB/s) to 4000 kbps (5,00 MB/s)
     */
    public Double net(String node, Integer last) {
        double r = Math.random();
        if (overloadedNodes.contains(node)) {
            if (last == null) {
                return 3000 + 1000 * r;
            } else {
                double v = last + 100 * r;
                if (v > 4000) return 4000d;
                if (v < 3000) return 3000d;
                return v;
            }
        }
        if (last == null) {
            return 1000 + 100 * r;
        } else {
            double v = (last < 3500) ? last + 100  * r : last - 100  * r; 
            if (v > 4000) return 4000d;
            if (v < 0) return 0d;
            return v;
        }
    }

    /**
     * Processes (count)
     * Overloaded values : from 1024 to 1500
     */
    public Double proc(String node, Integer last) {
        double r = Math.random();
        if (overloadedNodes.contains(node)) {
            if (last == null) {
                return 1024 + 100 * r;
            } else {
                double v = last + 100 * r;
                if (v > 1500) return 1500d;
                if (v < 0) return 0d;
                return v;
            }
        }
        if (last == null) {
            return 280 + 100 * r;
        } else {
            double v = last + 10 * r;
            if (v > 1500) return 1500d;
            if (v < 0) return 0d;
            return v;
        }
    }

    /**
     * Disk space (Gb) 
     * Overloaded values : from 3000Gb (3Tb) to 4000Gb (4Tb) 
     */
    public Double disk(String node, Integer last) {
        double r = Math.random();
        if (overloadedNodes.contains(node)) {
            if (last == null) {
                return 3000 + 100 * r;
            } else {
                double v = last + 100 * r;
                if (v > 4000) return 4000d;
                if (v < 3000) return 3000d;
                return v;
            }
        }
        if (last == null) {
            return 1000 + 10 * r;
        } else {
            double v = last + 10 * r;
            if (v > 4000) return 40000d;
            if (v < 0) return 0d;
            return v;
        }
    }

    /**
     * CPU (%) 
     * Overloaded values : from 90% to 100% 
     */
    public Double cpu(String node, Integer last) {
        double r = Math.random() - 0.5;
        if (overloadedNodes.contains(node)) {
            if (last == null) {
                return 90 + 10 * r;
            } else {
                double v = last + 10 * r;
                if (v > 100) return 100d;
                if (v < 90) return 90d;
                return v;
            }
        }
        if (last == null) {
            return 20 + 20 * r;
        } else {
            double v = last + 10 * r;
            if (v > 100) return 100d;
            if (v < 0) return 0d;
            return v;
        }
    }

    /**
     * Memory (Gb) 
     * Overloaded values : from 3Gb to 4Gb 
     */
    public Double mem(String node, Integer last) {
        double r = Math.random();
        if (overloadedNodes.contains(node)) {
            if (last == null) {
                return 3 + 0.5 * r;
            } else {
                double v = last + 0.5 * r;
                if (v > 4) return 4d;
                if (v < 0) return 0d;
                return v;
            }
        }
        if (last == null) {
            return 1 + 1 * r;
        } else {
            double v = (last < 3.5) ? last + 1.5 * r : last - 1.5 * r;
            if (v > 4) return 4d;
            if (v < 0) return 0d;
            return v;
        }
    }

    public static void main(String[] args) throws Exception {
        ClusterMetricsDataSetGenerator g = new ClusterMetricsDataSetGenerator();
        Map<String,String> params = new HashMap<String, String>();
        params.put("aliveNodes", "server1");
        params.put("timeFrame", "10second");
        System.out.println("************* Single node not overloaded *******************************");
        for (int i = 0; i < 5; i++) {
            DataSet dataSet = g.buildDataSet(params);
            printDataSet(dataSet);
            Thread.sleep(1000);
        }

        System.out.println("************* Two nodes and the second one overloaded *******************************");
        g = new ClusterMetricsDataSetGenerator();
        params = new HashMap<String, String>();
        params.put("aliveNodes", "server1,server2");
        params.put("overloadedNodes", "server2");
        params.put("timeFrame", "10second");
        for (int i = 0; i < 5; i++) {
            DataSet dataSet = g.buildDataSet(params);
            printDataSet(dataSet);
            Thread.sleep(1000);
        }
    }

    /**
     * Helper method to print to standard output the dataset values.
     */
    protected static void printDataSet(DataSet dataSet) {
        final String SPACER = "| \t |";

        if (dataSet == null) System.out.println("DataSet is null");
        if (dataSet.getRowCount() == 0) System.out.println("DataSet is empty");

        List<DataColumn> dataSetColumns = dataSet.getColumns();
        int colColunt = dataSetColumns.size();
        int rowCount = dataSet.getRowCount();

        System.out.println("********************************************************************************************************************************************************");
        for (int row = 0; row < rowCount; row++) {
            System.out.print(SPACER);
            for (int col= 0; col< colColunt; col++) {
                Object value = dataSet.getValueAt(row, col);
                String colId = dataSet.getColumnByIndex(col).getId();
                System.out.print(colId + ": " +  value);
                System.out.print(SPACER);
            }
            System.out.println("");
        }
        System.out.println("********************************************************************************************************************************************************");
    }
}
