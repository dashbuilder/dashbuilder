package org.dashbuilder.client.widgets.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface DataSetEditorMessages extends Messages {

    public static final DataSetEditorMessages INSTANCE = GWT.create(DataSetEditorMessages.class);
    
    String dataSetCount(int count);
    String newDataSet(String providerType);
}
