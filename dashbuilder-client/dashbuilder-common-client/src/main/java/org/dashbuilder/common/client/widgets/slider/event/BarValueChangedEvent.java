package org.dashbuilder.common.client.widgets.slider.event;

import com.google.gwt.event.shared.GwtEvent;

public class BarValueChangedEvent extends GwtEvent<BarValueChangedHandler> {
	public static Type<BarValueChangedHandler> TYPE = new Type<BarValueChangedHandler>();
	
	int value;
	
	public BarValueChangedEvent(int position){
		this.value = position;
	}
	
	public int getValue() {
		return value;
	}

	public void setValue(int position) {
		this.value = position;
	}	

	@Override
	public Type<BarValueChangedHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(BarValueChangedHandler handler) {
		handler.onBarValueChanged(this);
	}


}
