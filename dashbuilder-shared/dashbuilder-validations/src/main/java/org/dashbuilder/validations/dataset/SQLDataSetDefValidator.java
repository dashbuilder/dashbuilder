package org.dashbuilder.validations.dataset;

import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.dashbuilder.dataset.validation.groups.SQLDataSetDefDbSQLValidation;
import org.dashbuilder.dataset.validation.groups.SQLDataSetDefDbTableValidation;
import org.dashbuilder.dataset.validation.groups.SQLDataSetDefValidation;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * <p>The singleton application SQL data set definition validator.</p>
 * 
 * @since 0.4.0
 */
@ApplicationScoped
public class SQLDataSetDefValidator extends AbstractDataSetDefValidator<SQLDataSetDef> {

    @Override
    public Iterable<ConstraintViolation<?>> validateAttributes(SQLDataSetDef dataSetDef, Object... params) {
        assert params != null && params.length == 1;
        final Boolean isQuery = (Boolean) params[0];
        Validator validator = getDashbuilderValidator();
        Set<ConstraintViolation<SQLDataSetDef>> _violations = validator.validate(dataSetDef,
                SQLDataSetDefValidation.class, isQuery ? SQLDataSetDefDbSQLValidation.class : SQLDataSetDefDbTableValidation.class);
        return toIterable(_violations);
    }

    @Override
    public Iterable<ConstraintViolation<?>> validate(SQLDataSetDef dataSetDef, boolean isCacheEnabled, boolean isPushEnabled, boolean isRefreshEnabled, Object... params) {
        assert params != null && params.length == 1;
        final Boolean isQuery = (Boolean) params[0];
        Validator validator = getDashbuilderValidator();
        Set<ConstraintViolation<SQLDataSetDef>> _violations = validator.validate(dataSetDef,
                getValidationGroups(isCacheEnabled, isPushEnabled, isRefreshEnabled, SQLDataSetDefValidation.class,
                        isQuery ? SQLDataSetDefDbSQLValidation.class : SQLDataSetDefDbTableValidation.class));
        return toIterable(_violations);
    }
    
}
