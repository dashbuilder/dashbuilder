package org.dashbuilder.common.client.widgets.slider.presenter;

public class SliderBarCalculator {
	protected int maxValue;
	protected int absMaxLength;
	protected int minPosition, maxPosition;
	
	private int step;
	private double k;
	
	public int getMaxValue() {
		return maxValue;
	}
	 
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}
	
	public void setAbsMaxLength(int absMaxLength) {
		this.absMaxLength = absMaxLength;
	}
			
	protected void processParams(){
		int usedAbsLength = absMaxLength;
		minPosition = 0;
		maxPosition = absMaxLength;
		if (absMaxLength >= maxValue){
			usedAbsLength = absMaxLength - absMaxLength % maxValue; 
			minPosition = (absMaxLength - usedAbsLength) / 2;
			maxPosition = minPosition + usedAbsLength;
		}
		step = usedAbsLength/maxValue;
		if (step == 0){
			step = 1;
		}
		k = (double)usedAbsLength/maxValue;
	}	
		
	public int checkAbsPosition(int absPosition){
		if (absPosition < minPosition){
			absPosition = minPosition;
		}
		if (absPosition > maxPosition){
			absPosition = maxPosition;
		}
		return absPosition;
	}
	
	protected int clcValueByAbsPosition(int currentAbsPosition){
		currentAbsPosition = checkAbsPosition(currentAbsPosition) - minPosition;
		int value = (int)Math.round((currentAbsPosition/k));
		return value;
	}
	
	protected int clcAbsPositionByValue(int value){
		int absPosition;
		if (k >= 1){
		   absPosition = value * step;
		} else {
			absPosition = (int)Math.round(value * k); 
		}
		return absPosition  + minPosition;
	}
}
