package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.security.AuthenticationController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownloadListWidgetViewImpl implements DownloadListWidgetView, IsWidget {
	Div mainContainer = new Div();
	Div downloadListContainer = new Div();
	AuthenticationController authController;
	SynapseJSNIUtils jsniUtils;
	EventBus eventBus;
	GWTWrapper gwt;
	boolean isViewingDownloadList = false;
	
	@Inject
	public DownloadListWidgetViewImpl(AuthenticationController authController, SynapseJSNIUtils jsniUtils, EventBus eventBus, GWTWrapper gwt) {
		this.authController = authController;
		this.jsniUtils = jsniUtils;
		this.eventBus = eventBus;
		this.gwt = gwt;
		mainContainer.addStyleName("mainContainer");
		mainContainer.add(downloadListContainer);
		downloadListContainer.addAttachHandler(event -> {
			if (!event.isAttached() && isViewingDownloadList) {
				jsniUtils.unmountComponentAtNode(downloadListContainer.getElement());
				isViewingDownloadList = false;
			}
		});
		downloadListContainer.addStyleName("downloadListContainer");
		gwt.scheduleFixedDelay(() -> {
			// update the DownloadList when this react component is being shown (let the header know that something might be changing)
			if (isViewingDownloadList) {
				eventBus.fireEvent(new DownloadListUpdatedEvent());
			}
		}, 5000);
	}
	
	@Override
	public void refreshView() {
		isViewingDownloadList = false;
		if (authController.isLoggedIn()) {
			_showDownloadList(downloadListContainer.getElement(), authController.getCurrentUserSessionToken());
			isViewingDownloadList = true;	
		}
	}

	private static native void _showDownloadList(Element el, String sessionToken) /*-{
		try {
			var props = {
				token : sessionToken
			};
			$wnd.ReactDOM.render($wnd.React.createElement(
					$wnd.SRC.SynapseComponents.DownloadListTable, props, null),
					el);
		} catch (err) {
			console.error(err);
		}
	}-*/;
	
	
	@Override
	public Widget asWidget() {
		return mainContainer.asWidget();
	}
}
