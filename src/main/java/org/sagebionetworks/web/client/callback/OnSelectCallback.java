package org.sagebionetworks.web.client.callback;

import org.sagebionetworks.web.client.jsni.ReferenceJSNIObject;

import com.google.gwt.core.client.JsArray;

public interface OnSelectCallback {
	void onSelect(JsArray<ReferenceJSNIObject> selected);
}
