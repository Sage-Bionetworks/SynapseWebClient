package org.sagebionetworks.web.client.widget.header;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.EmptyProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.widget.FullWidthAlert;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventBinder;

public class HeaderViewImpl extends Composite implements HeaderView {
	public interface Binder extends UiBinder<Widget, HeaderViewImpl> {
	}
	@UiField
	Div header;
	@UiField
	FullWidthAlert cookieNotificationAlert;

	@UiField
	Div portalAlert;
	@UiField
	Image portalLogo;
	@UiField
	Span portalName;
	@UiField
	FocusPanel portalLogoFocusPanel;
	Div synapseNavDrawerContainer = new Div();
	@UiField
	Alert stagingAlert;
	
	private Presenter presenter;
	String portalHref = "";
	SynapseContextPropsProvider propsProvider;
	GlobalApplicationState globalAppState;
	
	@Inject
	public HeaderViewImpl(Binder binder, SynapseContextPropsProvider propsProvider, GlobalApplicationState globalAppState) {
		this.initWidget(binder.createAndBindUi(this));
		this.propsProvider = propsProvider;
		this.globalAppState = globalAppState;
		cookieNotificationAlert.addPrimaryCTAClickHandler(event -> {
			presenter.onCookieNotificationDismissed();
		});

		initClickHandlers();
		clear();
		synapseNavDrawerContainer.addAttachHandler(event -> {
			if (event.isAttached()) {
				EmptyProps props = EmptyProps.create();
				ReactElement component = React.createElementWithSynapseContext(SRC.SynapseComponents.SynapseNavDrawer, props, propsProvider.getJsInteropContextProps());
				ReactDOM.render(component, synapseNavDrawerContainer.getElement());
			}
		});
	}

	@Override
	public void clear() {
	}


	public void initClickHandlers() {
		portalLogoFocusPanel.addClickHandler(event -> {
			if (DisplayUtils.isDefined(portalHref)) {
				Window.Location.assign(portalHref);
			}
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		refresh();
	}

	private void detachNavBar() {
		synapseNavDrawerContainer.removeFromParent();
		Document.get().getBody().removeClassName("SynapseNavDrawerIsShowing");
	}
	private void attachNavBar() {
		header.add(synapseNavDrawerContainer);
		Document.get().getBody().addClassName("SynapseNavDrawerIsShowing");
	}
	
	@Override
	public void refresh() {
		detachNavBar();
		if (globalAppState.getCurrentPlace() != null && 
			!(globalAppState.getCurrentPlace() instanceof Home || globalAppState.getCurrentPlace() instanceof LoginPlace)) {
			attachNavBar();
		}
	}

	@Override
	public void openNewWindow(String url) {
		DisplayUtils.newWindow(url, "", "");
	}

	@Override
	public void setStagingAlertVisible(boolean visible) {
		stagingAlert.setVisible(visible);
	}

	@Override
	public void setCookieNotificationVisible(boolean visible) {
		cookieNotificationAlert.setVisible(visible);
	}

	/** Event binder code **/
	interface EBinder extends EventBinder<Header> {
	};

	private final EBinder eventBinder = GWT.create(EBinder.class);

	@Override
	public EventBinder<Header> getEventBinder() {
		return eventBinder;
	}

	@Override
	public void setPortalAlertVisible(boolean visible, JSONObjectAdapter json) {
		if (visible) {
			try {
				if (json.has("callbackUrl")) {
					String href = json.getString("callbackUrl");
					portalHref = href;
				}
				if (json.has("portalName")) {
					String name = json.getString("portalName");
					if (!name.trim().isEmpty()) {
						portalName.setText(name);
						portalName.setVisible(true);
						portalLogo.setVisible(false);
					}
				}
				if (json.has("logoUrl")) {
					String logoUrl = json.getString("logoUrl");
					if (!logoUrl.trim().isEmpty()) {
						portalLogo.setUrl(logoUrl);
						portalName.setVisible(false);
						portalLogo.setVisible(true);
					}
				}
				portalAlert.setVisible(true);
			} catch (JSONObjectAdapterException e) {
				e.printStackTrace();
			}
		}
		portalAlert.setVisible(visible);
	}
}
