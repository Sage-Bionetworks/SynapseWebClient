package org.sagebionetworks.web.client.widget.entity.controller;

public interface EntityRefProvEntryView extends ProvenanceEntry {

	String getEntryId();

	void configure(String synId, String versionNumber);

	String getEntryVersion();

}
