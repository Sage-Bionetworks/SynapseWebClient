package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;

public class DoiWidgetViewImpl extends Composite implements DoiWidgetView {
	
	private Presenter presenter;

	private Anchor doiAnchor;
	private HandlerRegistration handlerRegistration;
	@Inject
	public DoiWidgetViewImpl() {
		doiAnchor = new Anchor();
		doiAnchor.addStyleName("link");
		initWidget(doiAnchor);
	}

	@Override
	public void showCreateDoi() {
		//clear href, if there is one
		doiAnchor.setHref(null);
		handlerRegistration = doiAnchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//if there is an href, ignore it
				event.preventDefault();
				presenter.createDoi();
			}
		});
	}
	
	@Override
	public void showDoi(Doi doi) {
		//clear old handler, if there is one
		if (handlerRegistration != null)
			handlerRegistration.removeHandler();
		doiAnchor.setHref(url);	
	}
	
	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void clear() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	

}
