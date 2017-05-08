package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;

public interface PlotlyConfigView extends IsWidget {

	void setPresenter(Presenter presenter);
	String getTitle();
	void setTitle(String title);
	String getGraphType();
	void setGraphType(String type);
	String getXAxisLabel();
	void setXAxisLabel(String label);
	String getYAxisLabel();
	void setYAxisLabel(String label);
	String getWhereClause();
	void setWhereClause(String v);
	String getGroupByClause();
	void setGroupByClause(String v);
	String getBarMode();
	void setBarMode(String barMode);
	
	public interface Presenter {
	}
}
