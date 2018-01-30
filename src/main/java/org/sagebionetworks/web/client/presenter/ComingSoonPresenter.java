package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.ComingSoon;
import org.sagebionetworks.web.client.services.LayoutServiceAsync;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.view.ComingSoonView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ComingSoonPresenter extends AbstractActivity implements ComingSoonView.Presenter, Presenter<ComingSoon> {
		
	private ComingSoon place;
	private ComingSoonView view;
	SynapseJSNIUtils jsniUtils;
	SynapseJavascriptClient jsClient;
	
	@Inject
	public ComingSoonPresenter(ComingSoonView view,
			SynapseJSNIUtils jsniUtils,
			SynapseJavascriptClient jsClient) {
		this.view = view;
		this.jsClient = jsClient;
		this.jsniUtils = jsniUtils;
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
		
		// test react-based view
		// get the data
		jsClient.getUserGroupHeadersByPrefix("Bob", TypeFilter.USERS_ONLY, 20, 0, new AsyncCallback<UserGroupHeaderResponsePage>() {
			@Override
			public void onSuccess(UserGroupHeaderResponsePage result) {
				view.setUserList(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				
			}
		});
	}

	@Override
    public String mayStop() {
        view.clear();
        return null;
        
    }
}
