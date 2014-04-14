$registerPerspective({
    id: "Sales Reports",
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
