package org.sagebionetworks.web.client.widget.csv;

/**
 * Wrapper for PapaParse so that the static call can be mocked
 */
public class PapaCSVParser {

  public PapaParseResult parse(String string) {
    PapaParseConfig config = new PapaParseConfig();
    return PapaParse.parse(string, config);
  }
}
