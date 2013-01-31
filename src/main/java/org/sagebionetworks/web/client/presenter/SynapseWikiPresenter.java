package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.SynapseWiki;
import org.sagebionetworks.web.client.view.SynapseWikiView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SynapseWikiPresenter extends AbstractActivity implements SynapseWikiView.Presenter {
		
	private SynapseWiki place;
	private SynapseWikiView view;
	private SynapseClientAsync synapseClient;
	
	@Inject
	public SynapseWikiPresenter(SynapseWikiView view, SynapseClientAsync synapseClient){
		this.view = view;
		this.synapseClient = synapseClient;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	public void setPlace(SynapseWiki place) {
		this.place = place;
		this.view.setPresenter(this);
		
		configure(place.getOwnerId(), place.getOwnerType(), place.getWikiId());
	}
	
	
	@Override
	public void configure(final String ownerId, final String ownerType, final String wikiId) {
		synapseClient.hasAccess(ownerId, ownerType, ACCESS_TYPE.UPDATE.toString(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				view.showPage(ownerId, ownerType, result, wikiId);		
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
