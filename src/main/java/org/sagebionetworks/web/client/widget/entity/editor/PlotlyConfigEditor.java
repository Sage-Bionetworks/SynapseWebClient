package org.sagebionetworks.web.client.widget.entity.editor;

import static org.sagebionetworks.web.shared.WidgetConstants.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.plotly.BarMode;
import org.sagebionetworks.web.client.plotly.GraphType;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;

import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PlotlyConfigEditor implements PlotlyConfigView.Presenter, WidgetEditorPresenter {
	private Map<String, String> descriptor;
	private PlotlyConfigView view;
	
	private static final String QUERY_FIRST_COLUMN_REG_EX = "select[\\s]+(\\w+)[,]";
	private static final RegExp X_COLUMN_PATTERN = RegExp.compile(QUERY_FIRST_COLUMN_REG_EX, "gi");
	private static final String QUERY_OTHER_COLUMNS_REG_EX = "select[\\s]+(\\w+)[,]{1}(.+)from";
	private static final RegExp Y_COLUMNS_PATTERN = RegExp.compile(QUERY_OTHER_COLUMNS_REG_EX, "gi");

	EntityFinder finder;
	String selectedSynapseId;
	String xColumn;
	List<String> yColumnsList = new ArrayList<>();
	
	@Inject
	public PlotlyConfigEditor(PlotlyConfigView view, 
			EntityFinder finder) {
		this.view = view;
		this.finder = finder;
		view.setPresenter(this);
	}		
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		yColumnsList.clear();
		descriptor = widgetDescriptor;
		if (descriptor.containsKey(TABLE_QUERY_KEY)) {
			String sql = descriptor.get(TABLE_QUERY_KEY);
			xColumn = getXColumnFromSql(sql);
			String[] yColumns = getYColumnsFromSql(sql);
			for (int i = 0; i < yColumns.length; i++) {
				yColumnsList.add(yColumns[i]);
			}
		}
		if (descriptor.containsKey(TITLE)) {
			view.setTitle(descriptor.get(TITLE));
		}
		
		if (descriptor.containsKey(X_AXIS_TITLE)) {
			view.setXAxisLabel(descriptor.get(X_AXIS_TITLE));
		}
		
		if (descriptor.containsKey(Y_AXIS_TITLE)) {
			view.setYAxisLabel(descriptor.get(Y_AXIS_TITLE));
		}
						
		if (descriptor.containsKey(TYPE)) {
			GraphType graphType = GraphType.valueOf(descriptor.get(TYPE));
			view.setGraphType(graphType.toString());
		}

		if (descriptor.containsKey(BAR_MODE)) {
			BarMode barMode = BarMode.valueOf(descriptor.get(BAR_MODE));
			view.setBarMode(barMode.toString());
		}
	}
	
	/**
	 * Get the x column from a query string
	 * @param query
	 * @return
	 */
	public static String getXColumnFromSql(String query){
		if(query == null){
			return null;
		}
		MatchResult matcher = X_COLUMN_PATTERN.exec(query.toLowerCase());
		if(matcher != null){
			if (matcher.getGroupCount() > 0) {
				return matcher.getGroup(1);
			}
		}
		return null;
	}
	
	/**
	 * Get the y columns from a query string
	 * @param query
	 * @return
	 */
	public static String[] getYColumnsFromSql(String query){
		if(query == null){
			return null;
		}
		MatchResult matcher = Y_COLUMNS_PATTERN.exec(query.toLowerCase());
		if(matcher != null){
			if (matcher.getGroupCount() > 1) {
				return matcher.getGroup(2).split(",");
			}
		}
		return null;
	}

	
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void updateDescriptorFromView() {
		//update widget descriptor from the view
		String sql = getSql();
		descriptor.put(TABLE_QUERY_KEY, sql);
		descriptor.put(TITLE, view.getTitle());
		descriptor.put(X_AXIS_TITLE, view.getXAxisLabel());
		descriptor.put(Y_AXIS_TITLE, view.getYAxisLabel());
		descriptor.put(TYPE, GraphType.valueOf(view.getGraphType()).toString());
		descriptor.put(TYPE, BAR_MODE.valueOf(view.getBarMode()).toString());
	}
	
	public String getSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(xColumn);
		sql.append(",");
		for (Iterator iterator = yColumnsList.iterator(); iterator.hasNext();) {
			String col = (String) iterator.next();
			sql.append(col);
			if (iterator.hasNext()) {
				sql.append(",");
			}
		}
		sql.append(" from ");
		sql.append(selectedSynapseId);
		String whereClause = view.getWhereClause();
		if (DisplayUtils.isDefined(whereClause)) {
			sql.append(" where ");
			sql.append(whereClause);
		}
		
		String groupByClause = view.getGroupByClause();
		if (DisplayUtils.isDefined(groupByClause)) {
			sql.append(" group by ");
			sql.append(groupByClause);
		}
		
		return sql.toString();
	}
	
	@Override
	public String getTextToInsert() {
		return null;
	}
	
	@Override
	public List<String> getNewFileHandleIds() {
		return null;
	}
	@Override
	public List<String> getDeletedFileHandleIds() {
		return null;
	}
	
	/*
	 * Private Methods
	 */
}
