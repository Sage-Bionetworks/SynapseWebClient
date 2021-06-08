package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsni.SynapseContextProviderPropsJSNIObject;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownViewImpl implements DownView {
	public static final String SYNAPSE_DOWN_MAINTENANCE_TITLE = "Sorry, Synapse is down for maintenance.";
	private Header headerWidget;
	private SynapseContextPropsProvider propsProvider;
	@UiField
	ReactComponentDiv srcDownContainer;
	String message;

	public static enum ErrorPageType {
		maintenance,
		noAccess,
		unavailable
	}
	public interface Binder extends UiBinder<Widget, DownViewImpl> {
	}

	Widget widget;

	@Inject
	public DownViewImpl(Binder uiBinder, Header headerWidget, final SynapseContextPropsProvider propsProvider
	) {
		widget = uiBinder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		this.propsProvider = propsProvider;
		headerWidget.configure();
		widget.addAttachHandler(event -> {
			if (event.isAttached()) {
				_createSRCErrorPage(srcDownContainer.getElement(), ErrorPageType.maintenance.name(), SYNAPSE_DOWN_MAINTENANCE_TITLE, message, propsProvider.getJsniContextProps());
			}
		});
	}

	@Override
	public void init() {
		headerWidget.configure();
		com.google.gwt.user.client.Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setMessage(String message) {
		this.message = message;
		if (widget.isAttached()) {
			_createSRCErrorPage(srcDownContainer.getElement(), ErrorPageType.maintenance.name(), SYNAPSE_DOWN_MAINTENANCE_TITLE, message, propsProvider.getJsniContextProps());
		}
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}
	
	public static native void _createSRCErrorPage(Element el, String type, String title, String message, SynapseContextProviderPropsJSNIObject wrapperProps) /*-{
		try {
			var props = {
				image : type,
				title : title,
				message: message
			};
			var component = $wnd.React.createElement($wnd.SRC.SynapseComponents.ErrorPage, props, null)
			var wrapper = $wnd.React.createElement($wnd.SRC.SynapseContext.SynapseContextProvider, wrapperProps, component)
			$wnd.ReactDOM.render(wrapper, el);
		} catch (err) {
			console.error(err);
		}
	}-*/;
}
