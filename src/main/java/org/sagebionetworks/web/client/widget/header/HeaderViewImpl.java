package org.sagebionetworks.web.client.widget.header;

import java.util.List;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDown;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.place.SynapseForumPlace;
import org.sagebionetworks.web.client.widget.search.SearchBox;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventBinder;

public class HeaderViewImpl extends Composite implements HeaderView {
	public interface Binder extends UiBinder<Widget, HeaderViewImpl> {
	}

	@UiField
	Image synapseLogo;
	@UiField
	Div headerDiv;
	@UiField
	Anchor projectHeadingAnchor;
	@UiField
	DropDown headerFavDropdown;
	@UiField
	Anchor headerFavAnchor;
	@UiField
	DropDownMenu headerFavDropdownMenu;
	@UiField
	Strong signedInAsName;
	@UiField
	Div projectFavoritePanelUI;
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
	AnchorListItem myProfileLink;
	@UiField
	AnchorListItem myDashboardLink;
	@UiField
	AnchorListItem myTeamsLink;
	@UiField
	AnchorListItem myChallengesLink;
	@UiField
	AnchorListItem mySettingsLink;
	@UiField
	AnchorListItem myDownloadsLink;
	@UiField
	AnchorListItem helpForumLink;
	@UiField
	AnchorListItem sendFeedbackLink;
	@UiField
	AnchorListItem emailSynapseSupportLink;
	@UiField
	AnchorListItem xsFavoritesLink;
	@UiField
	AnchorListItem xsSearchLink;

	@UiField
	DropDown dashboardDropdown;
	@UiField
	DropDownMenu dashboardDropdownMenu;
	@UiField
	Div searchBoxContainer;
	@UiField
	Alert stagingAlert;
	@UiField
	AnchorListItem documentationLink;
	@UiField
	Div downloadListNotificationUI;
	@UiField
	FocusPanel downloadListLink;
	@UiField
	Label downloadListFileCount;
	@UiField
	Alert cookieNotificationAlert;
	@UiField
	Anchor cookieNotificationLearnMoreLink;
	@UiField
	Button cookieNotificationAlertOkButton;

	@UiField
	Alert portalAlert;
	@UiField
	Icon portalGoBackArrow;
	@UiField
	Image portalLogo;
	@UiField
	Span portalName;
	@UiField
	FocusPanel portalLogoFocusPanel;

	private Presenter presenter;
	private SearchBox searchBox;
	private CookieProvider cookies;
	SageImageBundle sageImageBundle;
	private GlobalApplicationState globalAppState;
	UserBadge userBadge;
	String userId;
	AnchorListItem defaultItem = new AnchorListItem("Empty");
	String portalHref = "";

	@Inject
	public HeaderViewImpl(Binder binder, SageImageBundle sageImageBundle, SearchBox searchBox, CookieProvider cookies, UserBadge userBadge, GlobalApplicationState globalAppState) {
		this.initWidget(binder.createAndBindUi(this));
		this.searchBox = searchBox;
		this.cookies = cookies;
		this.sageImageBundle = sageImageBundle;
		this.userBadge = userBadge;
		this.globalAppState = globalAppState;
		userBadge.setTooltipHidden(true);
		userBadge.setTextHidden(true);
		userBadge.addStyleNames("padding-top-15 padding-bottom-15 padding-left-10");
		// add search panel first
		searchBox.setVisible(true);
		searchBoxContainer.add(searchBox.asWidget());
		dashboardDropdownAnchor.add(userBadge.asWidget());
		Icon angleDown = new Icon(IconType.ANGLE_DOWN);
		angleDown.setPull(Pull.RIGHT);
		angleDown.setPaddingTop(20);
		angleDown.setPaddingRight(10);
		angleDown.setPaddingBottom(19);
		angleDown.setPaddingLeft(5);
		angleDown.setMarginLeft(0);
		dashboardDropdownAnchor.add(angleDown);
		Icon starIcon = new Icon(IconType.STAR);
		starIcon.setColor("white");
		starIcon.setSize(IconSize.TIMES2);
		starIcon.setPaddingTop(13);
		starIcon.setPaddingBottom(12);
		starIcon.setPaddingLeft(5);
		headerFavAnchor.add(starIcon);

		angleDown = new Icon(IconType.ANGLE_DOWN);
		angleDown.setColor("white");
		angleDown.setPull(Pull.RIGHT);
		angleDown.setPaddingTop(20);
		angleDown.setPaddingRight(10);
		angleDown.setPaddingBottom(19);
		angleDown.setPaddingLeft(1);
		angleDown.setMarginLeft(0);
		headerFavAnchor.add(angleDown);

		cookieNotificationAlertOkButton.addClickHandler(event -> {
			presenter.onCookieNotificationDismissed();
		});

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
		projectFavoritePanelUI.clear();
		projectFavoritePanelUI.add(favWidget);
	}

	@Override
	public void showProjectFavoriteWidget() {
		projectFavoritePanelUI.setVisible(true);
	}

	@Override
	public void hideProjectFavoriteWidget() {
		projectFavoritePanelUI.setVisible(false);
	}

	public void initClickHandlers() {
		documentationLink.addClickHandler(event -> {
			event.preventDefault();
			DisplayUtils.newWindow(WebConstants.DOCS_BASE_URL, "", "");
		});
		emailSynapseSupportLink.addClickHandler(event -> {
			event.preventDefault();
			DisplayUtils.newWindow("mailto:synapseinfo@sagebionetworks.org", "", "");
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
		synapseLogo.addClickHandler(event -> {
			presenter.onLogoClick();
		});

		myProfileLink.addClickHandler(event -> {
			Profile place = new Profile(userId, ProfileArea.PROFILE);
			globalAppState.getPlaceChanger().goTo(place);
		});
		myDashboardLink.addClickHandler(event -> {
			Profile place = new Profile(userId, ProfileArea.PROJECTS);
			globalAppState.getPlaceChanger().goTo(place);
		});
		myTeamsLink.addClickHandler(event -> {
			Profile place = new Profile(userId, ProfileArea.TEAMS);
			globalAppState.getPlaceChanger().goTo(place);
		});
		myChallengesLink.addClickHandler(event -> {
			Profile place = new Profile(userId, ProfileArea.CHALLENGES);
			globalAppState.getPlaceChanger().goTo(place);
		});
		mySettingsLink.addClickHandler(event -> {
			Profile place = new Profile(userId, ProfileArea.SETTINGS);
			globalAppState.getPlaceChanger().goTo(place);
		});
		myDownloadsLink.addClickHandler(event -> {
			Profile place = new Profile(userId, ProfileArea.DOWNLOADS);
			globalAppState.getPlaceChanger().goTo(place);
		});
		helpForumLink.addClickHandler(event -> {
			SynapseForumPlace place = new SynapseForumPlace("default");
			globalAppState.getPlaceChanger().goTo(place);
		});
		sendFeedbackLink.addClickHandler(event -> {
			// pendo should also listen for click event on this element
		});

		xsFavoritesLink.addClickHandler(event -> {
			Profile place = new Profile(userId + "/projects/favorites");
			globalAppState.getPlaceChanger().goTo(place);
		});

		xsSearchLink.addClickHandler(event -> {
			Search place = new Search("");
			globalAppState.getPlaceChanger().goTo(place);
		});
		downloadListLink.addClickHandler(event -> {
			Profile place = new Profile(userId + "/downloads");
			globalAppState.getPlaceChanger().goTo(place);
		});
		portalLogoFocusPanel.addClickHandler(event -> {
			if (DisplayUtils.isDefined(portalHref)) {
				Window.Location.assign(portalHref);
			}
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
	public void setUser(UserProfile profile) {
		boolean isInTestWebsite = DisplayUtils.isInTestWebsite(cookies);
		trashLink.setVisible(isInTestWebsite);
		userBadge.clearState();
		signedInAsName.setText("");
		if (profile != null) {
			// has user data, update the user name and add user commands (and set to the current user name)
			userBadge.configure(profile);
			userBadge.setDoNothingOnClick();
			userId = profile.getOwnerId();
			loginLinkUI.setVisible(false);
			logoutLink.setVisible(true);
			dashboardDropdown.setVisible(true);
			headerFavDropdown.setVisible(true);
			signedInAsName.setText(profile.getUserName());
		} else {
			userId = null;
			loginLinkUI.setVisible(true);
			logoutLink.setVisible(false);
			dashboardDropdown.setVisible(false);
			headerFavDropdown.setVisible(false);
		}
	}

	@Override
	public void clearFavorite() {
		headerFavDropdownMenu.clear();
	}

	@Override
	public void setEmptyFavorite() {
		headerFavDropdownMenu.add(defaultItem);
	}

	@Override
	public void addFavorite(List<EntityHeader> headers) {
		for (final EntityHeader header : headers) {
			AnchorListItem favItem = new AnchorListItem(header.getName());
			favItem.addClickHandler(event -> {
				headerFavDropdownMenu.removeStyleName("hover");
				Synapse place = new Synapse(header.getId());
				globalAppState.getPlaceChanger().goTo(place);
			});
			headerFavDropdownMenu.add(favItem);
		}
	}

	@Override
	public void setStagingAlertVisible(boolean visible) {
		stagingAlert.setVisible(visible);
	}

	@Override
	public void setCookieNotificationVisible(boolean visible) {
		cookieNotificationAlert.setVisible(visible);
	}

	/** Event binder code **/
	interface EBinder extends EventBinder<Header> {
	};

	private final EBinder eventBinder = GWT.create(EBinder.class);

	@Override
	public EventBinder<Header> getEventBinder() {
		return eventBinder;
	}

	@Override
	public void setDownloadListFileCount(Integer count) {
		downloadListFileCount.setText(count.toString());
	}

	@Override
	public void setDownloadListUIVisible(boolean visible) {
		downloadListNotificationUI.setVisible(visible);
	}

	@Override
	public void setPortalAlertVisible(boolean visible, JSONObjectAdapter json) {
		if (visible) {
			try {
				if (json.has("backgroundColor")) {
					String color = json.getString("backgroundColor");
					String oldStyle = portalAlert.getElement().getAttribute("style");
					portalAlert.getElement().setAttribute("style", oldStyle + ";background-color: " + color + ";");
				}
				if (json.has("foregroundColor")) {
					String color = json.getString("foregroundColor");
					portalGoBackArrow.setColor(color);
				}
				if (json.has("callbackUrl")) {
					String href = json.getString("callbackUrl");
					portalHref = href;
				}
				if (json.has("portalName")) {
					String name = json.getString("portalName");
					if (!name.trim().isEmpty()) {
						portalName.setText(name);
						portalName.setVisible(true);
						portalLogo.setVisible(false);
					}
				}
				if (json.has("logoUrl")) {
					String logoUrl = json.getString("logoUrl");
					if (!logoUrl.trim().isEmpty()) {
						portalLogo.setUrl(logoUrl);
						portalName.setVisible(false);
						portalLogo.setVisible(true);
					}
				}
				portalAlert.setVisible(true);
			} catch (JSONObjectAdapterException e) {
				e.printStackTrace();
			}
		}
		portalAlert.setVisible(visible);
	}
}
