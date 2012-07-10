package org.sagebionetworks.web.client.presenter;

import java.util.Date;

import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.LinkedInServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.shared.LinkedInInfo;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.users.UserData;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;
import com.google.inject.Inject;

public class ProfilePresenter extends AbstractActivity implements ProfileView.Presenter {
		
	private Profile place;
	private ProfileView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private UserAccountServiceAsync userService;
	private LinkedInServiceAsync linkedInService;
	private GlobalApplicationState globalApplicationState;
	private CookieProvider cookieProvider;
	private UserProfile ownerProfile;
	
	@Inject
	public ProfilePresenter(ProfileView view,
			AuthenticationController authenticationController,
			UserAccountServiceAsync userService,
			LinkedInServiceAsync linkedInService,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			CookieProvider cookieProvider) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.userService = userService;
		this.linkedInService = linkedInService;
		this.globalApplicationState = globalApplicationState;
		this.cookieProvider = cookieProvider;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Set the presenter on the view
		this.view.render();
		
		// Install the view
		panel.setWidget(view);
		
	}

	public void setPlace(Profile place) {
		this.place = place;
		this.view.setPresenter(this);
		this.view.clear();
		showView(place);
	}

	@Override
	public void updateProfile(final String firstName, final String lastName, final String summary, final String position, final String location, final String industry, final String company, final AttachmentData pic) {
		final UserData currentUser = authenticationController.getLoggedInUser();
		if(currentUser != null) {
				//get the owner profile (may or may not be currently set
				synapseClient.getUserProfile(null, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String userProfileJson) {
						try {
							final UserProfile profile = nodeModelCreator.createEntity(userProfileJson, UserProfile.class);
							ownerProfile = profile;
							
							ownerProfile.setFirstName(firstName);
							ownerProfile.setLastName(lastName);
							ownerProfile.setSummary(summary);
							ownerProfile.setPosition(position);
							ownerProfile.setLocation(location);
							ownerProfile.setIndustry(industry);
							ownerProfile.setCompany(company);
							if (pic != null)
								ownerProfile.setPic(pic);
							
							JSONObjectAdapter adapter;
							try {
								adapter = ownerProfile.writeToJSONObject(JSONObjectGwt.createNewAdapter());
								userProfileJson = adapter.toJSONString();
							
								synapseClient.updateUserProfile(userProfileJson, new AsyncCallback<Void>() {
									@Override
									public void onSuccess(Void result) {
										view.showUserUpdateSuccess();
										view.showInfo("Success", "Your profile has been updated.");
										
										AsyncCallback<UserData> callback = new AsyncCallback<UserData>() {
											@Override
											public void onFailure(Throwable caught) { }

											@Override
											public void onSuccess(UserData result) {
												view.refreshHeader();
											}
										};
										
										if(currentUser.isSSO()) {
											authenticationController.loginUserSSO(currentUser.getToken(), callback);
										} else {
											authenticationController.loginUser(currentUser.getToken(), callback);
										}
									}
									
									@Override
									public void onFailure(Throwable caught) {
										view.userUpdateFailed();
										view.showErrorMessage("An error occured. Please try reloading the page.");
									}
								});
							} catch (JSONObjectAdapterException e) {
								DisplayUtils.handleServiceException(e, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser());
							}

						} catch (RestServiceException e) {
							onFailure(e);
						}    				
					}
					@Override
					public void onFailure(Throwable caught) {
						DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser());    					    				
					}
				});
				
		}
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	
	@Override
	public void redirectToLinkedIn() {
		linkedInService.returnAuthUrl(new AsyncCallback<LinkedInInfo>() {
			@Override
			public void onSuccess(LinkedInInfo result) {
				// Store the requestToken secret in a cookie, set to expire in five minutes
				Date date = new Date(System.currentTimeMillis() + 300000);
				cookieProvider.setCookie(CookieKeys.LINKEDIN, result.getRequestSecret(), date);
				// Open the LinkedIn authentication window in the same tab
				Window.open(result.getAuthUrl(), "_self", "");
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage("An error occured. Please try reloading the page.");					
			}
		});
	}
	
	@Override
	public void redirectToEditProfile() {
		globalApplicationState.getPlaceChanger().goTo(new Profile(Profile.EDIT_PROFILE_PLACE_TOKEN));
	}
	@Override
	public void redirectToViewProfile() {
		globalApplicationState.getPlaceChanger().goTo(new Profile(Profile.VIEW_PROFILE_PLACE_TOKEN));
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}

	/**
	 * This method will update the current user's profile using LinkedIn
	 */
	@Override
	public void updateProfileWithLinkedIn(String requestToken, String verifier) {
		// Grab the requestToken secret from the cookie. If it's expired, show an error message.
		// If not, grab the user's info for an update.
		String secret = cookieProvider.getCookie(CookieKeys.LINKEDIN);
		if(secret == null || secret.equals("")) {
			view.showErrorMessage("You request has timed out. Please reload the page and try again.");
		} else {
			linkedInService.getCurrentUserInfo(requestToken, secret, verifier, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					Document linkedInProfile = XMLParser.parse(result);
				    final String firstName = linkedInProfile.getElementsByTagName("first-name").item(0).getFirstChild().getNodeValue();
				    final String lastName = linkedInProfile.getElementsByTagName("last-name").item(0).getFirstChild().getNodeValue();
				    final String summary = linkedInProfile.getElementsByTagName("summary").item(0).getFirstChild().getNodeValue();
				    final String industry = linkedInProfile.getElementsByTagName("industry").item(0).getFirstChild().getNodeValue();
				    //location is in child element <location><name>locationname</name></location>
				    String location = "";
				    //parse out position
				    StringBuilder position = new StringBuilder();
				    //and company
				    StringBuilder company = new StringBuilder();
				    
				    try {
				    	location = ((Element)linkedInProfile.getElementsByTagName("location").item(0)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
					    Element threeCurrentPositionsElement = (Element)linkedInProfile.getElementsByTagName("three-current-positions").item(0);
					    Element positionElement = (Element) threeCurrentPositionsElement.getElementsByTagName("position").item(0);
					    position.append(positionElement.getElementsByTagName("title").item(0).getFirstChild().getNodeValue());
					    Element companyElement = (Element) positionElement.getElementsByTagName("company").item(0);
					    company.append(companyElement.getElementsByTagName("name").item(0).getFirstChild().getNodeValue());
				    }
				    catch (Throwable t) {
				    	//error trying to import position, company, or location. go ahead and update the profile with partial results
				    	t.printStackTrace();
				    }
					 
					    //TODO: get the profile picture data from picture-url
				    String picUrl = linkedInProfile.getElementsByTagName("picture-url").item(0).getFirstChild().getNodeValue();
				    //update the profile, if the image is successfully saved.
				    AttachmentData pic = null;
				    
				    updateProfile(firstName, lastName, summary, position.toString(), location, industry, company.toString(), pic);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage("An error occured. Please try reloading the page.");									
				}
			});
		}
	}
	
	private void updateProfileView(boolean editable) {
		updateProfileView(null, editable);
	}
	
	private void updateProfileView(String userId, final boolean editable) {
		
		final boolean isOwner = userId == null;
		synapseClient.getUserProfile(userId, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String userProfileJson) {
					try {
						final UserProfile profile = nodeModelCreator.createEntity(userProfileJson, UserProfile.class);
						if (isOwner)
							ownerProfile = profile;
						view.updateView(profile, editable, isOwner);
					} catch (RestServiceException e) {
						onFailure(e);
					}    				
				}
				@Override
				public void onFailure(Throwable caught) {
					DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser());    					    				
				}
			});
	}
	
	private void showView(Profile place) {
		String token = place.toToken();
		if (Profile.VIEW_PROFILE_PLACE_TOKEN.equals(token)) {
			//View (my) profile
			updateProfileView(false);
		}
		else if (Profile.EDIT_PROFILE_PLACE_TOKEN.equals(token)) {
			//Edit my profile (current user must equal the profile being displayed)
			updateProfileView(true);
		}
		else if(!"".equals(token) && token != null) {
			//if this contains an oauth_token, it's from linkedin
			if (token.contains("oauth_token"))
			{
				// User just logged in to LinkedIn. Get the request token and their info to update
				// their profile with.
				
				String requestToken = "";
				String verifier = "";
				String[] oAuthTokens = token.split("&");
				for(String s : oAuthTokens) {
					String[] tokenParts = s.split("=");
					if(tokenParts[0].equals("oauth_token")) {
						requestToken = tokenParts[1];
					} else if(tokenParts[0].equals("oauth_verifier")) {
						verifier = tokenParts[1];
					}
				}
				
				if(!requestToken.equals("") && !verifier.equals("")) {
					updateProfileWithLinkedIn(requestToken, verifier);
				} else {
					view.showErrorMessage("An error occured. Please try reloading the page.");
				}
			}
			else {
				//otherwise, this is a user id
				updateProfileView(token, false);
			}
		}
	}
}

