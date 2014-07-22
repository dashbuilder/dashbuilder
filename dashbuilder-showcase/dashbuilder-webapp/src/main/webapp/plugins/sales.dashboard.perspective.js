$registerPerspective({
    id: "Sales Dashboard",
    is_serializable: "true",
    roles: [ "admins", "users" ],
    panel_type: "root_tab",
    view: {
        parts: [
            {
                place: "DisplayerScreen",
                parameters: {"uuid": "opps-by-status", "token" : "4"}
            },
            {
                place: "DisplayerScreen",
                parameters: {"uuid": "opps-by-salesman", "token" : "6"}
            },
            {
                place: "DisplayerScreen",
                parameters: {"uuid": "opps-by-product", "token" : "7"}
            },
            {
                place: "DisplayerScreen",
                parameters: {"uuid": "opps-by-country", "token" : "5"}
            },
            {
                place: "DisplayerScreen",
                parameters: {"uuid": "opps-country-summary", "token" : "8"}
            }
        ],
        panels: [
            {
                height: 500,
                position: "north",
                panel_type: "simple",
                parts: [
                    {
                        place: "DisplayerScreen",
                        parameters: {"uuid": "opps-expected-pipeline", "token" : "2"}
                    }
                ],
                panels: [
                    {
                        height: 400,
                        position: "east",
                        panel_type: "simple",
                        parts: [
                            {
                                place: "DisplayerScreen",
                                parameters: {"uuid": "opps-by-pipeline", "token" : "3"}
                            }
                        ]
                    }
                ]
            }
        ]
    }
});
