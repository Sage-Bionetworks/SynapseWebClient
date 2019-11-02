package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.view.SynapseWikiView;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SynapseWikiPresenter extends AbstractActivity implements SynapseWikiView.Presenter, Presenter<Wiki> {

	private Wiki place;
	private SynapseWikiView view;
	private SynapseClientAsync synapseClient;

	@Inject
	public SynapseWikiPresenter(SynapseWikiView view, SynapseClientAsync synapseClient) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(Wiki place) {
		this.place = place;
		this.view.setPresenter(this);

		configure(new WikiPageKey(place.getOwnerId(), place.getOwnerType(), place.getWikiId()));
	}

	@Override
	public void configure(final WikiPageKey wikiKey) {
		synapseClient.hasAccess(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), ACCESS_TYPE.UPDATE.toString(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				view.showPage(wikiKey, result);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});

	}

	@Override
	public String mayStop() {
		view.clear();
		return null;
	}
}
