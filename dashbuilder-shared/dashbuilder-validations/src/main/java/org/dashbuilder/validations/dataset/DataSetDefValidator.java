package org.dashbuilder.validations.dataset;

import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.validation.groups.DataSetDefBasicAttributesGroup;
import org.dashbuilder.dataset.validation.groups.DataSetDefProviderTypeGroup;
import org.dashbuilder.validations.DashbuilderValidator;
import org.jboss.errai.ioc.client.container.SyncBeanManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * <p>The singleton application data set definition validator.</p>
 * 
 * @since 0.4.0
 */
@ApplicationScoped
public class DataSetDefValidator extends DashbuilderValidator {

    SyncBeanManager beanManager;

    public DataSetDefValidator() {
    }

    @Inject
    public DataSetDefValidator(SyncBeanManager beanManager) {
        this.beanManager = beanManager;
    }

    public AbstractDataSetDefValidator validatorFor(final DataSetProviderType type) {
        final boolean isSQL = type != null && DataSetProviderType.SQL.equals(type);
        final boolean isBean = type != null && DataSetProviderType.BEAN.equals(type);
        final boolean isCSV = type != null && DataSetProviderType.CSV.equals(type);
        final boolean isEL = type != null && DataSetProviderType.ELASTICSEARCH.equals(type);
        
        Class validatorClass = null;
        if (isSQL) {
            validatorClass = SQLDataSetDefValidator.class;
        } else if (isCSV) {
            validatorClass = CSVDataSetDefValidator.class;
        } else if (isBean) {
            validatorClass = BeanDataSetDefValidator.class;
        } else if (isEL) {
            validatorClass = ElasticSearchDataSetDefValidator.class;
        }
        
        return (AbstractDataSetDefValidator) beanManager.lookupBean(validatorClass).newInstance();
    }
    
    public Iterable<ConstraintViolation<?>> validateProviderType(final DataSetDef dataSetDef) {
        Set<ConstraintViolation<DataSetDef>> _violations = validateUnTypedDataSetDef(dataSetDef, DataSetDefProviderTypeGroup.class);
        return toIterable(_violations);
    }

    public Iterable<ConstraintViolation<?>> validateBasicAttributes(final DataSetDef dataSetDef) {
        Set<ConstraintViolation<DataSetDef>> _violations = validateUnTypedDataSetDef(dataSetDef, DataSetDefBasicAttributesGroup.class);
        return toIterable(_violations);
    }

    // Note: Value columns explicitly validation here due to https://github.com/gwtproject/gwt/issues/8816
    public Set<ConstraintViolation<DataSetDef>> validateUnTypedDataSetDef(final DataSetDef dataSetDef, final Class... validationGroups) {
        Validator validator = getDashbuilderValidator();
        return validator.validate(dataSetDef, validationGroups);
    }

    protected Iterable<ConstraintViolation<?>> toIterable(Set<ConstraintViolation<DataSetDef>> violations) {
        return  ((Iterable<ConstraintViolation<?>>) (Set<?>) violations);
    }
    
}
