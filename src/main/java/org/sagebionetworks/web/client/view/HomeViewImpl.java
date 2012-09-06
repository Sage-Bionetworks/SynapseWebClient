package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowser;
import org.sagebionetworks.web.client.widget.filter.QueryFilter;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.HomeSearchBox;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Panel;
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
	SimplePanel bccSignup;
	@UiField
	SimplePanel projectPanel;
	@UiField
	SimplePanel newsFeed;
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	private GlobalApplicationState globalApplicationState;
	private HomeSearchBox homeSearchBox;	
	private MyEntitiesBrowser myEntitiesBrowser;
	
	@Inject
	public HomeViewImpl(HomeViewImplUiBinder binder, Header headerWidget,
			Footer footerWidget, IconsImageBundle icons, QueryFilter filter,
			SageImageBundle imageBundle,
			GlobalApplicationState globalApplicationState,
			HomeSearchBox homeSearchBox, MyEntitiesBrowser myEntitiesBrowser) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.globalApplicationState = globalApplicationState;
		this.homeSearchBox = homeSearchBox;
		this.myEntitiesBrowser = myEntitiesBrowser;
		
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		
		bigSearchBox.clear();
		bigSearchBox.add(homeSearchBox.asWidget());

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
		
		injectProjectPanel(); 
		
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
				"<h5><a class=\"link\" href=\"#BCCOverview:0\">Sage / DREAM Breast Cancer Prognosis Challenge</a></h5>"+
				description+
				"<a class=\"button_readmore\" href=\"#BCCOverview:0\"></a></div>")));
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
		if(presenter.showLoggedInDetails()) {
			// My Projects
			LayoutContainer projectDiv = new LayoutContainer();
			
			LayoutContainer separator = new LayoutContainer();
			separator.setStyleName("span-24 separator");
			
			LayoutContainer mainService = new LayoutContainer();
			mainService.setStyleName("span-24 main-service");
			
			// Create a project
			mainService.add(new HTML(SafeHtmlUtils.fromSafeConstant(
					"<div class=\"span-12 notopmargin\">" +
					"	<h3>Start Your Own Project</h3>" +
   					"	<p>Get started using Synapse today by creating your own Project. Projects provide an organizational structure for you to interact with your data, code and analyses.</p>" +
   					"<ul class=\"list arrow-list\"><li>Organize your work</li><li>Store Data, Code & Results</li><li>Set Sharing Level</li><li>Custom, Searchable Annotations </li><li>Full Programmatic API & R Integration </li><li>Attach Figures and Documents</li><li>Describe & Version </li><li>Create Links & See Usage </li></ul>" + 
					" 	<div class=\"mega-button\" style=\"margin-top: 10px;\">" +
					" 		<a id=\"" + DisplayConstants.ID_BTN_START_PROJECT + "\" href=\"#ProjectsHome:0\">Start a Project</a>" +
					" 	</div>" +
					"</div>")));

			
			// My Projects
			LayoutContainer myProjectLayout = new LayoutContainer();
			myProjectLayout.setStyleName("span-12 notopmargin last");
			VerticalPanel vp = new VerticalPanel();
			vp.add(new HTML(SafeHtmlUtils.fromSafeConstant("<h3>My Projects</h3>")));
			vp.add(myEntitiesBrowser.asWidget());
			myProjectLayout.add(vp);			
			mainService.add(myProjectLayout);
			
			projectDiv.add(separator);
			projectDiv.add(mainService);
			projectDiv.layout(true);
						
			projectPanel.add(projectDiv);
		}
	}
	
}
