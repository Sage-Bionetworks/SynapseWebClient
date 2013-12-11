package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidgetView.Presenter;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.ButtonCellBase;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiHistoryWidgetViewImpl extends LayoutContainer implements WikiHistoryWidgetView {
	private DataGrid<HistoryEntry> historyTable;
	private boolean canEdit;
	private List<V2WikiHistorySnapshot> historyList;
	private List<HistoryEntry> historyEntries;
	WikiHistoryWidgetView.Presenter presenter;
	WikiPageWidgetView.Presenter wikiPagePresenter;

	@Inject
	public WikiHistoryWidgetViewImpl() {
		
	}
	
	private static class HistoryEntry {
		private CompositeCell<HistoryEntry> actions;
	    private final String version;
	    private final Date modifiedOn;
	    private final String user;

	    public HistoryEntry(String user, Date modifiedOn, String version, CompositeCell<HistoryEntry> actions) {
	      this.user = user;
	      this.version = version;
	      this.modifiedOn = modifiedOn;
	      this.actions = actions;
	    }
	}
	
	private static class ActionHasCell implements HasCell<HistoryEntry, HistoryEntry> {
	    private ActionCell<HistoryEntry> cell;

	    public ActionHasCell(String text, Delegate<HistoryEntry> delegate) {
	        cell = new ActionCell<HistoryEntry>(text, delegate);
	    }

	    @Override
	    public Cell<HistoryEntry> getCell() {
	        return cell;
	    }

	    @Override
	    public FieldUpdater<HistoryEntry, HistoryEntry> getFieldUpdater() {
	        return null;
	    }

		@Override
		public HistoryEntry getValue(HistoryEntry object) {
			return object;
		}
	}
	
	@Override
	public void configure(boolean canEdit,
			List<V2WikiHistorySnapshot> historyAsList, WikiPageWidgetView.Presenter wikiPageWidgetPresenter) {
		this.canEdit = canEdit;
		this.historyList = historyAsList;
		this.wikiPagePresenter = wikiPageWidgetPresenter;
		createHistoryEntries();
		createAndPopulate();
	}
	
	private void createHistoryEntries() {
		if(historyList != null) {
			historyEntries = new ArrayList<HistoryEntry>();
			for(V2WikiHistorySnapshot snapshot: historyList) {
				// Create an entry
				List<HasCell<HistoryEntry, ?>> cells = new LinkedList<HasCell<HistoryEntry, ?>>();
			    cells.add(new ActionHasCell("Preview", new Delegate<HistoryEntry>() {
					@Override
					public void execute(HistoryEntry object) {
						wikiPagePresenter.previewClicked();
					}
			    }));
			    if(canEdit) {
			    	cells.add(new ActionHasCell("Restore", new Delegate<HistoryEntry>() {
						@Override
						public void execute(HistoryEntry object) {
							wikiPagePresenter.restoreClicked();
						}
				    }));
			    }

				CompositeCell<HistoryEntry> actions = new CompositeCell<HistoryEntry>(cells);
				HistoryEntry entry = new HistoryEntry(snapshot.getModifiedBy(), snapshot.getModifiedOn(), snapshot.getVersion(), actions);
				historyEntries.add(entry);
			}
		}
	}
	
	private void createAndPopulate() {
		historyTable = new DataGrid<HistoryEntry>();
		List<HasCell<HistoryEntry, ?>> cells = new LinkedList<HasCell<HistoryEntry, ?>>();
		CompositeCell<HistoryEntry> actions = new CompositeCell<HistoryEntry>(cells);
		historyTable.addColumn(new Column<HistoryEntry, HistoryEntry>(actions) {
			@Override
			public HistoryEntry getValue(HistoryEntry object) {
				return object;
			}
	    }, "Actions");
		
	    historyTable.addColumn(new TextColumn<HistoryEntry>() {
			@Override
			public String getValue(HistoryEntry object) {
				return object.version;
			}
	    }, "Version");
	    
	    DateCell dateCell = new DateCell();
		historyTable.addColumn(new Column<HistoryEntry, Date>(dateCell) {
			@Override
		      public Date getValue(HistoryEntry object) {
		        return object.modifiedOn;
		      }
		}, "Modified On");

		historyTable.addColumn(new TextColumn<HistoryEntry>() {
			@Override
			public String getValue(HistoryEntry object) {
				return object.version;
			}
	    }, "Modified By");
		
		// Push the data into the widget.
		if(historyEntries != null) {
			historyTable.setRowData(0, historyEntries);
		}
		historyTable.setVisible(true);
		historyTable.setRowCount(historyEntries.size());
		
		System.out.println("Added table to layout");
		FlowPanel panel = new FlowPanel();
		panel.add(wrapWidget(historyTable, "margin-top-5"));
		add(panel);
		layout(true);
		
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
	public void showLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showInfo(String title, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showErrorMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPresenter(Presenter presenter, WikiPageWidgetView.Presenter wikiPageViewPresenter) {
		this.presenter = presenter;
		this.wikiPagePresenter = wikiPageViewPresenter;
	}
}
