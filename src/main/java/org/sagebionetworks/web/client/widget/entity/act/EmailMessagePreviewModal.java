package org.sagebionetworks.web.client.widget.entity.act;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EmailMessagePreviewModal implements EmailMessagePreviewModalView.Presenter, IsWidget {
	
	private EmailMessagePreviewModalView view;

	@Inject
	public EmailMessagePreviewModal(EmailMessagePreviewModalView view) {
		this.view = view;
		this.view.setPresenter(this);
	}

	public void configure(String message) {
		this.view.setMessageBody(message);
	}
	
	public void show() {
		view.show();
	}

	@Override
	public Widget asWidget() {
		view.setPresenter(this);			
		return view.asWidget();
	}

}
