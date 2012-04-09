package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.place.ComingSoon;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowser;
import org.sagebionetworks.web.client.widget.filter.QueryFilter;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.HomeSearchBox;

import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
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
	Anchor demoCharlesLink;
	@UiField
	Anchor seeAllContributors;
	@UiField
	SimplePanel bigSearchBox;
	@UiField
	SimplePanel myEntityOrRegister;
	@UiField
	SimplePanel bccSignup;
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	private GlobalApplicationState globalApplicationState;
	private HomeSearchBox homeSearchBox;
	private Html registerHtml;
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
		
		seeAllContributors.addStyleName("bulleted");
		if(DisplayConstants.showDemoHtml) {
			demoCharlesLink.setHref("people_charles.html");
			seeAllContributors.setHref("people.html");
		} else {
			demoCharlesLink.setHref("#" + globalApplicationState.getAppPlaceHistoryMapper().getToken(new ComingSoon(DisplayUtils.DEFAULT_PLACE_TOKEN)));
			seeAllContributors.setHref("#" + globalApplicationState.getAppPlaceHistoryMapper().getToken(new ComingSoon(DisplayUtils.DEFAULT_PLACE_TOKEN)));
		}

		bigSearchBox.clear();
		bigSearchBox.add(homeSearchBox.asWidget());

		registerHtml = new Html("<h3>Call for ALPHA USERS</h3>" 
		+ "<p>Are you interested in working with Sage to start a pilot project on Synapse?  Please let us know at <a class=\"link\" href=\"mailto:synapseInfo@sagebase.org\">synapseInfo@sagebase.org</a></p>"
		+ "<div class=\"mega-button\">"
		+ "	<a href=\"#RegisterAccount:0\">Create an Account</a>"
		+ "</div>");		
		
	}


	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		Window.scrollTo(0, 0); // scroll user to top of page		
	}

	@Override
	public void refresh() {
		headerWidget.refresh();
		headerWidget.setSearchVisible(false);
		myEntityOrRegister.clear();		
		if(presenter.showMyProjects()) {
			VerticalPanel vp = new VerticalPanel();
			vp.add(new Html("<h3>My Projects</h3>"));
			vp.add(myEntitiesBrowser.asWidget());
			myEntityOrRegister.add(vp);
		} else {
			myEntityOrRegister.add(registerHtml);
		}
		presenter.showBCCSignup(new AsyncCallback<String>() {
				
				public void onSuccess(String showBCCSignup) {
					if (showBCCSignup==null || !showBCCSignup.equalsIgnoreCase("true")) return;
					Panel sp = new SimplePanel();
					sp.add(new Html(
							"<div class=\"span-6 inner-6 view notopmargin\">" +
							"<h5><a class=\"link\" href=\"#BCCOverview:0\">Sage / DREAM Breast Cancer Prognosis Challenge</a></h5>"+
	                    "<p>The goal of the breast cancer prognosis challenge is to assess the accuracy of computational models "+
					    "designed to predict breast cancer survival based on clinical information about the patient's tumor as "+
					    "well as genome-wide molecular profiling data including gene expression and copy number profiles.</p>"+
	                	"<a class=\"button_readmore\"  href=\"#BCCOverview:0\"></a></div>"));
					
					bccSignup.add(sp);
				}
				public void onFailure(Throwable t) {
					// do nothing
				} // "span-6 inner-6 view notopmargin"
		});
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

	
}
