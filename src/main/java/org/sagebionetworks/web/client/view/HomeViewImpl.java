package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Help;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.StandaloneWiki;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.client.widget.login.UserListener;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HomeViewImpl extends Composite implements HomeView {
	public static final String FAVORITE_STAR_HTML = "<span style=\"font-size:19px;color:#f0ad4e\" class=\"fa fa-star\"></span>";
	
	public interface HomeViewImplUiBinder extends UiBinder<Widget, HomeViewImpl> {}
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel newsFeed;
	@UiField
	org.gwtbootstrap3.client.ui.Button dashboardBtn;
	@UiField
	Div dashboardUI;
	@UiField
	Div registerUI;
	@UiField
	Div loginUI;
	
	@UiField
	FocusPanel dreamChallengesBox;
	@UiField
	FocusPanel openResearchProjectsBox;
	@UiField
	FocusPanel researchCommunitiesBox;
	
	@UiField
	FocusPanel termsOfUseBox;
	@UiField
	FocusPanel becomeCertifiedBox;
	@UiField
	FocusPanel creditForResearchBox;
	@UiField
	FocusPanel organizeResearchAssetsBox;
	@UiField
	FocusPanel collaborateBox;
	
	
	@UiField
	FocusPanel gettingStartedBox;
	
	@UiField
	Heading organizeDigitalResearchAssetsHeading;
	@UiField
	Heading getCreditHeading;
	@UiField
	Heading collaborateHeading;
	@UiField
	Heading userDisplayName;
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	IconsImageBundle iconsImageBundle;
	SynapseJSNIUtils synapseJSNIUtils;
	UserBadge userBadge;
	HorizontalPanel myDashboardButtonContents;
	LoginWidget loginWidget;
	
	@Inject
	public HomeViewImpl(HomeViewImplUiBinder binder, 
			Header headerWidget,
			Footer footerWidget, 
			IconsImageBundle icons, 
			SageImageBundle imageBundle,
			final GlobalApplicationState globalApplicationState,
			CookieProvider cookies,
			final AuthenticationController authController,
			SynapseJSNIUtils synapseJSNIUtils,
			UserBadge userBadge,
			RegisterWidget registerWidget,
			LoginWidget loginWidget) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.iconsImageBundle = icons;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.userBadge = userBadge;
		this.loginWidget = loginWidget;
		userBadge.setSize(BadgeSize.DEFAULT_PICTURE_ONLY);
		myDashboardButtonContents = new HorizontalPanel();
		myDashboardButtonContents.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		myDashboardButtonContents.add(userBadge.asWidget());
		myDashboardButtonContents.add(new Span("My Dashboard"));
		myDashboardButtonContents.addStyleName("margin-auto");
		
		addUserPicturePanel();
		
		headerWidget.configure(true);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		
		dashboardBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Profile(authController.getCurrentUserPrincipalId()));
			}
		});
		
		registerUI.add(registerWidget.asWidget());
		
		loginWidget.setUserListener(new UserListener() {
			@Override
			public void userChanged(UserSessionData newUser) {
				presenter.onUserChange();
			}
		});
		// Other links
		dreamChallengesBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.open("http://dreamchallenges.org/", "", "");
			}
		});
		openResearchProjectsBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//go to new open research project page
				globalApplicationState.getPlaceChanger().goTo(new StandaloneWiki("OpenResearchProjects"));
			}
		});
		
		researchCommunitiesBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//go to new research communities page
				globalApplicationState.getPlaceChanger().goTo(new StandaloneWiki("ResearchCommunities"));
			}
		});
		
		creditForResearchBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.scrollTo(0, getCreditHeading.getAbsoluteTop());
			}
		});
		
		organizeResearchAssetsBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.scrollTo(0, organizeDigitalResearchAssetsHeading.getAbsoluteTop());
			}
		});
		collaborateBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.scrollTo(0, collaborateHeading.getAbsoluteTop());
			}
		});
		termsOfUseBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Help("Governance"));
			}
		});
		
		gettingStartedBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Help("GettingStarted"));
			}
		});
		
		becomeCertifiedBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new StandaloneWiki("Certification"));
			}
		});
		
	}
	@Override
	public void prepareTwitterContainer(final String elementId) {
		newsFeed.clear();
		final Div newDiv = new Div();
		newDiv.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					newDiv.getElement().setId(elementId);
					presenter.twitterContainerReady(elementId);
				}
			}
		});
		newsFeed.add(newDiv);
	}
	/**
	 * Clear the divider/caret from the user button, and add the picture container
	 * @param button
	 */
	public void addUserPicturePanel() {
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
            public void execute() {
				dashboardBtn.add(myDashboardButtonContents);
			}
		});
	}

	
	@Override
	public void showLoggedInUI(UserSessionData userData) {
		setUserProfilePicture(userData);
		dashboardUI.setVisible(true);
		userDisplayName.setText(userData.getProfile().getUserName().toUpperCase());
	}

	@Override
	public void showLoginUI() {
		loginUI.clear();
		loginWidget.asWidget().removeFromParent();
		loginUI.add(loginWidget.asWidget());
		loginUI.setVisible(true);
	}
	
	@Override
	public void showRegisterUI() {
		registerUI.setVisible(true);
	}
	
	private void clearUserProfilePicture() {
		userBadge.clearState();
		userBadge.configurePicture();
	}
	
	private void setUserProfilePicture(UserSessionData userData) {
		if (userData != null && userData.getProfile() != null) {
			UserProfile profile = userData.getProfile();
			userBadge.configure(profile);
		}
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		Window.scrollTo(0, 0); // scroll user to top of page		
	}
	
	@Override
	public void refresh() {
		header.clear();
		headerWidget.configure(true);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();
		clear();
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}


	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		clearUserProfilePicture();
		dashboardUI.setVisible(false);
		registerUI.setVisible(false);
		loginUI.setVisible(false);
		userDisplayName.setText("");
	}

	private void configureNewWindowLink(Anchor a, String href, String text) {
		a.addStyleName("link");
		a.setTarget("_blank");
		a.setHref(href);
		a.setText(text);
	}
}
