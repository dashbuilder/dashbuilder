package org.dashbuilder.validations.dataset;

import org.dashbuilder.dataset.def.BeanDataSetDef;
import org.dashbuilder.dataset.validation.groups.BeanDataSetDefValidation;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * <p>The singleton application BEAN data set definition validator.</p>
 * 
 * @since 0.4.0
 */
@ApplicationScoped
public class BeanDataSetDefValidator extends AbstractDataSetDefValidator<BeanDataSetDef> {

    @Override
    public Iterable<ConstraintViolation<?>> validateAttributes(BeanDataSetDef dataSetDef, Object... params) {
        Validator validator = getDashbuilderValidator();
        Set<ConstraintViolation<BeanDataSetDef>> _violations = validator.validate(dataSetDef,
                BeanDataSetDefValidation.class);
        return toIterable(_violations);
    }

    @Override
    public Iterable<ConstraintViolation<?>> validate(BeanDataSetDef dataSetDef, boolean isCacheEnabled, boolean isPushEnabled, boolean isRefreshEnabled, Object... params) {
        Validator validator = getDashbuilderValidator();
        Set<ConstraintViolation<BeanDataSetDef>> _violations = validator.validate(dataSetDef,
                getValidationGroups(isCacheEnabled, isPushEnabled, isRefreshEnabled, BeanDataSetDefValidation.class));
        return toIterable(_violations);
    }

}
