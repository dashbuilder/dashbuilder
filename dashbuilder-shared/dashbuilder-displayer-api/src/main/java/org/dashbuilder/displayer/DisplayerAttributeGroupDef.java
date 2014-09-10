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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DisplayerAttributeGroupDef extends DisplayerAttributeDef {

    // SUBGROUPS

    public static final DisplayerAttributeGroupDef TITLE_GROUP =
            new DisplayerAttributeGroupDef( DisplayerAttributeGroupDef.COMMON_GROUP, "title",
                                            DisplayerAttributeDef.TITLE_VISIBLE,
                                            DisplayerAttributeDef.TITLE);

    public static final DisplayerAttributeGroupDef CHART_MARGIN_GROUP =
            new DisplayerAttributeGroupDef( DisplayerAttributeGroupDef.CHART_GROUP, "margin",
                                            DisplayerAttributeDef.CHART_MARGIN_BOTTOM,
                                            DisplayerAttributeDef.CHART_MARGIN_TOP,
                                            DisplayerAttributeDef.CHART_MARGIN_LEFT,
                                            DisplayerAttributeDef.CHART_MARGIN_RIGHT);

    public static final DisplayerAttributeGroupDef CHART_LEGEND_GROUP =
            new DisplayerAttributeGroupDef( DisplayerAttributeGroupDef.CHART_GROUP, "legend",
                                            DisplayerAttributeDef.CHART_SHOWLEGEND,
                                            DisplayerAttributeDef.CHART_LEGENDPOSITION);

    public static final DisplayerAttributeGroupDef TABLE_SORT_GROUP =
            new DisplayerAttributeGroupDef( DisplayerAttributeGroupDef.TABLE_GROUP, "sort",
                                            DisplayerAttributeDef.TABLE_SORTENABLED,
                                            DisplayerAttributeDef.TABLE_SORTCOLUMNID,
                                            DisplayerAttributeDef.TABLE_SORTORDER);

    public static final DisplayerAttributeGroupDef XAXIS_GROUP =
            new DisplayerAttributeGroupDef( DisplayerAttributeGroupDef.AXIS_GROUP, "x",
                                            DisplayerAttributeDef.XAXIS_SHOWLABELS,
                                            DisplayerAttributeDef.XAXIS_TITLE);

    public static final DisplayerAttributeGroupDef YAXIS_GROUP =
            new DisplayerAttributeGroupDef( DisplayerAttributeGroupDef.AXIS_GROUP, "y",
                                            DisplayerAttributeDef.YAXIS_SHOWLABELS,
                                            DisplayerAttributeDef.YAXIS_TITLE);

    // ROOT-GROUPS

    public static final DisplayerAttributeGroupDef COMMON_GROUP =
            new DisplayerAttributeGroupDef("common", DisplayerAttributeDef.TYPE,
                    DisplayerAttributeDef.RENDERER,
                    DisplayerAttributeDef.COLUMNS,
                    DisplayerAttributeGroupDef.TITLE_GROUP);

    public static final DisplayerAttributeGroupDef FILTER_GROUP =
            new DisplayerAttributeGroupDef("filter", DisplayerAttributeDef.FILTER_ENABLED,
                    DisplayerAttributeDef.FILTER_SELFAPPLY_ENABLED,
                    DisplayerAttributeDef.FILTER_LISTENING_ENABLED,
                    DisplayerAttributeDef.FILTER_NOTIFICATION_ENABLED);

    public static final DisplayerAttributeGroupDef CHART_GROUP =
            new DisplayerAttributeGroupDef("chart", DisplayerAttributeDef.CHART_WIDTH,
                    DisplayerAttributeDef.CHART_HEIGHT,
                    DisplayerAttributeDef.CHART_3D,
                    DisplayerAttributeGroupDef.CHART_MARGIN_GROUP,
                    DisplayerAttributeGroupDef.CHART_LEGEND_GROUP);

    public static final DisplayerAttributeGroupDef TABLE_GROUP =
            new DisplayerAttributeGroupDef("table", DisplayerAttributeDef.TABLE_PAGESIZE,
                    DisplayerAttributeDef.TABLE_WIDTH,
                    DisplayerAttributeGroupDef.TABLE_SORT_GROUP);

    public static final DisplayerAttributeGroupDef AXIS_GROUP =
            new DisplayerAttributeGroupDef("axis", DisplayerAttributeGroupDef.XAXIS_GROUP,
                    DisplayerAttributeGroupDef.YAXIS_GROUP);

    public static final DisplayerAttributeGroupDef METER_GROUP =
            new DisplayerAttributeGroupDef("meter", DisplayerAttributeDef.METER_START,
                    DisplayerAttributeDef.METER_WARNING,
                    DisplayerAttributeDef.METER_CRITICAL,
                    DisplayerAttributeDef.METER_END);

    private Set<DisplayerAttributeDef> flatMembers = new HashSet<DisplayerAttributeDef>();

    public DisplayerAttributeGroupDef() {
    }

    public DisplayerAttributeGroupDef( String id ) {
        super( id );
    }

    public DisplayerAttributeGroupDef( String id, DisplayerAttributeDef parent ) {
        super( id, parent );
    }

    private DisplayerAttributeGroupDef( String id, DisplayerAttributeDef ... members ) {
        super(id);
        addGroupMembers( members );
    }

    private DisplayerAttributeGroupDef( DisplayerAttributeDef parent, String id, DisplayerAttributeDef ... members ) {
        super(id, parent);
        addGroupMembers( members );
    }

    @Override
    // Recursively get all sub-members
    public DisplayerAttributeDef[] getMembers() {
        Set<DisplayerAttributeDef> members = new HashSet<DisplayerAttributeDef>();
        for ( DisplayerAttributeDef member : flatMembers ) {
            members.addAll( Arrays.asList( member.getMembers() ) );
        }
        return members.toArray( new DisplayerAttributeDef[]{} );
    }

    public DisplayerAttributeGroupDef addGroupMember( DisplayerAttributeDef member ) {
        member.setParent( this );
        flatMembers.add( member );
        return this;
    }

    private void addGroupMembers( DisplayerAttributeDef ... members ) {
        for ( DisplayerAttributeDef member : members ) {
            addGroupMember( member );
        }
    }
}
