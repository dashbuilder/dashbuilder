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

import javax.enterprise.context.ApplicationScoped;

import static org.dashbuilder.displayer.DisplayerEditorConfig.ATTRIBUTE_PATH_SEPARATOR;

@ApplicationScoped
public class DisplayerAttributeDef {

    public static final DisplayerAttributeDef TYPE = new DisplayerAttributeDef("type");
    public static final DisplayerAttributeDef RENDERER = new DisplayerAttributeDef("renderer");
    public static final DisplayerAttributeDef COLUMNS = new DisplayerAttributeDef("columns");
    public static final DisplayerAttributeDef TITLE = new DisplayerAttributeDef("title");
    public static final DisplayerAttributeDef TITLE_VISIBLE = new DisplayerAttributeDef("visible");

    public static final DisplayerAttributeDef FILTER_ENABLED = new DisplayerAttributeDef("enabled");
    public static final DisplayerAttributeDef FILTER_SELFAPPLY_ENABLED = new DisplayerAttributeDef("selfapply_enabled");
    public static final DisplayerAttributeDef FILTER_NOTIFICATION_ENABLED = new DisplayerAttributeDef("notification_enabled");
    public static final DisplayerAttributeDef FILTER_LISTENING_ENABLED = new DisplayerAttributeDef("listening_enabled");

    public static final DisplayerAttributeDef CHART_WIDTH = new DisplayerAttributeDef("width");
    public static final DisplayerAttributeDef CHART_HEIGHT = new DisplayerAttributeDef("height");
    public static final DisplayerAttributeDef CHART_3D = new DisplayerAttributeDef("3d");
    public static final DisplayerAttributeDef CHART_MARGIN_TOP = new DisplayerAttributeDef("top");
    public static final DisplayerAttributeDef CHART_MARGIN_BOTTOM = new DisplayerAttributeDef("bottom");
    public static final DisplayerAttributeDef CHART_MARGIN_LEFT = new DisplayerAttributeDef("left");
    public static final DisplayerAttributeDef CHART_MARGIN_RIGHT = new DisplayerAttributeDef("right");
    public static final DisplayerAttributeDef CHART_SHOWLEGEND = new DisplayerAttributeDef("show");
    public static final DisplayerAttributeDef CHART_LEGENDPOSITION = new DisplayerAttributeDef("position");

    public static final DisplayerAttributeDef TABLE_PAGESIZE = new DisplayerAttributeDef("pageSize");
    public static final DisplayerAttributeDef TABLE_WIDTH = new DisplayerAttributeDef("width");
    public static final DisplayerAttributeDef TABLE_SORTENABLED = new DisplayerAttributeDef("enabled");
    public static final DisplayerAttributeDef TABLE_SORTCOLUMNID = new DisplayerAttributeDef("columnId");
    public static final DisplayerAttributeDef TABLE_SORTORDER = new DisplayerAttributeDef("order");

    public static final DisplayerAttributeDef XAXIS_SHOWLABELS = new DisplayerAttributeDef("labels_show");
    public static final DisplayerAttributeDef XAXIS_TITLE = new DisplayerAttributeDef("title");
    public static final DisplayerAttributeDef YAXIS_SHOWLABELS = new DisplayerAttributeDef("labels_show");
    public static final DisplayerAttributeDef YAXIS_TITLE = new DisplayerAttributeDef("title");

    public static final DisplayerAttributeDef METER_START = new DisplayerAttributeDef("start");
    public static final DisplayerAttributeDef METER_WARNING = new DisplayerAttributeDef("warning");
    public static final DisplayerAttributeDef METER_CRITICAL = new DisplayerAttributeDef("critical");
    public static final DisplayerAttributeDef METER_END = new DisplayerAttributeDef("end");

    public static final DisplayerAttributeDef BARCHART_HORIZONTAL = new DisplayerAttributeDef("horizontal");

    protected String id;
    protected DisplayerAttributeDef parent;

    public DisplayerAttributeDef() {
    }

    public DisplayerAttributeDef( String id ) {
        this( id, null );
    }

    public DisplayerAttributeDef( String id, DisplayerAttributeDef parent ) {
        this.id = id;
        this.parent = parent;
    }

    public String getFullId() {
        return parent != null ? parent.getFullId() + ATTRIBUTE_PATH_SEPARATOR + id : id;
    }

    public DisplayerAttributeDef[] getMembers() {
        return new DisplayerAttributeDef[]{ this };
    }

    public DisplayerAttributeDef getParent() {
        return parent;
    }

    public void setParent( DisplayerAttributeDef parent ) {
        // Disallow reuse of DisplayerAttributeDefs and group definitions
        if ( this.parent != null && !parent.equals( this.parent ) )
            throw new RuntimeException( "The attribute definition (" + id + ") is already assigned to " + this.parent.getFullId() );
        this.parent = parent;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj == null ) {
            return false;
        }
        if ( !( obj.getClass().getName().equalsIgnoreCase(this.getClass().getName()) ) ) {
            return false;
        }
        DisplayerAttributeDef that = (DisplayerAttributeDef) obj;
        return that.getFullId().equalsIgnoreCase( this.getFullId() );
    }

    @Override
    public int hashCode() {
        int result = 23;
        result = 31 * result + getFullId().hashCode();
        return result;
    }
}
