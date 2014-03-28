$registerPerspective({
    id: "Dashboard",
    roles: [ "admins", "users" ],
    panel_type: "root_static",
    view: {
        parts: [
            {
                place: "KPIPresenter",
                parameters: {"kpi": "sample0", "token" : "3"}
            }
        ],
        panels: [
            {
                width: 370,
                height: 340,
                position: "south",
                panel_type: "static",
                parts: [
                    {
                        place: "KPIPresenter",
                        parameters: {"kpi": "sample1", "token" : "2"}
                    }
                ],
                panels: [
                    {
                        width: 570,
                        height: 340,
                        position: "east",
                        panel_type: "static",
                        parts: [
                            {
                                place: "KPIPresenter",
                                parameters: {"kpi": "sample1", "token" : "4"}
                            }
                        ],
                        panels: [
                        {
                            width: 520,
                            height: 340,
                            position: "east",
                            panel_type: "static",
                            parts: [
                                {
                                    place: "KPIPresenter",
                                    parameters: {"kpi": "sample0", "token" : "5"}
                                }
                            ]
                        }
                    ]
                    }
                ]
            },
            {
                width: 700,
                min_width: 330,
                position: "east",
                panel_type: "static",
                parts: [
                    {
                        place: "KPIPresenter",
                        parameters: {"kpi": "sample1", "token" : "0"}
                    }
                ],

                panels: [
                    {
                        width: 380,
                        height: 330,
                        position: "east",
                        panel_type: "static",
                        parts: [
                            {
                                place: "KPIPresenter",
                                parameters: {"kpi": "sample0", "token" : "1"}
                            }
                        ]
                    }
                ]
            }
        ]
    }
});
