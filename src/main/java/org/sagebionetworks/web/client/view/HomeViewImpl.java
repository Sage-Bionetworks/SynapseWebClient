package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.ProjectsHome;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.filter.QueryFilter;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.HomeSearchBox;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Panel;
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
	SimplePanel bccSignup;
	@UiField
	SimplePanel projectPanel;
	@UiField
	SimplePanel newsFeed;
	@UiField
	SimplePanel loginBtnPanel;
	@UiField
	SimplePanel registerBtnPanel;		
	@UiField
	HTMLPanel whatIsSynapseContainer;
	@UiField
	HTMLPanel howToUseSynapseContainer;
	@UiField
	HTMLPanel getStartedContainer;
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	private GlobalApplicationState globalApplicationState;
	private HomeSearchBox homeSearchBox;	
	private EntityTreeBrowser myProjectsTreeBrowser;
	private EntityTreeBrowser favoritesTreeBrowser;
	IconsImageBundle iconsImageBundle;
	
	@Inject
	public HomeViewImpl(HomeViewImplUiBinder binder, Header headerWidget,
			Footer footerWidget, IconsImageBundle icons, QueryFilter filter,
			SageImageBundle imageBundle,
			final GlobalApplicationState globalApplicationState,
			HomeSearchBox homeSearchBox, 
			EntityTreeBrowser myProjectsTreeBrowser,
			EntityTreeBrowser favoritesTreeBrowser) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.globalApplicationState = globalApplicationState;
		this.homeSearchBox = homeSearchBox;
		this.myProjectsTreeBrowser = myProjectsTreeBrowser;
		this.favoritesTreeBrowser = favoritesTreeBrowser;
		this.iconsImageBundle = icons;
		
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		
		bigSearchBox.clear();
		bigSearchBox.add(homeSearchBox.asWidget());
		
		Button loginBtn = new Button(DisplayConstants.BUTTON_LOGIN);
		loginBtn.removeStyleName("gwt-Button");
		loginBtn.addStyleName("btn btn-large btn-block");
		loginBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new LoginPlace(DisplayUtils.DEFAULT_PLACE_TOKEN));
			}
		});
		loginBtnPanel.setWidget(loginBtn);
		
		Button registerBtn = new Button(DisplayConstants.REGISTER_BUTTON);
		registerBtn.removeStyleName("gwt-Button");
		registerBtn.addStyleName("btn btn-large");
		registerBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new RegisterAccount(DisplayUtils.DEFAULT_PLACE_TOKEN));
			}
		});
		registerBtnPanel.setWidget(registerBtn);
		
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
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();
		headerWidget.setSearchVisible(false);			

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
		
	    presenter.showBCCSignup(new AsyncCallback<String>() {
				
				public void onSuccess(String showBCCSignup) {
					if (showBCCSignup==null || !showBCCSignup.equalsIgnoreCase("true")) return;
					//now, pull the content
					presenter.loadBccOverviewDescription();
				}
				public void onFailure(Throwable t) {
					// do nothing
				} // "span-6 inner-6 view notopmargin"
		});
	}
	
	@Override
	public void showBccOverview(String description){
		Panel sp = new SimplePanel();
		
		sp.add(new HTML(SafeHtmlUtils.fromSafeConstant(
				"<div class=\"span-6 inner-6 view notopmargin\">" +
				"<h5><a class=\"link\" href=\"#!BCCOverview:0\">Sage / DREAM Breast Cancer Prognosis Challenge</a></h5>"+
				description+
				"<a class=\"button_readmore\" href=\"#!BCCOverview:0\"></a></div>")));
		bccSignup.clear();
		bccSignup.add(sp);
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
		// Overall container
		LayoutContainer container = new LayoutContainer();
		container.setStyleName("span-16 notopmargin last");			
		
		// My Projects
		LayoutContainer myProjContainer = new LayoutContainer();
		myProjContainer.setStyleName("span-8 notopmargin");
		myProjContainer.add(new HTML(SafeHtmlUtils.fromSafeConstant("<h3>"+ DisplayConstants.MY_PROJECTS +"</h3>")));
		myProjContainer.add(myProjectsTreeBrowser.asWidget());					
		container.add(myProjContainer);

		// Favorites
		LayoutContainer favoritesContainer = new LayoutContainer();
		favoritesContainer.setStyleName("span-8 notopmargin last");
		favoritesContainer.add(
				new HTML(SafeHtmlUtils.fromSafeConstant("<h3>" + DisplayConstants.FAVORITES + " " + AbstractImagePrototype.create(iconsImageBundle.star16()).getHTML() + "</h3>")));
		favoritesContainer.add(favoritesTreeBrowser.asWidget());
		container.add(favoritesContainer);

		// Create a project
		LayoutContainer createProjectContainer = new LayoutContainer();
		createProjectContainer.addStyleName("span-16 last");		
		
		final TextBox input = new TextBox();
		input.addStyleName("form-signinInput displayInline");
		input.setWidth("200px");
		input.getElement().setAttribute("placeholder", DisplayConstants.NEW_PROJECT_NAME);
		createProjectContainer.add(input, new MarginData(0, 10, 0, 0));		
		
		Button createBtn = new Button(DisplayConstants.LABEL_CREATE);
		createBtn.removeStyleName("gwt-Button");
		createBtn.addStyleName("btn displayInline form-inputButton");
		createBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.createProject(input.getValue());
			}
		});
		createProjectContainer.add(createBtn);		

		Anchor whatProj = new Anchor(DisplayConstants.WHAT_IS_A_PROJECT);
		whatProj.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new ProjectsHome(DisplayUtils.DEFAULT_PLACE_TOKEN));
			}
		});
		createProjectContainer.add(whatProj, new MarginData(0, 0, 0, 15));
		
		container.add(createProjectContainer);

		projectPanel.add(container);		
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
	
}
