package org.dashbuilder.displayer;

public enum DisplayerSettingId {

    TYPE,
    RENDERER,

    TITLE,
    TITLE_VISIBLE,
    FILTER_ENABLED,
    FILTER_SELFAPPLY_ENABLED,
    FILTER_NOTIFICATION_ENABLED,
    FILTER_LISTENING_ENABLED,

    // (abstract)chartdisplayersettings
    CHART_WIDTH,
    CHART_HEIGHT,
    CHART_MARGIN_TOP,
    CHART_MARGIN_BOTTOM,
    CHART_MARGIN_LEFT,
    CHART_MARGIN_RIGHT,
    CHART_SHOWLEGEND,
    CHART_LEGENDPOSITION,

    // tabledisplayersettings
    TABLE_PAGESIZE,
    TABLE_WIDTH,
    TABLE_SORTENABLED,
    TABLE_DEFAULTSORTCOLUMNID,
    TABLE_DEFAULTSORTORDER,

    // displayersettings for selector-, area-, map-, pie-, line- : none to be added

    // abstractxaxisdisplayersettings
    AXIS_SHOWLABELS,
    AXIS_LABELSANGLE,
    AXIS_TITLE,

    //meterchartdisplayersettings
    METER_START,
    METER_WARNING,
    METER_CRITICAL,
    METER_END,

    // barchartdisplayersettings
    BARCHART_THREEDIMENSION,
    BARCHART_HORIZONTAL;

}
