package org.sagebionetworks.web.client.widget.entity.renderer;


import java.util.List;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.plotly.AxisType;
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

	public interface Binder extends UiBinder<Widget, PlotlyWidgetViewImpl> {
	}

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
	public PlotlyWidgetViewImpl(Binder binder, SynapseJSNIUtils jsniUtils) {
		w = binder.createAndBindUi(this);
		w.addAttachHandler(event -> {
			if (!event.isAttached()) {
				// detach event, clean up react component
				jsniUtils.unmountComponentAtNode(chartContainer.getElement());
			}
		});
		_addPlotlyClickEventListener(chartContainer.getElement(), this);
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
	public void showChart(String title, String xTitle, String yTitle, List<PlotlyTraceWrapper> xyData, String barMode, AxisType xAxisType, AxisType yAxisType, boolean showLegend) {
		chartContainer.clear();
		String xAxisTypeString = AxisType.AUTO.equals(xAxisType) ? "-" : xAxisType.toString().toLowerCase();
		String yAxisTypeString = AxisType.AUTO.equals(yAxisType) ? "-" : yAxisType.toString().toLowerCase();
		_showChart(chartContainer.getElement(), getPlotlyTraceArray(xyData), barMode, title, xTitle, yTitle, xAxisTypeString, yAxisTypeString, showLegend);
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

	private static native void _showChart(Element el, JavaScriptObject[] xyData, String barMode, String plotTitle, String xTitle, String yTitle, String xAxisType, String yAxisType, boolean showLegend) /*-{

		try {

			var plot = $wnd.createPlotlyComponent($wnd.Plotly);

			// SWC-3668: We must manually construct an Object from the parent window Object prototype.  This is a general GWT js integration issue.
			// If we define the layout in the standard way, like "xaxis: {title:"mytitle"}", then  Object.getPrototypeOf(obj) === Object.prototype is false.
			// And if this condition is false, then plotly clears our input layout params object. It instead uses defaults (based on the data).

			var xAxisLayoutObject = new $wnd.Object();
			xAxisLayoutObject.title = xTitle;
			xAxisLayoutObject.type = xAxisType;
			xAxisLayoutObject.tickangle = 45;
			xAxisLayoutObject.automargin = true;

			var yAxisLayoutObject = new $wnd.Object();
			yAxisLayoutObject.title = yTitle;
			yAxisLayoutObject.type = yAxisType;
			yAxisLayoutObject.automargin = true;

			var props = {
				data : xyData,
				layout : {
					title : plotTitle,
					xaxis : xAxisLayoutObject,
					yaxis : yAxisLayoutObject,
					barmode : barMode,
					showlegend : showLegend,
					autosize : true
				},
				useResizeHandler : true,
				// note: we'd like to just hide the "save and edit plot in cloud" command, 
				// but the parameter provided in the docs (showLink: false) has no effect.
				// hide the entire bar by setting displayModeBar to false.
				config : {
					displayModeBar : false
				},
				style : {
					width : "100%",
					height : "100%"
				}
			};
			$wnd.ReactDOM.render($wnd.React.createElement(plot, props), el);
		} catch (err) {
			console.error(err);
		}
	}-*/;

	private static native void _addPlotlyClickEventListener(Element el, PlotlyWidgetViewImpl thisWidget) /*-{
		try {
			//after plot is drawn, add handler for click events
			$wnd
					.jQuery(el)
					.on(
							'plotly_afterplot',
							function() {
								if (!this.plotlyClickInitialized) {
									this.plotlyClickInitialized = true;
									this.children[0]
											.on(
													'plotly_click',
													function(data) {
														data.event
																.stopPropagation();
														var pnt = data.points[0];
														thisWidget.@org.sagebionetworks.web.client.widget.entity.renderer.PlotlyWidgetViewImpl::onClick(Ljava/lang/String;Ljava/lang/String;)(pnt.x, pnt.y);
													});
								}
							});
		} catch (err) {
			console.error(err);
		}
	}-*/;

	private void onClick(String x, String y) {
		presenter.onClick(x, y);
	}

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

	@Override
	public void newWindow(String url) {
		DisplayUtils.newWindow(url, "_blank", "");
	}
}
