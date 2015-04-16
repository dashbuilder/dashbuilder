package org.dashbuilder.validations;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.validation.client.AbstractGwtValidatorFactory;
import com.google.gwt.validation.client.impl.AbstractGwtValidator;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.validations.factory.DashbuilderValidationFactory;

import javax.validation.Validator;

/**
 * @since 0.3.0
 */
public final class ValidatorFactory {

    public static Validator getDataSetValidator() {
        AbstractGwtValidator validator = GWT.create(DashbuilderValidationFactory.DataSetValidator.class);
        return getValidator(validator);
    }

    public static Validator getDataColumnValidator() {
        AbstractGwtValidator validator = GWT.create(DashbuilderValidationFactory.DataColumnValidator.class);
        return getValidator(validator);
    }
    
    public static Validator getDataSetDefValidator() {
        AbstractGwtValidator validator = GWT.create(DashbuilderValidationFactory.DataSetDefValidator.class);
        return getValidator(validator);
    }

    public static Validator getSQLDataSetDefValidator() {
        AbstractGwtValidator validator = GWT.create(DashbuilderValidationFactory.SQLDataSetDefValidator.class);
        return getValidator(validator);
    }

    public static Validator getCSVDataSetDefValidator() {
        AbstractGwtValidator validator = GWT.create(DashbuilderValidationFactory.CSVDataSetDefValidator.class);
        return getValidator(validator);
    }

    public static Validator getBeanDataSetDefValidator() {
        AbstractGwtValidator validator = GWT.create(DashbuilderValidationFactory.BeanDataSetDefValidator.class);
        return getValidator(validator);
    }

    public static Validator getELDataSetDefValidator() {
        AbstractGwtValidator validator = GWT.create(DashbuilderValidationFactory.ELDataSetDefValidator.class);
        return getValidator(validator);
    }
    
    private static Validator getValidator(AbstractGwtValidator validator) {
        AbstractGwtValidatorFactory validationFactory = (AbstractGwtValidatorFactory) com.google.gwt.validation.client.impl.Validation.buildDefaultValidatorFactory();
        validator.init(validationFactory.getConstraintValidatorFactory(), validationFactory.getMessageInterpolator(), validationFactory.getTraversableResolver() );
        return validator;
    }
}
