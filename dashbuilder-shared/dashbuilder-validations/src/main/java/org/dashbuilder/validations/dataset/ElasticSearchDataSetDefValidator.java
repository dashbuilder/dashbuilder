package org.dashbuilder.validations.dataset;

import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.validation.groups.ElasticSearchDataSetDefValidation;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * <p>The singleton application ElasticSearch data set definition validator.</p>
 * 
 * @since 0.4.0
 */
@ApplicationScoped
public class ElasticSearchDataSetDefValidator extends AbstractDataSetDefValidator<ElasticSearchDataSetDef> {

    @Override
    public Iterable<ConstraintViolation<?>> validateAttributes(ElasticSearchDataSetDef dataSetDef, Object... params) {
        Validator validator = getDashbuilderValidator();
        Set<ConstraintViolation<ElasticSearchDataSetDef>> _violations = validator.validate(dataSetDef,
                ElasticSearchDataSetDefValidation.class);
        return toIterable(_violations);
    }

    @Override
    public Iterable<ConstraintViolation<?>> validate(ElasticSearchDataSetDef dataSetDef, boolean isCacheEnabled, boolean isPushEnabled, boolean isRefreshEnabled, Object... params) {
        Validator validator = getDashbuilderValidator();
        Set<ConstraintViolation<ElasticSearchDataSetDef>> _violations = validator.validate(dataSetDef,
                getValidationGroups(isCacheEnabled, isPushEnabled, isRefreshEnabled, ElasticSearchDataSetDefValidation.class));
        return toIterable(_violations);
    }

}
