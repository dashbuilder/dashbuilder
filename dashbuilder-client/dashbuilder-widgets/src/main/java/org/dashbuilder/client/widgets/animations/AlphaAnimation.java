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

    private static final String STYLE_ATTRIBUTE = "style";
    private Widget widget;
    private String staticStyle;
    private Boolean showing;

    public AlphaAnimation(final Widget widget, final String staticStyle) {
        this.widget = widget;
        this.staticStyle = staticStyle;
    }

    public AlphaAnimation(final Widget widget) {
        this.widget = widget;
    }

    public AlphaAnimation(final AnimationScheduler scheduler, final Widget widget, final String staticStyle) {
        super(scheduler);
        this.widget = widget;
        this.staticStyle = staticStyle;
    }

    public AlphaAnimation(final AnimationScheduler scheduler, final Widget widget) {
        super(scheduler);
        this.widget = widget;
    }
    
    public void show(final int duration) {
        showing = true;
        run(duration);
    }
    
    public void hide(final int duration) {
        showing = false;
        run(duration);
    }

    public void setStaticStyle(String style) {
        this.staticStyle = style;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (showing == null) {
            if (widget.isVisible()) showing = false;
            else showing = true;
        }
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
            final String style = this.staticStyle != null ? this.staticStyle + ";": "";
            panel.getElement().setAttribute(STYLE_ATTRIBUTE, style + " filter: alpha(opacity=5);opacity: " + alpha);
        }
    }
    
}
