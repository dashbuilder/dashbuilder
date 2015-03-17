package org.dashbuilder.common.shared.validation;

import javax.validation.Constraint;
import java.lang.annotation.*;

/**
 * <p>JSR303 annotation that checks if the property value is a number.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD,ElementType.CONSTRUCTOR,ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy= IsNumberImpl.class)
public @interface IsNumber {
    String message();

    Class[] groups() default {};

    Class[] payload() default {};

}
