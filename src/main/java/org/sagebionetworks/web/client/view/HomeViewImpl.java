package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.widget.entity.ProgrammaticClientCode;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.HomeSearchBox;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
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
	SimplePanel bigSearchBox;
	@UiField
	SimplePanel newsFeed;
	@UiField
	org.gwtbootstrap3.client.ui.Button dashboardBtn;
	@UiField
	Div registerUI;
	
	@UiField
	HTMLPanel whatIsSynapseContainer;
	@UiField
	HTMLPanel howToUseSynapseContainer;
	@UiField
	SimplePanel rClientInstallPanel;
	@UiField
	SimplePanel pythonClientInstallPanel;
	@UiField
	SimplePanel javaClientInstallPanel;
	@UiField
	SimplePanel clClientInstallPanel;
	@UiField
	Anchor rAPILink;	
	@UiField
	Anchor rExampleCodeLink;	
	@UiField
	Anchor pythonAPILink;	
	@UiField
	Anchor pythonExampleCodeLink;	
	@UiField
	Anchor clAPILink;	
	@UiField
	Anchor clExampleCodeLink;	
	@UiField
	Anchor javaAPILink;	
	@UiField
	Anchor javaExampleCodeLink;	
	@UiField
	Anchor aboutSynapseLink;
	@UiField
	Anchor restApiLink;
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	IconsImageBundle iconsImageBundle;
	SynapseJSNIUtils synapseJSNIUtils;
	UserBadge userBadge;
	HorizontalPanel myDashboardButtonContents;
	
	@Inject
	public HomeViewImpl(HomeViewImplUiBinder binder, 
			Header headerWidget,
			Footer footerWidget, 
			IconsImageBundle icons, 
			SageImageBundle imageBundle,
			final GlobalApplicationState globalApplicationState,
			HomeSearchBox homeSearchBox, 
			CookieProvider cookies,
			final AuthenticationController authController,
			SynapseJSNIUtils synapseJSNIUtils,
			UserBadge userBadge,
			RegisterWidget registerWidget) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.iconsImageBundle = icons;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.userBadge = userBadge;
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
		
		bigSearchBox.clear();
		bigSearchBox.add(homeSearchBox.asWidget());
		
		dashboardBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Profile(authController.getCurrentUserPrincipalId()));
			}
		});
		
		// Programmatic Clients
		fillProgrammaticClientInstallCode();
		
		registerUI.add(registerWidget.asWidget());
		
		// Other links
		configureNewWindowLink(aboutSynapseLink, ClientProperties.ABOUT_SYNAPSE_URL, DisplayConstants.MORE_DETAILS_SYNAPSE);
		configureNewWindowLink(restApiLink, ClientProperties.REST_API_URL, DisplayConstants.REST_API_DOCUMENTATION);
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
		clearUserProfilePicture();
		setUserProfilePicture(userData);
		registerUI.setVisible(false);
		dashboardBtn.setVisible(true);
	}
	@Override
	public void showAnonymousUI() {
		clearUserProfilePicture();
		registerUI.setVisible(true);
		dashboardBtn.setVisible(false);
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
	public void showNews(String html){
		HTMLPanel panel = new HTMLPanel(html);		
		DisplayUtils.sendAllLinksToNewWindow(panel);
		newsFeed.clear();
		newsFeed.add(panel);
	}
	
	@Override
	public void refresh() {
		header.clear();
		headerWidget.configure(true);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();
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
	}


	private void fillProgrammaticClientInstallCode() {
		configureNewWindowLink(rAPILink, ClientProperties.CLIENT_R_API_URL, DisplayConstants.API_DOCUMENTATION);
		configureNewWindowLink(rExampleCodeLink, ClientProperties.CLIENT_R_EXAMPLE_CODE_URL, DisplayConstants.EXAMPLE_CODE);
		configureNewWindowLink(pythonAPILink, ClientProperties.CLIENT_PYTHON_API_URL, DisplayConstants.API_DOCUMENTATION);
		configureNewWindowLink(pythonExampleCodeLink, ClientProperties.CLIENT_PYTHON_EXAMPLE_CODE_URL, DisplayConstants.EXAMPLE_CODE);
		configureNewWindowLink(clAPILink, ClientProperties.CLIENT_CL_API_URL, DisplayConstants.API_DOCUMENTATION);
		configureNewWindowLink(clExampleCodeLink, ClientProperties.CLIENT_CL_EXAMPLE_CODE_URL, DisplayConstants.EXAMPLE_CODE);
		configureNewWindowLink(javaAPILink, ClientProperties.CLIENT_JAVA_API_URL, DisplayConstants.API_DOCUMENTATION);
		configureNewWindowLink(javaExampleCodeLink, ClientProperties.CLIENT_JAVA_EXAMPLE_CODE_URL, DisplayConstants.EXAMPLE_CODE);
		
		rClientInstallPanel.add(new HTML(ProgrammaticClientCode.getRClientInstallHTML()));
		pythonClientInstallPanel.add(new HTML(ProgrammaticClientCode.getPythonClientInstallHTML()));
		clClientInstallPanel.add(new HTML(ProgrammaticClientCode.getPythonClientInstallHTML()));

		Button showJava = new Button(DisplayConstants.SHOW);
		showJava.removeStyleName("gwt-Button");
		showJava.addStyleName("btn btn-default btn-lg btn-block margin-top-5");
		showJava.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				Bootbox.alert("<h4>"+DisplayConstants.INSTALL_JAVA_MAVEN + "</h4>" + ProgrammaticClientCode.getJavaClientInstallHTML().asString(), new AlertCallback() {
					@Override
					public void callback() {
					}
				});
			}
		});	
		javaClientInstallPanel.add(showJava);
	}

	private void configureNewWindowLink(Anchor a, String href, String text) {
		a.addStyleName("link");
		a.setTarget("_blank");
		a.setHref(href);
		a.setText(text);
	}
}
