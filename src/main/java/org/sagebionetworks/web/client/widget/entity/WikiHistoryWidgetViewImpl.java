package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.cell.client.ValueUpdater;

public class WikiHistoryWidgetViewImpl extends LayoutContainer implements WikiHistoryWidgetView {
	CellTable<HistoryEntry> historyTable;
	Button loadMoreHistoryButton;
	HTMLPanel inlineErrorMessagePanel;
	FlowPanel historyPanel;
	private boolean canEdit;
	private List<V2WikiHistorySnapshot> historyList;
	private List<HistoryEntry> historyEntries;
	private String currentVersion;
	WikiHistoryWidgetView.Presenter presenter;
	private ActionHandler actionHandler;
	private boolean isFirstGetHistory;
	private int offset;
	private int resultSize;
	private boolean hasReachedEndOfHistory;

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
	public void configure(boolean canEdit, ActionHandler actionHandler) {
		this.canEdit = canEdit;
		this.actionHandler = actionHandler;
		this.isFirstGetHistory = true;
		this.hasReachedEndOfHistory = false;
		this.offset = 0;
		// Reset history
		historyList = new ArrayList<V2WikiHistorySnapshot>();
		historyEntries = new ArrayList<HistoryEntry>();
		presenter.configureNextPage(new Long(offset), new Long(10));
	}
	
	@Override
	public void updateHistoryList(List<V2WikiHistorySnapshot> historyResults) {
		for(int i = 0; i < historyResults.size(); i++) {
			historyList.add(historyResults.get(i));
		}
		resultSize = historyResults.size();
		if(isFirstGetHistory) {
			currentVersion = historyResults.get(0).getVersion();
		}
	}
	
	@Override
	public void buildHistoryWidget() {
		// We have all the data to create entries for the table
		createHistoryEntries();
		// Create or build upon the history widget
		if(isFirstGetHistory) {
			isFirstGetHistory = false;
			createHistoryWidget();
		} else {
			historyTable.setRowCount(historyEntries.size());
			// Set the range to display all of retrieved history so far
		    historyTable.setVisibleRange(0, historyEntries.size());
		    // Push in data
		    historyTable.setRowData(0, historyEntries);
		    // Disable button if no more history exists
		    if(hasReachedEndOfHistory) {
				hideLoadMoreButton();
			}
		}
	}
	
	private void createHistoryEntries() {
		if(historyList != null) {
			for(int i = offset; i < historyList.size(); i++) {
				V2WikiHistorySnapshot snapshot = historyList.get(i);
				// Create an entry
				String userId = snapshot.getModifiedBy();
				String modifiedByName = presenter.getNameForUserId(userId);
				if(modifiedByName == null) {
					modifiedByName = userId;
				}
				HistoryEntry entry = new HistoryEntry(snapshot.getVersion(), modifiedByName, snapshot.getModifiedOn());
				historyEntries.add(entry);
			}
			// Check if we've reached the end of the history from this recent call
			V2WikiHistorySnapshot lastSnapshot = historyList.get(historyList.size() - 1);
			if(lastSnapshot.getVersion().equals("0")) {
				hasReachedEndOfHistory = true;
			}
		}
	}
	
	private void createHistoryWidget() {
		// Remove any old table or inline error message first
		if(historyPanel != null || inlineErrorMessagePanel != null) {
			hideHistoryWidget();
		}
		
		historyTable = new CellTable<HistoryEntry>();

		loadMoreHistoryButton = DisplayUtils.createIconButton("Load more history", DisplayUtils.ButtonType.DEFAULT, null);
		loadMoreHistoryButton.setStyleName("wikiHistoryButton", true);
		loadMoreHistoryButton.setStyleName("margin-top-10", true);
		loadMoreHistoryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
		        offset += resultSize;
		        presenter.configureNextPage(new Long(offset), new Long(10));
			}
			
		});
		if(hasReachedEndOfHistory) {
			hideLoadMoreButton();
		}
		
	    // Restore if edit permissions granted
	    if(canEdit) {
		    ActionCell<HistoryEntry> restoreCell = new ActionCell<HistoryEntry>("Restore", new ActionCell.Delegate<HistoryEntry>() {
		        @Override
		        public void execute(HistoryEntry object) {
		        	showRestorationWarning(new Long(object.version));
		        }
		    }) {
		    	@Override
		        public void render(Cell.Context context, HistoryEntry value, SafeHtmlBuilder sb) {
		        	if(value.version.equals(currentVersion)) {
		        		sb.appendHtmlConstant("<span>(Current Wiki)</span>");
		        	} else {
		        		sb.appendHtmlConstant("<button>Restore</button>");
		        	}
		        }
		    };
		    
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
	    historyTable.setVisibleRange(0, historyEntries.size());
	    historyTable.setRowData(0, historyEntries);
	    
		historyPanel = new FlowPanel();
		historyPanel.add(wrapWidget(historyTable, "margin-top-5"));
		historyPanel.add(loadMoreHistoryButton);
		add(historyPanel);
		layout(true);
		
	}
	
	@Override
	public void hideHistoryWidget() {
		if(historyPanel != null) {
			historyPanel.setVisible(false);
		}
		if(inlineErrorMessagePanel != null) {
			inlineErrorMessagePanel.setVisible(false);
		}
		layout(true);
	}
	
	@Override
	public void showHistoryWidget() {
		if(historyPanel != null) {
			historyPanel.setVisible(true);
		}
	}
	
	private void hideLoadMoreButton() {
		loadMoreHistoryButton.setVisible(false);
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
		DisplayUtils.showOkCancelMessage(DisplayConstants.RESTORING_WIKI_VERSION_WARNING_TITLE, DisplayConstants.RESTORING_WIKI_VERSION_WARNING_MESSAGE, MessagePopup.WARNING, 500, okCallback, cancelCallback);
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
		// Show an inline error message
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant(message);
		inlineErrorMessagePanel = new HTMLPanel(builder.toSafeHtml());
		add(inlineErrorMessagePanel);
		layout(true);
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
