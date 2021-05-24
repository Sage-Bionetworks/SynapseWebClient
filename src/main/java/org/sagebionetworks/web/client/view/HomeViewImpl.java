package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsni.SynapseContextProviderPropsJSNIObject;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.core.client.GWT;
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
	private SynapseContextPropsProvider propsProvider;

	private static native void _showHomepageComponent(Element el, String projectViewId, SynapseContextProviderPropsJSNIObject wrapperProps) /*-{
		try {
			var props = {
				projectViewId: projectViewId,
			};

			var component = $wnd.React.createElement($wnd.SRC.SynapseComponents.SynapseHomepage, props, null);
			var wrapper = $wnd.React.createElement($wnd.SRC.SynapseComponents.SynapseContextProvider, wrapperProps, component);

			$wnd.ReactDOM.render(wrapper, el);
		} catch (err) {
			console.error(err);
		}
	}-*/;


	@Inject
	public HomeViewImpl(HomeViewImplUiBinder binder, Header headerWidget, final SynapseContextPropsProvider propsProvider) {
		initWidget(binder.createAndBindUi(this));

		this.headerWidget = headerWidget;
		this.propsProvider = propsProvider;

		headerWidget.configure();
	}

	@Override
	public void render() {
		scrollToTop();
		SynapseContextProviderPropsJSNIObject wrapperProps = propsProvider.getJsniContextProps();
		_showHomepageComponent(container.getElement(), PROJECT_VIEW_ID, wrapperProps);
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
