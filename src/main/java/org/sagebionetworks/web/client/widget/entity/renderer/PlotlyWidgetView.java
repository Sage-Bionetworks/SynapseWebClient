package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.plotly.PlotlyTrace;

import com.google.gwt.user.client.ui.IsWidget;

public interface PlotlyWidgetView extends IsWidget {
	void setSynAlertWidget(IsWidget w);
	void setPresenter(Presenter p);
	void setTitle(String title);
	void showChart(String xTitle, String yTitle, PlotlyTrace[] xyData, String barMode);
	void clearChart();
	void setLoadingVisible(boolean visible);
	void setLoadingMessage(String message);
	boolean isAttached();
	public interface Presenter {
	}
}
