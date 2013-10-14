package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.place.Challenges;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.ProjectsHome;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.widget.entity.MyEvaluationEntitiesList;
import org.sagebionetworks.web.client.widget.entity.ProgrammaticClientCode;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.HomeSearchBox;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
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
	SimplePanel projectPanel;
	@UiField
	SimplePanel newsFeed;
	@UiField
	SimplePanel loginBtnPanel;
	@UiField
	SimplePanel registerBtnPanel;		
	@UiField
	SimplePanel dream8BtnPanel;
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
	private EntityTreeBrowser myProjectsTreeBrowser;
	private EntityTreeBrowser favoritesTreeBrowser;
	IconsImageBundle iconsImageBundle;
	private MyEvaluationEntitiesList myEvaluationsList;
	
	@Inject
	public HomeViewImpl(HomeViewImplUiBinder binder, 
			Header headerWidget,
			Footer footerWidget, 
			IconsImageBundle icons, 
			SageImageBundle imageBundle,
			final GlobalApplicationState globalApplicationState,
			HomeSearchBox homeSearchBox, 
			EntityTreeBrowser myProjectsTreeBrowser,
			EntityTreeBrowser favoritesTreeBrowser,
			MyEvaluationEntitiesList myEvaluationsList) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.globalApplicationState = globalApplicationState;
		this.homeSearchBox = homeSearchBox;
		this.myProjectsTreeBrowser = myProjectsTreeBrowser;
		this.favoritesTreeBrowser = favoritesTreeBrowser;
		this.myEvaluationsList = myEvaluationsList;
		this.iconsImageBundle = icons;
		
		headerWidget.configure(true);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		
		bigSearchBox.clear();
		bigSearchBox.add(homeSearchBox.asWidget());
		
		Button loginBtn = new Button(DisplayConstants.BUTTON_LOGIN);
		loginBtn.removeStyleName("gwt-Button");
		loginBtn.addStyleName("btn btn-default btn-lg btn-block");
		loginBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
			}
		});
		loginBtnPanel.setWidget(loginBtn);
		
		Button registerBtn = new Button(DisplayConstants.REGISTER_BUTTON);
		registerBtn.removeStyleName("gwt-Button");
		registerBtn.addStyleName("btn btn-default btn-lg btn-block");
		registerBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new RegisterAccount(ClientProperties.DEFAULT_PLACE_TOKEN));
			}
		});
		registerBtnPanel.setWidget(registerBtn);
		
		Button dream8Btn = new Button(DisplayConstants.BUTTON_DREAM_8);
		dream8Btn.removeStyleName("gwt-Button");
		dream8Btn.addStyleName("btn btn-default btn-lg btn-block");
		dream8Btn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Challenges("DREAM8"));
			}
		});
		dream8BtnPanel.setWidget(dream8Btn);
		
		// Programmatic Clients
		fillProgrammaticClientInstallCode();
		
		// Other links
		configureNewWindowLink(aboutSynapseLink, ClientProperties.ABOUT_SYNAPSE_URL, DisplayConstants.MORE_DETAILS_SYNAPSE);
		configureNewWindowLink(restApiLink, ClientProperties.REST_API_URL, DisplayConstants.REST_API_DOCUMENTATION);
		
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

		boolean isLoggedIn = presenter.showLoggedInDetails();
		
		if(isLoggedIn) {
			whatIsSynapseContainer.setVisible(false);
			getStartedContainer.setVisible(false);
			injectProjectPanel(); 
		} else {
			whatIsSynapseContainer.setVisible(true);
			getStartedContainer.setVisible(true);
			projectPanel.clear();
		}		
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


	/*
	 * Private Methods
	 */
	private void injectProjectPanel() {
		projectPanel.clear();
		LayoutContainer container = new LayoutContainer();		
		// Evaluations and Projects
		LayoutContainer evalsAndProjects = new LayoutContainer();
		evalsAndProjects.setStyleName("col-md-4");
		evalsAndProjects.add(getMyEvaluationsContainer());
		evalsAndProjects.add(getMyProjectsContainer());
		evalsAndProjects.add(createCreateProjectWidget()); 
		container.add(evalsAndProjects);
		// Favorites
		container.add(getFavoritesContainer());

		
		projectPanel.add(container);		
	}

	private LayoutContainer createCreateProjectWidget() {
		LayoutContainer createProjectContainer = new LayoutContainer();
		createProjectContainer.setStyleName("row margin-top-15");
		
		LayoutContainer col1 = new LayoutContainer();
		col1.addStyleName("col-md-7");
		final TextBox input = new TextBox();
		input.addStyleName("form-control");
		input.getElement().setAttribute("placeholder", DisplayConstants.NEW_PROJECT_NAME);
		col1.add(input);
		createProjectContainer.add(col1);		
		
		LayoutContainer col2 = new LayoutContainer();
		col2.addStyleName("col-md-5");
		Button createBtn = new Button(DisplayConstants.CREATE_PROJECT);
		createBtn.removeStyleName("gwt-Button");
		createBtn.addStyleName("btn btn-default btn-block");
		createBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				String name = input.getValue();
				if(name == null || name.isEmpty()) {
					showErrorMessage(DisplayConstants.PLEASE_ENTER_PROJECT_NAME);
					return;
				}
				presenter.createProject(input.getValue());
			}
		});
		col2.add(createBtn);
		createProjectContainer.add(col2);		

		LayoutContainer col3 = new LayoutContainer();
		col3.addStyleName("col-md-12 right");		
		Anchor whatProj = new Anchor(DisplayConstants.WHAT_IS_A_PROJECT);
		whatProj.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new ProjectsHome(ClientProperties.DEFAULT_PLACE_TOKEN));
			}
		});
		col3.add(whatProj);
		createProjectContainer.add(col3);
		return createProjectContainer;
	}
	
	private LayoutContainer getFavoritesContainer() {
		LayoutContainer favoritesContainer = new LayoutContainer();
		favoritesContainer.setStyleName("col-md-4");
		favoritesContainer.add(
				new HTML(SafeHtmlUtils.fromSafeConstant("<h3>" + DisplayConstants.FAVORITES + " " + AbstractImagePrototype.create(iconsImageBundle.star16()).getHTML() + "</h3>")));
		favoritesContainer.add(favoritesTreeBrowser.asWidget());
		return favoritesContainer;
	}
	
	private LayoutContainer getMyEvaluationsContainer() {
		//My Evaluations
		LayoutContainer myEvaluations = new LayoutContainer();
		myEvaluations.add(myEvaluationsList.asWidget());					
		return myEvaluations;
	}
	
	private LayoutContainer getMyProjectsContainer() {
		LayoutContainer myProjContainer = new LayoutContainer();
		myProjContainer.add(new HTML(SafeHtmlUtils.fromSafeConstant("<h3>"+ DisplayConstants.MY_PROJECTS +"</h3>")));
		myProjContainer.add(myProjectsTreeBrowser.asWidget());					
		return myProjContainer;
	}

	@Override
	public void setMyProjects(List<EntityHeader> myProjects) {
		myProjectsTreeBrowser.configure(myProjects, true);
	}

	@Override
	public void setMyProjectsError(String string) {
	}

	@Override
	public void setFavorites(List<EntityHeader> favorites) {
		favoritesTreeBrowser.configure(favorites, true);
	}

	@Override
	public void setFavoritesError(String string) {
	}
	
	@Override
	public void setMyEvaluationList(List<EntityHeader> myEvaluationEntities) {
		myEvaluationsList.configure(myEvaluationEntities);
	}
	
	@Override
	public void setMyEvaluationsError(String string) {
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

		final Dialog javaWindow = new Dialog();
		javaWindow.add(new HTML(ProgrammaticClientCode.getJavaClientInstallHTML()));
		javaWindow.setSize(560, 287);
		javaWindow.setPlain(true);
		javaWindow.setModal(false);
		javaWindow.setHeading(DisplayConstants.INSTALL_JAVA_MAVEN);
		javaWindow.setLayout(new FitLayout());			    
	    javaWindow.setButtons(Dialog.CLOSE);
	    javaWindow.setButtonAlign(HorizontalAlignment.RIGHT);

		Button showJava = new Button(DisplayConstants.SHOW);
		showJava.removeStyleName("gwt-Button");
		showJava.addStyleName("btn btn-default btn-lg btn-block margin-top-5");
		showJava.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				javaWindow.show();
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
