package org.dashbuilder.common.shared.validation.factory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.validation.client.AbstractGwtValidatorFactory;
import com.google.gwt.validation.client.GwtValidation;
import com.google.gwt.validation.client.impl.AbstractGwtValidator;
import org.dashbuilder.dataset.def.DataSetDef;

import javax.validation.Validator;

public class DashbuilderValidationFactory extends AbstractGwtValidatorFactory {

    @GwtValidation(DataSetDef.class)
    public interface DataSetDefValidator extends Validator {
    }

    @Override
    public AbstractGwtValidator createValidator() {
        return GWT.create(DataSetDefValidator.class);
    }
}