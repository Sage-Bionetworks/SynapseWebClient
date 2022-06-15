package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.DownloadCartPageProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownloadCartPageViewImpl implements DownloadCartPageView {
	ReactComponentDiv container;

	private Header headerWidget;
	private SynapseContextPropsProvider propsProvider;
	private Presenter presenter;
	@Inject
	public DownloadCartPageViewImpl(AuthenticationController authenticationController, Header headerWidget, SynapseContextPropsProvider propsProvider) {
		container = new ReactComponentDiv();
		this.headerWidget = headerWidget;
		this.propsProvider = propsProvider;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void render() {
		Window.scrollTo(0, 0); // scroll user to top of page
		headerWidget.configure();
		DownloadCartPageProps props = DownloadCartPageProps.create( entityId -> {
			presenter.onViewSharingSettingsClicked(entityId);
		});
		ReactElement component = React.createElementWithSynapseContext(SRC.SynapseComponents.DownloadCartPage, props, propsProvider.getJsInteropContextProps());
		ReactDOM.render(component, container.getElement());
	}
	
	@Override
	public Widget asWidget() {
		return container.asWidget();
	}
}