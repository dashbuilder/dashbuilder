package org.dashbuilder.validations;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.validation.client.AbstractGwtValidatorFactory;
import com.google.gwt.validation.client.impl.AbstractGwtValidator;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.validations.factory.DashbuilderValidationFactory;

import javax.validation.Validator;

public class ValidatorFactory {
    
    public static Validator getDataSetDefValidator() {
        AbstractGwtValidator validator = GWT.create(DashbuilderValidationFactory.DataSetDefValidator.class);
        return getValidator(validator);
    }

    public static Validator getSQLDataSetDefValidator() {
        AbstractGwtValidator validator = GWT.create(DashbuilderValidationFactory.SQLDataSetDefValidator.class);
        return getValidator(validator);
    }
    
    private static Validator getValidator(AbstractGwtValidator validator) {
        AbstractGwtValidatorFactory validationFactory = (AbstractGwtValidatorFactory) com.google.gwt.validation.client.impl.Validation.buildDefaultValidatorFactory();
        validator.init(validationFactory.getConstraintValidatorFactory(), validationFactory.getMessageInterpolator(), validationFactory.getTraversableResolver() );
        return validator;
    }
}
