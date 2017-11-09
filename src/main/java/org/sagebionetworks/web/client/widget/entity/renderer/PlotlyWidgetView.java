package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.sagebionetworks.web.client.plotly.PlotlyTraceWrapper;

import com.google.gwt.user.client.ui.IsWidget;

public interface PlotlyWidgetView extends IsWidget {
	void setSynAlertWidget(IsWidget w);
	void setPresenter(Presenter p);
	void showChart(String title, String xTitle, String yTitle, List<PlotlyTraceWrapper> xyData, String barMode, String xAxisType, String yAxisType);
	void clearChart();
	void setLoadingVisible(boolean visible);
	void setLoadingMessage(String message);
	boolean isAttached();
	void setSourceDataLink(String url);
	void setSourceDataLinkVisible(boolean visible);
	public interface Presenter {
	}
}
