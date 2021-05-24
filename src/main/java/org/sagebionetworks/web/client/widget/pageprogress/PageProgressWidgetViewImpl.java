package org.sagebionetworks.web.client.widget.pageprogress;

import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsni.SynapseContextProviderPropsJSNIObject;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PageProgressWidgetViewImpl implements PageProgressWidgetView, IsWidget {
	public interface PageProgressWidgetViewImplUiBinder extends UiBinder<Widget, PageProgressWidgetViewImpl> {
	}

	@UiField
	ReactComponentDiv srcContainer;
	Widget widget;
	SynapseJSNIUtils jsniUtils;
	SynapseContextPropsProvider propsProvider;
	boolean isConfigured = false;

	@Inject
	public PageProgressWidgetViewImpl(PageProgressWidgetViewImplUiBinder binder, SynapseJSNIUtils jsniUtils, SynapseProperties synapseProperties, SynapseContextPropsProvider propsProvider) {
		widget = binder.createAndBindUi(this);
		this.jsniUtils = jsniUtils;
		this.propsProvider = propsProvider;
	}

	@Override
	public void configure(String barColor, int barPercent, String backBtnLabel, Callback backBtnCallback, String forwardBtnLabel, Callback forwardBtnCallback, boolean isForwardActive) {
		_createSRCWidget(srcContainer.getElement(), barColor, barPercent, backBtnLabel, backBtnCallback, forwardBtnLabel, forwardBtnCallback, isForwardActive, propsProvider.getJsniContextProps());
		isConfigured = true;
	}

	private static native void _createSRCWidget(Element el, String barColor, int barPercent, String backBtnLabel, Callback backBtnCallback, String forwardBtnLabel, Callback forwardBtnCallback, boolean isForwardActive, SynapseContextProviderPropsJSNIObject wrapperProps) /*-{
		function backBtnCallbackFunction() {
			backBtnCallback.@org.sagebionetworks.web.client.utils.Callback::invoke()();
		}
		function forwardBtnCallbackFunction() {
			forwardBtnCallback.@org.sagebionetworks.web.client.utils.Callback::invoke()();
		}
		
		try {
			var props = {
				barColor: barColor,
				barPercent: barPercent,
				backBtnLabel: backBtnLabel,
				backBtnCallback: backBtnCallbackFunction,
				forwardBtnLabel: forwardBtnLabel,
				forwardBtnCallback: forwardBtnCallbackFunction,
				forwardBtnActive: isForwardActive,
			}

			var component = $wnd.React.createElement($wnd.SRC.SynapseComponents.PageProgress, props, null);
			var wrapper = $wnd.React.createElement($wnd.SRC.SynapseComponents.SynapseContextProvider, props, component);

			$wnd.ReactDOM.render(wrapper, el);
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
