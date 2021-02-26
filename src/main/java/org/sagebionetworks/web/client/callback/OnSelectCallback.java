package org.sagebionetworks.web.client.callback;

import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderV2ViewImpl;

import com.google.gwt.core.client.JsArray;

public interface OnSelectCallback {
	void onSelect(JsArray<EntityFinderV2ViewImpl.ReferenceJso> md5HexValue);
}
