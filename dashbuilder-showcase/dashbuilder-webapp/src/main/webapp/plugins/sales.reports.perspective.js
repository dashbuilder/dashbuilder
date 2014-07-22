$registerPerspective({
    id: "Sales Reports",
    is_serializable: "true",
    roles: [ "admins", "users" ],
    panel_type: "root_tab",
    view: {
        parts: [
            {
                place: "DisplayerScreen",
                parameters: {"uuid": "opps-country-summary", "token" : "4"}
            },
            {
                place: "DisplayerScreen",
                parameters: {"uuid": "opps-allopps-listing", "token" : "6"}
            }
        ]
    }
});
