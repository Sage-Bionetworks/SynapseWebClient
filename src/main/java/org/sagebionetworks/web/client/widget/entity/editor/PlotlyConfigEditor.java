package org.sagebionetworks.web.client.widget.entity.editor;

import static org.sagebionetworks.web.shared.WidgetConstants.BAR_MODE;
import static org.sagebionetworks.web.shared.WidgetConstants.IS_HORIZONTAL;
import static org.sagebionetworks.web.shared.WidgetConstants.SHOW_LEGEND;
import static org.sagebionetworks.web.shared.WidgetConstants.TABLE_QUERY_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.TITLE;
import static org.sagebionetworks.web.shared.WidgetConstants.TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.X_AXIS_TITLE;
import static org.sagebionetworks.web.shared.WidgetConstants.X_AXIS_TYPE;
import static org.sagebionetworks.web.shared.WidgetConstants.Y_AXIS_TITLE;
import static org.sagebionetworks.web.shared.WidgetConstants.Y_AXIS_TYPE;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.plotly.AxisType;
import org.sagebionetworks.web.client.plotly.BarMode;
import org.sagebionetworks.web.client.plotly.GraphType;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PlotlyConfigEditor implements PlotlyConfigView.Presenter, WidgetEditorPresenter {
	private Map<String, String> descriptor;
	private PlotlyConfigView view;

	private static final String QUERY_FIRST_COLUMN_REG_EX = "select\\s+[\"']?([a-zA-Z0-9_ ]+)[\"']?[,]{1}";
	private static final RegExp X_COLUMN_PATTERN = RegExp.compile(QUERY_FIRST_COLUMN_REG_EX, "i");
	private static final String QUERY_OTHER_COLUMNS_REG_EX = "select\\s+[\"']?([a-zA-Z0-9_ ]+)[\"']?[,]{1}(.+)from";
	private static final RegExp Y_COLUMNS_PATTERN = RegExp.compile(QUERY_OTHER_COLUMNS_REG_EX, "i");

	EntityFinder finder;
	String xColumnName;
	List<String> yColumnsList = new ArrayList<>();
	SynapseAlert synAlert;
	String sql;
	List<String> allAvailableColumnNames;
	boolean isAdvancedVisible;
	String tableSynapseId;
	public static final String SHOW = "Show Advanced";
	public static final String HIDE = "Hide Advanced";
	Button showHideAdvancedButton;
	SynapseJavascriptClient jsClient;

	@Inject
	public PlotlyConfigEditor(final PlotlyConfigView view, EntityFinder finder, SynapseAlert synAlert, Button showHideAdvancedButton, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.finder = finder;
		this.synAlert = synAlert;
		this.jsClient = jsClient;
		this.showHideAdvancedButton = showHideAdvancedButton;
		view.setSynAlert(synAlert);
		view.setPresenter(this);
		view.setShowHideButton(showHideAdvancedButton);

		isAdvancedVisible = false;
		showHideAdvancedButton.setSize(ButtonSize.EXTRA_SMALL);
		showHideAdvancedButton.setText(SHOW);
		showHideAdvancedButton.setIcon(IconType.TOGGLE_RIGHT);
		view.setAdvancedUIVisible(false);
		showHideAdvancedButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// toggle
				setAdvancedModeVisible(!isAdvancedVisible);
			}
		});
	}

	public void setAdvancedModeVisible(boolean visible) {
		isAdvancedVisible = visible;
		String buttonText = isAdvancedVisible ? HIDE : SHOW;
		showHideAdvancedButton.setText(buttonText);
		showHideAdvancedButton.setIcon(isAdvancedVisible ? IconType.TOGGLE_DOWN : IconType.TOGGLE_RIGHT);
		view.setAdvancedUIVisible(isAdvancedVisible);
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		yColumnsList.clear();
		descriptor = widgetDescriptor;
		if (descriptor.containsKey(TABLE_QUERY_KEY)) {
			sql = descriptor.get(TABLE_QUERY_KEY);
			setTableId(QueryBundleUtils.getTableIdFromSql(sql));
			String advancedClause = getAdvancedClauseFromQuery(sql);
			setAdvancedModeVisible(DisplayUtils.isDefined(advancedClause));
			view.setAdvancedClause(advancedClause);
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
			GraphType graphType = GraphType.valueOf(descriptor.get(TYPE).toUpperCase());
			view.setGraphType(graphType);
		}

		if (descriptor.containsKey(BAR_MODE)) {
			BarMode barMode = BarMode.valueOf(descriptor.get(BAR_MODE).toUpperCase());
			view.setBarMode(barMode);
		}
		if (descriptor.containsKey(SHOW_LEGEND)) {
			view.setShowLegend(Boolean.valueOf(descriptor.get(SHOW_LEGEND)));
		} else {
			view.setShowLegend(true);
		}

		if (descriptor.containsKey(IS_HORIZONTAL)) {
			view.setBarOrientationHorizontal(Boolean.valueOf(descriptor.get(IS_HORIZONTAL)));
		}
		if (descriptor.containsKey(X_AXIS_TYPE)) {
			AxisType axisType = AxisType.valueOf(descriptor.get(X_AXIS_TYPE).toUpperCase());
			view.setXAxisType(axisType);
		}
		if (descriptor.containsKey(Y_AXIS_TYPE)) {
			AxisType axisType = AxisType.valueOf(descriptor.get(Y_AXIS_TYPE).toUpperCase());
			view.setYAxisType(axisType);
		}
	}

	/**
	 * Get the x column from a query string
	 * 
	 * @param query
	 * @return
	 */
	public static String getXColumnFromSql(String query) {
		if (query == null) {
			return null;
		}
		MatchResult matcher = X_COLUMN_PATTERN.exec(query);
		if (matcher != null) {
			if (matcher.getGroupCount() > 0) {
				return unquote(matcher.getGroup(1));
			}
		}
		return null;
	}

	/**
	 * Get the y columns from a query string
	 * 
	 * @param query
	 * @return
	 */
	public static String[] getYColumnsFromSql(String query) {
		if (query == null) {
			return null;
		}
		MatchResult matcher = Y_COLUMNS_PATTERN.exec(query);
		if (matcher != null) {
			if (matcher.getGroupCount() > 1) {
				String[] yColumns = matcher.getGroup(2).split(",");
				// clean up result (in place)
				if (yColumns != null && yColumns.length > 0) {
					for (int i = 0; i < yColumns.length; i++) {
						yColumns[i] = unquote(yColumns[i]);
					}
				}
				return yColumns;
			}
		}
		return null;
	}

	public static String getAdvancedClauseFromQuery(String query) {
		if (query == null) {
			return null;
		}
		String lowercaseQuery = query.toLowerCase();
		int whereIndex = lowercaseQuery.indexOf("where");
		if (whereIndex > -1) {
			return query.substring(whereIndex);
		}
		int groupByIndex = lowercaseQuery.indexOf("group by");
		if (groupByIndex > -1) {
			return query.substring(groupByIndex);
		}
		return null;
	}

	public static String unquote(String s) {
		return s.replace('\'', ' ').replace('\"', ' ').trim();
	}

	public void clearState() {}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		if (yColumnsList.isEmpty()) {
			throw new IllegalArgumentException("Please select at least one y data column.");
		}
		if (!DisplayUtils.isDefined(xColumnName)) {
			throw new IllegalArgumentException("Please define the x data column.");
		}

		// update widget descriptor from the view
		descriptor.clear();
		String sql = getSql();
		descriptor.put(TABLE_QUERY_KEY, sql);
		descriptor.put(TITLE, view.getTitle());
		if (DisplayUtils.isDefined(view.getXAxisLabel())) {
			descriptor.put(X_AXIS_TITLE, view.getXAxisLabel());
		}
		if (DisplayUtils.isDefined(view.getYAxisLabel())) {
			descriptor.put(Y_AXIS_TITLE, view.getYAxisLabel());
		}
		descriptor.put(TYPE, view.getGraphType().toString());
		if (GraphType.BAR.equals(view.getGraphType())) {
			descriptor.put(BAR_MODE, view.getBarMode().toString());
			descriptor.put(IS_HORIZONTAL, Boolean.toString(view.isBarOrientationHorizontal()));
		}
		descriptor.put(SHOW_LEGEND, Boolean.toString(view.isShowLegend()));
		AxisType xAxisType = view.getXAxisType();
		if (!AxisType.AUTO.equals(xAxisType)) {
			descriptor.put(X_AXIS_TYPE, xAxisType.toString());
		}
		AxisType yAxisType = view.getYAxisType();
		if (!AxisType.AUTO.equals(yAxisType)) {
			descriptor.put(Y_AXIS_TYPE, yAxisType.toString());
		}
	}

	public String getSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("select \"");
		sql.append(xColumnName);
		sql.append("\", ");
		for (Iterator iterator = yColumnsList.iterator(); iterator.hasNext();) {
			String col = (String) iterator.next();
			sql.append("\"");
			sql.append(col);
			sql.append("\"");
			if (iterator.hasNext()) {
				sql.append(", ");
			}
		}
		sql.append(" from ");
		sql.append(tableSynapseId);
		String advancedClause = view.getAdvancedClause();
		if (DisplayUtils.isDefined(advancedClause)) {
			sql.append(" ");
			sql.append(advancedClause);
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
		this.tableSynapseId = synId;
		view.setTableName("");
		jsClient.getEntity(tableSynapseId, new AsyncCallback<Entity>() {
			@Override
			public void onFailure(Throwable caught) {
				view.setTableName(caught.getMessage());
			}

			public void onSuccess(Entity result) {
				view.setTableName(result.getName());
			};
		});

		// get the columns
		jsClient.getColumnModelsForTableEntity(synId, new AsyncCallback<List<ColumnModel>>() {
			@Override
			public void onSuccess(List<ColumnModel> columnModels) {
				allAvailableColumnNames = new ArrayList<String>();
				for (ColumnModel cm : columnModels) {
					allAvailableColumnNames.add(cm.getName());
				}
				xColumnName = getXColumnFromSql(sql);
				yColumnsList.clear();
				view.clearYAxisColumns();
				String[] yColumns = getYColumnsFromSql(sql);
				if (yColumns != null) {
					for (int i = 0; i < yColumns.length; i++) {
						onAddYColumn(yColumns[i]);
					}
				}
				refreshAvailableColumnNames();
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	public void refreshAvailableColumnNames() {
		if (allAvailableColumnNames.size() > 0) {
			if (!DisplayUtils.isDefined(xColumnName) || !allAvailableColumnNames.contains(xColumnName)) {
				xColumnName = allAvailableColumnNames.get(0);
			}
		}

		List<String> availableColumnNames = new ArrayList<String>(allAvailableColumnNames);
		availableColumnNames.removeAll(yColumnsList);
		availableColumnNames.remove(xColumnName);
		view.setAvailableColumns(availableColumnNames);
		view.resetSelectedYColumn();
		view.setXAxisColumnName(xColumnName);
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
		if (DisplayUtils.isDefined(yColumnName) && allAvailableColumnNames.contains(yColumnName)) {
			// add to list
			yColumnsList.add(yColumnName);
			// add y column to view
			view.addYAxisColumn(yColumnName);
			refreshAvailableColumnNames();
			view.resetSelectedYColumn();
		}
	}

	@Override
	public void onRemoveYColumn(String yColumnName) {
		yColumnsList.remove(yColumnName);
		refreshAvailableColumnNames();
	}

	@Override
	public void onXColumnChanged() {
		xColumnName = view.getXAxisColumnName();
		refreshAvailableColumnNames();
	}
}
