$registerPerspective({
    id: "Sales Dashboard",
    roles: [ "admins", "users" ],
    panel_type: "root_tab",
    view: {
        parts: [
            {
                place: "KPIPresenter",
                parameters: {"kpi": "opps-by-status", "token" : "4"}
            },
            {
                place: "KPIPresenter",
                parameters: {"kpi": "opps-by-salesman", "token" : "6"}
            },
            {
                place: "KPIPresenter",
                parameters: {"kpi": "opps-by-product", "token" : "7"}
            },
            {
                place: "KPIPresenter",
                parameters: {"kpi": "opps-by-country", "token" : "5"}
            },
            {
                place: "KPIPresenter",
                parameters: {"kpi": "opps-by-prob", "token" : "0"}
            }
        ],
        panels: [
            {
                height: 500,
                position: "north",
                panel_type: "simple",
                parts: [
                    {
                        place: "KPIPresenter",
                        parameters: {"kpi": "opps-expected-pipeline", "token" : "2"}
                    }
                ],
                panels: [
                    {
                        height: 500,
                        position: "east",
                        panel_type: "simple",
                        parts: [
                            {
                                place: "KPIPresenter",
                                parameters: {"kpi": "opps-by-pipeline", "token" : "3"}
                            }
                        ]
                    }
                ]
            }
        ]
    }
});
