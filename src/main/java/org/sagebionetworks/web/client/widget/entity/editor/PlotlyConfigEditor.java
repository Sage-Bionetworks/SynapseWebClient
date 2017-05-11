package org.sagebionetworks.web.client.widget.entity.editor;

import static org.sagebionetworks.web.shared.WidgetConstants.BAR_MODE;
import static org.sagebionetworks.web.shared.WidgetConstants.TABLE_QUERY_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.TITLE;
import static org.sagebionetworks.web.shared.WidgetConstants.TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.X_AXIS_TITLE;
import static org.sagebionetworks.web.shared.WidgetConstants.Y_AXIS_TITLE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.plotly.BarMode;
import org.sagebionetworks.web.client.plotly.GraphType;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PlotlyConfigEditor implements PlotlyConfigView.Presenter, WidgetEditorPresenter {
	private Map<String, String> descriptor;
	private PlotlyConfigView view;
	
	private static final String QUERY_FIRST_COLUMN_REG_EX = "select[\\s]+[']?(\\w+)[']?[,]";
	private static final RegExp X_COLUMN_PATTERN = RegExp.compile(QUERY_FIRST_COLUMN_REG_EX, "i");
	private static final String QUERY_OTHER_COLUMNS_REG_EX = "select[\\s]+[']?(\\w+)[']?[,]{1}(.+)from";
	private static final RegExp Y_COLUMNS_PATTERN = RegExp.compile(QUERY_OTHER_COLUMNS_REG_EX, "i");

	EntityFinder finder;
	List<String> yColumnsList = new ArrayList<>();
	SynapseAlert synAlert;
	SynapseClientAsync synapseClient;
	String sql;
	@Inject
	public PlotlyConfigEditor(PlotlyConfigView view, 
			EntityFinder finder,
			SynapseAlert synAlert,
			SynapseClientAsync synapseClient) {
		this.view = view;
		this.finder = finder;
		this.synAlert = synAlert;
		this.synapseClient = synapseClient;
		view.add(finder);
		view.setSynAlert(synAlert);
		view.setPresenter(this);
	}
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		yColumnsList.clear();
		descriptor = widgetDescriptor;
		if (descriptor.containsKey(TABLE_QUERY_KEY)) {
			sql = descriptor.get(TABLE_QUERY_KEY);
			setTableId(QueryBundleUtils.getTableIdFromSql(sql));
			String[] yColumns = getYColumnsFromSql(sql);
			if (yColumns != null) {
				for (int i = 0; i < yColumns.length; i++) {
					onAddYColumn(yColumns[i]);
				}
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
			view.setGraphType(graphType);
		}

		if (descriptor.containsKey(BAR_MODE)) {
			BarMode barMode = BarMode.valueOf(descriptor.get(BAR_MODE));
			view.setBarMode(barMode);
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
		MatchResult matcher = X_COLUMN_PATTERN.exec(query);
		if(matcher != null){
			if (matcher.getGroupCount() > 0) {
				return unquote(matcher.getGroup(1));
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
		MatchResult matcher = Y_COLUMNS_PATTERN.exec(query);
		if(matcher != null){
			if (matcher.getGroupCount() > 1) {
				return matcher.getGroup(2).split(",");
			}
		}
		return null;
	}
	
	public static String unquote(String s) {
		return s.replace('\'', ' ').trim();
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
		descriptor.clear();
		String sql = getSql();
		descriptor.put(TABLE_QUERY_KEY, sql);
		descriptor.put(TITLE, view.getTitle());
		descriptor.put(X_AXIS_TITLE, view.getXAxisLabel());
		descriptor.put(Y_AXIS_TITLE, view.getYAxisLabel());
		descriptor.put(TYPE, view.getGraphType().toString());
		if (GraphType.BAR.equals(view.getGraphType())) {
			descriptor.put(BAR_MODE, view.getBarMode().toString());	
		}
	}
	
	public String getSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("select \'");
		sql.append(view.getXAxisColumnName());
		sql.append("\',");
		for (Iterator iterator = yColumnsList.iterator(); iterator.hasNext();) {
			String col = (String) iterator.next();
			sql.append("\'");
			sql.append(col);
			sql.append("\'");
			if (iterator.hasNext()) {
				sql.append(",");
			}
		}
		sql.append(" from ");
		sql.append(view.getTableSynId());
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
	
	public void setTableId(String synId) {
		synAlert.clear();
		view.setTableSynId(synId);
		// get the columns
		synapseClient.getColumnModelsForTableEntity(synId, new AsyncCallback<List<ColumnModel>>() {
			
			@Override
			public void onSuccess(List<ColumnModel> columnModels) {
				GWT.debugger();
				List<String> columnNames = new ArrayList<String>();
				for (ColumnModel cm : columnModels) {
					columnNames.add(cm.getName());
				}
				view.setColumnNames(columnNames);
				String xColumnName = getXColumnFromSql(sql);
				if (columnNames.contains(xColumnName)) {
					view.setXAxisColumnName(xColumnName);	
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public void onFindTable() {
		finder.configure(EntityFilter.PROJECT_OR_TABLE, false, new DisplayUtils.SelectedHandler<Reference>() {
			@Override
			public void onSelected(Reference selected) {
				setTableId(selected.getTargetId());
				finder.hide();
			}
		});
		finder.show();
	}
	
	@Override
	public void onAddYColumn(String yColumnName) {
		yColumnName = unquote(yColumnName);
		if (DisplayUtils.isDefined(yColumnName)) {
			// add to list
			yColumnsList.add(yColumnName);
			//add y column to view
			view.addYAxisColumn(yColumnName);
		}
	}
	
	@Override
	public void onRemoveYColumn(String yColumnName) {
		yColumnsList.remove(yColumnName);
	}
}
