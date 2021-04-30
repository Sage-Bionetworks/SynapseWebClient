package org.sagebionetworks.web.client.widget.entity.browse;

public enum EntityFinderScope {
    ALL_PROJECTS("All Projects"),
    CURRENT_PROJECT("Current Project"),
    CREATED_BY_ME("Projects Created By Me"),
    FAVORITES("My Favorites");

    private final String value;

    EntityFinderScope(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
