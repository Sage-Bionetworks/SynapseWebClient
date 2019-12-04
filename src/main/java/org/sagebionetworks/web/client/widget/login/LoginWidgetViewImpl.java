package org.sagebionetworks.web.client.widget.login;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.view.users.RegisterAccountViewImpl;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginWidgetViewImpl implements LoginWidgetView, IsWidget {
	public interface LoginWidgetViewImplUiBinder extends UiBinder<Widget, LoginWidgetViewImpl> {
	}

	@UiField
	Div srcLoginContainer;
	Widget widget;
	SynapseJSNIUtils jsniUtils;

	@Inject
	public LoginWidgetViewImpl(LoginWidgetViewImplUiBinder binder, SynapseJSNIUtils jsniUtils) {
		widget = binder.createAndBindUi(this);
		this.jsniUtils = jsniUtils;
		widget.addAttachHandler(event -> {
			if (event.isAttached()) {
				_createSRCLogin(srcLoginContainer.getElement(), RegisterAccountViewImpl.GOOGLE_OAUTH_CALLBACK_URL);
			} else {
				// detach event, clean up react component
				jsniUtils.unmountComponentAtNode(srcLoginContainer.getElement());
			}
		});
	}

	private static native void _createSRCLogin(Element el, String googleSSORedirectUrl) /*-{
		try {
			var props = {
				theme : 'light',
				icon : true,
				googleRedirectUrl : googleSSORedirectUrl
			};

			$wnd.ReactDOM.render($wnd.React.createElement(
					$wnd.SRC.SynapseComponents.Login, props, null), el);
		} catch (err) {
			console.error(err);
		}
	}-*/;

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clear() {}

	@Override
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}
}
