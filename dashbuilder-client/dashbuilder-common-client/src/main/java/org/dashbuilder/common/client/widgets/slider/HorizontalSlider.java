package org.dashbuilder.common.client.widgets.slider;

import com.google.gwt.user.client.ui.Image;
import org.dashbuilder.common.client.resources.bundles.DashbuilderCommonResources;
import org.dashbuilder.common.client.widgets.slider.view.SliderBarHorizontal;

/*
    Usage:

        HorizontalSlider slider = new HorizontalSlider(1000, "300px", true);
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
public class HorizontalSlider extends SliderBarHorizontal {

    public HorizontalSlider(final int maxValue, final String width, final boolean showRows) {
        
        // Scale icon.
        final Image scaleImage = new Image(DashbuilderCommonResources.IMAGES.scaleh().getSafeUri());
        final Image lessImage = new Image(DashbuilderCommonResources.IMAGES.lessh());
        final Image moreImage = new Image(DashbuilderCommonResources.IMAGES.moreh());
        final Image dragImage= new Image(DashbuilderCommonResources.IMAGES.dragh());
        
        if (showRows){
            setLessWidget(lessImage);
            setScaleWidget(scaleImage, 16);
            setMoreWidget(moreImage);
        } else {
            setScaleWidget(scaleImage, 16);
        }
        setDragWidget(dragImage);
        this.setWidth(width);
        this.setMaxValue(maxValue);
    }

    
}
