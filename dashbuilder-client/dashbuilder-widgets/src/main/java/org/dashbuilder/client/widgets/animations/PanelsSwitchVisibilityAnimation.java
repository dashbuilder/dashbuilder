package org.dashbuilder.client.widgets.animations;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Panel;

/**
 * <p>Animation for switching visibility between two GWT panels, by applying a linear alpha animation during a given period of time.</p> 
 */
public class PanelsSwitchVisibilityAnimation extends Animation {
    
    private Panel panelA;
    private Panel panelB;
    
    private boolean isShowA;

    public PanelsSwitchVisibilityAnimation(Panel panelA, Panel panelB) {
        this.panelA = panelA;
        this.panelB = panelB;
    }

    public PanelsSwitchVisibilityAnimation(AnimationScheduler scheduler, Panel panelA, Panel panelB) {
        super(scheduler);
        this.panelA = panelA;
        this.panelB = panelB;
    }
    
    public void showA(final int duration) {
        isShowA = true;
        panelA.setVisible(false);
        run(duration);
    }

    public void showB(final int duration) {
        isShowA = false;
        panelB.setVisible(false);
        run(duration);
    }

    @Override
    protected void onUpdate(double progress) {
        final Panel panelToShow = isShowA ? panelA : panelB;
        final Panel panelToHide = isShowA ? panelB : panelA;
        final double alphaToShow =  progress;
        final double alphaToHide =  1 - progress;

        applyAlpha(panelToShow, alphaToShow);
        if (panelToHide.isVisible()) applyAlpha(panelToHide, alphaToHide);
    }

    @Override
    protected void onComplete() {
        super.onComplete();
        final Panel panelToShow = isShowA ? panelA : panelB;
        final Panel panelToHide = isShowA ? panelB : panelA;
        panelToShow.setVisible(true);
        panelToHide.setVisible(false);
    }

    private void applyAlpha(final Panel panel, final double alpha) {
        if (alpha <= 1 && alpha >= 0) {
            // if (panel == panelA) GWT.log("alpha="+alpha);
            panel.getElement().setAttribute("style", "filter: alpha(opacity=5);opacity: " + alpha);
        }
    }
    
}
