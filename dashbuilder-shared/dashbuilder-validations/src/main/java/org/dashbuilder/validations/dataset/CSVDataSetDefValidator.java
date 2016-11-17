package org.dashbuilder.validations.dataset;

import org.dashbuilder.dataset.def.CSVDataSetDef;
import org.dashbuilder.dataset.validation.groups.CSVDataSetDefFilePathValidation;
import org.dashbuilder.dataset.validation.groups.CSVDataSetDefFileURLValidation;
import org.dashbuilder.dataset.validation.groups.CSVDataSetDefValidation;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * <p>The singleton application CSV data set definition validator.</p>
 * 
 * @since 0.4.0
 */
@ApplicationScoped
public class CSVDataSetDefValidator extends AbstractDataSetDefValidator<CSVDataSetDef> {

    @Override
    public Iterable<ConstraintViolation<?>> validateAttributes(CSVDataSetDef dataSetDef, Object... params) {
        assert params != null && params.length == 1;
        final Boolean isFilePath = (Boolean) params[0];
        Validator validator = getDashbuilderValidator();
        Set<ConstraintViolation<CSVDataSetDef>> _violations = validator.validate(dataSetDef,
                CSVDataSetDefValidation.class, isFilePath ? CSVDataSetDefFilePathValidation.class : CSVDataSetDefFileURLValidation.class);
        return toIterable(_violations);
    }

    @Override
    public Iterable<ConstraintViolation<?>> validate(CSVDataSetDef dataSetDef, boolean isCacheEnabled, boolean isPushEnabled, boolean isRefreshEnabled, Object... params) {
        assert params != null && params.length == 1;
        final Boolean isFilePath = (Boolean) params[0];
        Validator validator = getDashbuilderValidator();
        Set<ConstraintViolation<CSVDataSetDef>> _violations = validator.validate(dataSetDef,
                getValidationGroups(isCacheEnabled, isPushEnabled, isRefreshEnabled, CSVDataSetDefValidation.class,
                        isFilePath ? CSVDataSetDefFilePathValidation.class : CSVDataSetDefFileURLValidation.class));
        return toIterable(_violations);
    }

}
