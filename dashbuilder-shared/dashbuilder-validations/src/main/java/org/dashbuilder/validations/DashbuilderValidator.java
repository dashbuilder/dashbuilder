package org.dashbuilder.validations;

import com.google.gwt.validation.client.impl.Validation;

import javax.validation.Validator;

/**
 * <p>The singleton application bean validator implementation.</p>
 * 
 * @since 0.4.0
 */
public class DashbuilderValidator {

    public Validator getDashbuilderValidator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
    
}
