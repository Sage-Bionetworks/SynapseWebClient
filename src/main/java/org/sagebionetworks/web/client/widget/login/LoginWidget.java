package org.sagebionetworks.web.client.widget.login;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginWidget implements LoginWidgetView.Presenter {

	private LoginWidgetView view;
	private AuthenticationController authenticationController;	
	private List<UserListener> listeners = new ArrayList<UserListener>();	
	private String openIdActionUrl;
	private String openIdReturnUrl;
	private NodeModelCreator nodeModelCreator;
	private GlobalApplicationState globalApplicationState;
	
	@Inject
	public LoginWidget(LoginWidgetView view, AuthenticationController controller, NodeModelCreator nodeModelCreator, GlobalApplicationState globalApplicationState) {
		this.view = view;
		view.setPresenter(this);
		this.authenticationController = controller;	
		this.nodeModelCreator = nodeModelCreator;
		this.globalApplicationState = globalApplicationState;
	}

	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
	public void addUserListener(UserListener listener){
		listeners.add(listener);
	}
	
	@Override
	public void setUsernameAndPassword(final String username, final String password) {		
		authenticationController.loginUser(username, password, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.clear();
				UserSessionData toBeParsed = null;
				if (result != null){
					try {
						toBeParsed = nodeModelCreator.createJSONEntity(result, UserSessionData.class);
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}
				}
				final UserSessionData userSessionData = toBeParsed;
				
				if (!userSessionData.getSession().getAcceptsTermsOfUse()) {
					authenticationController.getTermsOfUse(new AsyncCallback<String>() {
						public void onSuccess(String termsOfUseContent) {
							view.showTermsOfUse(termsOfUseContent, 
									new AcceptTermsOfUseCallback() {
										public void accepted() {
											authenticationController.signTermsOfUse(true, new AsyncCallback<Void> () {

												@Override
												public void onFailure(Throwable caught) {
													view.showError("An error occurred. Please try logging in again.");
												}

												@Override
												public void onSuccess(Void result) {
													// Have to get the UserSessionData again, 
													// since it won't contain the UserProfile if the terms haven't been signed
													authenticationController.loginUserSSO(userSessionData.getSession().getSessionToken(), new AsyncCallback<String>() {

														@Override
														public void onFailure(
																Throwable caught) {
															view.showError("An error occurred. Please try logging in again.");
														}

														@Override
														public void onSuccess(
																String result) {
															// All setup complete
															fireUserChange(userSessionData);
														}	
														
													});
												}
												
											});
										}
										public void rejected() {
											authenticationController.signTermsOfUse(false, new AsyncCallback<Void> () {

												@Override
												public void onFailure(Throwable caught) {
													view.showError("An error occurred. Please try logging in again.");
												}

												@Override
												public void onSuccess(Void result) {
													authenticationController.logoutUser();
												}
												
											});
										}
									});							
						}
						public void onFailure(Throwable t) {
							view.showTermsOfUseDownloadFailed();							
						}
					});
				} else {
					fireUserChange(userSessionData);
				}
			}

			@Override
			public void onFailure(Throwable caught) {				
				view.clear();
				if(caught instanceof ReadOnlyModeException) {
					view.showError(DisplayConstants.LOGIN_READ_ONLY_MODE);
				} else if(caught instanceof SynapseDownException) {
					view.showError(DisplayConstants.LOGIN_DOWN_MODE);
				} else {				
					view.showAuthenticationFailed();
				}
			}
		});
	}
	
	public void clear() {
		view.clear();
	}

	// needed?
	private void fireUserChange(UserSessionData user) {
		for(UserListener listener: listeners){
			listener.userChanged(user);
		}
	}
	
	public void setOpenIdActionUrl(String url) {
		this.openIdActionUrl = url;
	}

	public void setOpenIdReturnUrl(String url) {
		this.openIdReturnUrl = url;
	}
	
	@Override
	public String getOpenIdActionUrl() {
		return openIdActionUrl;
	}
	
	@Override
	public String getOpenIdReturnUrl() {
		return openIdReturnUrl;
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
}
