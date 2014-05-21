package org.dashbuilder.dataset;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.dashbuilder.config.Config;
import org.dashbuilder.dataset.engine.DataSetOpEngine;
import org.dashbuilder.dataset.index.spi.DataSetIndexRegistry;
import org.dashbuilder.model.dataset.DataSetManager;
import org.dashbuilder.model.dataset.sort.DataSetSortAlgorithm;
import org.uberfire.commons.services.cdi.Startup;

@ApplicationScoped
@Startup
public class DataSetServices {

    protected static DataSetManager _static_dataSetManager = null;
    protected static DataSetOpEngine _static_dataSetOpEngine = null;
    protected static DataSetIndexRegistry _static_dataSetIndexRegistry = null;
    protected static DataSetSortAlgorithm _static_dataSetSortAlgorithm = null;

    public static void setDataSetManager(DataSetManager implementation) {
        _static_dataSetManager = implementation;
    }

    public static void setDataSetOpEngine(DataSetOpEngine implementation) {
        _static_dataSetOpEngine = implementation;
    }

    public static void setDataSetIndexRegistry(DataSetIndexRegistry implementation) {
        _static_dataSetIndexRegistry = implementation;
    }

    public static void setDataSetSortAlgorithm(DataSetSortAlgorithm implementation) {
        _static_dataSetSortAlgorithm = implementation;
    }

    public DataSetManager getDataSetManager() {
        if (_static_dataSetManager != null) return _static_dataSetManager;
        return dataSetManager;
    }

    public DataSetOpEngine getDataSetOpEngine() {
        if (_static_dataSetOpEngine != null) return _static_dataSetOpEngine;
        return dataSetOpEngine;
    }

    public DataSetIndexRegistry getDataSetIndexRegistry() {
        if (_static_dataSetIndexRegistry != null) return _static_dataSetIndexRegistry;
        return dataSetIndexRegistry;
    }

    public DataSetSortAlgorithm getDataSetSortAlgorithm() {
        if (_static_dataSetSortAlgorithm != null) return _static_dataSetSortAlgorithm;
        return dataSetSortAlgorithm;
    }

    /**
     * The DataSetStorage implementation used to persist data set indexes.
     */
    @Inject @Config("org.dashbuilder.dataset.index.TransientDataSetIndexRegistry")
    protected String DATASET_INDEX_REGISTRY_CLASS;

    /**
     * The SortAlgorithm implementation used to sort data sets.
     */
    @Inject @Config("org.dashbuilder.dataset.sort.CollectionsDataSetSort")
    protected String DATASET_SORT_CLASS;

    @Inject
    protected DataSetManager dataSetManager;

    @Inject
    protected DataSetOpEngine dataSetOpEngine;

    @Inject
    protected Instance<DataSetIndexRegistry> dataSetIndexRegList;
    protected DataSetIndexRegistry dataSetIndexRegistry;

    @Inject
    protected Instance<DataSetSortAlgorithm> sortAlgorithmList;
    protected DataSetSortAlgorithm dataSetSortAlgorithm;

    @PostConstruct
    public void init() {

        for (DataSetIndexRegistry i : dataSetIndexRegList) {
            String c = i.getClass().getName();
            if (c.contains(DATASET_INDEX_REGISTRY_CLASS)) {
                dataSetIndexRegistry = i;
            }
        }
        if (dataSetIndexRegistry == null) {
            throw new IllegalStateException("DATASET_INDEX_REGISTRY_CLASS not found: " + DATASET_INDEX_REGISTRY_CLASS);
        }
        for (DataSetSortAlgorithm i : sortAlgorithmList) {
            String c = i.getClass().getName();
            if (c.contains(DATASET_SORT_CLASS)) {
                dataSetSortAlgorithm = i;
            }
        }
        if (dataSetSortAlgorithm == null) {
            throw new IllegalStateException("DATASET_SORT_CLASS not found: " + DATASET_SORT_CLASS);
        }
    }
}

