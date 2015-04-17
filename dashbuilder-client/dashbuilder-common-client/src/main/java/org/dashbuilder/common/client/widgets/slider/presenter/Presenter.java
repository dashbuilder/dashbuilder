package org.dashbuilder.common.client.widgets.slider.presenter;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.widgets.slider.event.BarValueChangedEvent;
import org.dashbuilder.common.client.widgets.slider.event.BarValueChangedHandler;
import org.dashbuilder.common.client.widgets.slider.view.Mark;

import java.util.ArrayList;

public class Presenter {
	
	public static enum Orientation{
		VERTICAL,HORIZONTAL
	}

	protected String POINTER = "Pointer";
	protected String DEFAULT = "Default";

    protected HandlerManager handlerManager = new HandlerManager(this);
	protected SliderBarCalculator sliderBarCalulator = new SliderBarCalculator();
	protected Display display;
	protected int maxValue;
	protected int touchPosition;
	protected boolean inDrag;
	protected int currentValue = 0, lastFiredValue = -1;
	protected Orientation orientation;
	protected int minMarkStep = 10;
		
	public Presenter(Display display, Orientation orientation){
		this.display = display;		
		this.orientation = orientation;
	}
	
	public Presenter(Orientation orientation){
		this(null, orientation);
	}
	
	public void setDislay(Display display) {
		this.display = display;
	}

	public HandlerRegistration addBarValueChangedHandler(BarValueChangedHandler barValueChangedHandler){
		return handlerManager.addHandler(BarValueChangedEvent.TYPE, barValueChangedHandler);
	}
		
	public void setMaxValue(int maxValue){
		maxValue = (maxValue >= 0) ? maxValue : 0;
		if (maxValue == 0){
			display.setDragVisible(false);
		} else {
			display.setDragVisible(true);
		}
		this.maxValue = maxValue;
		sliderBarCalulator.setMaxValue(maxValue);
	}	
	
	public void setAbsMaxLength(int absMaxLength){
		sliderBarCalulator.setAbsMaxLength(absMaxLength);
	}	
	
	public void setBarPixelSize(int barPixelSize){
		display.drawScrollBar(barPixelSize);
		sliderBarCalulator.setAbsMaxLength(display.getAbsMaxLength());
	}
		
	public void setValue(int value){
		value = checkValue(value);
		currentValue = value;
		if (!display.isAttached()){
			return;
		}		
		int absPosition  = sliderBarCalulator.clcAbsPositionByValue(value); 
		setDragPosition(absPosition, true);
	}
	
	public void processParams(){
		if (this.maxValue == 0){
			return;
		}
    	sliderBarCalulator.processParams();
    }
		
	public int getValue(){
		return currentValue;
	}

	protected void onRootMouseWheel(MouseWheelEvent event){
		increaseValue(event.getDeltaY());
	}
	
	protected void onRootKeyUpLeft(){
		increaseValue(-1);
	}
	
	protected void onRootKeyDownRight(){
		increaseValue(1);
	}
	
	protected void onDragMouseDown(MouseDownEvent event){
		if (this.maxValue == 0){
			return;
		}
		stopDefaultAndPropagationForEvent(event);
		DOM.setCapture(display.getDragWidget().getElement());
		inDrag = true;
		touchPosition = display.getScaleTouchPosition(event);
	}
	
	protected void onDragMouseUp(MouseUpEvent event){
		if (this.maxValue == 0){
			return;
		}
		inDrag = false;
		stopDefaultAndPropagationForEvent(event);
		DOM.releaseCapture(display.getDragWidget().getElement());
		currentValue = sliderBarCalulator.clcValueByAbsPosition(display.getDragPosition());
		setDragPosition(sliderBarCalulator.clcAbsPositionByValue(currentValue),true);
	}

	protected void onDragMouseMove(MouseMoveEvent event){
		if (this.maxValue == 0){
			return;
		}
		event.preventDefault();
		if (! inDrag){
			return;
		}
		int newTochPosition = display.getScaleTouchPosition(event);
		setDragPosition(sliderBarCalulator.checkAbsPosition(display.getDragPosition() +  newTochPosition - touchPosition), false);
		touchPosition = newTochPosition;		
	}
	
	protected void onScaleMouseDown(MouseDownEvent event){
		if (this.maxValue == 0){
			return;
		}
		stopDefaultAndPropagationForEvent(event);
		currentValue = sliderBarCalulator.clcValueByAbsPosition(display.getScaleTouchPosition(event));
		setDragPosition(sliderBarCalulator.clcAbsPositionByValue(currentValue),true);
	}

	protected void onRootMouseDown(MouseDownEvent event){
		if (this.maxValue == 0){
			return;
		}
		currentValue = sliderBarCalulator.clcValueByAbsPosition(display.getScaleTouchPosition(event));
		setDragPosition(sliderBarCalulator.clcAbsPositionByValue(currentValue), true);
	}
		
	protected void onRootMouseOver(MouseOverEvent event){
		if (this.maxValue == 0){
			return;
		}
		setCursorType(POINTER);
		display.getRootWidget().getElement().focus();	
	}
	
	protected void onRootMouseOut(MouseOutEvent event){
		if (this.maxValue == 0){
			return;
		}
		setCursorType(DEFAULT);
	}	
	
	protected void onLessMouseDown(MouseDownEvent event){
		if (this.maxValue == 0){
			return;
		}		
		stopDefaultAndPropagationForEvent(event);
		increaseValue(-1);
	}
	
	protected void onMoreMouseDown(MouseDownEvent event){
		if (this.maxValue == 0){
			return;
		}
		stopDefaultAndPropagationForEvent(event);
		increaseValue(1);
	}	
		
	public void bind(){

		((HasMouseWheelHandlers)display.getRootWidget()).addMouseWheelHandler(new MouseWheelHandler(){
			public void onMouseWheel(MouseWheelEvent event) {	
				event.preventDefault();
				onRootMouseWheel(event);
			}			
		});				
		
		((HasKeyDownHandlers) display.getRootWidget())
				.addKeyDownHandler(new KeyDownHandler() {
					public void onKeyDown(KeyDownEvent event) {
						int nativeKeyCode = event.getNativeKeyCode();
						if (orientation == Orientation.VERTICAL) {
							if (nativeKeyCode == KeyCodes.KEY_UP) {
								onRootKeyUpLeft();
							}
							if (nativeKeyCode == KeyCodes.KEY_DOWN) {
								onRootKeyDownRight();
							}
						} else {
							if (nativeKeyCode == KeyCodes.KEY_LEFT) {
								onRootKeyUpLeft();
							}
							if (nativeKeyCode == KeyCodes.KEY_RIGHT) {
								onRootKeyDownRight();
							}
						}
					};
				});
		
		((HasMouseDownHandlers)display.getDragWidget()).addMouseDownHandler(new MouseDownHandler(){
			public void onMouseDown(MouseDownEvent event) {
				onDragMouseDown(event);
			}			
		});
		
		((HasMouseMoveHandlers)display.getDragWidget()).addMouseMoveHandler(new MouseMoveHandler(){
			public void onMouseMove(MouseMoveEvent event) {
				onDragMouseMove(event);
			}			
		});
		
		((HasMouseUpHandlers)display.getDragWidget()).addMouseUpHandler(new MouseUpHandler(){
			public void onMouseUp(MouseUpEvent event) {
				onDragMouseUp(event);
			}			
		});
		
		((HasMouseDownHandlers)display.getScaleWidget()).addMouseDownHandler(new MouseDownHandler() {
			public void onMouseDown(MouseDownEvent event) {
				onScaleMouseDown(event);
			}
		});
		
		((HasMouseDownHandlers)display.getRootWidget()).addMouseDownHandler(new MouseDownHandler() {
			public void onMouseDown(MouseDownEvent event) {
				onRootMouseDown(event);
			}
		});	
		
		((HasMouseOverHandlers)display.getRootWidget()).addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				onRootMouseOver(event);
			}
		});	

		((HasMouseOutHandlers)display.getRootWidget()).addMouseOutHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent event) {
				onRootMouseOut(event);
			}
		});	
		
		ArrayList<Widget> lessWidgets = display.getLessWidgets();
		if (lessWidgets != null) {
			for (int i = 0; i < lessWidgets.size(); i++) {
				((HasMouseDownHandlers) lessWidgets.get(i))
						.addMouseDownHandler(new MouseDownHandler() {
							public void onMouseDown(MouseDownEvent event) {
								onLessMouseDown(event);
							}
						});
			}
		}

		ArrayList<Widget> moreWidgets = display.getMoreWidgets();
		if (moreWidgets != null) {
			for (int i = 0; i < moreWidgets.size(); i++) {
				((HasMouseDownHandlers) moreWidgets.get(i))
						.addMouseDownHandler(new MouseDownHandler() {
							public void onMouseDown(MouseDownEvent event) {
								onMoreMouseDown(event);
							}
						});
			}
		}
	}

	public int getMaxValue() {
		return maxValue;
	}
	
	protected int checkValue(int value){
		value = (value >= maxValue) ? maxValue : value; 
		value = (value < 0) ? 0 : value; 
//	    if (value >= maxValue){
//	    	value = maxValue;
//	    }
//	    if (value < 0){
//      	    value = 0;
//	    }    
	    return value;
	}
	
	protected void increaseValue(int stepCount){
		currentValue += stepCount;
		currentValue = checkValue(currentValue);
	    int cDragPosition = display.getDragPosition();
	    int nPosition     = sliderBarCalulator.clcAbsPositionByValue(currentValue);
	    if (cDragPosition == nPosition){
	    	nPosition += stepCount/Math.abs(stepCount);
	    	currentValue = sliderBarCalulator.clcValueByAbsPosition(nPosition);
	    }
	    setDragPosition(sliderBarCalulator.clcAbsPositionByValue(currentValue), true);
	}
	
	public void setDragPosition(int position, boolean fireEvent) {
		currentValue = sliderBarCalulator.clcValueByAbsPosition(position);
		display.setDragPosition(position);
		if (fireEvent && currentValue != lastFiredValue){
			handlerManager.fireEvent(new BarValueChangedEvent(currentValue));
		}
	}	
	
	protected void stopDefaultAndPropagationForEvent(DomEvent event){
		event.preventDefault();
		event.stopPropagation();
	}
	
	public void setCursorType(String cursorType) {
		DOM.setStyleAttribute(display.getRootWidget().getElement(), "cursor", cursorType);
	}
	
	public void drawMarks( String color, int delimSize ){
		if (!isMarkAvailable()){
			return;
		}
		int markWidth, markHeight;
		if (this.orientation == Orientation.VERTICAL){
			markWidth  = delimSize;
			markHeight = 1;
		} else {
			markWidth  = 1;
			markHeight = delimSize;			
		}
		
		for (int i = 0; i<= this.maxValue; i++){
		   Mark mark = new Mark(color, markWidth, markHeight);
		   int markPosition = sliderBarCalulator.clcAbsPositionByValue(i);
		   display.putMark(mark, markPosition);
		}
	}
	
	protected int getMarkStep(){
		return 
		sliderBarCalulator.clcAbsPositionByValue(1) - sliderBarCalulator.clcAbsPositionByValue(0);
	}
	
	protected boolean isMarkAvailable() {
		int currentMarkStep = getMarkStep();
		if (currentMarkStep < this.minMarkStep || this.getMaxValue() == 0) {
			return false;
		} else {
			return true;
		}
	}

	public void setMinMarkStep(int minMarkStep) {
		this.minMarkStep = minMarkStep;
	}
			
}

