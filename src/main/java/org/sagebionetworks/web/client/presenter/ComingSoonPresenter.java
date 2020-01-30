package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.ComingSoon;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.view.ComingSoonView;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ComingSoonPresenter extends AbstractActivity implements ComingSoonView.Presenter, Presenter<ComingSoon> {

	private ComingSoon place;
	private ComingSoonView view;
	SynapseJSNIUtils jsniUtils;
	SynapseJavascriptClient jsClient;
	ResourceLoader resourceLoader;

	@Inject
	public ComingSoonPresenter(ComingSoonView view, SynapseJSNIUtils jsniUtils, SynapseJavascriptClient jsClient, ResourceLoader resourceLoader) {
		this.view = view;
		this.jsClient = jsClient;
		this.jsniUtils = jsniUtils;
		this.resourceLoader = resourceLoader;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(ComingSoon place) {
		this.place = place;
		this.view.setPresenter(this);
		final String token = place.toToken();
		view.showSRCComponent();
	}

	@Override
	public String mayStop() {
		view.clear();
		return null;

	}
}
