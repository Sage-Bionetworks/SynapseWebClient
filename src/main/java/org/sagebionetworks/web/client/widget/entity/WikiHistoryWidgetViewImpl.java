package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.schema.adapter.JSONEntity;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.cell.client.ValueUpdater;

public class WikiHistoryWidgetViewImpl extends LayoutContainer implements WikiHistoryWidgetView {
	@UiField(provided = true)
	CellTable<HistoryEntry> historyTable;

	@UiField(provided = true)
	SimplePager pager;

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

	    //public HistoryEntry(CompositeCell<HistoryEntry> actions, String version, String user, Date modifiedOn) {
	    public HistoryEntry(String version, String user, Date modifiedOn) {
	      this.user = user;
	      this.version = version;
	      this.modifiedOn = modifiedOn;
	      //this.actions = actions;
	    }
	}
	
	private static class ActionHasCell implements HasCell<HistoryEntry, HistoryEntry> {
	    private ActionCell<HistoryEntry> cell;
	    private String buttonText;
	    final private Delegate<HistoryEntry> handler;

	    public ActionHasCell(String text, Delegate<HistoryEntry> delegate) {
	    	this.handler = delegate;
	        cell = new ActionCell<HistoryEntry>(text, delegate) {
	        	@Override
	        	public void render(Context context, HistoryEntry value, SafeHtmlBuilder sb) {
	        		sb.appendHtmlConstant("<button>");
	    			sb.appendEscaped(buttonText);
	            	sb.appendHtmlConstant("</button>");
	        	}
	        	
	        	@Override
	        	protected void onEnterKeyDown(Cell.Context context, Element parent, HistoryEntry value, 
	        			NativeEvent event, ValueUpdater<HistoryEntry> valueUpdater) {
	        		handler.execute(value);
	        	}
	        };
	        buttonText = text;
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
	public void configure(boolean canEdit, List<JSONEntity> historyAsList, 
			WikiPageWidgetView.Presenter wikiPageWidgetPresenter) {
		this.canEdit = canEdit;
		List<V2WikiHistorySnapshot> historyAsListOfHeaders = new ArrayList<V2WikiHistorySnapshot>();
		for(int i = 0; i < historyAsList.size(); i++) {
			V2WikiHistorySnapshot snapshot = (V2WikiHistorySnapshot) historyAsList.get(i);
			historyAsListOfHeaders.add(snapshot);
		}
		this.historyList = historyAsListOfHeaders;
		this.wikiPagePresenter = wikiPageWidgetPresenter;
		createHistoryEntries();
		createAndPopulate();
	}
	
	private void createHistoryEntries() {
		if(historyList != null) {
			historyEntries = new ArrayList<HistoryEntry>();
			for(V2WikiHistorySnapshot snapshot: historyList) {
				// Create an entry
				
				final List<HasCell<HistoryEntry, ?>> cells = new LinkedList<HasCell<HistoryEntry, ?>>();
				/*
			   cells.add(new ActionHasCell("Preview", new Delegate<HistoryEntry>() {
			@Override
			public void execute(HistoryEntry object) {
				System.out.println("Executing for PREVIEW button");
				wikiPagePresenter.previewClicked(new Long(object.version));
			}
	    }));
	    if(canEdit) {
	    	cells.add(new ActionHasCell("Restore", new Delegate<HistoryEntry>() {
				@Override
				public void execute(HistoryEntry object) {
					wikiPagePresenter.restoreClicked(new Long(object.version));
				}
		    }));
	    }
			    */
				CompositeCell<HistoryEntry> actions = new CompositeCell<HistoryEntry>(cells);
				//HistoryEntry entry = new HistoryEntry(actions, snapshot.getVersion(), snapshot.getModifiedBy(), snapshot.getModifiedOn());
				HistoryEntry entry = new HistoryEntry(snapshot.getVersion(), snapshot.getModifiedBy(), snapshot.getModifiedOn());
				historyEntries.add(entry);
			}
		}
	}
	
	private void createAndPopulate() {
		// Remove any old table first
		if(historyTable != null) {
			hideHistory();
		}
		
		historyTable = new CellTable<HistoryEntry>();
		CellTable.setStyleName(historyTable.getElement(), "wikiHistoryWidget", true);
		// Create a Pager to control the table.
	    SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
	    pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
	    pager.setDisplay(historyTable);
	    pager.setStyleName("wikiHistoryWidget", true);
	    /*
	    final List<HasCell<HistoryEntry, ?>> cells = new LinkedList<HasCell<HistoryEntry, ?>>();
	    cells.add(new ActionHasCell("Preview", new Delegate<HistoryEntry>() {
			@Override
			public void execute(HistoryEntry object) {
				System.out.println("Executing for PREVIEW button");
				wikiPagePresenter.previewClicked(new Long(object.version));
			}
	    }));
	    if(canEdit) {
	    	cells.add(new ActionHasCell("Restore", new Delegate<HistoryEntry>() {
				@Override
				public void execute(HistoryEntry object) {
					wikiPagePresenter.restoreClicked(new Long(object.version));
				}
		    }));
	    }
	    CompositeCell<HistoryEntry> compositeCell = new CompositeCell<HistoryEntry>(cells) {
	    	@Override
            public void render(Context context, HistoryEntry value, SafeHtmlBuilder sb) {
                for (HasCell<HistoryEntry, ?> hasCell : cells) {
                	//render(context, value, sb, hasCell);
                	ActionCell<HistoryEntry> cell = (ActionCell<HistoryEntry>) hasCell.getCell();
                	cell.render(context, value, sb);
                }
			}
			
			@Override
            protected Element getContainerElement(Element parent) {
                return parent;
            }
	    };
	    
		Column<HistoryEntry, HistoryEntry> actionColumn = new Column<HistoryEntry, HistoryEntry>(compositeCell) {
			@Override
			public HistoryEntry getValue(HistoryEntry object) {
				return object;
			}
		};
	    */
	    
	    if(canEdit) {
		    ActionCell<HistoryEntry> restoreCell = new ActionCell<HistoryEntry>("Restore", new ActionCell.Delegate<HistoryEntry>() {
		        @Override
		        public void execute(HistoryEntry object) {
		        	wikiPagePresenter.restoreClicked(new Long(object.version));
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
		
	    ActionCell<HistoryEntry> previewCell = new ActionCell<HistoryEntry>("Preview", new ActionCell.Delegate<HistoryEntry>() {
	        @Override
	        public void execute(HistoryEntry object) {
	        	wikiPagePresenter.previewClicked(new Long(object.version));
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
		
		// Set the range to display. In this case, our visible range is smaller than
	    // the data set.
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
	public void hideHistory() {
		historyTable.removeFromParent();
		pager.removeFromParent();
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
