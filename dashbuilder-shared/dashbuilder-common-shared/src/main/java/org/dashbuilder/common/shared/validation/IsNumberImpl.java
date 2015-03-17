package org.dashbuilder.common.shared.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * <p>JSR303 annotation implementation for <code>org.dashbuilder.common.shared.validation.IsNumber</code>.</p>
 */
public class IsNumberImpl implements ConstraintValidator<IsNumber, String> {

    private static final String NUMBER_PATTERN = "[0-9]*";
    
    @Override
    public void initialize(IsNumber constraintAnnotation) {
        // Do nothing.
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return NUMBER_PATTERN.matches(value);
    }

}
