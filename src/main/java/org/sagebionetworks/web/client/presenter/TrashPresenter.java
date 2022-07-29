package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.view.TrashView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class TrashPresenter extends AbstractActivity implements Presenter<Trash> {

	private Trash place;
	private TrashView view;
	private SynapseContextPropsProvider propsProvider;
	@Inject
	public TrashPresenter(TrashView view, SynapseContextPropsProvider propsProvider) {
		this.view = view;
		this.propsProvider = propsProvider;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
		view.createReactComponentWidget(propsProvider);
	}

	@Override
	public void setPlace(Trash place) {
		this.place = place;
	}

}
