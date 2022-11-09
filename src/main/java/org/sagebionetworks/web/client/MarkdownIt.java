package org.sagebionetworks.web.client;

import com.google.gwt.core.client.JavaScriptException;

public interface MarkdownIt {
  public String markdown2Html(String md, String uniqueSuffix)
    throws JavaScriptException;
}
