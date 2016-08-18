package org.sagebionetworks.web.client.widget.docker;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DigestWidget implements DigestWidgetView.Presenter {

	private DigestWidgetView view;

	@Inject
	public DigestWidget(
			DigestWidgetView view) {
		this.view = view;
	}

	public void configure(String digest) {
		view.setDigest(digest);
	}

	public Widget asWidget() {
		return view.asWidget();
	}
}
