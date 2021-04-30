package org.sagebionetworks.web.client.utils;

import java.util.List;

import com.google.gwt.core.client.JsArrayString;

public class JavaScriptArrayUtils {
    public static JsArrayString convertToJsArray(List<String> list) {
        JsArrayString jsArrayString = JsArrayString.createArray().cast();
        for (String s : list) {
            jsArrayString.push(s);
        }
        return jsArrayString;
    }
}
