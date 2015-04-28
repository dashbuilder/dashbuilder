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

    @GwtValidation(value = DataSetDef.class)
    public interface DataSetValidator extends Validator {
    }

    @GwtValidation(value = DataColumnDef.class)
    public interface DataColumnValidator extends Validator {
    }
    
    @GwtValidation(value = SQLDataSetDef.class, groups = {SQLDataSetDefDbTableValidation.class, SQLDataSetDefDbSQLValidation.class})
    public interface SQLDataSetDefValidator extends DataSetDefValidator {
    }

    @GwtValidation(value = CSVDataSetDef.class, groups = {CSVDataSetDefFilePathValidation.class, CSVDataSetDefFileURLValidation.class})
    public interface CSVDataSetDefValidator extends DataSetDefValidator {
    }

    @GwtValidation(value = BeanDataSetDef.class)
    public interface BeanDataSetDefValidator extends DataSetDefValidator {
    }

    @GwtValidation(value = ElasticSearchDataSetDef.class)
    public interface ELDataSetDefValidator extends DataSetDefValidator {
    }
    
    @GwtValidation(value = DataSetDef.class,  groups = {DataSetDefRefreshIntervalValidation.class, DataSetDefPushSizeValidation.class, DataSetDefCacheRowsValidation.class})
    public interface DataSetDefValidator extends Validator {
    }

    @Override
    public AbstractGwtValidator createValidator() {
        return GWT.create(DataSetDefValidator.class);
    }
}