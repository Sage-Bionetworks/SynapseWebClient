package org.sagebionetworks.web.client.widget.docker;

import com.google.inject.Inject;

public class DigestWidget {

	private DigestWidgetView view;

	@Inject
	public DigestWidget(
			DigestWidgetView view) {
		this.view = view;
	}

	public void configure(String digest) {
		view.setDigest(digest);
	}
}
