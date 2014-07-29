package org.sagebionetworks.web.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

import org.sagebionetworks.web.client.presenter.TrashPresenter;
import org.sagebionetworks.web.client.utils.BootstrapTable;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.footer.Footer;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.gwt.user.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

public class TrashViewImpl extends Composite implements TrashView {
	
	public interface TrashViewImplUiBinder extends UiBinder<Widget, TrashViewImpl> {}
	
	private static final String HEADER_CHECKBOX = "";
	private static final String HEADER_NAME = "Name";
	private static final String HEADER_DELETED_ON = "Deleted On";
	private static final String HEADER_RESTORE = "";
	
	private static final int HEADER_CHECKBOX_IDX = 0;
	private static final int HEADER_NAME_IDX = 1;
	private static final int HEADER_DELETED_ON_IDX = 2;
	private static final int HEADER_RESTORE_IDX = 3;
	
	private static final String RESTORE_BUTTON_TEXT = "Restore";
	
	private static final String EMPTY_TRASH_TITLE = "Erase all items in your Trash?";
	private static final String EMPTY_TRASH_MESSAGE = "You can't undo this action.";
	private static final String TRASH_IS_EMPTY_DISPLAY = "Your trash is empty.";
	
	private static final int MAX_PAGES_IN_PAGINATION = 10;
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	Button deleteAllButton;
	@UiField
	SimplePanel trashListPanel;
	@UiField
	Button deleteSelectedButton;
	@UiField
	SimplePanel paginationPanel;
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	private SageImageBundle sageImageBundle;
	private SynapseJSNIUtils synapseJsniUtils;
	BootstrapTable trashList;
	Map<TrashedEntity, Integer> trash2Row;
	Set<TrashedEntity> selectedTrash;
	Set<CheckBox> checkBoxes;
	boolean selectAllChecked;		// TODO: hacky and wrong. How to get this info from checkbox "Event"?
									// Psych I'm not gonna worry about it since I'm redoing the whole table.
	@Inject
	public TrashViewImpl(TrashViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget,
			SageImageBundle sageImageBundle, SynapseJSNIUtils synapseJsniUtils) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.sageImageBundle = sageImageBundle;
		this.synapseJsniUtils = synapseJsniUtils;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		
		trash2Row = new HashMap<TrashedEntity, Integer>();
		selectedTrash = new HashSet<TrashedEntity>();
		checkBoxes = new HashSet<CheckBox>();
		
		// Set up the delete all button.
		deleteAllButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.showConfirmDialog(EMPTY_TRASH_TITLE, EMPTY_TRASH_MESSAGE, new Callback() {
					
					@Override
					public void invoke() {
						presenter.purgeAll();
					}
					
				});
			}
		});
		
		// Set up delete selected button.
		deleteSelectedButton.setEnabled(false);
		deleteSelectedButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.purgeEntities(selectedTrash);
			}
		});
		
		// Add the trashList table to its panel so it is displayed.
		trashList = initTable();
		trashListPanel.setWidget(trashList);
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();
		clear();
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void configure(List<TrashedEntity> trashedEntities) {
		for (TrashedEntity trashedEntity : trashedEntities) {
			displayTrashedEntity(trashedEntity);
		}
		int start = presenter.getOffset();
		String pageTitleStartNumber = start > 0 ? " (from item " + (start+1) + ")" : ""; 
		synapseJsniUtils.setPageTitle("Trash Can" + pageTitleStartNumber);
		createPagination();
	}
	
	@Override
	public void displayEmptyTrash() {
		trashListPanel.setWidget(new Label(TRASH_IS_EMPTY_DISPLAY));
	}
	
	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void alertErrorMessage(String message) {
		Window.alert(message + " Click \"OK\" to reload page.");
	}

	@Override
	public void clear() {
		trashList.removeAllRows();
		selectedTrash.clear();
	}
	
	@Override
	public void refreshTable() {
		clear();
		presenter.getTrash(presenter.getOffset());
	}
	
	@Override
	public void displayTrashedEntity(final TrashedEntity trashedEntity) {
		if (trashedEntity == null) throw new IllegalArgumentException("Cannot display null entity.");
		
		// Get current row.
		int row = trashList.getRowCount();
		
		// Make checkbox.
		final CheckBox cb = new CheckBox();
		cb.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				boolean checked = cb.getValue();
				if (checked) {
					if (selectedTrash.isEmpty())
						deleteSelectedButton.setEnabled(true);
					selectedTrash.add(trashedEntity);
				} else {
					selectedTrash.remove(trashedEntity);
					if (selectedTrash.isEmpty())
						deleteSelectedButton.setEnabled(false);
				}
			}
			
		});
		
		// Make restore button.
		Button restoreButton = DisplayUtils.createButton(RESTORE_BUTTON_TEXT);
		restoreButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.restoreEntity(trashedEntity);
			}
			
		});
		
		// Update fields.
		trash2Row.put(trashedEntity, row);
		checkBoxes.add(cb);
		
		// Add the row elements.
		trashList.setWidget(row, HEADER_CHECKBOX_IDX, cb);
		trashList.setText(row, HEADER_NAME_IDX, trashedEntity.getEntityName());
		trashList.setText(row, HEADER_DELETED_ON_IDX, trashedEntity.getDeletedOn().toString());
		trashList.setWidget(row, HEADER_RESTORE_IDX, restoreButton);
	}
	
	@Override
	public void removeDisplayTrashedEntity(TrashedEntity trashedEntity) {
		if (trashedEntity == null) throw new IllegalArgumentException("Cannot un-display null entity.");
		
		if (trash2Row.containsKey(trashedEntity)) {
			int removeRow = trash2Row.get(trashedEntity);
			
			// Update fields.
			trash2Row.remove(trashedEntity);
			checkBoxes.remove(trashList.getWidget(removeRow, HEADER_CHECKBOX_IDX));
			decrementBeyondRemovedRow(removeRow);
			
			// Remove row from trashList.
			trashList.removeRow(removeRow);
		}
	}


	/*
	 * Private Methods
	 */
	
	/**
	 * Initializes the trash list table to have only the header row.
	 * @return Table with only the header row.
	 */
	private BootstrapTable initTable() {
		
		trashList = new BootstrapTable();
		trashList.addStyleName("trashList-striped trashList-bordered trashList-condensed");
		
		// Set up table header.
		List<String> headerRow = new ArrayList<String>();
		headerRow.add(HEADER_CHECKBOX_IDX, HEADER_CHECKBOX);	// put in string of html. <span id="selectAllCheckBox" />
		headerRow.add(HEADER_NAME_IDX, HEADER_NAME);
		headerRow.add(HEADER_DELETED_ON_IDX, HEADER_DELETED_ON);
		headerRow.add(HEADER_RESTORE_IDX, HEADER_RESTORE);
		List<List<String>> trashListHeaderRows = new ArrayList<List<String>>();
		trashListHeaderRows.add(headerRow);
		trashList.setHeaders(trashListHeaderRows);	

		// Set up select all checkbox.
		selectAllChecked = false;
		CheckBox selectAllCheckBox = new CheckBox();
		Element selectAllElement = selectAllCheckBox.getElement();
		
		// Note: This line is very unstable. Temporarily ignoring, as the table is going to be entirely reimplemented.
		DOM.appendChild(DOM.getChild(DOM.getChild(DOM.getChild(trashList.getElement(), 0), 0), 0), selectAllElement);
		DOM.sinkEvents(selectAllElement, Event.ONCLICK);
		DOM.setEventListener(selectAllElement, new EventListener() {
		    @Override
		    public void onBrowserEvent(Event event) {
		        if (!selectAllChecked) {
		        	// Select all of the trash entities.
		        	for (CheckBox checkBox : checkBoxes) {
		        		checkBox.setChecked(true);
		        	}
		        	deleteSelectedButton.setEnabled(true);
		        } else {
		        	// Deselect all of the trash.
		        	for (CheckBox checkBox : checkBoxes) {
		        		checkBox.setChecked(false);
		        	}
		        	deleteSelectedButton.setEnabled(false);
		        }
		        selectAllChecked = !selectAllChecked;
		    }
		});
		
		trashList.setWidth("100%");		
		trashList.getColumnFormatter().setWidth(HEADER_CHECKBOX_IDX, "10%");
		trashList.getColumnFormatter().setWidth(HEADER_NAME_IDX, "43%");
		trashList.getColumnFormatter().setWidth(HEADER_DELETED_ON_IDX, "33%");
		trashList.getColumnFormatter().setWidth(HEADER_RESTORE_IDX, "14%");

		return trashList;
	}
	
	/**
	 * Decrements the associated row in trash2Row for all entities with
	 * rows greater than removedRow.
	 * @param removedRow The any row with index greater than removed
	 * row will be decremented.
	 */
	private void decrementBeyondRemovedRow(int removedRow) {
		for (TrashedEntity entity : trash2Row.keySet()) {
			if (trash2Row.get(entity) > removedRow) {
				trash2Row.put(entity, trash2Row.get(entity) - 1);
			}
		}
	}
	
	private void createPagination() {
		FlowPanel fp = new FlowPanel();
		UnorderedListPanel ul = new UnorderedListPanel();
		ul.setStyleName("pagination pagination-lg");
		
		List<PaginationEntry> entries = presenter.getPaginationEntries(TrashPresenter.TRASH_LIMIT, MAX_PAGES_IN_PAGINATION);
		if(entries != null) {
			for(PaginationEntry pe : entries) {
				if(pe.isCurrent())
					ul.add(createPaginationAnchor(pe.getLabel(), pe.getStart()), "active");
				else
					ul.add(createPaginationAnchor(pe.getLabel(), pe.getStart()));
			}
		}
		
		fp.add(ul);
		paginationPanel.clear();
		if (entries.size() > 1)
			paginationPanel.add(fp);
	}
	
	private Anchor createPaginationAnchor(String anchorName, final int newStart) {
		Anchor a = new Anchor();
		a.setHTML(anchorName);
		a.setHref(DisplayUtils.getTrashHistoryToken("", newStart));
		return a;
	}
}
