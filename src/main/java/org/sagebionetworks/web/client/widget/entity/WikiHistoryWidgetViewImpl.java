package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget.ActionHandler;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.cell.client.ValueUpdater;

public class WikiHistoryWidgetViewImpl extends LayoutContainer implements WikiHistoryWidgetView {
	CellTable<HistoryEntry> historyTable;
	SimplePager pager;

	private boolean canEdit;
	private List<V2WikiHistorySnapshot> historyList;
	private List<HistoryEntry> historyEntries;
	private String currentVersion;
	WikiHistoryWidgetView.Presenter presenter;
	private ActionHandler actionHandler;

	@Inject
	public WikiHistoryWidgetViewImpl() {
	}
	
	private static class HistoryEntry {
	    private final String version;
	    private final Date modifiedOn;
	    private final String user;

	    public HistoryEntry(String version, String user, Date modifiedOn) {
	      this.user = user;
	      this.version = version;
	      this.modifiedOn = modifiedOn;
	    }
	}

	@Override
	public void configure(boolean canEdit, List<JSONEntity> historyAsList, 
			ActionHandler actionHandler) {
		this.canEdit = canEdit;
		List<V2WikiHistorySnapshot> historyAsListOfHeaders = new ArrayList<V2WikiHistorySnapshot>();
		for(int i = 0; i < historyAsList.size(); i++) {
			V2WikiHistorySnapshot snapshot = (V2WikiHistorySnapshot) historyAsList.get(i);
			historyAsListOfHeaders.add(snapshot);
		}
		this.historyList = historyAsListOfHeaders;
		this.currentVersion = historyList.get(0).getVersion();
		this.actionHandler = actionHandler;
		createHistoryEntries();
		createHistoryWidget();
	}
	
	private void createHistoryEntries() {
		if(historyList != null) {
			historyEntries = new ArrayList<HistoryEntry>();
			for(V2WikiHistorySnapshot snapshot: historyList) {
				// Create an entry
				HistoryEntry entry = new HistoryEntry(snapshot.getVersion(), snapshot.getModifiedBy(), snapshot.getModifiedOn());
				historyEntries.add(entry);
			}
		}
	}
	
	private void createHistoryWidget() {
		// Remove any old table first
		if(historyTable != null) {
			removeHistoryWidget();
		}

		historyTable = new CellTable<HistoryEntry>();
		CellTable.setStyleName(historyTable.getElement(), "wikiHistoryWidget", true);
		// Create a Pager to control the table.
	    SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
	    pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
	    pager.setDisplay(historyTable);
	    pager.setStyleName("wikiHistoryWidget", true);
	    
	    // Restore if edit permissions granted
	    if(canEdit) {
		    ActionCell<HistoryEntry> restoreCell = new ActionCell<HistoryEntry>("Restore", new ActionCell.Delegate<HistoryEntry>() {
		        @Override
		        public void execute(HistoryEntry object) {
		        	showRestorationWarning(new Long(object.version));
		        }
		    });
		    
		    Column<HistoryEntry, HistoryEntry> restoreColumn = new Column<HistoryEntry, HistoryEntry>(restoreCell) {
				@Override
				public HistoryEntry getValue(HistoryEntry object) {
					return object;
				}
			};
			historyTable.addColumn(restoreColumn, "Restore");
	    }
		
	    // Preview
	    ActionCell<HistoryEntry> previewCell = new ActionCell<HistoryEntry>("Preview", new ActionCell.Delegate<HistoryEntry>() {
	        @Override
	        public void execute(HistoryEntry object) {
	        	actionHandler.previewClicked(new Long(object.version), new Long(currentVersion));
	        }
	    });
	    
	    Column<HistoryEntry, HistoryEntry> previewColumn = new Column<HistoryEntry, HistoryEntry>(previewCell) {
			@Override
			public HistoryEntry getValue(HistoryEntry object) {
				return object;
			}
		};
	    
		// Version
	    Column<HistoryEntry, String> versionColumn = new Column<HistoryEntry, String>(
	        new TextCell()) {
	      @Override
	      public String getValue(HistoryEntry object) {
	        return object.version;
	      }
	    };
	    
	    // Modified by
	    Column<HistoryEntry, String> modifiedByColumn = new Column<HistoryEntry, String>(
	        new TextCell()) {
	      @Override
	      public String getValue(HistoryEntry object) {
	        return object.user;
	      }
	    };
	    
	    // Modified on
	    Column<HistoryEntry, Date> modifiedOnColumn = new Column<HistoryEntry, Date>(
	        new DateCell()) {
	      @Override
	      public Date getValue(HistoryEntry object) {
	        return object.modifiedOn;
	      }
	    };

	    historyTable.addColumn(previewColumn, "Preview");
		historyTable.addColumn(versionColumn, "Version");
		historyTable.addColumn(modifiedByColumn, "Modified By");
		historyTable.addColumn(modifiedOnColumn, "Modified On");
		
		historyTable.setVisible(true);
		historyTable.setRowCount(historyEntries.size());
		
		// Set the range to display in the table at one time
	    historyTable.setVisibleRange(0, 3);

	    // Create a data provider.
	    AsyncDataProvider<HistoryEntry> dataProvider = new AsyncDataProvider<HistoryEntry>() {
	      @Override
	      protected void onRangeChanged(HasData<HistoryEntry> display) {
	        final Range range = display.getVisibleRange();
	        int start = range.getStart();
            int end = start + range.getLength();
            if(end > historyEntries.size()) {
            	end = historyEntries.size();
            }
	        List<HistoryEntry> dataInRange = historyEntries.subList(start, end);
            // Push the data back into the list.
            historyTable.setRowData(start, dataInRange);
	      }
	    };

	    // Connect the list to the data provider.
	    dataProvider.addDataDisplay(historyTable);
		FlowPanel panel = new FlowPanel();
		panel.add(wrapWidget(historyTable, "margin-top-5"));
		panel.add(pager);
		add(panel);
		layout(true);
		
	}
	
	@Override
	public void removeHistoryWidget() {
		historyTable.removeFromParent();
		pager.removeFromParent();
		layout(true);
	}
	
	public void showRestorationWarning(final Long wikiVersion) {
		org.sagebionetworks.web.client.utils.Callback okCallback = new org.sagebionetworks.web.client.utils.Callback() {
			@Override
			public void invoke() {
				actionHandler.restoreClicked(wikiVersion);
			}	
		};
		org.sagebionetworks.web.client.utils.Callback cancelCallback = new org.sagebionetworks.web.client.utils.Callback() {
			@Override
			public void invoke() {
			}	
		};
		DisplayUtils.showOkCancelMessage("Warning", "Are you sure you want to replace the current version with this one?", MessageBox.WARNING, 500, okCallback, cancelCallback);
	}
	
	private SimplePanel wrapWidget(Widget widget, String styleNames) {
		SimplePanel widgetWrapper = new SimplePanel();
		widgetWrapper.addStyleName(styleNames);
		widgetWrapper.add(widget);
		return widgetWrapper;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	
	
	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void clear() {
		removeAll(true);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
}
