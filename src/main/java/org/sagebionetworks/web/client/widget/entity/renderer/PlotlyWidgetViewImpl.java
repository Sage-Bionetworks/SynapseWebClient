package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.constants.Alignment;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.plotly.PlotlyTrace;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
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
	@UiField
	Heading plotTitle;
	
	Widget w;
	Presenter presenter;
	@Inject
	public PlotlyWidgetViewImpl(Binder binder) {
		w=binder.createAndBindUi(this);
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
	public void setSynAlertWidget(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	
	@Override
	public void clearChart() {
		chartContainer.clear();
	}
	
	@Override
	public void showChart(String xTitle, String yTitle, PlotlyTrace[] xyData, String barMode) {
		chartContainer.clear();
		_showChart(chartContainer.getElement(), xTitle, yTitle, xyData, barMode);
	}
	
	private static native void _showChart(Element el, String xTitle, String yTitle, PlotlyTrace[] xyData, String barMode) /*-{
		var layout = {
		  xaxis: {
		    title: xTitle,
		  },
		  yaxis: {
		    title: yTitle,
		  },
		  barmode: barMode,
		  margin: { t: 0 },
		  autosize: true
		};
		
		$wnd.Plotly.plot(el, 
			xyData, 
			layout);
		$wnd.onresize = function() {
		    $wnd.Plotly.Plots.resize(el);
		};
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
	public void setTitle(String title) {
		plotTitle.setText(title);
	}
}
