package org.sagebionetworks.web.client.widget.login;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface LoginWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		
		public void setUsernameAndPassword(String username, String password, boolean explicitlyAcceptsTermsOfUse);
		
		public String getOpenIdActionUrl();
		
		public String getOpenIdReturnUrl();
		
		public void acceptTermsOfUse();
	}

	public void showAuthenticationFailed();
	
	public void showTermsOfUseDownloadFailed();
	
	public void showTermsOfUse(String content, AcceptTermsOfUseCallback callback);
	
	public void showError(String message);
	
	public void clear();

	public void acceptTermsOfUse();

}
