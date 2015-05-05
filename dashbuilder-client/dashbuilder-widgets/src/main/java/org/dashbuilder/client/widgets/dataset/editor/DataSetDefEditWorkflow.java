package org.dashbuilder.client.widgets.dataset.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.AbstractEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.DataSetAdvancedAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.DataSetBasicAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.DataSetProviderTypeEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.bean.BeanDataSetDefAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.csv.CSVDataSetDefAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.datacolumn.DataColumnBasicEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.elasticsearch.ELDataSetDefAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.sql.SQLDataSetDefAttributesEditor;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.dataset.validation.groups.*;
import org.dashbuilder.validations.ValidatorFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * <p>Default workflow for editing a data set defintion instance using Data Set Editor component.</p>
 * @since 0.3.0 
 */
public final class DataSetDefEditWorkflow {

    public DataSetDefEditWorkflow() {
        
    }

    interface BasicAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetBasicAttributesEditor> {}
    interface ProviderTypeAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetProviderTypeEditor> {}
    interface AdvancedAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetAdvancedAttributesEditor> {}
    interface DataColumnDriver extends SimpleBeanEditorDriver<DataColumnDef, DataColumnBasicEditor> {}
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
     * <p>Handles a data set column.</p> 
     */
    public final List<DataColumnDriver> columnDrivers = new LinkedList<DataColumnDriver>();
    
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

    private DataSetBasicAttributesEditor basicAttributesEditor = null;
    private DataSetProviderTypeEditor providerTypeAttributeEditor = null;
    private DataSetAdvancedAttributesEditor advancedAttributesEditor = null;
    final private List<DataColumnBasicEditor> columnEditors = new LinkedList<DataColumnBasicEditor>();
    private SQLDataSetDefAttributesEditor sqlAttributesEditor = null;
    private BeanDataSetDefAttributesEditor beanAttributesEditor = null;
    private CSVDataSetDefAttributesEditor csvAttributesEditor = null;
    private ELDataSetDefAttributesEditor elasticSearchAttributesEditor = null;
    
    public DataSetDefEditWorkflow edit(final DataSetBasicAttributesEditor view, final DataSetDef p) {
        basicAttributesDriver.initialize(view);
        basicAttributesDriver.edit(p);
        basicAttributesEditor = view;
        return this;
    }

    public DataSetDefEditWorkflow edit(final DataSetProviderTypeEditor view, final DataSetDef p) {
        providerTypeAttributesDriver.initialize(view);
        providerTypeAttributesDriver.edit(p);
        providerTypeAttributeEditor = view;
        return this;
    }

    public DataSetDefEditWorkflow edit(final DataSetAdvancedAttributesEditor view, final DataSetDef p) {
        advancedAttributesDriver.initialize(view);
        advancedAttributesDriver.edit(p);
        advancedAttributesEditor = view;
        return this;
    }

    public DataSetDefEditWorkflow edit(final DataColumnBasicEditor view, final DataColumnDef d) {
        if (!columnEditors.contains(view)) {
            final DataColumnDriver driver = GWT.create(DataColumnDriver.class);
            driver.initialize(view);
            driver.edit(d);
            columnDrivers.add(driver);
            columnEditors.add(view);
        }
        
        return this;
    }

    public DataSetDefEditWorkflow remove(final DataColumnBasicEditor view, final DataColumnDef d) {
        final int i = columnEditors.indexOf(view);
        if (i > -1) {
            columnEditors.remove(i);
            columnDrivers.remove(i);
        }
        
        return this;
    }

    public DataSetDefEditWorkflow edit(final SQLDataSetDefAttributesEditor view, final SQLDataSetDef p) {
        sqlAttributesDriver.initialize(view);
        sqlAttributesDriver.edit(p);
        sqlAttributesEditor = view;
        return this;
    }

    public DataSetDefEditWorkflow edit(final CSVDataSetDefAttributesEditor view, final CSVDataSetDef p) {
        csvAttributesDriver.initialize(view);
        csvAttributesDriver.edit(p);
        csvAttributesEditor = view;
        return this;
    }

    public DataSetDefEditWorkflow edit(final BeanDataSetDefAttributesEditor view, final BeanDataSetDef p) {
        beanAttributesDriver.initialize(view);
        beanAttributesDriver.edit(p);
        beanAttributesEditor = view;
        return this;
    }

    public DataSetDefEditWorkflow edit(final ELDataSetDefAttributesEditor view, final ElasticSearchDataSetDef p) {
        elAttributesDriver.initialize(view);
        elAttributesDriver.edit(p);
        elasticSearchAttributesEditor = view;
        return this;
    }
    
    public DataSetDefEditWorkflow save() {
        if (basicAttributesEditor != null) saveBasicAttributes();
        if (providerTypeAttributeEditor != null) saveProviderTypeAttribute();
        if (advancedAttributesEditor != null) saveAdvancedAttributes();
        if (sqlAttributesEditor != null) saveSQLAttributes();
        if (csvAttributesEditor != null) saveCSVAttributes();
        if (beanAttributesEditor != null) saveBeanAttributes();
        if (elasticSearchAttributesEditor!= null) saveELAttributes();
        if (!columnEditors.isEmpty()) saveColumns();
        
        return this;
    }

    /**
     * <p>Saves <code>UUID</code> and <code>name</code> data set definition attributes.</p> 
     */
    private DataSetDefEditWorkflow saveBasicAttributes() {
        DataSetDef edited = (DataSetDef) basicAttributesDriver.flush();
        return validate(edited, basicAttributesEditor, basicAttributesDriver);
    }

    /**
     * <p>Saves <code>provider</code> data set definition attribute.</p> 
     */
    private DataSetDefEditWorkflow saveProviderTypeAttribute() {
        DataSetDef edited = (DataSetDef) providerTypeAttributesDriver.flush();
        return validate(edited, providerTypeAttributeEditor, providerTypeAttributesDriver);
    }

    /**
     * <p>Saves backend cache, client cache and refresh data set definition related attributes.</p> 
     */
    private DataSetDefEditWorkflow saveAdvancedAttributes() {
        DataSetDef edited = (DataSetDef) advancedAttributesDriver.flush();
        
        List<Class<?>> groups = new LinkedList<Class<?>>();
        if (edited.isCacheEnabled()) groups.add(DataSetDefCacheRowsValidation.class);
        if (edited.isPushEnabled()) groups.add(DataSetDefPushSizeValidation.class);
        if (edited.getRefreshTime() != null) groups.add(DataSetDefRefreshIntervalValidation.class);

        // Validate custom groups, if necessary.
        if (!groups.isEmpty()) {
            groups.add(javax.validation.groups.Default.class);
            validate(edited, advancedAttributesEditor, advancedAttributesDriver, groups.toArray(new Class[groups.size()]));
        } else {
            validate(edited, advancedAttributesEditor, advancedAttributesDriver);
        }

        return this;
    }

    /**
     * <p>Handles s data set column.</p> 
     */
    private DataSetDefEditWorkflow saveColumns() {
        
        for (int x = 0; x < columnDrivers.size(); x++) {
            final DataColumnDriver driver = columnDrivers.get(x);
            final DataColumnBasicEditor editor = columnEditors.get(x);
            final DataColumnDef edited = driver.flush();
            validateDataColumn(edited, editor, driver);
        }

        return this;
    }

    /**
     * <p>Saves sql data set definition attributes.</p> 
     */
    private DataSetDefEditWorkflow saveSQLAttributes() {
        SQLDataSetDef edited = sqlAttributesDriver.flush();
        
        // Validate either table or query.
        List<Class<?>> groups = new LinkedList<Class<?>>();
        if (sqlAttributesEditor.isUsingTable()) {
            // Save using table attribute.
            edited.setDbSQL(null);
            groups.add(SQLDataSetDefDbTableValidation.class);
        } else {
            // Save using query attribute.
            edited.setDbTable(null);
            groups.add(SQLDataSetDefDbSQLValidation.class);
        }

        // Validate custom groups, if necessary.
        if (!groups.isEmpty()) {
            groups.add(javax.validation.groups.Default.class);
            return validateSQL(edited, sqlAttributesEditor, sqlAttributesDriver, groups.toArray(new Class[groups.size()]));
        } else {
            return validateSQL(edited, sqlAttributesEditor, sqlAttributesDriver);
        }
    }

    /**
     * <p>Saves CSV data set definition attributes.</p> 
     */
    private DataSetDefEditWorkflow saveCSVAttributes() {
        CSVDataSetDef edited = csvAttributesDriver.flush();
        
        // Validate either file path or file URL.
        if (csvAttributesEditor.isUsingFilePath()) return validateCSV(edited, csvAttributesEditor, csvAttributesDriver, CSVDataSetDefFilePathValidation.class, javax.validation.groups.Default.class);
        else if (csvAttributesEditor.isUsingFileURL()) return validateCSV(edited, csvAttributesEditor, csvAttributesDriver, CSVDataSetDefFileURLValidation.class, javax.validation.groups.Default.class);
        return this;
    }

    /**
     * <p>Saves EL data set definition attributes.</p> 
     */
    private DataSetDefEditWorkflow saveELAttributes() {
        ElasticSearchDataSetDef edited = elAttributesDriver.flush();
        return validateEL(edited, elasticSearchAttributesEditor, elAttributesDriver);
    }

    /**
     * <p>Saves Bean
     * * data set definition attributes.</p> 
     */
    private DataSetDefEditWorkflow saveBeanAttributes() {
        BeanDataSetDef edited = beanAttributesDriver.flush();
        return validateBean(edited, beanAttributesEditor, beanAttributesDriver);
    }
    
    
    private DataSetDefEditWorkflow validateDataColumn(final DataColumnDef dataColumn, final AbstractEditor editor, final SimpleBeanEditorDriver driver) {
        final Validator validator = ValidatorFactory.getDataColumnValidator();
        final Set<ConstraintViolation<DataColumnDef>> violations = validator.validate(dataColumn);
        final Set<?> test = violations;
        setViolations(editor, driver, (Iterable<ConstraintViolation<?>>) test);
        return this;

    }

    private DataSetDefEditWorkflow validateSQL(final SQLDataSetDef def, final AbstractEditor editor, final SimpleBeanEditorDriver driver, final Class<?>... groups) {
        final Validator validator = ValidatorFactory.getSQLDataSetDefValidator();
        final Set<ConstraintViolation<SQLDataSetDef>> violations = groups != null ? validator.validate(def, groups) : validator.validate(def);
        final Set<?> test = violations;
        setViolations(editor, driver, (Iterable<ConstraintViolation<?>>) test);
        return this;
    }

    private DataSetDefEditWorkflow validateCSV(final CSVDataSetDef def, final AbstractEditor editor, final SimpleBeanEditorDriver driver, final Class<?>... groups) {
        final Validator validator = ValidatorFactory.getCSVDataSetDefValidator();
        final Set<ConstraintViolation<CSVDataSetDef>> violations = groups != null ? validator.validate(def, groups) : validator.validate(def);
        final Set<?> test = violations;
        setViolations(editor, driver, (Iterable<ConstraintViolation<?>>) test);
        return this;
    }

    private DataSetDefEditWorkflow validateEL(final ElasticSearchDataSetDef def, final AbstractEditor editor, final SimpleBeanEditorDriver driver) {
        final Validator validator = ValidatorFactory.getELDataSetDefValidator();
        final Set<ConstraintViolation<ElasticSearchDataSetDef>> violations = validator.validate(def);
        final Set<?> test = violations;
        setViolations(editor, driver, (Iterable<ConstraintViolation<?>>) test);
        return this;
    }

    private DataSetDefEditWorkflow validateBean(final BeanDataSetDef def, final AbstractEditor editor, final SimpleBeanEditorDriver driver) {
        final Validator validator = ValidatorFactory.getBeanDataSetDefValidator();
        final Set<ConstraintViolation<BeanDataSetDef>> violations = validator.validate(def);
        final Set<?> test = violations;
        setViolations(editor, driver, (Iterable<ConstraintViolation<?>>) test);
        return this;
    }
    
    private DataSetDefEditWorkflow validate(final DataSetDef def, final AbstractEditor editor, final SimpleBeanEditorDriver driver) {
        final Validator validator = ValidatorFactory.getDataSetDefValidator();
        final Set<ConstraintViolation<DataSetDef>> violations = validator.validate(def);
        final Set<?> test = violations;
        setViolations(editor, driver, (Iterable<ConstraintViolation<?>>) test);
        return null;
    }
    
    private DataSetDefEditWorkflow validate(final DataSetDef def, final AbstractEditor editor, final SimpleBeanEditorDriver driver,  final Class<?>... groups) {
        final Validator validator = ValidatorFactory.getDataSetDefValidator();
        final Set<ConstraintViolation<DataSetDef>> violations = groups != null ? validator.validate(def, groups) : validator.validate(def);
        final Set<?> test = violations;
        setViolations(editor, driver, (Iterable<ConstraintViolation<?>>) test);
        return this;
    }
    
    private void setViolations(final AbstractEditor editor, final SimpleBeanEditorDriver driver, final Iterable<ConstraintViolation<?>> violations) {
        driver.setConstraintViolations(violations);
        if (driver.hasErrors()) {
            editor.setViolations(violations);
        } else {
            editor.setViolations(null);
        }
    }
    
    public DataSetDefEditWorkflow clear() {
        basicAttributesEditor = null;
        providerTypeAttributeEditor = null;
        advancedAttributesEditor = null;
        sqlAttributesEditor = null;
        csvAttributesEditor = null;
        beanAttributesEditor = null;
        elasticSearchAttributesEditor = null;
        columnEditors.clear();
        columnDrivers.clear();
        return this;
    }
}
