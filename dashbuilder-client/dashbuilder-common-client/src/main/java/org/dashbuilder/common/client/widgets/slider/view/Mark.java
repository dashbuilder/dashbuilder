package org.dashbuilder.common.client.widgets.slider.view;

import com.google.gwt.user.client.DOM;

public class Mark extends TouchableAbsolutePanelPK { //AbsolutePanelPK {

	int markWidth, markHeight;
	
	public Mark(String color, int width, int height) {

		this.markWidth = width;
		this.markHeight = height;
		setPixelSize(width, height);
		DOM.setStyleAttribute(this.getElement(), "backgroundColor", color);

	}

	public int getMarkWidth() {
		return markWidth;
	}

	public int getMarkHeight() {
		return markHeight;
	}
}
