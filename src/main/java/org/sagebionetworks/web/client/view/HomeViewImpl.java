package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.StandaloneWiki;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HomeViewImpl extends Composite implements HomeView {

	public interface HomeViewImplUiBinder extends UiBinder<Widget, HomeViewImpl> {
	}

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
	@UiField
	Div registerWidgetContainer;

	private Header headerWidget;
	UserBadge userBadge;
	HorizontalPanel myDashboardButtonContents;
	LoginWidget loginWidget;

	@Inject
	public HomeViewImpl(HomeViewImplUiBinder binder, Header headerWidget, final GlobalApplicationState globalApplicationState, final AuthenticationController authController, UserBadge userBadge, RegisterWidget registerWidget, LoginWidget loginWidget) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.userBadge = userBadge;
		this.loginWidget = loginWidget;
		userBadge.setTooltipHidden(true);
		userBadge.setTextHidden(true);
		myDashboardButtonContents = new HorizontalPanel();
		myDashboardButtonContents.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		myDashboardButtonContents.add(userBadge.asWidget());
		myDashboardButtonContents.add(new Span("My Dashboard"));
		myDashboardButtonContents.addStyleName("margin-auto");

		addUserPicturePanel();

		headerWidget.configure();

		dashboardBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Profile(authController.getCurrentUserPrincipalId(), ProfileArea.PROJECTS));
			}
		});
		registerWidgetContainer.add(registerWidget.asWidget());
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
				// go to new open research project page
				globalApplicationState.getPlaceChanger().goTo(new StandaloneWiki("OpenResearchProjects"));
			}
		});

		researchCommunitiesBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// go to new research communities page
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
				DisplayUtils.newWindow(WebConstants.DOCS_URL + "governance.html", "", "");
			}
		});

		gettingStartedBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(WebConstants.DOCS_URL + "getting_started.html", "", "");
			}
		});

		becomeCertifiedBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(WebConstants.DOCS_URL + "accounts_certified_users_and_profile_validation.html", "", "");
			}
		});
	}

	/**
	 * Clear the divider/caret from the user button, and add the picture container
	 * 
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
	public void showLoggedInUI(UserProfile profile) {
		setUserProfilePicture(profile);
		dashboardUI.setVisible(true);
		userDisplayName.setText(profile.getUserName().toUpperCase());
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

	private void setUserProfilePicture(UserProfile profile) {
		if (profile != null) {
			userBadge.configure(profile);
			userBadge.setDoNothingOnClick();
		}
	}

	@Override
	public void refresh() {
		headerWidget.configure();
		headerWidget.refresh();
		clear();
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void scrollToTop() {
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void clear() {
		userBadge.clearState();
		dashboardUI.setVisible(false);
		registerUI.setVisible(false);
		loginUI.setVisible(false);
		userDisplayName.setText("");
	}
}
