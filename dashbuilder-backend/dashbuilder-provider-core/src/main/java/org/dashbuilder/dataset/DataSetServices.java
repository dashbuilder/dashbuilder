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

    public DataSetManager getDataSetManager() {
        return dataSetManager;
    }

    public DataSetOpEngine getDataSetOpEngine() {
        return dataSetOpEngine;
    }

    public DataSetIndexRegistry getDataSetIndexRegistry() {
        return dataSetIndexRegistry;
    }

    public DataSetSortAlgorithm getDataSetSortAlgorithm() {
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

