package org.sagebionetworks.web.client.widget.login;

import static org.sagebionetworks.web.shared.WebConstants.REPO_SERVICE_URL_KEY;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginWidgetViewImpl implements LoginWidgetView, IsWidget {
	public static final String ROOT_PORTAL_URL = Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/";
	public static final String GOOGLE_OAUTH_CALLBACK_URL = ROOT_PORTAL_URL + "Portal/oauth2callback?oauth2provider=GOOGLE_OAUTH_2_0";
	public static final String GOOGLE_OAUTH_WITH_STATE_CALLBACK_URL = GOOGLE_OAUTH_CALLBACK_URL + "&state=";
	
	public interface LoginWidgetViewImplUiBinder extends UiBinder<Widget, LoginWidgetViewImpl> {}
	@UiField
	Div srcLoginContainer;
	Widget widget;
	SynapseJSNIUtils jsniUtils;
	
	@Inject
	public LoginWidgetViewImpl(LoginWidgetViewImplUiBinder binder, SynapseJSNIUtils jsniUtils, SynapseProperties synapseProperties) {
		widget = binder.createAndBindUi(this);
		this.jsniUtils = jsniUtils;
		widget.addAttachHandler(event -> {
			if (event.isAttached()) {
				String endpoint = "https://" + synapseProperties.getSynapseProperty(REPO_SERVICE_URL_KEY);
				_createSRCLogin(srcLoginContainer.getElement(), ROOT_PORTAL_URL, GOOGLE_OAUTH_CALLBACK_URL, endpoint);
			} else {
				//detach event, clean up react component
				jsniUtils.unmountComponentAtNode(srcLoginContainer.getElement());
			}
		});
	}
	
	private static native void _createSRCLogin(Element el, String rootPortalURL, String googleSSORedirectUrl, String fullRepoEndpoint) /*-{
		try {
			var repoEndpoint = 'https://' + new URL(fullRepoEndpoint).hostname;
			var props = {
				theme:'light',
				icon:true,
				googleRedirectUrl: googleSSORedirectUrl,
				endpoint:repoEndpoint,
				swcEndpoint:rootPortalURL
			};
			
			$wnd.ReactDOM.render(
				$wnd.React.createElement($wnd.SRC.SynapseComponents.Login, props, null), 
				el
			);
		} catch (err) {
			console.error(err);
		}
	}-*/;
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clear() {
	}
	@Override
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}
}
