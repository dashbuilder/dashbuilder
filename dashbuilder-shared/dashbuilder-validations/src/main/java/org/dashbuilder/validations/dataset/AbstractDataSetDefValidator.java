package org.dashbuilder.validations.dataset;

import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.validation.groups.*;
import org.dashbuilder.validations.DashbuilderValidator;

import javax.validation.ConstraintViolation;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * <p>The base data set definition validator.</p>
 * 
 * @since 0.4.0
 */
public abstract class AbstractDataSetDefValidator<T extends DataSetDef> extends DashbuilderValidator {

    public abstract Iterable<ConstraintViolation<?>> validateAttributes(T dataSetDef, Object... params);

    public abstract Iterable<ConstraintViolation<?>> validate(T dataSetDef, 
                                                              boolean isCacheEnabled, 
                                                              boolean isPushEnabled, 
                                                              boolean isRefreshEnabled,
                                                              Object... params);
    
    protected Class[] getValidationGroups(final boolean isCacheEnabled, final boolean isPushEnabled, final boolean isRefreshEnabled,
                                          final Class... groups) {
        List<Class> classes = new LinkedList<Class>();
        classes.add(DataSetDefBasicAttributesGroup.class);
        classes.add(DataSetDefProviderTypeGroup.class);
        if (isCacheEnabled) {
            classes.add(DataSetDefCacheRowsValidation.class);
        }
        if (isPushEnabled) {
            classes.add(DataSetDefPushSizeValidation.class);
        }
        if (isRefreshEnabled) {
            classes.add(DataSetDefRefreshIntervalValidation.class);
        }
        if (groups != null) {
            for (final Class group : groups) {
                classes.add(group);
            }
        }
        
        return classes.toArray(new Class[classes.size()]);
    }

    protected Iterable<ConstraintViolation<?>> toIterable(Set<ConstraintViolation<T>> violations) {
        return  ((Iterable<ConstraintViolation<?>>) (Set<?>) violations);
    }

}
