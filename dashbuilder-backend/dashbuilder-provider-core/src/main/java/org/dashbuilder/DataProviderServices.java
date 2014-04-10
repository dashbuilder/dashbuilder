package org.dashbuilder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.dashbuilder.function.ScalarFunctionManager;
import org.dashbuilder.model.dataset.DataSetManager;
import org.uberfire.commons.services.cdi.Startup;

@ApplicationScoped
@Startup
public class DataProviderServices {

    protected static DataSetManager dataSetManager;
    protected static ScalarFunctionManager scalarFunctionManager;

    public static DataSetManager getDataSetManager() {
        return dataSetManager;
    }

    public static ScalarFunctionManager getScalarFunctionManager() {
        return scalarFunctionManager;
    }

    @PostConstruct
    public void init() {
        dataSetManager = _dataSetManager;
        scalarFunctionManager = _scalarFunctionManager;
    }

    @Inject
    protected DataSetManager _dataSetManager;

    @Inject
    protected ScalarFunctionManager _scalarFunctionManager;
}

