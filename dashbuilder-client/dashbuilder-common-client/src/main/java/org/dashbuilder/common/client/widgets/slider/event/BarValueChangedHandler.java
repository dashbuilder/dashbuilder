package org.dashbuilder.common.client.widgets.slider.event;

import com.google.gwt.event.shared.EventHandler;

public interface BarValueChangedHandler extends EventHandler {
	void onBarValueChanged(BarValueChangedEvent event);

}
