package org.dashbuilder.client.uftable;

import org.dashbuilder.client.displayer.DataViewer;
import org.dashbuilder.client.displayer.RendererLibrary;
import org.dashbuilder.model.displayer.DataDisplayer;
import org.dashbuilder.model.displayer.DataDisplayerType;

public class UFTableRenderer implements RendererLibrary {

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
