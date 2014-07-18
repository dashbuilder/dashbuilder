/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.displayer;

/*
 * Interface for chart displayers
 */
public interface ChartDisplayerSettings extends DisplayerSettings {

    public static int DEFAULT_WIDTH = 600;
    public static int DEFAULT_HEIGHT = 300;
    public static int DEFAULT_MARGINTOP = 20;
    public static int DEFAULT_MARGINBOTTOM = 50;
    public static int DEFAULT_MARGINLEFT = 80;
    public static int DEFAULT_MARGINRIGHT = 80;
    public static boolean DEFAULT_LEGEND_SHOW = true;
    public static Position DEFAULT_LEGEND_POSITION = Position.POSITION_RIGHT;

    int getWidth();

    void setWidth( int width );

    int getHeight();

    void setHeight( int height );

    int getMarginTop();

    void setMarginTop( int marginTop );

    int getMarginBottom();

    void setMarginBottom( int marginBottom );

    int getMarginLeft();

    void setMarginLeft( int marginLeft );

    int getMarginRight();

    void setMarginRight( int marginRight );

    boolean isLegendShow();

    void setLegendShow( boolean showLegend );

    Position getLegendPosition();

    void setLegendPosition( Position position );
}
