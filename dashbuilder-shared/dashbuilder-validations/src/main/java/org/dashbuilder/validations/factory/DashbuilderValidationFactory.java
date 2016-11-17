package org.dashbuilder.validations.factory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.validation.client.AbstractGwtValidatorFactory;
import com.google.gwt.validation.client.GwtValidation;
import com.google.gwt.validation.client.impl.AbstractGwtValidator;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.dataset.validation.groups.*;

import javax.validation.Validator;

/**
 * @since 0.3.0
 */
public class DashbuilderValidationFactory extends AbstractGwtValidatorFactory {

    @GwtValidation(value = {SQLDataSetDef.class, BeanDataSetDef.class, CSVDataSetDef.class, ElasticSearchDataSetDef.class, 
            DataSetDef.class, DataColumnDef.class},  
            groups = {DataSetDefBasicAttributesGroup.class, DataSetDefProviderTypeGroup.class,
                    DataSetDefRefreshIntervalValidation.class, 
                    DataSetDefPushSizeValidation.class, DataSetDefCacheRowsValidation.class,
                    SQLDataSetDefValidation.class, SQLDataSetDefDbTableValidation.class, SQLDataSetDefDbSQLValidation.class,
                    CSVDataSetDefValidation.class, CSVDataSetDefFilePathValidation.class, CSVDataSetDefFileURLValidation.class,
                    BeanDataSetDefValidation.class, ElasticSearchDataSetDefValidation.class
                    
    })
    public interface DashbuilderValidator extends Validator {
    }

    @Override
    public AbstractGwtValidator createValidator() {
        return GWT.create(DashbuilderValidator.class);
    }
}