package com.ait.lienzo.charts.client.resizer;

import com.ait.lienzo.client.core.animation.*;
import com.ait.lienzo.client.core.event.*;
import com.ait.lienzo.client.core.shape.Arrow;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.ArrowType;
import com.ait.lienzo.shared.core.types.ColorName;
import com.google.gwt.event.shared.HandlerRegistration;

public class ChartResizer extends Group {

    private static final int RECTANGLE_SIZE = 30;
    private static final double RECTANGLE_INITIA_ALPHA = 0.2d;
    private static final double RECTANGLE_ANIMATION_DURATION = 500;
    protected static final double ANIMATION_DURATION = 1000;
    private double width;
    private double height;
    private int initialXPosition;
    private int initialYPosition;

    public ChartResizer(final double width, final double height) {
        build(width, height);
    }

    public Group build(final double width, final double height) {
        this.width = width;
        this.height = height;
        final double rectangleXPos = width - RECTANGLE_SIZE;
        final double rectangleYPos = height - RECTANGLE_SIZE;
        final Rectangle resizeRectangleButton = new Rectangle(RECTANGLE_SIZE, RECTANGLE_SIZE).setX(rectangleXPos).setY(rectangleYPos).setFillColor(ColorName.GREY).setDraggable(true).setAlpha(RECTANGLE_INITIA_ALPHA);
        final Rectangle resizeRectangle = new Rectangle(width, height).setX(0).setY(0).setFillColor(ColorName.GREY).setAlpha(0);
        final Arrow resizeArrow1 = new Arrow(new Point2D(width / 2, height / 2), new Point2D(width, height / 2), 0, 10, 10, 10, ArrowType.AT_END_TAPERED).setFillColor(ColorName.BLACK).setAlpha(0);
        final Arrow resizeArrow2 = new Arrow(new Point2D(width / 2, height / 2), new Point2D(width / 2, height), 0, 10, 10, 10, ArrowType.AT_END_TAPERED).setFillColor(ColorName.BLACK).setAlpha(0);
        final Arrow resizeArrow3 = new Arrow(new Point2D(width / 2, height / 2), new Point2D(0, height / 2), 0, 10, 10, 10, ArrowType.AT_END_TAPERED).setFillColor(ColorName.BLACK).setAlpha(0);
        final Arrow resizeArrow4 = new Arrow(new Point2D(width / 2, height / 2), new Point2D(width / 2, 0), 0, 10, 10, 10, ArrowType.AT_END_TAPERED).setFillColor(ColorName.BLACK).setAlpha(0);

        resizeRectangleButton.addNodeMouseEnterHandler(new NodeMouseEnterHandler() {
            @Override
            public void onNodeMouseEnter(NodeMouseEnterEvent event) {
                // Apply alphas.
                AnimationProperties animationProperties = new AnimationProperties();
                animationProperties.push(AnimationProperty.Properties.ALPHA(0.5));
                resizeRectangleButton.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);
                resizeRectangle.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);
                resizeArrow1.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);
                resizeArrow2.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);
                resizeArrow3.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);
                resizeArrow4.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);

            }
        });

        resizeRectangleButton.addNodeMouseExitHandler(new NodeMouseExitHandler() {
            @Override
            public void onNodeMouseExit(NodeMouseExitEvent event) {
                // Apply alphas.
                AnimationProperties animationProperties = new AnimationProperties();
                animationProperties.push(AnimationProperty.Properties.ALPHA(RECTANGLE_INITIA_ALPHA));
                resizeRectangleButton.animate(AnimationTweener.LINEAR, animationProperties, RECTANGLE_ANIMATION_DURATION);

                // Apply alphas.
                AnimationProperties animationProperties2 = new AnimationProperties();
                animationProperties2.push(AnimationProperty.Properties.ALPHA(0));
                resizeRectangle.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);
                resizeArrow1.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);
                resizeArrow2.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);
                resizeArrow3.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);
                resizeArrow4.animate(AnimationTweener.LINEAR, animationProperties2, RECTANGLE_ANIMATION_DURATION);
            }
        });

        resizeRectangleButton.addNodeDragStartHandler(new NodeDragStartHandler() {
            @Override
            public void onNodeDragStart(NodeDragStartEvent event) {
                initialXPosition = event.getX();
                initialYPosition = event.getY();
            }
        });

        resizeRectangleButton.addNodeDragEndHandler(new NodeDragEndHandler() {
            @Override
            public void onNodeDragEnd(NodeDragEndEvent event) {
                int currentX = event.getX();
                int currentY = event.getY();
                int incrementX = currentX - initialXPosition;
                int incrementY = currentY - initialYPosition;
                initialXPosition = currentX;
                initialYPosition = currentY;
                double finalWidth = ChartResizer.this.width + incrementX;
                double finalHeight = ChartResizer.this.height + incrementY;

                // Animate the resize rectangle to its final position.
                AnimationProperties rectangleAnimationProperties = new AnimationProperties();
                rectangleAnimationProperties.push(AnimationProperty.Properties.X(finalWidth - RECTANGLE_SIZE));
                rectangleAnimationProperties.push(AnimationProperty.Properties.Y(finalHeight - RECTANGLE_SIZE));
                IAnimationHandle rectangleAnimationHandle = resizeRectangleButton.animate(AnimationTweener.LINEAR, rectangleAnimationProperties, ANIMATION_DURATION);

                // Fire the resize event with apply flag set (final size).
                ChartResizer.this.fireEvent(new ChartResizeEvent(finalWidth, finalHeight, true));

                ChartResizer.this.width = finalWidth;
                ChartResizer.this.height = finalHeight;
            }
        });

        resizeRectangleButton.addNodeDragMoveHandler(new NodeDragMoveHandler() {
            @Override
            public void onNodeDragMove(NodeDragMoveEvent event) {
                int currentX = event.getX();
                int currentY = event.getY();
                int incrementX = currentX - initialXPosition;
                int incrementY = currentY - initialYPosition;
                double finalWidth = ChartResizer.this.width + incrementX;
                double finalHeight = ChartResizer.this.height + incrementY;
                resizeRectangle.setWidth(finalWidth).setHeight(finalHeight);
                Point2D start = new Point2D(finalWidth / 2, finalHeight / 2);
                resizeArrow1.setStart(start).setEnd(new Point2D(finalWidth, finalHeight / 2));
                resizeArrow2.setStart(start).setEnd(new Point2D(finalWidth / 2, finalHeight));
                resizeArrow3.setStart(start).setEnd(new Point2D(0, finalHeight / 2));
                resizeArrow4.setStart(start).setEnd(new Point2D(finalWidth / 2, 0));

                // Fire the resize event with apply flag not set (not final size yet).
                ChartResizer.this.fireEvent(new ChartResizeEvent(finalWidth, finalHeight, false));
                
                LayerRedrawManager.get().schedule(resizeRectangle.getLayer());
            }
        });

        this.add(resizeRectangle);
        this.add(resizeArrow1);
        this.add(resizeArrow2);
        this.add(resizeArrow3);
        this.add(resizeArrow4);
        this.add(resizeRectangleButton);

        return this;
    }

    public HandlerRegistration addChartResizeEventHandler(ChartResizeEventHandler handler)
    {
        return addEnsureHandler(ChartResizeEvent.TYPE, handler);
    }


}
