package org.dashbuilder.dataset.client.validation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import org.dashbuilder.dataset.client.widgets.editors.DataSetAdvancedAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetBasicAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetProviderTypeEditor;
import org.dashbuilder.dataset.client.widgets.editors.bean.BeanDataSetDefAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.csv.CSVDataSetDefAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.elasticsearch.ELDataSetDefAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.sql.SQLDataSetDefAttributesEditor;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.dataset.validation.groups.DataSetDefCacheRowsValidation;
import org.dashbuilder.dataset.validation.groups.DataSetDefPushSizeValidation;
import org.dashbuilder.dataset.validation.groups.DataSetDefRefreshIntervalValidation;
import org.dashbuilder.validations.ValidatorFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class DataSetDefEditWorkflow {

    private final Set<ConstraintViolation<? extends DataSetDef>> violations;

    public DataSetDefEditWorkflow() {
        violations = new LinkedHashSet<ConstraintViolation<? extends DataSetDef>>();
    }

    interface BasicAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetBasicAttributesEditor> {}
    interface ProviderTypeAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetProviderTypeEditor> {}
    interface AdvancedAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetAdvancedAttributesEditor> {}
    interface SQLAttributesDriver extends SimpleBeanEditorDriver<SQLDataSetDef, SQLDataSetDefAttributesEditor> {}
    interface CSVAttributesDriver extends SimpleBeanEditorDriver<CSVDataSetDef, CSVDataSetDefAttributesEditor> {}
    interface BeanAttributesDriver extends SimpleBeanEditorDriver<BeanDataSetDef, BeanDataSetDefAttributesEditor> {}
    interface ELAttributesDriver extends SimpleBeanEditorDriver<ElasticSearchDataSetDef, ELDataSetDefAttributesEditor> {}

    // Create the drivers.
    /**
     * <p>Handles <code>UUID</code> and <code>name</code> data set definition attributes.</p> 
     */
    public final BasicAttributesDriver basicAttributesDriver = GWT.create(BasicAttributesDriver.class);
    /**
     * <p>Handles <code>provider</code> data set definition attribute.</p> 
     */
    public final ProviderTypeAttributesDriver providerTypeAttributesDriver = GWT.create(ProviderTypeAttributesDriver.class);
    /**
     * <p>Handles backend cache, client cache and refresh data set definition related attributes.</p> 
     */
    public final AdvancedAttributesDriver advancedAttributesDriver = GWT.create(AdvancedAttributesDriver.class);

    /**
     * <p>Handles SQL specific data set definition attributes.</p> 
     */
    public final SQLAttributesDriver sqlAttributesDriver = GWT.create(SQLAttributesDriver.class);

    /**
     * <p>Handles CSV specific data set definition attributes.</p> 
     */
    public final CSVAttributesDriver csvAttributesDriver = GWT.create(CSVAttributesDriver.class);

    /**
     * <p>Handles Bean specific data set definition attributes.</p> 
     */
    public final BeanAttributesDriver beanAttributesDriver = GWT.create(BeanAttributesDriver.class);

    /**
     * <p>Handles ElasticSearch specific data set definition attributes.</p> 
     */
    public final ELAttributesDriver elAttributesDriver = GWT.create(ELAttributesDriver.class);
    
    private boolean saveBasicAttributes = false;
    private boolean saveProviderTypeAttribute = false;
    private boolean saveAdvancedAttributes = false;
    private boolean saveSQLAttributes = false;
    private boolean saveBeanAttributes = false;
    private boolean saveCSVAttributes = false;
    private boolean saveELAttributes = false;
    
    public DataSetDefEditWorkflow edit(final DataSetBasicAttributesEditor view, final DataSetDef p) {
        basicAttributesDriver.initialize(view);
        basicAttributesDriver.edit(p);
        saveBasicAttributes = true;
        return this;
    }

    public DataSetDefEditWorkflow edit(final DataSetProviderTypeEditor view, final DataSetDef p) {
        providerTypeAttributesDriver.initialize(view);
        providerTypeAttributesDriver.edit(p);
        saveProviderTypeAttribute = true;
        return this;
    }

    public DataSetDefEditWorkflow edit(final DataSetAdvancedAttributesEditor view, final DataSetDef p) {
        advancedAttributesDriver.initialize(view);
        advancedAttributesDriver.edit(p);
        saveAdvancedAttributes = true;
        return this;
    }

    public DataSetDefEditWorkflow edit(final SQLDataSetDefAttributesEditor view, final SQLDataSetDef p) {
        sqlAttributesDriver.initialize(view);
        sqlAttributesDriver.edit(p);
        saveSQLAttributes = true;
        return this;
    }

    public DataSetDefEditWorkflow edit(final CSVDataSetDefAttributesEditor view, final CSVDataSetDef p) {
        csvAttributesDriver.initialize(view);
        csvAttributesDriver.edit(p);
        saveCSVAttributes = true;
        return this;
    }

    public DataSetDefEditWorkflow edit(final BeanDataSetDefAttributesEditor view, final BeanDataSetDef p) {
        beanAttributesDriver.initialize(view);
        beanAttributesDriver.edit(p);
        saveBeanAttributes = true;
        return this;
    }

    public DataSetDefEditWorkflow edit(final ELDataSetDefAttributesEditor view, final ElasticSearchDataSetDef p) {
        elAttributesDriver.initialize(view);
        elAttributesDriver.edit(p);
        saveELAttributes = true;
        return this;
    }
    
    public Set<ConstraintViolation<? extends DataSetDef>> save() {
        this.violations.clear();
        if (saveBasicAttributes) saveBasicAttributes();
        if (saveProviderTypeAttribute) saveProviderTypeAttribute();
        if (saveAdvancedAttributes) saveAdvancedAttributes();
        if (saveSQLAttributes) saveSQLAttributes();
        if (saveCSVAttributes) saveCSVAttributes();

        return violations;
    }

    /**
     * <p>Saves <code>UUID</code> and <code>name</code> data set definition attributes.</p> 
     */
    private DataSetDefEditWorkflow saveBasicAttributes() {
        DataSetDef edited = (DataSetDef) basicAttributesDriver.flush();
        return validate(edited, basicAttributesDriver);
    }

    /**
     * <p>Saves <code>provider</code> data set definition attribute.</p> 
     */
    private DataSetDefEditWorkflow saveProviderTypeAttribute() {
        DataSetDef edited = (DataSetDef) providerTypeAttributesDriver.flush();
        return validate(edited, providerTypeAttributesDriver);
    }

    /**
     * <p>Saves backend cache, client cache and refresh data set definition related attributes.</p> 
     */
    private DataSetDefEditWorkflow saveAdvancedAttributes() {
        DataSetDef edited = (DataSetDef) advancedAttributesDriver.flush();
        validate(edited, advancedAttributesDriver);
        List<Class<?>> groups = new LinkedList<Class<?>>();
        if (edited.isCacheEnabled()) groups.add(DataSetDefCacheRowsValidation.class);
        if (edited.isPushEnabled()) groups.add(DataSetDefPushSizeValidation.class);
        if (edited.isRefreshAlways()) groups.add(DataSetDefRefreshIntervalValidation.class);

        // Validate custom groups, if necessary.
        if (!groups.isEmpty()) {
            validate(edited, advancedAttributesDriver, groups.toArray(new Class[groups.size()]));
        }

        return this;
    }

    /**
     * <p>Saves sql data set definition attributes.</p> 
     */
    private DataSetDefEditWorkflow saveSQLAttributes() {
        SQLDataSetDef edited = sqlAttributesDriver.flush();
        return validateSQL(edited, sqlAttributesDriver);
    }

    /**
     * <p>Saves CSV data set definition attributes.</p> 
     */
    private DataSetDefEditWorkflow saveCSVAttributes() {
        CSVDataSetDef edited = csvAttributesDriver.flush();
        return validateCSV(edited, csvAttributesDriver);
    }

    private DataSetDefEditWorkflow validateSQL(final SQLDataSetDef def, final SimpleBeanEditorDriver driver) {
        Validator validator = ValidatorFactory.getSQLDataSetDefValidator();
        Set<ConstraintViolation<SQLDataSetDef>> violations = validator.validate(def);
        Set<?> test = violations;
        driver.setConstraintViolations(test);
        if (driver.hasErrors()) {
            this.violations.addAll(violations);
        }
        return this;
    }

    private DataSetDefEditWorkflow validateCSV(final CSVDataSetDef def, final SimpleBeanEditorDriver driver) {
        Validator validator = ValidatorFactory.getCSVDataSetDefValidator();
        Set<ConstraintViolation<CSVDataSetDef>> violations = validator.validate(def);
        Set<?> test = violations;
        driver.setConstraintViolations(test);
        if (driver.hasErrors()) {
            this.violations.addAll(violations);
        }
        return this;
    }
    
    private DataSetDefEditWorkflow validate(final DataSetDef def, final SimpleBeanEditorDriver driver) {
        Validator validator = ValidatorFactory.getDataSetDefValidator();
        Set<ConstraintViolation<DataSetDef>> violations = validator.validate(def);
        Set<?> test = violations;
        driver.setConstraintViolations(test);
        if (driver.hasErrors()) {
            this.violations.addAll(violations);
        }
        return null;
    }
    
    private DataSetDefEditWorkflow validate(final DataSetDef def, final SimpleBeanEditorDriver driver,  final Class<?>... groups) {
        Validator validator = ValidatorFactory.getDataSetDefValidator();
        Set<ConstraintViolation<DataSetDef>> violations = validator.validate(def, groups);
        Set<?> test = violations;
        driver.setConstraintViolations(test);
        if (driver.hasErrors()) {
            this.violations.addAll(violations);
        }
        return this;
    }

    public DataSetDefEditWorkflow clear() {
        saveBasicAttributes = false;
        saveProviderTypeAttribute = false;
        saveAdvancedAttributes = false;
        saveSQLAttributes = false;
        violations.clear();
        return this;
    }
}
