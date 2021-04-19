package org.sagebionetworks.web.client.callback;

import org.sagebionetworks.web.client.jsni.Reference;

import com.google.gwt.core.client.JsArray;

public interface OnSelectCallback {
	void onSelect(JsArray<Reference> selected);
}
