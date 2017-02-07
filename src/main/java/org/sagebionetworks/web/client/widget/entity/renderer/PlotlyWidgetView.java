package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.plotly.XYData;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface PlotlyWidgetView extends IsWidget {
	void setSynAlertWidget(Widget w);
	void setPresenter(Presenter p);
	void showChart(String title, String xTitle, String yTitle, XYData[] xyData);
	void clearChart();
	void setLoadingVisible(boolean visible);
	void setLoadingMessage(String message);
	boolean isAttached();
	public interface Presenter {
	}
}
