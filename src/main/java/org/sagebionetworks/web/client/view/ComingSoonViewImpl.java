package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMap;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ComingSoonViewImpl extends Composite implements ComingSoonView {

	public interface ComingSoonViewImplUiBinder extends UiBinder<Widget, ComingSoonViewImpl> {}

	@UiField
	Div widgetContainer;
	@UiField
	Div chart;
	@UiField
	Div reactWidget;
	private Presenter presenter;
	
	private Header headerWidget;
	SynapseJSNIUtils synapseJSNIUtils;
	JiraURLHelper jiraErrorHelper;
	AuthenticationController authenticationController;
	GoogleMap map;
	JSONObjectAdapter jsonObjectAdapter;
	@Inject
	public ComingSoonViewImpl(ComingSoonViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget,
			SynapseJSNIUtils synapseJSNIUtils,
			PortalGinInjector ginInjector,
			JiraURLHelper jiraErrorHelper, 
			AuthenticationController authenticationController,
			GoogleMap map,
			JSONObjectAdapter jsonObjectAdapter) {		
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.jiraErrorHelper = jiraErrorHelper;
		this.authenticationController = authenticationController;
		headerWidget.configure(false);
		widgetContainer.add(map.asWidget());
//		map.configure();
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		//provenanceWidget.setHeight(400);
//		((LayoutContainer)provenanceWidget.asWidget()).setAutoHeight(true);
		
		headerWidget.configure(false);
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
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
	
	@Override
	public void setUserList(UserGroupHeaderResponsePage userGroupHeaders) {
		JSONObjectAdapter jsonAdapter = jsonObjectAdapter.createNew();
		try {
			userGroupHeaders.writeToJSONObject(jsonAdapter);
			String userGroupHeadersJson = jsonAdapter.toJSONString();
			_showUserList(userGroupHeadersJson, reactWidget.getElement());
		} catch (JSONObjectAdapterException e) {
			e.printStackTrace();
		}
	}
	
	private static native void _showUserList(String userGroupHeadersJson, Element el) /*-{
		$wnd.ReactDOM.render(
			$wnd.React.createElement(
				$wnd.SynapseReactComponents,
				{ usergroupheaders : JSON.parse(userGroupHeadersJson).children }), 
				el
			);
	}-*/;
}
