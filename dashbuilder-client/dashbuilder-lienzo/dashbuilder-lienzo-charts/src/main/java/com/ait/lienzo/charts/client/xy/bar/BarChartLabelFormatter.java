package com.ait.lienzo.charts.client.xy.bar;

import com.ait.lienzo.client.core.animation.*;
import com.ait.lienzo.client.core.event.NodeMouseEnterEvent;
import com.ait.lienzo.client.core.event.NodeMouseEnterHandler;
import com.ait.lienzo.client.core.event.NodeMouseExitEvent;
import com.ait.lienzo.client.core.event.NodeMouseExitHandler;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Text;
import com.google.gwt.core.client.GWT;

import java.util.List;

public class BarChartLabelFormatter {

    private static final double ANIMATION_DURATION = 500;
    
    private List<BarChartLabel> labels;
    private BarChartLabelFormatterCallback callback;

    public BarChartLabelFormatter(List<BarChartLabel> labels) {
        this.labels = labels;
    }

    public BarChartLabelFormatter(List<BarChartLabel> labels, BarChartLabelFormatterCallback callback) {
        this.labels = labels;
        this.callback = callback;
    }

    public void format(double maxWidth, double maxHeight) {

        double rotation = checkRotation(maxWidth);

        if (labels != null && !labels.isEmpty()) {
            for (BarChartLabel label : labels) {
                // label.getLabelContainer().setAlpha(1);
                // label.getLabelContainer().setFillColor(new Color(40 * label.getAxisLabel().getIndex(), 0, 0));

                label.getLabelContainer().setRotationDegrees(rotation);
                label.getLabel().setRotationDegrees(rotation);

                label.getLabelContainer().setWidth(maxWidth);
                label.getLabelContainer().setHeight(maxHeight);

                cut(label, maxWidth, maxHeight, rotation);
            }
        }
        
    }
    
    private double checkRotation(double maxWidth) {
        if (labels != null && !labels.isEmpty()) {
            for (BarChartLabel label : labels) {
                if (label.getLabel().getBoundingBox().getWidth() > maxWidth) return 45;
            }
        }
        return 0;        
    }

    /**
     * Formats the label Text shapes in the given axis by cutting text value.
     */
    private void cut(BarChartLabel label, double maxWidth, double maxHeight, double rotation)
    {
        String text = label.getLabel().getText();

        // Cut text.
        cutLabelText(label, maxWidth - 5, maxHeight - 5, rotation);

        String cutText = label.getLabel().getText();

        // If text is cut, add suffix characters.
        if (text.length() != cutText.length()) {
            label.getLabel().setText(label.getLabel().getText() + "...");
        }

        // TODO: Animate.
        // animate(label, text, cutText, originalRotation);

        // Move label to top.
        label.getLabelContainer().moveToTop();
    }

    private void cutLabelText(BarChartLabel label, double maxWidth, double maxHeight, double rotation) {
        String text = label.getLabel().getText();
        if (text != null && text.length() > 1 && label.getLabel().getBoundingBox().getWidth() > maxWidth) {
            int cutLength = text.length() - 2;
            String cuttedText = text.substring(0, cutLength);
            label.getLabel().setText(cuttedText);
            cutLabelText(label, maxWidth, maxHeight, rotation);
        }
        if (text != null && rotation > 0  && text.length() > 1 && label.getLabel().getBoundingBox().getHeight() > maxHeight) {
            int cutLength = text.length() - 2;
            String cuttedText = text.substring(0, cutLength);
            label.getLabel().setText(cuttedText);
            cutLabelText(label, maxWidth, maxHeight, rotation);
        }
    }

    private void animate(final BarChartLabel label, final String text, final String cutText, final double originalRotation) {
        final Rectangle labelContainer = label.getLabelContainer();

        labelContainer.addNodeMouseEnterHandler(new NodeMouseEnterHandler() {
            @Override
            public void onNodeMouseEnter(NodeMouseEnterEvent event) {
                GWT.log("label mouse enter at " + label.getLabel().getText());
                highlight(label, text, cutText, originalRotation);
            }
        });

        labelContainer.addNodeMouseExitHandler(new NodeMouseExitHandler() {
            @Override
            public void onNodeMouseExit(NodeMouseExitEvent event) {
                GWT.log("label mouse exit at " + label.getLabel().getText());
                unhighlight(label, text, cutText, originalRotation);
            }
        });

    }

    private void unhighlight(BarChartLabel label, String text, String cutText, double originalRotation) {
        highlight(label, text, cutText, false, originalRotation);
    }

    private void highlight(BarChartLabel label, String text, String cutText, double originalRotation) {
        highlight(label, text, cutText, true, 0);
    }

    private void highlight(final BarChartLabel label, final String text, final String cutText, final boolean highlighting, final double rotation) {
        label.getLabel().setText(highlighting ? text : cutText);
        AnimationProperties animationProperties = new AnimationProperties();
        animationProperties.push(AnimationProperty.Properties.ROTATION_DEGREES(rotation));
        label.getLabel().animate(AnimationTweener.LINEAR, animationProperties, ANIMATION_DURATION, new AnimationCallback() {
            @Override
            public void onClose(IAnimation animation, IAnimationHandle handle) {
                super.onClose(animation, handle);
                label.getLabelContainer().setRotationDegrees(rotation);
            }
        });
        for (Text _label : getLabelTexts()) {
            if (!_label.getID().equals(label.getLabel().getID())) {
                AnimationProperties animationProperties2 = new AnimationProperties();
                animationProperties2.push(AnimationProperty.Properties.ALPHA(highlighting ? 0d : 1d));
                _label.animate(AnimationTweener.LINEAR, animationProperties2, ANIMATION_DURATION);
            }
        }


        if (callback != null && highlighting) callback.onLabelHighlighed(label);
        if (callback != null && !highlighting) callback.onLabelUnHighlighed(label);
    }

    private Text[] getLabelTexts() {
        Text[] result = new Text[labels.size()];
        int i = 0;
        for (BarChartLabel label : labels) {
            result[i++] = label.getLabel();
        }
        return result;
    }

    private Rectangle[] getLabelContainers() {
        Rectangle[] result = new Rectangle[labels.size()];
        int i = 0;
        for (BarChartLabel label : labels) {
            result[i++] = label.getLabelContainer();
        }
        return result;
    }

    public interface BarChartLabelFormatterCallback {

        public void onLabelHighlighed(BarChartLabel label);

        public void onLabelUnHighlighed(BarChartLabel label);
    }

    /**
     * Formats the label Text shapes in the given axis using the <code>visibility</code> attribute.
     */
        /*public void visibility(int index, double width, boolean animate) {
            if (labels != null && !labels.isEmpty()) {
                AxisBuilder.AxisLabel lastVisibleLabel = null;
                Text lastVisibleText = null;
                if (index > 0)  {
                    int last = 1;
                    lastVisibleText = labelTexts[index - last];
                    while (lastVisibleText != null && !lastVisibleText.isVisible()) {
                        lastVisibleText = labelTexts[index - ++last];
                    }
                    lastVisibleLabel = labels.get(index - last);

                }
                AxisBuilder.AxisLabel label = labels.get(index);
                double position = label.getPosition();
                String text = label.getText();
                Text intervalText = labelTexts[index];
                final double lastTextWidth = lastVisibleText != null ? lastVisibleText.getBoundingBox().getWidth() : 0;
                final double textWidth = intervalText.getBoundingBox().getWidth();
                intervalText.setText(text);
                // If labels are overlapped, do not show it.
                if (lastVisibleLabel != null && lastVisibleLabel.getPosition() + lastTextWidth > label.getPosition()) {
                    intervalText.setVisible(false);
                } else {
                    intervalText.setVisible(true);
                    double xPos = (index>0 && index < (labels.size() -1) ) ? position - textWidth/2 : position;
                    setShapeAttributes(intervalText, xPos, 10d, null, width, animate);
                }
            }
        }*/
    
}
