package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.web.client.plotly.GraphType;
import org.sagebionetworks.web.client.plotly.XYData;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTracker;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.asynch.UpdatingAsynchProgressHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Example markdown:
 * ${graph?query=select animal%2C %27SF Zoo%27%2C %27LA Zoo%27 from syn8223164&type=SCATTER&title=plot test&xtitle=Animal&ytitle=Count}
 * @author Jay
 *
 */
public class PlotlyWidget implements PlotlyWidgetView.Presenter, WidgetRendererPresenter {
	
	private PlotlyWidgetView view;
	private Map<String, String> descriptor;
	private SynapseAlert synAlert;
	private String sql, title, xTitle, yTitle;
	GraphType graphType;
	private AsynchronousJobTracker jobTracker;
	// Mask to get all parts of a query.
	private static final Long ALL_PARTS_MASK = new Long(255);
	Query query;
	Long currentOffset;
	QueryBundleRequest qbr;
	public static final Long LIMIT = 150L;
	Map<String, List<String>> graphData;
	String xAxisColumnName;
	
	@Inject
	public PlotlyWidget(PlotlyWidgetView view,
			SynapseAlert synAlert,
			AsynchronousJobTracker jobTracker) {
		this.view = view;
		this.synAlert = synAlert;
		this.jobTracker = jobTracker;
		view.setSynAlertWidget(synAlert.asWidget());
		view.setPresenter(this);
	}
	
	public void clear() {
		synAlert.clear();
		view.clearChart();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		clear();
		graphData = null;
		xAxisColumnName = null;
		sql = descriptor.get(WidgetConstants.TABLE_QUERY_KEY);
		title = descriptor.get(WidgetConstants.TITLE);
		xTitle = descriptor.get(WidgetConstants.X_AXIS_TITLE);
		yTitle = descriptor.get(WidgetConstants.Y_AXIS_TITLE);
		graphType = GraphType.valueOf(descriptor.get(WidgetConstants.TYPE));
		//gather the data, and then show the chart
		view.setLoadingVisible(true);
		view.setLoadingMessage("Loading...");
		currentOffset = 0L;
		query = new Query();
		query.setSql(sql);
		query.setIsConsistent(false);
		query.setLimit(LIMIT);
		
		qbr = new QueryBundleRequest();
		qbr.setPartMask(ALL_PARTS_MASK);
		qbr.setQuery(query);
		qbr.setEntityId(QueryBundleUtils.getTableId(query));
		
		getMoreResults();
	}
	
	public void getMoreResults() {
		// run the job
		query.setOffset(currentOffset);
		jobTracker.startAndTrack(AsynchType.TableQuery, qbr, AsynchronousProgressWidget.WAIT_MS, new UpdatingAsynchProgressHandler() {
			@Override
			public void onFailure(Throwable failure) {
				synAlert.handleException(failure);
			}

			@Override
			public void onCancel() {
				view.clearChart();
				view.setLoadingVisible(false);
			}

			@Override
			public void onUpdate(AsynchronousJobStatus status) {
				view.setLoadingMessage("Loaded "+currentOffset+" rows: "+status.getProgressMessage());
			}

			@Override
			public void onComplete(AsynchronousResponseBody response) {
				view.setLoadingVisible(false);
				QueryResultBundle result = (QueryResultBundle) response;
				if (graphData == null) {
					initializeGraphData(result);
				}
				List<Row> rows = result.getQueryResult().getQueryResults().getRows();
				addRowData(result.getColumnModels(), rows);
				currentOffset += LIMIT;
				if (rows.size() > 0 && currentOffset < result.getQueryCount()) {
					//get more results
					getMoreResults();
				} else {
					//we're done! send all of the results to the graph
					showGraph();
				}
			}

			@Override
			public boolean isAttached() {
				return view.isAttached();
			}
		});
	}
	
	public void initializeGraphData(QueryResultBundle result) {
		graphData = new HashMap<String, List<String>>();
		xAxisColumnName = result.getColumnModels().get(0).getName();
		for (ColumnModel column : result.getColumnModels()) {
			graphData.put(column.getName(), new ArrayList<String>());
		}
	}
	
	public void addRowData(List<ColumnModel> columns, List<Row> rows) {
		for (int i = 0; i < columns.size(); i++) {
			ColumnModel column = columns.get(i);
			List<String> colData = graphData.get(column.getName());
			for (Row row : rows) {
				colData.add(row.getValues().get(i));
			}
		}
	}
	
	public void showGraph() {
		String[] xData = getStringArray(graphData.remove(xAxisColumnName));
		XYData[] plotlyGraphData = new XYData[graphData.size()];
		int i = 0;
		for (String columnName : graphData.keySet()) {
			plotlyGraphData[i] = new XYData();
			plotlyGraphData[i].setX(xData);
			double[] yData = getDoubleArray(graphData.get(columnName));
			plotlyGraphData[i].setY(yData);
			plotlyGraphData[i].setType(graphType);
			plotlyGraphData[i].setName(columnName);
			i++;
		}
		view.showChart(title, xTitle, yTitle, plotlyGraphData);
	}
	
	public double[] getDoubleArray(List<String> l) {
		double[] d = new double[l.size()];
		for (int i = 0; i < l.size(); i++) {
			d[i] = Double.valueOf(l.get(i));
		}
		return d;
	}
	

	public String[] getStringArray(List<String> l) {
		String[] d = new String[l.size()];
		for (int i = 0; i < l.size(); i++) {
			d[i] = l.get(i);
		}
		return d;
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
