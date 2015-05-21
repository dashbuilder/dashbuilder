package org.dashbuilder.common.client.widgets.slider.view;

import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.widgets.slider.presenter.Presenter;

import java.util.ArrayList;

/**
 * 
 * @author kiouri
 *
 */
public class SliderBarHorizontal extends SliderBar {
	
	protected int barWidth, barHeight, dragTopPosition, scaleWidth;
	
	protected int getBarHeight(){
		barHeight = getH(drag.getElement());		
		for (int i = 0; i < orderedWidgets.size(); i++){
			if (orderedWidgets.get(i) == scale){
				barHeight = Math.max(barHeight, scaleSize);
			} else {
				barHeight = Math.max(barHeight, getH(orderedWidgets.get(i).getElement()));
			}
		}		
		barHeight = Math.max(barHeight, scaleSize);
		return barHeight;		
	}
	
	protected void ajustScaleSize(int widgetWidth){
		scaleWidth = widgetWidth;
		if (less != null) {
			for (int i = 0; i < less.size(); i++) {
				scaleWidth -= getW(less.get(i).getElement());
			}
		}		
		if (more != null) {
			for (int i = 0; i < more.size(); i++) {
				scaleWidth -= getW(more.get(i).getElement());
			}
		}
		scale.setPixelSize(scaleWidth, scaleSize);
	}
	
	public int getAbsMaxLength() {
		return scaleWidth - getW(drag.getElement());
	}
	
	public void drawScrollBar(int barWidth) {
		absPanel.clear();
		putWidgetsToAbsPanel();		
		initVariables(barWidth);
		ajustScaleSize(barWidth);		
		this.setHeight(getBarHeight() + "px");		
		absPanel.setPixelSize(barWidth,getBarHeight());
		placeWidgets(orderedWidgets);
		absPanel.setWidgetPosition(drag, getScaleLeft(orderedWidgets), dragTopPosition);
	}	

	protected void initVariables(int barWidth){
		this.barWidth = barWidth;
		startPosition = getScaleLeft(orderedWidgets);
		dragTopPosition = (getBarHeight() - getH(drag.getElement()))/2;
	}
	
    protected int getScaleLeft(ArrayList<Widget> widgets){
    	int sPosition = 0; 
    	for (int i = 0; i < widgets.size(); i++){
    		if (widgets.get(i) != scale){
    			sPosition += getW(widgets.get(i).getElement()); 
    		} else {
    			return sPosition;
    		}
    	}
    	return sPosition;
    }
	
	protected void placeWidgets(ArrayList<Widget> widgets){
		int tmpPosition = 0;
		int barHeight = getBarHeight();
		for (int i = 0; i < widgets.size(); i++){
			if (widgets.get(i) == scale){
				absPanel.setWidgetPosition(widgets.get(i), tmpPosition, (barHeight - scaleSize)/2);
			} else {
				absPanel.setWidgetPosition(widgets.get(i), tmpPosition, (barHeight - getH(widgets.get(i).getElement()))/2);
		    }	
			tmpPosition += getW(widgets.get(i).getElement());
		}
	}    
	
	public void setDragPosition(int position){
		position = startPosition + position;
		absPanel.setWidgetPosition(drag, position, dragTopPosition );
	}
	
	public int getScaleTouchPosition(MouseEvent event) { 
		return event.getRelativeX(this.getElement()) - startPosition - getW(drag.getElement())/2; 
	}
	
	public int getDragPosition() {
		return absPanel.getWidgetLeft(drag) - startPosition;
	}	
	
    /**
     * Sets scale mark to scale
     */
	public void putMark(Mark mark, int markPosition) {
	      int markY = (this.barHeight - mark.getMarkHeight()) / 2;
	      this.absPanel.add(mark, this.startPosition + markPosition + getW(drag.getElement())/2, markY);				
		}

	public Presenter.Orientation getOrientation(){
		return Presenter.Orientation.HORIZONTAL;
	}
	
	@Override
	/**
	 * It is not possible to adjust height of horizontal sliderbar with help of this method. 
	 * Height of horizontal sliderbar is automatically calculated on base of height of components which are included in widget   
	 */
	public void setHeight(String height) {
		super.setHeight(getBarHeight() + "px");
	}

	public void setWidth(int width) {
		super.setWidth(width + "px");
		if (isAttached()) {
			presenter.setBarPixelSize(width);
			presenter.processParams();
			reDrawMarks();
			setValue(getValue());
		}
	}
		
	public void setScaleWidget(Widget scaleWidget, int scaleHeight){
		super.setScaleWidget(scaleWidget, scaleHeight);
	}
	 			
}
