package org.sagebionetworks.web.client.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.presenter.TrashPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.view.bootstrap.ButtonUtils;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TrashViewImpl extends Composite implements TrashView {

	public interface TrashViewImplUiBinder extends UiBinder<Widget, TrashViewImpl> {
	}

	private static final int HEADER_CHECKBOX_IDX = 0;
	private static final String RESTORE_BUTTON_TEXT = "Restore";

	private static final String DELETE_SELECTED_CONFIRM_TITLE = "Delete selected items from your Trash?";
	private static final String DELETE_SELECTED_CONFIRM_MESSAGE = "You can't undo this action.";
	private static final String EMPTY_TRASH_CONFIRM_TITLE = "Erase all items in your Trash?";
	private static final String EMPTY_TRASH_CONFIRM_MESSAGE = "You can't undo this action.";

	private static final int MAX_PAGES_IN_PAGINATION = 10;
	@UiField
	FlowPanel trashTableAndPaginationPanel;
	@UiField
	org.gwtbootstrap3.client.ui.Button deleteSelectedButton;
	@UiField
	SimplePanel paginationPanel;
	@UiField
	Table trashTable;
	@UiField
	CheckBox selectAllCheckBox;
	@UiField
	TBody tableBody;
	@UiField
	SimplePanel emptyTrashDisplay;
	@UiField
	SimplePanel synAlertPanel;

	private Presenter presenter;
	private Header headerWidget;
	private SynapseJSNIUtils synapseJsniUtils;
	private Map<TrashedEntity, Integer> trash2Row;
	private Set<TrashedEntity> selectedTrash;
	private Set<CheckBox> checkBoxes;

	@Inject
	public TrashViewImpl(TrashViewImplUiBinder binder, Header headerWidget, Footer footerWidget, SynapseJSNIUtils synapseJsniUtils) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.synapseJsniUtils = synapseJsniUtils;
		headerWidget.configure();
		trash2Row = new HashMap<TrashedEntity, Integer>();
		selectedTrash = new HashSet<TrashedEntity>();
		checkBoxes = new HashSet<CheckBox>();

		// Set up delete selected button.
		ButtonUtils.setEnabledAndType(false, deleteSelectedButton, ButtonType.DANGER);
		deleteSelectedButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.showConfirmDialog(DELETE_SELECTED_CONFIRM_TITLE, DELETE_SELECTED_CONFIRM_MESSAGE, new Callback() {

					@Override
					public void invoke() {
						presenter.purgeEntities(selectedTrash);
					}

				});
			}
		});

		// Set up selectAllCheckBox.
		selectAllCheckBox.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				boolean checked = ((CheckBox) event.getSource()).getValue();
				if (checked) {
					// Select all of the trash entities.
					for (CheckBox checkBox : checkBoxes) {
						checkBox.setValue(true);
					}
					selectedTrash.addAll(trash2Row.keySet());
					ButtonUtils.setEnabledAndType(true, deleteSelectedButton, ButtonType.DANGER);
				} else {
					// Deselect all of the trash.
					for (CheckBox checkBox : checkBoxes) {
						checkBox.setValue(false);
					}
					selectedTrash.removeAll(trash2Row.keySet());
					ButtonUtils.setEnabledAndType(false, deleteSelectedButton, ButtonType.DANGER);
				}
			}

		});
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
		headerWidget.refresh();
		clear();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void configure(List<TrashedEntity> trashedEntities) {
		// Show table and pagination panel. Hide empty trash display.
		trashTableAndPaginationPanel.setVisible(true);
		emptyTrashDisplay.setVisible(false);

		// Disable delete selected button and uncheck select all checkbox.
		ButtonUtils.setEnabledAndType(false, deleteSelectedButton, ButtonType.DANGER);
		selectAllCheckBox.setValue(false);

		showButtons();
		ButtonUtils.setEnabledAndType(false, deleteSelectedButton, ButtonType.DANGER);
		for (TrashedEntity trashedEntity : trashedEntities) {
			displayTrashedEntity(trashedEntity);
		}
		int start = presenter.getOffset();
		String pageTitleStartNumber = start > 0 ? " (from item " + (start + 1) + ")" : "";
		synapseJsniUtils.setPageTitle("Trash Can" + pageTitleStartNumber);
		createPagination();
	}

	@Override
	public void displayEmptyTrash() {
		hideButtons();
		trashTableAndPaginationPanel.setVisible(false);
		emptyTrashDisplay.setVisible(true);
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

	@Override
	public void displayFailureMessage(String title, String message) {
		// Hide buttons.
		hideButtons();

		// Make error display.
		FlowPanel errorPanel = new FlowPanel();
		errorPanel.setStyleName("panel panel-danger");

		// Make display heading.
		SimplePanel heading = new SimplePanel();
		heading.setStyleName("panel-heading");
		heading.setWidget(new Label(title));

		// Make display body.
		FlowPanel body = new FlowPanel();
		body.setStyleName("panel-body");
		body.add(new Label(message));

		// Make reload button.
		Button reloadButton = new Button("Reload Page");
		reloadButton.setStyleName("btn btn-success right margin-top-10");
		reloadButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Window.Location.reload();
			}

		});

		// Put error display parts together.
		body.add(reloadButton);
		errorPanel.add(heading);
		errorPanel.add(body);

		// Clear and show error panel.
		trashTableAndPaginationPanel.clear();
		trashTableAndPaginationPanel.add(errorPanel);
	}

	@Override
	public void clear() {
		tableBody.clear();
		selectedTrash.clear();
	}

	@Override
	public void refreshTable() {
		clear();
		presenter.getTrash(presenter.getOffset());
	}

	@Override
	public void displayTrashedEntity(final TrashedEntity trashedEntity) {
		if (trashedEntity == null)
			throw new IllegalArgumentException("Cannot display null entity.");

		// Get current row.
		int row = tableBody.getWidgetCount();

		// Make checkbox.
		final CheckBox cb = new CheckBox();
		cb.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				boolean checked = cb.getValue();
				if (checked) {
					if (selectedTrash.isEmpty()) {
						ButtonUtils.setEnabledAndType(true, deleteSelectedButton, ButtonType.DANGER);
					}
					selectedTrash.add(trashedEntity);
				} else {
					selectedTrash.remove(trashedEntity);
					selectAllCheckBox.setValue(false);
					if (selectedTrash.isEmpty()) {
						ButtonUtils.setEnabledAndType(false, deleteSelectedButton, ButtonType.DANGER);
					}
				}
			}

		});

		// Make restore button.
		Button restoreButton = DisplayUtils.createButton(RESTORE_BUTTON_TEXT);
		restoreButton.addStyleName("btn-block");
		restoreButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.restoreEntity(trashedEntity);
			}

		});

		// Update fields.
		trash2Row.put(trashedEntity, row);
		checkBoxes.add(cb);

		TableRow newRow = new TableRow();

		// Add checkbox to row.
		TableData data = new TableData();
		data.add(cb);
		newRow.add(data);

		// Add name to row.
		data = new TableData();
		data.add(new Label(trashedEntity.getEntityName()));
		newRow.add(data);

		// Add deleted on to row.
		data = new TableData();
		data.add(new Label(trashedEntity.getDeletedOn().toString()));
		newRow.add(data);

		// Add restore button to row.
		data = new TableData();
		data.add(restoreButton);
		newRow.add(data);

		tableBody.add(newRow);
	}

	@Override
	public void removeDisplayTrashedEntity(TrashedEntity trashedEntity) {
		if (trashedEntity == null)
			throw new IllegalArgumentException("Cannot un-display null entity.");

		if (trash2Row.containsKey(trashedEntity)) {
			int removeRowIndex = trash2Row.get(trashedEntity);
			TableRow removeRow = (TableRow) trashTable.getWidget(removeRowIndex);
			TableData checkBoxData = (TableData) removeRow.getWidget(HEADER_CHECKBOX_IDX);
			CheckBox checkBox = (CheckBox) checkBoxData.getWidget(0); // Only child.

			// Update fields.
			checkBoxes.remove(checkBox);
			trash2Row.remove(trashedEntity);
			decrementBeyondRemovedRow(removeRowIndex);
			selectedTrash.remove(trashedEntity);

			if (selectedTrash.isEmpty())
				ButtonUtils.setEnabledAndType(false, deleteSelectedButton, ButtonType.DANGER);

			// Remove that row from the table
			trashTable.remove(removeRowIndex);
		}
	}


	/*
	 * Private Methods
	 */

	/**
	 * Decrements the associated row in trash2Row for all entities with rows greater than removedRow.
	 * 
	 * @param removedRow The any row with index greater than removed row will be decremented.
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
		if (entries != null) {
			for (PaginationEntry pe : entries) {
				if (pe.isCurrent())
					ul.add(createPaginationAnchor(pe.getLabel(), pe.getStart()), "active");
				else
					ul.add(createPaginationAnchor(pe.getLabel(), pe.getStart()));
			}
		}

		fp.add(ul);
		paginationPanel.clear();
		if (entries.size() > 1)
			paginationPanel.setWidget(fp);
	}

	private Anchor createPaginationAnchor(String anchorName, final int newStart) {
		Anchor a = new Anchor();
		a.setHTML(SafeHtmlUtils.htmlEscape(anchorName));
		a.setHref(DisplayUtils.getTrashHistoryToken("", newStart));
		return a;
	}

	private void hideButtons() {
		deleteSelectedButton.setVisible(false);
	}

	private void showButtons() {
		deleteSelectedButton.setVisible(true);
	}

	@Override
	public void setSynAlertWidget(Widget synAlert) {
		this.synAlertPanel.setWidget(synAlert);
	}
}
