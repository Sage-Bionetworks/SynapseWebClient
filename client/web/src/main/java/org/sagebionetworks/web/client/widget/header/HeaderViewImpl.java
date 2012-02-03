package org.sagebionetworks.web.client.widget.header;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.place.ComingSoon;
import org.sagebionetworks.web.client.place.DatasetsHome;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.ProjectsHome;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;
import org.sagebionetworks.web.client.widget.search.SearchBox;
import org.sagebionetworks.web.shared.users.UserData;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

@SuppressWarnings("unused")
public class HeaderViewImpl extends Composite implements HeaderView {

	public interface Binder extends UiBinder<Widget, HeaderViewImpl> {
	}

	@UiField
	SpanElement userName;
	@UiField
	Anchor topRightLink1;
	@UiField
	Hyperlink topRightLink2;
	@UiField
	SimplePanel searchBoxPanel;
		
	private Presenter presenter;
	private Map<MenuItems, Element> itemToElement;
	private AuthenticationController authenticationController;	
	private IconsImageBundle iconsImageBundle;
	private GlobalApplicationState globalApplicationState;
	private LayoutContainer jumpTo;
	private TextField<String> jumpToField;
	private Button goButton;
	private SearchBox searchBox;	
	
	@Inject
	public HeaderViewImpl(Binder binder, AuthenticationControllerImpl authenticationController, SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle, GlobalApplicationState globalApplicationState, SearchBox searchBox) {
		this.initWidget(binder.createAndBindUi(this));
		this.iconsImageBundle = iconsImageBundle;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.searchBox = searchBox;
				
		// add search panel
		searchBoxPanel.clear();		
		searchBoxPanel.add(searchBox.asWidget());
		searchBoxPanel.setVisible(false);
		
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		setUser(presenter.getUser());		
	}

	@Override
	public void setMenuItemActive(MenuItems menuItem) {
	}

	@Override
	public void removeMenuItemActive(MenuItems menuItem) {
	}

	@Override
	public void refresh() {
		setUser(presenter.getUser());		
	}

	@Override
	public void setSearchVisible(boolean searchVisible) {
		searchBoxPanel.setVisible(searchVisible);
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void setUser(UserData userData) {
		if(userData != null) {
			topRightLink1.setHTML(DisplayConstants.BUTTON_MY_PROFILE);
			//topRightLink1.setTargetHistoryToken(DisplayUtils.getDefaultHistoryTokenForPlace(Profile.class));
			topRightLink1.setHref("#" + globalApplicationState.getAppPlaceHistoryMapper().getToken(new Profile(DisplayUtils.DEFAULT_PLACE_TOKEN))); //demo
			if(DisplayConstants.showDemoHtml) {
				topRightLink1.setHref("edit_profile.html");
			}	

			userName.setInnerHTML(DisplayConstants.LABEL_WELCOME + " " + userData.getUserName());			
			topRightLink2.setHTML(DisplayConstants.BUTTON_LOGOUT);		
			topRightLink2.setTargetHistoryToken(globalApplicationState.getAppPlaceHistoryMapper().getToken(new LoginPlace(LoginPlace.LOGOUT_TOKEN)));			
		} else {
			topRightLink1.setHTML(DisplayConstants.BUTTON_REGISTER);
			//topRightLink1.setTargetHistoryToken(DisplayUtils.getDefaultHistoryTokenForPlace(RegisterAccount.class));			
			topRightLink1.setHref("#" + globalApplicationState.getAppPlaceHistoryMapper().getToken(new RegisterAccount(DisplayUtils.DEFAULT_PLACE_TOKEN))); // demo

			userName.setInnerHTML("");			
			topRightLink2.setHTML(DisplayConstants.BUTTON_LOGIN);		
			topRightLink2.setTargetHistoryToken(globalApplicationState.getAppPlaceHistoryMapper().getToken(new LoginPlace(LoginPlace.LOGIN_TOKEN)));
			
		}

	}

}


