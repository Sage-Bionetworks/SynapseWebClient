package org.sagebionetworks.web.client;

import com.google.inject.Inject;

public class LinkifyImpl implements Linkify {

	@Inject
	public LinkifyImpl() {
	}
	
	@Override
	public String linkify(String s) {
		return _linkify(s);
	}

	private final static native String _linkify(String s) /*-{
		try {
			return $wnd.linkifyStr(s);
		} catch (err) {
			console.error(err);
			return s;
		}
	}-*/;

}
