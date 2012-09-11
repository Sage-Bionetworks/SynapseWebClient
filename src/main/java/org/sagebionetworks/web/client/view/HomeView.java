package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface HomeView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
		
	public void refresh();
	
	public void showNews(String html);
	
	public void showBccOverview(String description);
	
	public interface Presenter extends SynapsePresenter {

		boolean showLoggedInDetails();	
		void loadBccOverviewDescription();
		void showBCCSignup(AsyncCallback<String> callback);			
	}

}
