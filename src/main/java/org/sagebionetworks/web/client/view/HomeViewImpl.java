package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HomeViewImpl extends Composite implements HomeView {

	public interface HomeViewImplUiBinder extends UiBinder<Widget, HomeViewImpl> {
	}

	@UiField
	ReactComponentDiv container;

	private static final String PROJECT_VIEW_ID = "syn23593547.3";

	private Header headerWidget;
	private SynapseJSNIUtils jsniUtils;
	private AuthenticationController authController;

	private static native void _showHomepageComponent(Element el, String sessionToken, String projectViewId) /*-{
		try {
			var props = {
				token: sessionToken,
				projectViewId: projectViewId,
			};
			$wnd.ReactDOM.render($wnd.React.createElement(
					$wnd.SRC.SynapseComponents.SynapseHomepage, props, null),
					el);
		} catch (err) {
			console.error(err);
		}
	}-*/;


	@Inject
	public HomeViewImpl(HomeViewImplUiBinder binder, Header headerWidget, final AuthenticationController authController, SynapseJSNIUtils jsniUtils) {
		initWidget(binder.createAndBindUi(this));

		this.jsniUtils = jsniUtils;
		this.headerWidget = headerWidget;
		this.authController = authController;

		headerWidget.configure();
	}

	@Override
	public void render() {
		scrollToTop();
		_showHomepageComponent(container.getElement(), authController.getCurrentUserAccessToken(), PROJECT_VIEW_ID);
	}


	@Override
	public void refresh() {
		headerWidget.configure();
		headerWidget.refresh();
	}


	@Override
	public void scrollToTop() {
		Window.scrollTo(0, 0);
	}

}
