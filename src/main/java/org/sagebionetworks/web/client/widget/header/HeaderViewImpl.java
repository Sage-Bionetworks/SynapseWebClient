package org.sagebionetworks.web.client.widget.header;

import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;
import org.sagebionetworks.web.client.widget.search.SearchBox;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HeaderViewImpl extends Composite implements HeaderView {
	public interface Binder extends UiBinder<Widget, HeaderViewImpl> {
	}

	private static final String HEADER_LARGE_STYLE = "largeHeader";
	private static final String HEADER_SMALL_STYLE = "smallHeader";

	@UiField
	Image logoSmall;
	@UiField
	Image logoLarge;
	@UiField
	DivElement headerDiv;
	@UiField
	DivElement headerImageDiv;

	@UiField
	Button dashboardButton;
	@UiField
	ButtonGroup headerFavButtonGroup;
	@UiField
	Button headerFavButton;
	@UiField
	DropDownMenu headerFavList;

	@UiField
	AnchorListItem forumLink;
	@UiField
	AnchorListItem rLink;
	@UiField
	AnchorListItem pythonLink;
	@UiField
	AnchorListItem commandLineLink;
	@UiField
	AnchorListItem restAPILink;

	@UiField
	SimplePanel registerLinkUI;
	@UiField
	SimplePanel dashboardButtonUI;
	@UiField
	Button registerLink;
	@UiField
	SimplePanel loginLinkUI;
	@UiField
	Button loginLink;

	@UiField
	Button trashLink;
	@UiField
	Button logoutLink;

	@UiField
	FlowPanel testSitePanel;
	@UiField
	Anchor goToStandardSite;
	@UiField
	SimplePanel searchBoxContainer;
	private Presenter presenter;
	private SearchBox searchBox;
	private CookieProvider cookies;
	SageImageBundle sageImageBundle;
	boolean showLargeLogo;
	UserBadge userBadge;
	Span userBadgeText;
	HorizontalPanel myDashboardButtonContents;

	@Inject
	public HeaderViewImpl(Binder binder,
			SageImageBundle sageImageBundle,
			SearchBox searchBox,
			CookieProvider cookies,
			UserBadge userBadge) {
		this.initWidget(binder.createAndBindUi(this));
		this.searchBox = searchBox;
		this.cookies = cookies;
		this.sageImageBundle = sageImageBundle;
		this.userBadge = userBadge;
		userBadge.setSize(BadgeSize.SMALL_PICTURE_ONLY);
		// add search panel first
		searchBox.setVisible(true);
		searchBoxContainer.setWidget(searchBox.asWidget());
		myDashboardButtonContents = new HorizontalPanel();
		myDashboardButtonContents.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		myDashboardButtonContents.add(userBadge.asWidget());
		userBadgeText = new Span();
		myDashboardButtonContents.add(userBadgeText);
		addUserPicturePanel();
		showLargeLogo = false; // default
		initClickHandlers();
		refreshTestSiteHeader();
	}

	/**
	 * Clear the divider/caret from the user button, and add the picture container
	 * @param button
	 */
	public void addUserPicturePanel() {
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
            	dashboardButton.add(myDashboardButtonContents);
            }
        });
	}

	public void initClickHandlers() {
		goToStandardSite.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.setTestWebsite(false, cookies);
				Window.Location.reload();
			}
		});
		trashLink.addClickHandler(new ClickHandler() {
    		@Override
			public void onClick(ClickEvent event) {
    			presenter.onTrashClick();
			}
    	});
		logoutLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onLogoutClick();
			}
		});
		dashboardButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onDashboardClick();
			}
		});
		loginLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onLoginClick();
			}
		});
		registerLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onRegisterClick();
			}
		});
		forumLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow("http://support.sagebase.org", "", "");
			}
		});
		rLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(ClientProperties.CLIENT_R_API_URL, "", "");
			}
		});
		pythonLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(ClientProperties.CLIENT_PYTHON_API_URL, "", "");
			}
		});
		commandLineLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(ClientProperties.CLIENT_CL_API_URL, "", "");
			}
		});
		restAPILink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(ClientProperties.REST_API_URL, "", "");
			}
		});
		headerFavButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onFavoriteClick();
			}
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		refresh();
	}

	@Override
	public void setMenuItemActive(MenuItems menuItem) {
	}

	@Override
	public void removeMenuItemActive(MenuItems menuItem) {
	}

	@Override
	public void refresh() {
		setLogo();
		refreshTestSiteHeader();
		boolean isInTestWebsite = DisplayUtils.isInTestWebsite(cookies);
		trashLink.setVisible(isInTestWebsite);
	}

	@Override
	public void setSearchVisible(boolean searchVisible) {
		searchBox.setVisible(searchVisible);
	}

	@Override
	public void setUser(UserSessionData userData) {
		boolean isInTestWebsite = DisplayUtils.isInTestWebsite(cookies);
	 	trashLink.setVisible(isInTestWebsite);
	 	userBadge.clearState();
	 	if (userData != null && userData.getProfile() != null) {
			//has user data, update the user name and add user commands (and set to the current user name)
	 		userBadge.configure(userData.getProfile());
	 		userBadgeText.setText(DisplayUtils.getDisplayName(userData.getProfile()));
			loginLinkUI.setVisible(false);
			registerLinkUI.setVisible(false);
			logoutLink.setVisible(true);
			dashboardButtonUI.setVisible(true);
			headerFavButtonGroup.setVisible(true);
		} else {
			loginLinkUI.setVisible(true);
			registerLinkUI.setVisible(true);
			logoutLink.setVisible(false);
			dashboardButtonUI.setVisible(false);
			headerFavButtonGroup.setVisible(false);
		}
	}

	@Override
	public void clearFavorite() {
		headerFavList.clear();
	}

	@Override
	public void setEmptyFavorite() {
		AnchorListItem defaultItem = new AnchorListItem("Empty");
		headerFavList.add(defaultItem);
	}

	@Override
	public void addFavorite(List<EntityHeader> headers) {
		for (final EntityHeader header : headers) {
			AnchorListItem favItem = new AnchorListItem(header.getName());
			favItem.setHref(DisplayUtils.getSynapseHistoryToken(header.getId()));
			headerFavList.add(favItem);
		}
	}

	@Override
	public void setLargeLogo(boolean isLarge) {
		this.showLargeLogo = isLarge;
	}

	/*
	 * Private Methods
	 */

	private void setLogo() {
		if(showLargeLogo) {
			logoLarge.setVisible(true);
			logoSmall.setVisible(false);
			headerDiv.removeClassName(HEADER_SMALL_STYLE);
			headerDiv.addClassName(HEADER_LARGE_STYLE);
		} else {
			logoLarge.setVisible(false);
			logoSmall.setVisible(true);
			headerDiv.removeClassName(HEADER_LARGE_STYLE);
			headerDiv.addClassName(HEADER_SMALL_STYLE);
		}
	}

	private void refreshTestSiteHeader() {
		testSitePanel.setVisible(DisplayUtils.isInTestWebsite(cookies));
	}
}
