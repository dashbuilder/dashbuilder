package org.dashbuilder.common.client.widgets.slider.view;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.widgets.slider.event.BarValueChangedHandler;
import org.dashbuilder.common.client.widgets.slider.exception.WidgetNotFoundError;
import org.dashbuilder.common.client.widgets.slider.presenter.Display;
import org.dashbuilder.common.client.widgets.slider.presenter.Presenter;

import java.util.ArrayList;

/**
 * 
 * @author kiouri
 * The base class for Horizontal and Vertical SliderBar
 *
 */
public abstract class SliderBar extends FocusPanel implements Display {
	
    protected Widget scale, drag;
    protected int scaleSize, startPosition, delimSize;
    protected ArrayList<Widget> less = new ArrayList<Widget>(), 
                                more = new ArrayList<Widget>(),
                                orderedWidgets = new ArrayList<Widget>();
	protected TouchableAbsolutePanelPK absPanel = new  TouchableAbsolutePanelPK();
	protected boolean isMarksPlaced = false, wasInited = false;	
	protected String color; 
	protected Presenter presenter;
	
	public SliderBar(){
    	presenter = new Presenter(this, getOrientation());
		this.add(absPanel);
	}

	public SliderBar(Presenter presenter){
		if (presenter == null){
	    	this.presenter = new Presenter(this, getOrientation());			
		} else {
			this.presenter = presenter;
			presenter.setDislay(this);
		}
		this.add(absPanel);
	}
	
    /**
     * @return knob widget
     */
	public Widget getDragWidget() {
		return drag;
	}

	/**
	 * Sets knob visible. Knob becomes invisible if maxValue equals zero. 
	 */
	public void setDragVisible(boolean isVisible){
		if (drag != null){
			drag.setVisible(isVisible);
		}
	}
	
	/**
	 * @return list of buttons that increase the current value (knob moves right or down)
	 */
	public ArrayList<Widget> getLessWidgets() {
		return less;
	}	
	
	/**
	 *  @return list of buttons that reduce the current value (knob moves left or up)
	 */
	public ArrayList<Widget> getMoreWidgets() {
		 return more;
	}	
	
	public Widget getScaleWidget() {
		return scale;
	}
	
	protected int getStartPosition() {
		return this.startPosition;
	}	

	/**
	 * Sets the button for increase the current value (knob moves right or down)
	 * @param moreWidget - widget which supports handling of onMouseClick event 
	 */
	protected void setMoreWidget(Widget moreWidget){
		orderedWidgets.add(moreWidget);
		more.add(moreWidget);
	}
	
	/**
	 * Sets the button for decrease the current value (knob moves left or up)
	 */	
	protected void setLessWidget(Widget lessWidget){
		orderedWidgets.add(lessWidget);
		less.add(lessWidget);
	}
		
	/**
	 * 
	 * @param drag - widget for knob representing
	 */
	protected void setDragWidget(Widget drag){
		this.drag = drag;
	}
	
	/**
	 * 
	 * @param scaleWidget - widget for scale representing
	 * @param scaleSize - height of scale for horizontal sliderbar 
	 * or width of scale for vertical sliderbar.<br>
	 * It is possible to adjust thickness of scale with help of this parameter. 
	 *  
	 */
	protected void setScaleWidget(Widget scaleWidget, int scaleSize){
		orderedWidgets.add(scaleWidget);
		this.scale = scaleWidget;
		this.scaleSize = scaleSize;
	}
	
	public Widget getRootWidget() {
		return this;
	}
	
	/**
	 * With help of this method it is possible to control appearance of outline border around the sliderbar when sliderbar is in focus.
	 */
	public void setNotSelectedInFocus(){
		DOM.setStyleAttribute(this.getElement(), "outline", "0px");
	}
	
	/**
	 * With help of this method it is possible to draw marks of discret positions of knob on scale
	 * @param color - color of marks
	 * @param delimSize - height of marks for horizontal sliderbar or width of marks for vertical sliderbar
	 */
    public void drawMarks( String color, int delimSize ){
	   if (!isMarksPlaced){
		   this.color = color;
		   this.delimSize = delimSize;
		  presenter.drawMarks(color, delimSize);
		  isMarksPlaced = true;
       }	
    }
   
    /**
     *  Remove scale marks from sliderbar
     */
	public void removeMarks(){
		int childCount = absPanel.getWidgetCount();
		for (int i = 0; i < childCount; i++){
			Widget widget = absPanel.getWidget(i);
			if (widget instanceof Mark){
				absPanel.remove(i);
				childCount--;
				i--;
			}
		}
		isMarksPlaced = false;
	}
	
	/**
	 * Adds BarValueChangedHandler (for handling changes of knob position)
	 * @param barValueChangedHandler
	 * @return HandlerRegistration used to remove this handler
	 */
	public HandlerRegistration addBarValueChangedHandler(
			BarValueChangedHandler barValueChangedHandler) {
		return presenter.addBarValueChangedHandler(barValueChangedHandler);
	}

	/**
	 * Sets current value. Knob will be moved to correspond position.
	 * @param value
	 */
	public void setValue(int value){
		presenter.setValue(value);
	}

    protected void prepare(int maxValue, int pixelSize){
		drawScrollBar(pixelSize);		
		presenter.setMaxValue(maxValue);
		presenter.setAbsMaxLength(getAbsMaxLength());
		presenter.processParams();
		presenter.setValue(getValue());
    }
 
    /**
     * 
     * @return max possible value which knob may point to. This value corresponds to rightmost knob position for horizontal sliderbar or 
     * bottommost knob position for vertical sliderbar 
     */
	public int getMaxValue(){
		return presenter.getMaxValue();
	}

	/**
	 * Sets value which corresponds to rightmost position of knob for horizontal sliderbar o bottommost position 
	 * for vertical sliderbar.
	 * @param maxValue
	 */
	public void setMaxValue(int maxValue) {
		presenter.setMaxValue(maxValue);
		presenter.processParams();
		try {
			if (isMarksPlaced) {
				this.removeMarks();
				this.drawMarks(color, delimSize);
			}
			setValue(getValue());
		} catch (Exception e) {
		}
	}
	    
    public void processParams(){
    	presenter.processParams();
    	if (isMarksPlaced){
    		isMarksPlaced = false;
    		this.drawMarks(color, delimSize);
    	}
    }
    
    /**
     * Returns the value which corresponds to current position of knob 
     * @return
     */
    public int getValue(){
    	return presenter.getValue();
    }

	protected void onLoad() {
		super.onLoad();
		
		if (drag == null){
			throw new WidgetNotFoundError("Drag widget not found...");
		}
		
		if (scale == null){
			throw new WidgetNotFoundError("Scale widget not found...");
		}
		
		
		if (!wasInited) {
			presenter.bind();
			wasInited = true;
		}
		if (this.getOrientation() == Presenter.Orientation.HORIZONTAL) {
		    this.prepare(presenter.getMaxValue(), getW(getElement())); 
		} else {
			this.prepare(presenter.getMaxValue(), getH(getElement()));
		}
		reDrawMarks();
	}
	
	protected void reDrawMarks(){
		if (isMarksPlaced) {
			this.removeMarks();
			this.drawMarks(color, delimSize);
		}				
	}
	
   protected void putWidgetsToAbsPanel(){
	   for (int i = 0; i < orderedWidgets.size(); i++){
		   absPanel.add(orderedWidgets.get(i));
	   }
		this.absPanel.add(drag, 0,0);	   
		DOM.setStyleAttribute(drag.getElement(), "zIndex", "500");
   }

   /**
    * Sets minimal distance between scale marks in pixels. If distance between marks will be less then this value - marks will not be drowning.    
    * @param minMarkStep
    */
   public void setMinMarkStep(int minMarkStep){
	   presenter.setMinMarkStep(minMarkStep);
   }
    
    
    /*
    NOTE:
        - There exist a bug of gwt-slider-bar -> widget size is set to 0 if parent container is not visible.
        - The problem is that the method <code>this.getOffsetWidth</code> or <code>this.getOffsetHeight</code> returns 0 if parent container is not visible, so widget is initialized as size 0.
        - As a workaround, extract size from DOM w/h attribute and use it instead of previous methods.
     */
    
    
    protected int getW(final Element element) {
        final String w = DOM.getStyleAttribute(element, "width");
        return parseSize(w);
    }

    protected int getH(final Element element) {
        final String h = DOM.getStyleAttribute(element, "height");
        return parseSize(h);
    }
    
    // TODO: Improve.
    protected int parseSize(String size) {
        if (size == null || size.trim().length() == 0) return 0;
        if (size.endsWith("px")) return Integer.parseInt(size.substring(0, size.length() - 2));
        return 0;
    }

}
