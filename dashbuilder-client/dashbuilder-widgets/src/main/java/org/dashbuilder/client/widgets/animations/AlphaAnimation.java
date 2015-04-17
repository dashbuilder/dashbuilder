package org.dashbuilder.client.widgets.animations;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>Animation for switching widget visibility by applying a linear alpha animation.</p> 
 *
 * @since 0.3.0 
 */
public class AlphaAnimation extends Animation {
    
    private Widget widget;
    private boolean showing;

    public AlphaAnimation(Widget widget) {
        this.widget = widget;
    }

    public AlphaAnimation(AnimationScheduler scheduler, Widget widget) {
        super(scheduler);
        this.widget = widget;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (widget.isVisible()) showing = false;
        else showing = true;
    }

    @Override
    protected void onUpdate(double progress) {
        final double alpha = showing ? progress : 1 - progress;
        applyAlpha(widget, alpha);
    }

    @Override
    protected void onComplete() {
        super.onComplete();
        widget.setVisible(showing);
    }

    private void applyAlpha(final Widget panel, final double alpha) {
        if (alpha <= 1 && alpha >= 0) {
            panel.getElement().setAttribute("style", "filter: alpha(opacity=5);opacity: " + alpha);
        }
    }
    
}
