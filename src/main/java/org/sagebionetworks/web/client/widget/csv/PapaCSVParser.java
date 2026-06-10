package org.sagebionetworks.web.client.widget.csv;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Wrapper for PapaParse so that the static call can be mocked
 */
@Singleton
public class PapaCSVParser {

  @Inject
  public PapaCSVParser() {}

  public PapaParseResult parse(String string) {
    PapaParseConfig config = new PapaParseConfig();
    return PapaParse.parse(string, config);
  }
}
