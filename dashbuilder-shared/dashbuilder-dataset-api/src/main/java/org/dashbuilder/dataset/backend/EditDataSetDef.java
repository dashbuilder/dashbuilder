package org.dashbuilder.dataset.backend;

import org.dashbuilder.dataset.def.DataColumnDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;

/**
 * <p>Response model for a DataSetDef edition.</p>
 * <p>Provides a cloned DataSetDef instance from the original one and the original column definition list.</p>
 */
@Portable
public class EditDataSetDef {

    private DataSetDef definition;
    /** The UUID of the data set being edited. The cloned one has a new UUID for avoiding backend cache issues. */
    private String uuid;
    private List<DataColumnDef> columns;

    public EditDataSetDef() {
        
    }

    public EditDataSetDef(final String uuid, final DataSetDef definition, final List<DataColumnDef> columns) {
        this.uuid = uuid;
        this.definition = definition;
        this.columns = columns;
    }

    public String getUuid() {
        return uuid;
    }

    public DataSetDef getDefinition() {
        return definition;
    }

    public List<DataColumnDef> getColumns() {
        return columns;
    }
    
}
