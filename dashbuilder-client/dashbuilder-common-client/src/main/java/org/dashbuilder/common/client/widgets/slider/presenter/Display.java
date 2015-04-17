package org.dashbuilder.common.client.widgets.slider.presenter;

import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.widgets.slider.view.Mark;

import java.util.ArrayList;

/**
 * 
 * @author kiouri
 *
 */
public interface Display {
	
	Widget getRootWidget();
	ArrayList<Widget> getMoreWidgets();
	ArrayList<Widget> getLessWidgets();
	Widget getDragWidget();
	Widget getScaleWidget();
	int getAbsMaxLength();
	Presenter.Orientation getOrientation();
	int getScaleTouchPosition(MouseEvent event);
	
	int getDragPosition();
	void setDragPosition(int position);
	
	void drawScrollBar(int barPixelSize);
	Widget asWidget();
	
	void putMark(Mark mark, int markPosition);
	
	public boolean isAttached();
	
	void setDragVisible(boolean isVisible);
	
}
