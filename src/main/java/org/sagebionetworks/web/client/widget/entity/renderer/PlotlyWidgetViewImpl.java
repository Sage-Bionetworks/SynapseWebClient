package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.plotly.PlotlyTrace;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PlotlyWidgetViewImpl implements PlotlyWidgetView {

	public interface Binder extends UiBinder<Widget, PlotlyWidgetViewImpl> {}
	
	@UiField
	Div chartContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Div loadingUI;
	@UiField
	Text loadingMessage;
	Widget w;
	Presenter presenter;
	HandlerRegistration resizeHandler;
	@UiField
	Anchor sourceDataAnchor;
	
	@Inject
	public PlotlyWidgetViewImpl(Binder binder) {
		w=binder.createAndBindUi(this);
		resizeHandler = Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				if (chartContainer.isAttached()) {
					_resize(chartContainer.getElement());
				} else {
					resizeHandler.removeHandler();
				}
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
	public void clearChart() {
		chartContainer.clear();
	}
	
	private static native void _resize(Element el) /*-{
		$wnd.Plotly.Plots.resize(el);
	}-*/;
	
	@Override
	public void showChart(String title, String xTitle, String yTitle, PlotlyTrace[] xyData, String barMode) {
		chartContainer.clear();
		_showChart(chartContainer.getElement(), xyData, barMode, title, xTitle, yTitle);
		_resize(chartContainer.getElement());
	}
	
	private static native void _showChart(Element el, PlotlyTrace[] xyData, String barMode, String title, String xTitle, String yTitle) /*-{
		var layout = {
		  title: title,
		  xaxis: { title: xTitle },
		  yaxis: { title: yTitle },
		  barmode: barMode
		};
		
		$wnd.Plotly.plot(el, xyData, layout);
	}-*/;

	@Override
	public void setLoadingVisible(boolean visible) {
		loadingUI.setVisible(visible);
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
	public void setSourceDataLink(String url) {
		sourceDataAnchor.setHref(url);
	}
}
