package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import org.sagebionetworks.web.client.plotly.AxisType;
import org.sagebionetworks.web.client.plotly.BarMode;
import org.sagebionetworks.web.client.plotly.GraphType;
import com.google.gwt.user.client.ui.IsWidget;

public interface PlotlyConfigView extends IsWidget {

	void setPresenter(Presenter presenter);

	String getTitle();

	void setTitle(String title);

	void setGraphType(GraphType graphType);

	GraphType getGraphType();

	String getXAxisLabel();

	void setXAxisLabel(String label);

	String getYAxisLabel();

	void setYAxisLabel(String label);

	String getAdvancedClause();

	void setAdvancedClause(String v);

	void setBarMode(BarMode barMode);

	BarMode getBarMode();

	void clearYAxisColumns();

	void addYAxisColumn(String yColumnName);

	void setTableName(String value);

	void setXAxisColumnName(String value);

	String getXAxisColumnName();

	void setBarModeVisible(boolean visible);

	void setBarOrientationVisible(boolean visible);

	boolean isBarOrientationHorizontal();

	void setBarOrientationHorizontal(boolean isHorizontal);

	void setSynAlert(IsWidget w);

	void add(IsWidget w);

	void setAvailableColumns(List<String> names);

	void resetSelectedYColumn();

	void setShowHideButton(IsWidget w);

	void setAdvancedUIVisible(boolean visible);

	void setShowLegend(boolean value);

	boolean isShowLegend();

	AxisType getXAxisType();

	AxisType getYAxisType();

	void setXAxisType(AxisType axisType);

	void setYAxisType(AxisType axisType);

	public interface Presenter {
		void onFindTable();

		void onXColumnChanged();

		void onAddYColumn(String yColumnName);

		void onRemoveYColumn(String yColumnName);
	}
}
