package org.sagebionetworks.web.client.view;

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
	public DownViewImpl(Binder uiBinder, Header headerWidget) {
		widget = uiBinder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		headerWidget.configure();
		widget.addAttachHandler(event -> {
			if (event.isAttached()) {
				_createSRCErrorPage(srcDownContainer.getElement(), ErrorPageType.maintenance.name(), SYNAPSE_DOWN_MAINTENANCE_TITLE, message);
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
			_createSRCErrorPage(srcDownContainer.getElement(), ErrorPageType.maintenance.name(), SYNAPSE_DOWN_MAINTENANCE_TITLE, message);
		}
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}
	
	public static native void _createSRCErrorPage(Element el, String type, String title, String message) /*-{
		try {
			var props = {
				image : type,
				title : title,
				message: message
			};
			$wnd.ReactDOM.render($wnd.React.createElement(
					$wnd.SRC.SynapseComponents.ErrorPage, props, null), el);
		} catch (err) {
			console.error(err);
		}
	}-*/;
}
