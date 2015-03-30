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
public class SliderBarVertical extends SliderBar{
	
	protected int barWidth, barHeight, dragLeftPosition, scaleHeight;
	
	public SliderBarVertical(Presenter presenter){
		super(presenter);
	}

	public SliderBarVertical(){
		super();
	}
	
	public int getBarWidth(){
		barWidth = getW(drag.getElement());		
		for (int i = 0; i < orderedWidgets.size(); i++){
			if (orderedWidgets.get(i) == scale){
				barWidth = Math.max(barWidth, scaleSize);
			} else {
				barWidth = Math.max(barWidth, getW(orderedWidgets.get(i).getElement()));
			}			
		}		
		barWidth = Math.max(barWidth, scaleSize);
		return barWidth;				
	}	
	
	public void ajustScaleSize(int widgetHeight){	
		scaleHeight = widgetHeight;
		if (less != null) {
			for (int i = 0; i < less.size(); i++) {
				scaleHeight -= getH(less.get(i).getElement());
			}
		}		
		if (more != null) {
			for (int i = 0; i < more.size(); i++) {
				scaleHeight -= getH(more.get(i).getElement());
			}
		}
		scale.setPixelSize(scaleSize, scaleHeight);		
	}
	
	public int getAbsMaxLength() {
		return scaleHeight - getH(drag.getElement());
	}
	
	public void drawScrollBar(int barHeight) {		
		absPanel.clear();
		putWidgetsToAbsPanel();		
		initVariables(barHeight);
		ajustScaleSize(barHeight);		
		this.setWidth(getBarWidth() + "px");		
		absPanel.setPixelSize(getBarWidth(), barHeight);
		placeWidgets(orderedWidgets);
		absPanel.setWidgetPosition(drag, dragLeftPosition, getScaleTop(orderedWidgets));		
	}	

	protected void initVariables(int barHeight){		
		this.barHeight = barHeight;
		startPosition = getScaleTop(orderedWidgets);
		dragLeftPosition = (getBarWidth() - getW(drag.getElement()))/2;
	}
	
    protected int getScaleTop(ArrayList<Widget> widgets){    	
    	int sPosition = 0; 
    	for (int i = 0; i < widgets.size(); i++){
    		if (widgets.get(i) != scale){
    			sPosition += getH(widgets.get(i).getElement()); 
    		} else {
    			return sPosition;
    		}
    	}
    	return sPosition;    	
    }
	    
	protected void placeWidgets(ArrayList<Widget> widgets){
		int tmpPosition = 0;
		int barWidth = getBarWidth();
		for (int i = 0; i < widgets.size(); i++){
			if (widgets.get(i) == scale){
				absPanel.setWidgetPosition(widgets.get(i), (barWidth - scaleSize)/2, tmpPosition);				
			} else {
				absPanel.setWidgetPosition(widgets.get(i), (barWidth - getW(widgets.get(i).getElement()))/2, tmpPosition);
		    }	
			tmpPosition += getH(widgets.get(i).getElement());
		}
	}

	public void setDragPosition(int position){
		position = startPosition + position;
		absPanel.setWidgetPosition(drag, dragLeftPosition, position);
	}

	public int getScaleTouchPosition(MouseEvent event) {
		return event.getRelativeY(this.getElement()) - startPosition - getH(drag.getElement())/2;
	}

	public int getDragPosition() {
		return absPanel.getWidgetTop(drag) - startPosition;
	}
		
	public void putMark(Mark mark, int markPosition) {
	       int markX = (this.barWidth - mark.getMarkWidth()) / 2;
	       this.absPanel.add(mark, markX, startPosition + markPosition + getH(drag.getElement())/2);		
	}
			
	public Presenter.Orientation getOrientation(){
		return Presenter.Orientation.VERTICAL;
	}
	
	@Override
	public void setHeight(String height) {
		super.setHeight(height);
		if ( this.isAttached()) {
			presenter.setBarPixelSize(getH(getElement()));
			presenter.processParams();
			reDrawMarks();
			setValue(getValue());
		} 
	}

	/**
	 * It is not possible to adjust width of vertical sliderbar with help of this method. 
	 * Width of horizontal sliderbar is automatically calculated on base of height of components which are included in widget   
	 */
	@Override
	public void setWidth(String width) {
		super.setWidth(getBarWidth() + "px");
	}	
	
	public void setScaleWidget(Widget scaleWidget, int scaleWidth){
		super.setScaleWidget(scaleWidget, scaleWidth);
	}
	
}
