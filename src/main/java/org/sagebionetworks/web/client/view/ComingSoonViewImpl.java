package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.presenter.RejectReasonWidget;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.DownloadSpeedTester;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMap;
import org.sagebionetworks.web.client.widget.header.Header;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
	@UiField
	Button testDownloadSpeedButton;
	@UiField
	Heading downloadSpeedResult;
	
	private Presenter presenter;
	
	private Header headerWidget;
	JSONObjectAdapter jsonObjectAdapter;
	@Inject
	public ComingSoonViewImpl(ComingSoonViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget,
			SynapseJSNIUtils synapseJSNIUtils,
			PortalGinInjector ginInjector,
			JiraURLHelper jiraErrorHelper, 
			AuthenticationController authenticationController,
			GoogleMap map,
			RejectReasonWidget rejectReasonWidget,
			JSONObjectAdapter jsonObjectAdapter,
			DownloadSpeedTester downloadSpeedTester) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.jsonObjectAdapter = jsonObjectAdapter;
		headerWidget.configure();
		widgetContainer.add(map.asWidget());
		AsyncCallback<Double> downloadSpeedCallback = new AsyncCallback<Double>() {
			@Override
			public void onFailure(Throwable caught) {
				downloadSpeedResult.setText("Use SynapseAlert to handle error: " + caught.getMessage());
			}
			
			@Override
			public void onSuccess(Double result) {
				DisplayUtils.getFriendlySize(result, true);
				downloadSpeedResult.setText(DisplayUtils.getFriendlySize(result, true) + "/s");
			}
		};
		testDownloadSpeedButton.addClickHandler(event -> {
			downloadSpeedTester.testDownloadSpeed(downloadSpeedCallback);
		});
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		//provenanceWidget.setHeight(400);
//		((LayoutContainer)provenanceWidget.asWidget()).setAutoHeight(true);
		
		headerWidget.configure();
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
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
		try {
			$wnd.ReactDOM.render(
				$wnd.React.createElement(
					$wnd.SynapseReactComponents.UserListComponent,
					{ usergroupheaders : JSON.parse(userGroupHeadersJson).children }), 
					el
				);
		} catch (err) {
			console.error(err);
		}
	}-*/;
}
