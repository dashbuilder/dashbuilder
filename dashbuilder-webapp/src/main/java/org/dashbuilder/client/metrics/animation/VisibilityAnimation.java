package org.dashbuilder.client.metrics.animation;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

public class VisibilityAnimation extends Animation {
    
    private final Widget widget;
    private boolean         opening;

    public VisibilityAnimation(Widget widget)
    {
        this.widget = widget;
    }

    @Override
    protected void onComplete()
    {
        if(! opening)
            this.widget.setVisible(false);

        DOM.setStyleAttribute(this.widget.getElement(), "height", "auto");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        opening = ! this.widget.isVisible();

        if(opening)
        {
            DOM.setStyleAttribute(this.widget.getElement(), "height", "0px");
            this.widget.setVisible(true);
        }

    }

    @Override
    protected void onUpdate(double progress)
    {
        int scrollHeight = DOM.getElementPropertyInt(this.widget.getElement(), "scrollHeight");
        int height = (int) (progress * scrollHeight);
        if( !opening )
        {
            height = scrollHeight - height;
        }
        height = Math.max(height, 1);
        DOM.setStyleAttribute(this.widget.getElement(), "height", height + "px");
    }

}
