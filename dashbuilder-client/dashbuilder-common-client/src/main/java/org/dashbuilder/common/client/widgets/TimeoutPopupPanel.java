package org.dashbuilder.common.client.widgets;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import org.dashbuilder.common.client.resources.i18n.DashbuilderCommonMessages;
import org.gwtbootstrap3.client.shared.event.AlertClosedEvent;
import org.gwtbootstrap3.client.shared.event.AlertClosedHandler;
import org.gwtbootstrap3.client.ui.Alert;

/**
 * <p>A popup panel that provides time out.</p>
 * 
 * <p>If <code>duration</code> greater than zero and popup panel is visible for more that <code>duration</code> millisedoncs,
 * an eveny of type <code>org.dashbuilder.common.client.widgets.TimeoutPopupPanel.TimeoutFiredEvent</code> is fired. </p>
 * <ul>
 *     <li>If no <code>org.dashbuilder.common.client.widgets.TimeoutPopupPanel.TimeoutFiredEventHandler</code> added for 
 *     handling the timeout, a default timeout screen is shown.</li>     
 *     <li>If any <code>org.dashbuilder.common.client.widgets.TimeoutPopupPanel.TimeoutFiredEventHandler</code> added for 
 *     handling the timeout, that handler is responsible to give feedback to the user and close the popup, if necessary.</li>
 * </ul>
 * 
 * <p>This custom popup panel by default adds a Flow panel as first child, and all children are added or removed from it. So
 *    it can contains more than one widget.</p> 
 */
public class TimeoutPopupPanel extends PopupPanel {

    private Timer timer;
    private int duration;
    private FlowPanel mainPanel = new FlowPanel();
    private FlowPanel messagePanel = new FlowPanel();
    private FlowPanel timeoutPanel = new FlowPanel();
    
    public TimeoutPopupPanel() {
        init();
    }

    public TimeoutPopupPanel(boolean autoHide) {
        super(autoHide);
        init();
    }

    public TimeoutPopupPanel(boolean autoHide, boolean modal) {
        super(autoHide, modal);
        init();
    }
    
    private void init() {
        mainPanel.add(messagePanel);
        timeoutPanel.setVisible(false);
        mainPanel.add(timeoutPanel);
        super.add(mainPanel);
    }

    public void setTimeout(final int duration) {
        this.duration = duration;
    }

    @Override
    public void add(IsWidget child) {
        messagePanel.add(child);
    }

    @Override
    public void add(Widget w) {
        messagePanel.add(w);
    }

    @Override
    public boolean remove(IsWidget child) {
        return messagePanel.remove(child);
    }

    @Override
    public boolean remove(Widget w) {
        return messagePanel.remove(w);
    }

    @Override
    public void show() {
        if (duration == 0) endTimer();
        if (duration > 0) startTimer();
        messagePanel.setVisible(true);
        timeoutPanel.setVisible(false);
        super.show();
    }
    
    @Override
    public void hide() {
        endTimer();
        super.hide();
    }

    @Override
    public void hide(boolean autoClosed) {
        endTimer();
        super.hide(autoClosed);
    }

    public void cancel() {
        endTimer();
    }
    
    private void startTimer() {
        if (timer != null) endTimer();
        
        timer = new Timer() {
            @Override
            public void run() {
                timeout();
            }
        };

        // Schedule the timer to run once in 5 seconds.
        timer.schedule(duration);
    }
    
    private void endTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    
    private Alert buildDefaultTimeoutPanel() {
        final String d = getDurationInSeconds();
        final String m = DashbuilderCommonMessages.INSTANCE.timeout(d);
        
        final Alert alert = new Alert(m);
        alert.addClosedHandler(new AlertClosedHandler() {
            @Override
            public void onClosed(AlertClosedEvent closedEvent) {
                hide();
            }
        });

        return alert;
    }
    
    private String getDurationInSeconds() {
        if (duration > 0) {
            final float s = duration / 1000;
            return NumberFormat.getFormat("#.0").format(s);
        }
        return "0";   
    }
    
    private void timeout() {
        // If not fired event handlers to handle the timeout, show the default timeout screen.
        if (getHandlerCount(TimeoutFiredEvent.TYPE) == 0) {
            messagePanel.setVisible(false);
            timeoutPanel.setVisible(true);
            timeoutPanel.clear();
            final Alert timeoutAlert = buildDefaultTimeoutPanel();
            timeoutPanel.add(timeoutAlert);
        }
    }

    public HandlerRegistration addTimeoutFiredEventHandler(TimeoutFiredEventHandler handler) {
        return this.addHandler(handler, TimeoutFiredEvent.TYPE);
    }
    
    /**
     * @since 0.3.0
     */
    public static class TimeoutFiredEvent extends GwtEvent<TimeoutFiredEventHandler> {

        public static GwtEvent.Type<TimeoutFiredEventHandler> TYPE = new GwtEvent.Type<TimeoutFiredEventHandler>();

        public TimeoutFiredEvent() {
            super();
        }

        @Override
        public GwtEvent.Type getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(TimeoutFiredEventHandler handler) {
            handler.onTimeoutFired(this);
        }

    }

    /**
     * @since 0.3.0
     */
    public interface TimeoutFiredEventHandler extends EventHandler
    {
        void onTimeoutFired(TimeoutFiredEvent event);
    }
}
