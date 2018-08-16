package org.sagebionetworks.web.client.widget.header;

import java.util.List;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDown;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.widget.search.SearchBox;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HeaderViewImpl extends Composite implements HeaderView {
	public interface Binder extends UiBinder<Widget, HeaderViewImpl> {
	}

	@UiField
	Image synapseLogo;
	@UiField
	Row headerDiv;
	@UiField
	Anchor projectHeadingAnchor;
	@UiField
	Div headingPanel;
	@UiField
	DropDown headerFavButtonGroup;
	@UiField
	Anchor headerFavButton;
	@UiField
	DropDownMenu headerFavList;

	@UiField
	SimplePanel projectFavoritePanel;
	@UiField
	SimplePanel stuAnnouncementsContainer;
	@UiField
	Anchor dashboardDropdownAnchor;
	@UiField
	SimplePanel loginLinkUI;
	@UiField
	Button loginLink;
	
	@UiField
	Span headerButtons;
	
	@UiField
	AnchorListItem trashLink;
	@UiField
	AnchorListItem logoutLink;
	@UiField
	AnchorListItem myDashboardLink;
	@UiField
	AnchorListItem myTeamsLink;
	@UiField
	AnchorListItem myChallengesLink;
	@UiField
	AnchorListItem mySettingsLink;

	@UiField
	DropDown dashboardDropdown;
	@UiField
	DropDownMenu dashboardDropdownMenu;

	@UiField
	SimplePanel searchBoxContainer;
	@UiField
	Alert stagingAlert;
	@UiField
	AnchorListItem documentationLink;
	
	private Presenter presenter;
	private SearchBox searchBox;
	private CookieProvider cookies;
	SageImageBundle sageImageBundle;
	private GlobalApplicationState globalAppState;
	UserBadge userBadge;
	String userId;
	AnchorListItem defaultItem = new AnchorListItem("Empty");
	@Inject
	public HeaderViewImpl(Binder binder,
			SageImageBundle sageImageBundle,
			SearchBox searchBox,
			CookieProvider cookies,
			UserBadge userBadge,
			GlobalApplicationState globalAppState) {
		this.initWidget(binder.createAndBindUi(this));
		this.searchBox = searchBox;
		this.cookies = cookies;
		this.sageImageBundle = sageImageBundle;
		this.userBadge = userBadge;
		userBadge.setStyleNames("moveup-5");
		this.globalAppState = globalAppState;
		userBadge.setSize(BadgeSize.LARGE);
		userBadge.addUsernameLinkStyle("color-white textDecorationNone");
		// add search panel first
		searchBox.setVisible(true);
		searchBoxContainer.setWidget(searchBox.asWidget());
		dashboardDropdownAnchor.add(userBadge.asWidget());
		initClickHandlers();
		clear();
	}
	
	@Override
	public void clear() {
		setProjectHeaderText("");
	}
	
	@Override
	public void setProjectHeaderText(String text) {
		boolean isDefault = Header.SYNAPSE.equals(text);
		if (isDefault) {
			projectHeadingAnchor.addStyleName("letter-spacing-6");
		} else {
			projectHeadingAnchor.removeStyleName("letter-spacing-6");
		}
		projectHeadingAnchor.setText(text);
	}
	
	@Override
	public void setProjectHeaderAnchorTarget(String href) {
		projectHeadingAnchor.setHref(href);
	}
	
	@Override
	public void setProjectFavoriteWidget(IsWidget favWidget) {
		projectFavoritePanel.setWidget(favWidget);
	}
	
	@Override
	public void showProjectFavoriteWidget() {
		projectFavoritePanel.setVisible(true);
	}
	
	@Override
	public void hideProjectFavoriteWidget() {
		projectFavoritePanel.setVisible(false);
	}
	
	public void initClickHandlers() {
		documentationLink.addClickHandler(event -> {
			event.preventDefault();
			DisplayUtils.newWindow(WebConstants.DOCS_BASE_URL, "", "");
		});
		trashLink.addClickHandler(event -> {
    		presenter.onTrashClick();
		});
		logoutLink.addClickHandler(event -> {
			presenter.onLogoutClick();
		});
		loginLink.addClickHandler(event -> {
			presenter.onLoginClick();
		});
		headerFavButtonGroup.addDomHandler(event-> {
			headerFavList.addStyleName("hover");
			dashboardDropdownMenu.removeStyleName("hover");
			presenter.refreshFavorites();
		}, MouseOverEvent.getType());
		headerFavList.addDomHandler(event-> {
			headerFavList.removeStyleName("hover");
		}, MouseOutEvent.getType());
		searchBoxContainer.addDomHandler(event-> {
			headerFavList.removeStyleName("hover");
			dashboardDropdownMenu.removeStyleName("hover");
		}, MouseOverEvent.getType());
		dashboardDropdown.addDomHandler(event-> {
			dashboardDropdownMenu.addStyleName("hover");
			headerFavList.removeStyleName("hover");
		}, MouseOverEvent.getType());
		dashboardDropdownMenu.addDomHandler(event-> {
			dashboardDropdownMenu.removeStyleName("hover");
		}, MouseOutEvent.getType());
		synapseLogo.addClickHandler(event -> {
			presenter.onLogoClick();
		});
		myDashboardLink.addClickHandler(event -> {
			dashboardDropdownMenu.removeStyleName("hover");
			Profile place = new Profile(userId, ProfileArea.PROJECTS);
			globalAppState.getPlaceChanger().goTo(place);
		});
		myTeamsLink.addClickHandler(event -> {
			dashboardDropdownMenu.removeStyleName("hover");
			Profile place = new Profile(userId, ProfileArea.TEAMS);
			globalAppState.getPlaceChanger().goTo(place);
		});
		myChallengesLink.addClickHandler(event -> {
			dashboardDropdownMenu.removeStyleName("hover");
			Profile place = new Profile(userId, ProfileArea.CHALLENGES);
			globalAppState.getPlaceChanger().goTo(place);
		});
		mySettingsLink.addClickHandler(event -> {
			dashboardDropdownMenu.removeStyleName("hover");
			Profile place = new Profile(userId, ProfileArea.SETTINGS);
			globalAppState.getPlaceChanger().goTo(place);
		});

	}
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		refresh();
	}

	@Override
	public void refresh() {
		boolean isInTestWebsite = DisplayUtils.isInTestWebsite(cookies);
		trashLink.setVisible(isInTestWebsite);
	}

	@Override
	public void setSearchVisible(boolean searchVisible) {
		searchBox.setVisible(searchVisible);
	}

	@Override
	public void openNewWindow(String url) {
		DisplayUtils.newWindow(url, "", "");
	}
	
	@Override
	public void setUser(UserSessionData userData) {
		boolean isInTestWebsite = DisplayUtils.isInTestWebsite(cookies);
	 	trashLink.setVisible(isInTestWebsite);
	 	userBadge.clearState();
	 	if (userData != null && userData.getProfile() != null) {
			//has user data, update the user name and add user commands (and set to the current user name)
	 		userBadge.configure(userData.getProfile());
	 		userBadge.setDoNothingOnClick();
	 		userId = userData.getProfile().getOwnerId();
	 		loginLinkUI.setVisible(false);
			logoutLink.setVisible(true);
			dashboardDropdown.setVisible(true);
			headerFavButtonGroup.setVisible(true);
		} else {
			userId = null;
			loginLinkUI.setVisible(true);
			logoutLink.setVisible(false);
			dashboardDropdown.setVisible(false);
			headerFavButtonGroup.setVisible(false);
		}
	}

	@Override
	public void clearFavorite() {
		headerFavList.clear();
	}

	@Override
	public void setEmptyFavorite() {
		headerFavList.add(defaultItem);
	}

	@Override
	public void addFavorite(List<EntityHeader> headers) {
		for (final EntityHeader header : headers) {
			AnchorListItem favItem = new AnchorListItem(header.getName());
			favItem.addClickHandler(event -> {
				headerFavList.removeStyleName("hover");
				Synapse place = new Synapse(header.getId());
				globalAppState.getPlaceChanger().goTo(place);
			});
			headerFavList.add(favItem);
		}
	}

	@Override
	public void setStagingAlertVisible(boolean visible) {
		stagingAlert.setVisible(visible);	
	}
	
	@Override
	public void setStuAnnouncementWidget(Widget w) {
		stuAnnouncementsContainer.clear();
		stuAnnouncementsContainer.add(w);
	}
}
