$registerPerspective({
    id: "Sales Reports",
    is_serializable: "true",
    roles: [ "admins", "users" ],
    panel_type: "root_tab",
    view: {
        parts: [
            {
                place: "KPIScreen",
                parameters: {"kpi": "opps-country-summary", "token" : "4"}
            },
            {
                place: "KPIScreen",
                parameters: {"kpi": "opps-allopps-listing", "token" : "6"}
            }
        ]
    }
});
