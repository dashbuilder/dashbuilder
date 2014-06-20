package org.dashbuilder.client.uftable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.dashbuilder.client.displayer.AbstractRendererLibrary;
import org.dashbuilder.client.displayer.DataViewer;
import org.dashbuilder.client.displayer.RendererLibrary;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DataDisplayerType;

/**
 * Table renderer based on the Uberfire's core PagedTable widget.
 */
@ApplicationScoped
@Named(UFTableRenderer.UUID + "_renderer")
public class UFTableRenderer extends AbstractRendererLibrary {

    public static final String UUID = "uftable";

    @Override public String getUUID() {
        return UUID;
    }

    @Override public DataViewer lookupViewer(DataDisplayer displayer) {
        DataDisplayerType type = displayer.getType();
        if (DataDisplayerType.TABLE.equals(type)) return new UFTableViewer();

        return null;
    }
}
