package com.ait.lienzo.charts.client.xy.axis;

import com.ait.lienzo.charts.client.axis.Axis;
import com.ait.lienzo.charts.client.axis.DateAxis;
import com.ait.lienzo.charts.client.xy.XYChartData;

import java.util.Date;
import java.util.List;

// TODO
public final class DateAxisBuilder extends AxisBuilder<Date> {

    protected DateAxis axis;

    public DateAxisBuilder(XYChartData data, double chartSizeAttribute, Axis.AxisJSO jso) {
        super(data, chartSizeAttribute);
    }


    @Override
    public List<AxisLabel> getLabels() {
        return null;
    }

    @Override
    public List<AxisValue<Date>> getValues(String modelProperty) {
        return null;
    }
}