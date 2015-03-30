package org.dashbuilder.common.client.widgets.slider;

import com.google.gwt.user.client.ui.Image;
import org.dashbuilder.common.client.resources.bundles.DashbuilderCommonResources;
import org.dashbuilder.common.client.widgets.slider.view.SliderBarHorizontal;
import org.dashbuilder.common.client.widgets.slider.view.SliderBarVertical;
/*
    Usage:
    
        VerticalSlider slider = new VerticalSlider(1000, "300px", true);
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
public class VerticalSlider extends SliderBarVertical {

    public VerticalSlider(final int maxValue, final String height, final boolean showRows) {
        
        // Scale icon.
        final Image scaleImage = new Image(DashbuilderCommonResources.IMAGES.scalev().getSafeUri());
        final Image lessImage = new Image(DashbuilderCommonResources.IMAGES.lessv());
        final Image moreImage = new Image(DashbuilderCommonResources.IMAGES.morev());
        final Image dragImage= new Image(DashbuilderCommonResources.IMAGES.dragv());
        
        if (showRows){
            setLessWidget(lessImage);
            setScaleWidget(scaleImage, 16);
            setMoreWidget(moreImage);
        } else {
            setScaleWidget(scaleImage, 16);
        }
        setDragWidget(dragImage);
        this.setHeight(height);
        this.setMaxValue(maxValue);
    }

    
}
