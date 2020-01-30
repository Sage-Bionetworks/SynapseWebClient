package org.sagebionetworks.web.client.widget.entity.renderer;


import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SRCDemoWidgetViewImpl implements SRCDemoWidgetView {

	public interface Binder extends UiBinder<Widget, SRCDemoWidgetViewImpl> {
	}

	@UiField
	Div demoContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	LoadingSpinner loadingUI;
	Widget w;
	Presenter presenter;
	@UiField
	Span loadingMessage;

	@Inject
	public SRCDemoWidgetViewImpl(Binder binder, PortalGinInjector ginInjector) {
		w = binder.createAndBindUi(this);
		w.addAttachHandler(event -> {
			if (!event.isAttached()) {
				// detach event, clean up react component
				ginInjector.getSynapseJSNIUtils().unmountComponentAtNode(demoContainer.getElement());
			}
		});
	}

	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setSynAlertWidget(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void setDemoVisible(boolean visible) {
		demoContainer.setVisible(visible);
		_showDemo(demoContainer.getElement());
	}

	private static native void _showDemo(Element el) /*-{

		//		Example for QueryWrapperMenu
		try {
			var queryWrapperProps = {
				token : "",
				menuConfig : [ {
					facetDisplayValue : 'Organism',
					facetName : 'Organism',
					sql : 'SELECT * FROM syn9886254',
					synapseId : 'syn9886254',
					unitDescription : 'data files',
					visibleColumnCount : 3,
				}, {
					facetDisplayValue : 'Study',
					facetName : 'Study',
					sql : 'SELECT * FROM syn9886254',
					synapseId : 'syn9886254',
					unitDescription : 'data files',
					visibleColumnCount : 5,
				} ],
				rgbIndex : 2
			//			loadingScreen: $wnd.React.createElement('div', null, null)
			}

			$wnd.ReactDOM.render($wnd.React.createElement(
					$wnd.SRC.SynapseComponents.QueryWrapperMenu,
					queryWrapperProps, null), el);
		} catch (err) {
			console.error(err);
		}
	}-*/;

	@Override
	public void setLoadingVisible(boolean visible) {
		loadingUI.setVisible(visible);
		loadingMessage.setVisible(visible);
	}

	@Override
	public void setLoadingMessage(String message) {
		loadingMessage.setText(message);
	}

	@Override
	public boolean isAttached() {
		return w.isAttached();
	}

	@Override
	public void newWindow(String url) {
		DisplayUtils.newWindow(url, "_blank", "");
	}
}
