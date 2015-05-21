package org.dashbuilder.common.client.widgets.slider;

import com.google.gwt.user.client.ui.Image;
import org.dashbuilder.common.client.resources.bundles.DashbuilderCommonResources;
import org.dashbuilder.common.client.widgets.slider.view.SliderBarHorizontal;

/*
    Usage:

        TriangleSlider slider = new TriangleSlider(1000, "300px", true);
        slider.addBarValueChangedHandler(new BarValueChangedHandler() {
            @Override
            public void onBarValueChanged(BarValueChangedEvent event) {
                GWT.log("slider value = " + event.getValue());
            }
        });
        slider.drawMarks("white", 6);
        slider.setMinMarkStep(3);
        slider.setNotSelectedInFocus();
 */
public class TriangleSlider extends SliderBarHorizontal {

    public TriangleSlider(final int maxValue, final int width, final boolean showRows) {
        
        // Scale icon.
        final Image scaleImage = new Image(DashbuilderCommonResources.IMAGES.linet().getSafeUri());
        final Image lessImage = new Image(DashbuilderCommonResources.IMAGES.moreLesst());
        lessImage.setSize("1px","10px");
        final Image moreImage = new Image(DashbuilderCommonResources.IMAGES.moreLesst());
        moreImage.setSize("1px","10px");
        final Image dragImage= new Image(DashbuilderCommonResources.IMAGES.dragt());
        dragImage.setSize("20px","17px");
        
        if (showRows){
            setLessWidget(lessImage);
            setScaleWidget(scaleImage, 1);
            setMoreWidget(moreImage);
        } else {
            setScaleWidget(scaleImage, 1);
        }
        
        setDragWidget(dragImage);
        this.setWidth(width);
        this.setMaxValue(maxValue);
    }

    
}
