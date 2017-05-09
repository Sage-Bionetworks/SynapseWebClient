package org.sagebionetworks.web.client.widget.entity.editor;

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
	String getWhereClause();
	void setWhereClause(String v);
	String getGroupByClause();
	void setGroupByClause(String v);
	void setBarMode(BarMode barMode);
	BarMode getBarMode();
	void clearYAxisColumns();
	void addYAxisColumn(IsWidget w);
	void setTableSynId(String value);
	String getTableSynId();
	void setXAxisColumnName(String value);
	String getXAxisColumnName();
	void setYAxisColumnName(String value);
	void setAdvancedUIVisible(boolean visible);
	void setBarModeVisible(boolean visible);
	void setSynAlert(IsWidget w);
	void add(IsWidget w);
	public interface Presenter {
		void onFindTable();
		void onFindXColumn();
		void onFindYColumn();
		void onAddYColumn(String yColumnName);
	}
}
