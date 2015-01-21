package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
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
import org.sagebionetworks.web.client.place.Challenges;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.ProgrammaticClientCode;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.HomeSearchBox;

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
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HomeViewImpl extends Composite implements HomeView {

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
	org.gwtbootstrap3.client.ui.Button loginBtn;
	@UiField
	org.gwtbootstrap3.client.ui.Button registerBtn;
	@UiField
	org.gwtbootstrap3.client.ui.Button dreamBtn;
	@UiField
	org.gwtbootstrap3.client.ui.Button dashboardBtn;
	
	@UiField
	HTMLPanel whatIsSynapseContainer;
	@UiField
	HTMLPanel howToUseSynapseContainer;
	@UiField
	HTMLPanel getStartedContainer;
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
	private GlobalApplicationState globalApplicationState;
	private HomeSearchBox homeSearchBox;	
	IconsImageBundle iconsImageBundle;
	SimplePanel userPicturePanel;
	private CookieProvider cookies;
	SynapseJSNIUtils synapseJSNIUtils;
	
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
			SynapseJSNIUtils synapseJSNIUtils) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.globalApplicationState = globalApplicationState;
		this.homeSearchBox = homeSearchBox;
		this.iconsImageBundle = icons;
		this.cookies = cookies;
		this.synapseJSNIUtils = synapseJSNIUtils;
		userPicturePanel = new SimplePanel();
		userPicturePanel.addStyleName("displayInline margin-right-5");
		addUserPicturePanel();
		
		headerWidget.configure(true);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		
		bigSearchBox.clear();
		bigSearchBox.add(homeSearchBox.asWidget());
		
		loginBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
			}
		});
		registerBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new RegisterAccount(ClientProperties.DEFAULT_PLACE_TOKEN));
			}
		});
		dreamBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Challenges("DREAM"));
			}
		});
		dashboardBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Profile(authController.getCurrentUserPrincipalId()));
			}
		});
		
		// Programmatic Clients
		fillProgrammaticClientInstallCode();
		
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
            	dashboardBtn.add(userPicturePanel);
            }
        });
	}
	
	@Override
	public void showLoggedInUI(UserSessionData userData) {
		clearUserProfilePicture();
		setUserProfilePicture(userData);
		
		loginBtn.setVisible(false);
		registerBtn.setVisible(false);
		dashboardBtn.setVisible(true);
	}
	@Override
	public void showAnonymousUI() {
		clearUserProfilePicture();
		loginBtn.setVisible(true);
		registerBtn.setVisible(true);
		dashboardBtn.setVisible(false);
	}
	
	private void clearUserProfilePicture() {
		userPicturePanel.clear();
		dashboardBtn.setIcon(IconType.USER);
		dashboardBtn.setIconSize(IconSize.LARGE);
	}
	
	private void setUserProfilePicture(UserSessionData userData) {
		if (userData != null && userData.getProfile() != null) {
			UserProfile profile = userData.getProfile();
			if (profile.getPic() != null && profile.getPic().getPreviewId() != null && profile.getPic().getPreviewId().length() > 0) {
				dashboardBtn.setIcon(null);
				Image profilePicture = new Image();
				profilePicture.setUrl(DisplayUtils.createUserProfileAttachmentUrl(synapseJSNIUtils.getBaseProfileAttachmentUrl(), profile.getOwnerId(), profile.getPic().getPreviewId(), null));
				profilePicture.setWidth("25px");
				profilePicture.setHeight("25px");
				profilePicture.addStyleName("userProfileImage moveup-2");
				userPicturePanel.setWidget(profilePicture);
			}
		}
	}

	
	@Override
	public void onAttach() {
		super.onAttach();
		startCarousel();
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

	private static native void startCarousel() /*-{
		$wnd.jQuery('#myCarousel').carousel('cycle');
	}-*/;

	
}
