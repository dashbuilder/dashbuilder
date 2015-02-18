/*
   Copyright (c) 2014,2015 Ahome' Innovation Technologies. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ait.lienzo.charts.client.pie;

import com.ait.lienzo.charts.client.ChartAttribute;
import com.ait.lienzo.charts.client.ChartNodeType;
import com.ait.lienzo.charts.client.pie.PieChartData.PieChartDataJSO;
import com.ait.lienzo.client.core.animation.AnimationCallback;
import com.ait.lienzo.client.core.animation.AnimationProperties;
import com.ait.lienzo.client.core.animation.AnimationProperty;
import com.ait.lienzo.client.core.animation.AnimationTweener;
import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.event.NodeMouseEnterEvent;
import com.ait.lienzo.client.core.event.NodeMouseEnterHandler;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IContainer;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.Node;
import com.ait.lienzo.client.core.shape.Slice;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.shape.json.IFactory;
import com.ait.lienzo.client.core.shape.json.validators.ValidationContext;
import com.ait.lienzo.client.core.shape.json.validators.ValidationException;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class PieChart extends Group
{
    protected PieChart(JSONObject node, ValidationContext ctx) throws ValidationException
    {
        super(node, ctx);

        setNodeType(ChartNodeType.PIE_CHART);

        initialize();
    }

    public PieChart(double radius, PieChartData data)
    {
        setNodeType(ChartNodeType.PIE_CHART);

        setRadius(radius);

        setData(data);

        getMetaData().put("creator", "Dean S. Jones").put("version", "1.0.1.SNAPSHOT");

        initialize();
    }

    protected final void initialize()
    {
        double radius = getRadius();

        PieChartData data = getData();

        if (radius <= 0)
        {
            return;
        }
        if ((null == data) || (data.size() < 1))
        {
            return;
        }
        final PieChartEntry[] values = data.getEntries();

        double sofar = 0;

        double total = 0;

        for (int i = 0; i < values.length; i++)
        {
            total += values[i].getValue();
        }
        Group slices = new Group();

        Group labels = new Group();

        labels.setListening(false);

        for (int i = 0; i < values.length; i++)
        {
            double value = values[i].getValue() / total;

            final PieSlice slice = new PieSlice(radius, sofar, value);

            slice.setFillColor(values[i].getColor()).setStrokeColor(ColorName.BLACK).setStrokeWidth(3);

            slice.addNodeMouseEnterHandler(new NodeMouseEnterHandler()
            {
                @Override
                public void onNodeMouseEnter(NodeMouseEnterEvent event)
                {
                    if (false == slice.isAnimating())
                    {
                        slice.setAnimating(true);

                        slice.setListening(false);

                        slice.getLayer().batch();

                        slice.animate(AnimationTweener.LINEAR, AnimationProperties.toPropertyList(AnimationProperty.Properties.SCALE(1.3, 1.3)), 333, new AnimationCallback()
                        {
                            @Override
                            public void onClose(IAnimation animation, IAnimationHandle handle)
                            {
                                slice.animate(AnimationTweener.LINEAR, AnimationProperties.toPropertyList(AnimationProperty.Properties.SCALE(1, 1)), 333, new AnimationCallback()
                                {
                                    @Override
                                    public void onClose(IAnimation animation, IAnimationHandle handle)
                                    {
                                        slice.setScale(null);

                                        slice.setListening(true);

                                        slice.setAnimating(false);

                                        slice.getLayer().batch();
                                    }
                                });
                            }
                        });
                    }
                }
            });
            slices.add(slice);

            double s_ang = Math.PI * (2.0 * sofar);

            double e_ang = Math.PI * (2.0 * (sofar + value));

            double n_ang = (s_ang + e_ang) / 2.0;

            if (n_ang > (Math.PI * 2.0))
            {
                n_ang = n_ang - (Math.PI * 2.0);
            }
            else if (n_ang < 0)
            {
                n_ang = n_ang + (Math.PI * 2.0);
            }
            double lx = Math.sin(n_ang) * (radius + 50);

            double ly = 0 - Math.cos(n_ang) * (radius + 50);

            TextAlign align;

            if (n_ang <= (Math.PI * 0.5))
            {
                lx += 2;

                align = TextAlign.LEFT;
            }
            else if ((n_ang > (Math.PI * 0.5)) && (n_ang <= Math.PI))
            {
                lx += 2;

                align = TextAlign.LEFT;
            }
            else if ((n_ang > Math.PI) && (n_ang <= (Math.PI * 1.5)))
            {
                lx -= 2;

                align = TextAlign.RIGHT;
            }
            else
            {
                lx -= 2;

                align = TextAlign.RIGHT;
            }
            String label = values[i].getLabel();

            if (null == label)
            {
                label = "";
            }
            else
            {
                label = label + " ";
            }
            Text text = new Text(label + getLabel(value * 100), "Calibri", "bold", 14).setFillColor(ColorName.BLACK).setX(lx).setY(ly).setTextAlign(align).setTextBaseLine(TextBaseLine.MIDDLE);

            Line line = new Line((Math.sin(n_ang) * radius), 0 - (Math.cos(n_ang) * radius), (Math.sin(n_ang) * (radius + 50)), 0 - (Math.cos(n_ang) * (radius + 50))).setStrokeColor(ColorName.BLACK).setStrokeWidth(3);

            labels.add(text);

            labels.add(line);

            sofar += value;
        }
        add(labels);

        add(slices);
    }

    public final PieChart setRadius(double radius)
    {
        getAttributes().setRadius(radius);

        return this;
    }

    public final double getRadius()
    {
        return getAttributes().getRadius();
    }

    public final PieChart setData(PieChartData data)
    {
        if (null != data)
        {
            getAttributes().put(ChartAttribute.PIE_CHART_DATA.getProperty(), data.getJSO());
        }
        return this;
    }

    public final PieChartData getData()
    {
        PieChartDataJSO jso = getAttributes().getArrayOfJSO(ChartAttribute.PIE_CHART_DATA.getProperty()).cast();

        return new PieChartData(jso);
    }

    private final native String getLabel(double perc)
    /*-{
		var numb = perc;

		return numb.toFixed(2) + "%";
    }-*/;

    @Override
    public JSONObject toJSONObject()
    {
        JSONObject object = new JSONObject();

        object.put("type", new JSONString(getNodeType().getValue()));

        if (false == getMetaData().isEmpty())
        {
            object.put("meta", new JSONObject(getMetaData().getJSO()));
        }
        object.put("attributes", new JSONObject(getAttributes().getJSO()));

        return object;
    }

    @Override
    public IFactory<Group> getFactory()
    {
        return new PieChartFactory();
    }

    public static class PieChartFactory extends GroupFactory
    {
        public PieChartFactory()
        {
            setTypeName(ChartNodeType.PIE_CHART.getValue());

            addAttribute(ChartAttribute.RADIUS, true);

            addAttribute(ChartAttribute.PIE_CHART_DATA, true);
        }

        @Override
        public boolean addNodeForContainer(IContainer<?, ?> container, Node<?> node, ValidationContext ctx)
        {
            return false;
        }

        @Override
        public PieChart create(JSONObject node, ValidationContext ctx) throws ValidationException
        {
            return new PieChart(node, ctx);
        }
    }

    private static class PieSlice extends Slice
    {
        private boolean m_animating = false;

        public PieSlice(double radius, double sofar, double value)
        {
            super(radius, Math.PI * (-0.5 + 2 * sofar), Math.PI * (-0.5 + 2 * (sofar + value)), false);
        }

        public final void setAnimating(boolean animating)
        {
            m_animating = animating;
        }

        public final boolean isAnimating()
        {
            return m_animating;
        }
    }
}