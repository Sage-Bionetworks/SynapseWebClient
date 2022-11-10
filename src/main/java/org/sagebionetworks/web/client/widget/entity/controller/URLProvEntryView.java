package org.sagebionetworks.web.client.widget.entity.controller;

public interface URLProvEntryView extends ProvenanceEntry {
  String getURL();

  void configure(String title, String url);

  String getTitle();
}
