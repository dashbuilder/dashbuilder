$registerPerspective({
    id: "Sales Dashboard",
    is_serializable: "true",
    roles: [ "admins", "users" ],
    panel_type: "root_tab",
    view: {
        parts: [
            {
                place: "KPIScreen",
                parameters: {"kpi": "opps-by-status", "token" : "4"}
            },
            {
                place: "KPIScreen",
                parameters: {"kpi": "opps-by-salesman", "token" : "6"}
            },
            {
                place: "KPIScreen",
                parameters: {"kpi": "opps-by-product", "token" : "7"}
            },
            {
                place: "KPIScreen",
                parameters: {"kpi": "opps-by-country", "token" : "5"}
            },
            {
                place: "KPIScreen",
                parameters: {"kpi": "opps-by-prob", "token" : "0"}
            },
            {
                place: "KPIScreen",
                parameters: {"kpi": "opps-country-summary", "token" : "8"}
            }
        ],
        panels: [
            {
                height: 500,
                position: "north",
                panel_type: "simple",
                parts: [
                    {
                        place: "KPIScreen",
                        parameters: {"kpi": "opps-expected-pipeline", "token" : "2"}
                    }
                ],
                panels: [
                    {
                        height: 400,
                        position: "east",
                        panel_type: "simple",
                        parts: [
                            {
                                place: "KPIScreen",
                                parameters: {"kpi": "opps-by-pipeline", "token" : "3"}
                            }
                        ]
                    }
                ]
            }
        ]
    }
});
