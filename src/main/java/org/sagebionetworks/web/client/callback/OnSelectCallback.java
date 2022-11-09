package org.sagebionetworks.web.client.callback;

import com.google.gwt.core.client.JsArray;
import org.sagebionetworks.web.client.jsni.ReferenceJSNIObject;

public interface OnSelectCallback {
  void onSelect(JsArray<ReferenceJSNIObject> selected);
}
