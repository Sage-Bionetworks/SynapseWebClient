package org.sagebionetworks.web.client.utils;

import com.google.gwt.core.client.JsArrayString;
import java.util.List;

public class JavaScriptArrayUtils {

  public static JsArrayString convertToJsArray(List<String> list) {
    JsArrayString jsArrayString = JsArrayString.createArray().cast();
    for (String s : list) {
      jsArrayString.push(s);
    }
    return jsArrayString;
  }
}
