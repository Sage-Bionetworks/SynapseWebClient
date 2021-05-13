package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownloadListWidgetViewImpl implements DownloadListWidgetView, IsWidget {
	Div mainContainer = new Div();
	Div downloadListContainer = new ReactComponentDiv();
	AuthenticationController authController;
	SynapseJSNIUtils jsniUtils;
	EventBus eventBus;
	
	@Inject
	public DownloadListWidgetViewImpl(AuthenticationController authController, SynapseJSNIUtils jsniUtils, EventBus eventBus) {
		this.authController = authController;
		this.jsniUtils = jsniUtils;
		this.eventBus = eventBus;
		mainContainer.addStyleName("mainContainer");
		mainContainer.add(downloadListContainer);
		downloadListContainer.addStyleName("downloadListContainer");
	}
	public void fireDownloadListUpdatedEvent() {
		eventBus.fireEvent(new DownloadListUpdatedEvent());	
	}
	
	@Override
	public void refreshView() {
		if (authController.isLoggedIn()) {
			_showDownloadList(downloadListContainer.getElement(), authController.getCurrentUserAccessToken(), this);
		}
	}

	private static native void _showDownloadList(Element el, String sessionToken, DownloadListWidgetViewImpl w) /*-{
		try {
			function onUpdateDownloadList() {
				w.@org.sagebionetworks.web.client.widget.entity.file.downloadlist.DownloadListWidgetViewImpl::fireDownloadListUpdatedEvent()();
			}
			var props = {
				token: sessionToken,
				listUpdatedCallback: onUpdateDownloadList
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
