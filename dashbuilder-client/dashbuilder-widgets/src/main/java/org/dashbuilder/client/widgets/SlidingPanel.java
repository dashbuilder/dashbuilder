package org.dashbuilder.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;

import java.util.Iterator;

/**
 * <p>A sliding panel implementation based on a layout panel.</p>
 * <p>You can add several widgets and it provides the ability to show a given one by applying a slide animation.</p>
 * <p>Usage:</p>
 * <code>
 *     SlidingPanel slidingPanel = new SlidingPanel();
 *     FlowPanel panel1 = new FlowPanel();
 *     slidingPanel.add(panel1);
 *     FlowPanel panel2 = new FlowPanel();
 *     slidingPanel.add(panel2);
 *     
 *     // slide when needed
 *     slidingPanel.setWidget(panel2);
 * </code>
 * <p>NOTE: You must set the <code>height</code> CSS attribute for this sliding panel in order to be displayed.</p> 
 */
public class SlidingPanel extends ResizeComposite implements HasWidgets, HasOneWidget {
    private final LayoutPanel layoutPanel = new LayoutPanel();
    private int currentIndex = -1;

    /**
     * Default constructor
     */
    public SlidingPanel() {
        initWidget(layoutPanel);
    }

    /**
     * Add a widget
     *
     * @param w the widget to be added
     */
    public void add(IsWidget w) {
        add(w.asWidget());
    }

    /**
     * Add a widget
     */
    public void add(Widget w) {
        layoutPanel.add(w);
        if (currentIndex < 0) {
            currentIndex = 0;
        } else {
            layoutPanel.setWidgetLeftWidth(w, 100, Style.Unit.PCT, 100, Style.Unit.PCT);
        }
    }

    /**
     * Clear the widgets
     */
    public void clear() {
        setWidget(null);
    }

    public Widget getWidget() {
        return layoutPanel.getWidget(currentIndex);
    }

    /**
     * @return widget iterator
     */
    public Iterator iterator() {
        return layoutPanel.iterator();
    }

    /**
     * Remove a widget
     *
     * @param w widget to be removed
     * @return result
     */
    public boolean remove(Widget w) {
        return layoutPanel.remove(w);
    }

    /**
     * Set the widget
     *
     * @param w the widget
     */
    public void setWidget(IsWidget w) {
        if (w != null) {
            setWidget(asWidgetOrNull(w));
        }
    }

    @Override
    public void setWidget(Widget widget) {
        int newIndex = layoutPanel.getWidgetIndex(widget);

        if (newIndex < 0) {
            newIndex = layoutPanel.getWidgetCount();
            add(widget);
        }

        show(newIndex);
    }

    private void show(int newIndex) {
        if (newIndex == currentIndex) {
            return;
        }

        boolean fromLeft = newIndex < currentIndex;
        final Widget current = layoutPanel.getWidget(currentIndex);
        Widget widget = layoutPanel.getWidget(newIndex);
        currentIndex = newIndex;

        layoutPanel.setWidgetLeftWidth(widget, 0, Style.Unit.PCT, 100, Style.Unit.PCT);
        if (fromLeft) {
            layoutPanel.setWidgetLeftWidth(current, 100, Style.Unit.PCT, 100, Style.Unit.PCT);
        } else {
            layoutPanel.setWidgetLeftWidth(current, -100, Style.Unit.PCT, 100, Style.Unit.PCT);
        }
        layoutPanel.animate(500);
    }
}