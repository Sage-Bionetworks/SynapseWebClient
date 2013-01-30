package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.SynapseWiki;
import org.sagebionetworks.web.client.view.SynapseWikiView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SynapseWikiPresenter extends AbstractActivity implements SynapseWikiView.Presenter {
		
	private SynapseWiki place;
	private SynapseWikiView view;
	
	@Inject
	public SynapseWikiPresenter(SynapseWikiView view){
		this.view = view;
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
		
		configure(place.getOwnerId(), place.getOwnerType(), true, place.getWikiId());
	}
	
	
	@Override
	public void configure(String ownerId, String ownerType, boolean canEdit, String wikiId) {
		view.showPage(ownerId, ownerType, canEdit, wikiId);
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
}
