package org.dashbuilder.validations;

import com.google.gwt.validation.client.impl.Validation;

import javax.validation.Validator;

/**
 * @since 0.3.0
 */
public final class ValidatorFactory {

    public static Validator getDashbuilderValidator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
