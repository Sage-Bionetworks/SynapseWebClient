package org.sagebionetworks.web.client.widget.table.api;

import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.TableState;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.COLUMN_SORT_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.THead;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.table.TimedRetryWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeader;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableWidgetViewImpl implements APITableWidgetView {

	private Presenter presenter;
	PortalGinInjector ginInjector;
	Table table = new Table();
	Div div = new Div();
	THead thead = new THead();
	TBody tbody = new TBody();
	UnorderedListPanel pager = new UnorderedListPanel();
	SynapseJSNIUtils synapseJSNIUtils;
	boolean isPaging = true;
	List<SortableTableHeader> sortableTableHeaders = new ArrayList<>();
	GWTWrapper gwt;

	@Inject
	public APITableWidgetViewImpl(PortalGinInjector ginInjector, SynapseJSNIUtils synapseJSNIUtils, GWTWrapper gwt) {
		this.ginInjector = ginInjector;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.gwt = gwt;
		table.addStyleName("table table-striped table-condensed");
		table.add(thead);
		table.add(tbody);
	}

	@Override
	public void clear() {
		div.clear();
		div.add(table);
		// clear all table rows
		thead.clear();
		tbody.clear();
		table.setVisible(false);
	}

	@Override
	public void initializeTableSorter() {
		isPaging = false;
		// do not apply sorter if paging (service needs to be involved for a true column sort)
		table.addStyleName("tablesorter");
		synapseJSNIUtils.loadTableSorters();
	}

	@Override
	public void setColumnHeaders(List<APITableColumnConfig> headers) {
		thead.clear();
		sortableTableHeaders.clear();
		TableRow row = new TableRow();
		for (int i = 0; i < headers.size(); i++) {
			final int columnIndex = i;
			APITableColumnConfig columnConfig = headers.get(i);
			SortableTableHeader tableHeader = ginInjector.createSortableTableHeader();
			sortableTableHeaders.add(tableHeader);
			COLUMN_SORT_TYPE sort = columnConfig.getSort();
			SortDirection sortDir;
			if (sort == null || COLUMN_SORT_TYPE.NONE.equals(sort)) {
				sortDir = null;
			} else if (COLUMN_SORT_TYPE.ASC.equals(sort)) {
				sortDir = SortDirection.ASC;
			} else {
				sortDir = SortDirection.DESC;
			}
			tableHeader.setSortDirection(sortDir);
			String displayName = columnConfig.getDisplayColumnName();
			if (displayName == null || displayName.trim().isEmpty()) {
				displayName = columnConfig.getInputColumnNames().iterator().next();
			}
			tableHeader.configure(displayName, headerName -> {
				if (isPaging) {
					presenter.columnClicked(columnIndex);
				} else {
					refreshTableSorterHeaderUI();
				}
			});
			row.add(tableHeader);
		}
		thead.add(row);
		table.setVisible(true);
	}

	private void refreshTableSorterHeaderUI() {
		gwt.scheduleExecution(() -> {
			for (SortableTableHeader tableHeader : sortableTableHeaders) {
				String headerStyles = tableHeader.asWidget().getStyleName();
				if (headerStyles != null) {
					if (headerStyles.contains("headerSortUp")) {
						tableHeader.setSortDirection(SortDirection.DESC);
					} else if (headerStyles.contains("headerSortDown")) {
						tableHeader.setSortDirection(SortDirection.ASC);
					} else {
						tableHeader.setSortDirection(null);
					}
				}
			}
		}, 250);
	}

	@Override
	public void addRow(List<IsWidget> columnWidgets) {
		TableRow row = new TableRow();
		for (IsWidget columnWidget : columnWidgets) {
			TableData td = new TableData();
			td.add(columnWidget);
			row.add(td);
		}
		tbody.add(row);
	}

	@Override
	public void configurePager(int start, int end, int total) {
		isPaging = true;
		pager.clear();
		pager.setStyleName("pager padding-left-5-imp inline-block margin-top-5");
		Label label = new Label(start + "-" + end + " of " + total);
		label.addStyleName("inline-block margin-left-5 margin-right-5");

		Anchor prev = new Anchor();
		prev.setHTML("Previous");
		prev.addStyleName("link");
		prev.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.pageBack();
			}
		});

		Anchor next = new Anchor();
		next.setHTML("Next");
		next.addStyleName("link");
		next.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.pageForward();
			}
		});

		if (start == 1) {
			pager.add(prev, "disabled");
		} else {
			pager.add(prev);
		}
		pager.add(label, "pagerLabel");
		if (end == total) {
			pager.add(next, "disabled");
		} else {
			pager.add(next);
		}
		if (pager.getParent() == null)
			div.add(pager);
	}

	@Override
	public void showError(IsWidget synAlert) {
		clear();
		div.add(synAlert);
	}

	@Override
	public void showTableUnavailable() {
		clear();
		FlowPanel unavailableContainer = new FlowPanel();
		unavailableContainer.addStyleName("jumbotron");
		unavailableContainer.add(new HTML("<h2>" + DisplayConstants.TABLE_UNAVAILABLE + "</h2><p><strong>" + TableState.PROCESSING + "</strong>: " + DisplayConstants.TABLE_PROCESSING_DESCRIPTION + "</p>"));

		TimedRetryWidget tryAgain = new TimedRetryWidget();
		tryAgain.configure(10, new Callback() {

			@Override
			public void invoke() {
				presenter.refreshData();
			}
		});
		unavailableContainer.add(tryAgain);

		div.add(unavailableContainer);
	}

	@Override
	public Widget asWidget() {
		return div;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
