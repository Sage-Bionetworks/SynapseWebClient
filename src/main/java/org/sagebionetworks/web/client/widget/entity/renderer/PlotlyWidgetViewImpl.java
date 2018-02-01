package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.plotly.PlotlyTraceWrapper;
import org.sagebionetworks.web.client.widget.LoadingSpinner;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
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
	LoadingSpinner loadingUI;
	Widget w;
	Presenter presenter;
	@UiField
	Anchor sourceDataAnchor;
	@UiField
	Span loadingMessage;
	
	@Inject
	public PlotlyWidgetViewImpl(Binder binder) {
		w=binder.createAndBindUi(this);
		chartContainer.setWidth("100%");
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
	
	@Override
	public void showChart(
			String title, 
			String xTitle, 
			String yTitle, 
			List<PlotlyTraceWrapper> xyData, 
			String barMode, 
			String xAxisType, 
			String yAxisType, 
			boolean showLegend) {
		chartContainer.clear();
		_showChart(chartContainer.getElement(), getPlotlyTraceArray(xyData), barMode, title, xTitle, yTitle, xAxisType, yAxisType, showLegend);
	}
	
	public static JavaScriptObject[] getPlotlyTraceArray(List<PlotlyTraceWrapper> l) {
		if (l == null) {
			return null;
		}
		JavaScriptObject[] d = new JavaScriptObject[l.size()];
		for (int i = 0; i < l.size(); i++) {
			d[i] = l.get(i).getTrace();
		}
		return d;
	}
	
	private static native void _showChart(
			Element el, 
			JavaScriptObject[] xyData, 
			String barMode, 
			String plotTitle, 
			String xTitle, 
			String yTitle, 
			String xAxisType, 
			String yAxisType, 
			boolean showLegend) /*-{
		var plot =  $wnd.createPlotlyComponent($wnd.Plotly);
		var props = {
			data: xyData,
			fit: true,
			layout: {
				  xaxis: {
				  	title: xTitle,
				  	type: xAxisType
				  },
				  yaxis: { 
				  	title: yTitle,
				  	type: yAxisType
				  },
				  barmode: barMode,
				  showlegend: showLegend
				},

			// note: we'd like to just hide the "save and edit plot in cloud" command, 
			// but the parameter provided in the docs (showLink: false) has no effect.
			// hide the entire bar by setting displayModeBar to false.
			config: {displayModeBar: false}
		};
		$wnd.ReactDOM.render(
				$wnd.React.createElement(plot, props), 
				el
			);
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
	public void setSourceDataLink(String url) {
		sourceDataAnchor.setHref(url);
	}
	
	@Override
	public void setSourceDataLinkVisible(boolean visible) {
		sourceDataAnchor.setVisible(visible);
	}
}
