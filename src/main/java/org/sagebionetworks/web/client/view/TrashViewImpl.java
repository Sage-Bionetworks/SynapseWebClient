package org.sagebionetworks.web.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.utils.BootstrapTable;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.footer.Footer;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

public class TrashViewImpl extends Composite implements TrashView {
	
	public interface TrashViewImplUiBinder extends UiBinder<Widget, TrashViewImpl> {}
	
	private static final String HEADER_CHECKBOX = " ";
	private static final String HEADER_NAME = "Name";
	private static final String HEADER_LOCATION = "Location";
	private static final String HEADER_RESTORE = " ";
	
	private static final int HEADER_CHECKBOX_IDX = 0;
	private static final int HEADER_NAME_IDX = 1;
	private static final int HEADER_LOCATION_IDX = 2;
	private static final int HEADER_RESTORE_IDX = 3;
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	Button deleteAllButton;
	@UiField
	SimplePanel trashListPanel;	// TODO: BootstrapTable??
	@UiField
	Button deleteSelectedButton;
	// TODO: More fields related to Messages View
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	private IconsImageBundle iconsImageBundle;
	BootstrapTable trashList;
	Map<CheckBox, TrashedEntity> checkBox2Trash;
	Map<TrashedEntity, Integer> trash2Row;
	Map<Button, TrashedEntity> restoreButton2Trash;
	Set<TrashedEntity> selectedTrash;
	
	@Inject
	public TrashViewImpl(TrashViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget, IconsImageBundle iconsImageBundle) {
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.iconsImageBundle = iconsImageBundle;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		
		// TODO: Set up UI.
		checkBox2Trash = new HashMap<CheckBox, TrashedEntity>();
		trash2Row = new HashMap<TrashedEntity, Integer>();
		restoreButton2Trash = new HashMap<Button, TrashedEntity>();
		selectedTrash = new HashSet<TrashedEntity>();
		
		// set up delete all button
		deleteAllButton.setText("Delete All");
		deleteAllButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.purgeAll();
			}
		});
		
		// set up delete selected button
		deleteSelectedButton.setText("Delete Selected");
		deleteSelectedButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO: presenter.deleteAll(). This is or testing
				String wouldDelete = "";
				for (TrashedEntity entity : selectedTrash) {
					presenter.purgeEntity(entity);
				}
			}
		});
		
		// add trash list
		trashList = initTable();
		trashListPanel.add(trashList);
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
	public void clear() {
		trashList = initTable();
		trashListPanel.setWidget(trashList);
	}
	
	@Override
	public void displayTrashedEntity(TrashedEntity trashedEntity) {
		if (trashedEntity == null) throw new IllegalArgumentException("Cannot display null entity.");
		
		// Get current row.
		int row = trashList.getRowCount();

		// Make checkbox.
		CheckBox cb = new CheckBox();
		cb.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				boolean checked = ((CheckBox) event.getSource()).getValue();
				TrashedEntity trashedEntity = checkBox2Trash.get(event.getSource());
				if (checked) {
					selectedTrash.add(trashedEntity);
				} else {
					selectedTrash.remove(trashedEntity);
				}
			}
			
		});
		
		// Make button
		Button restoreButton = DisplayUtils.createButton("Restore");
		restoreButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.restoreEntity(restoreButton2Trash.get(event.getSource()));
			}
			
		});
		
		// Update fields
		trash2Row.put(trashedEntity, row);
		checkBox2Trash.put(cb, trashedEntity);
		restoreButton2Trash.put(restoreButton, trashedEntity);
		
		// 

		trashList.setWidget(row, HEADER_CHECKBOX_IDX, cb);
		trashList.setText(row, HEADER_NAME_IDX, trashedEntity.getEntityName());
		trashList.setText(row, HEADER_LOCATION_IDX, "Parent: " + trashedEntity.getOriginalParentId());
		trashList.setWidget(row, HEADER_RESTORE_IDX, restoreButton);
	}
	
	@Override
	public void removeDisplayTrashedEntity(TrashedEntity trashedEntity) {
		if (trashedEntity == null) throw new IllegalArgumentException("Cannot un-display null entity.");
		
		if (trash2Row.containsKey(trashedEntity)) {
			int removeRow = trash2Row.get(trashedEntity);
			
			// Update fields.
			trash2Row.remove(trashedEntity);
			checkBox2Trash.remove(trashList.getWidget(removeRow, HEADER_CHECKBOX_IDX));
			restoreButton2Trash.remove(trashList.getWidget(removeRow, HEADER_RESTORE_IDX));
			decrementBeyondRemovedRow(removeRow);
			
			// Remove row from trashList.
			trashList.removeRow(removeRow);
		}
	}

	/*
	 * Private Methods
	 */
	private BootstrapTable initTable() {

		trashList = new BootstrapTable();
		trashList.addStyleName("trashList-striped trashList-bordered trashList-condensed");
		List<String> headerRow = new ArrayList<String>();
		headerRow.add(HEADER_CHECKBOX_IDX, HEADER_CHECKBOX);
		headerRow.add(HEADER_NAME_IDX, HEADER_NAME);
		headerRow.add(HEADER_LOCATION_IDX, HEADER_LOCATION);
		headerRow.add(HEADER_RESTORE_IDX, HEADER_RESTORE);

		List<List<String>> trashListHeaderRows = new ArrayList<List<String>>();
		trashListHeaderRows.add(headerRow);
		trashList.setHeaders(trashListHeaderRows);			

		trashList.setWidth("100%");		
		
		trashList.getColumnFormatter().setWidth(HEADER_CHECKBOX_IDX, "10%");
		trashList.getColumnFormatter().setWidth(HEADER_NAME_IDX, "43%");
		trashList.getColumnFormatter().setWidth(HEADER_LOCATION_IDX, "33%");
		trashList.getColumnFormatter().setWidth(HEADER_RESTORE_IDX, "14%");

		return trashList;
	}
	
	/**
	 * Decrements the associated row in trash2Row for all entities with
	 * rows greater than removedRow.
	 * @param removedRow
	 */
	private void decrementBeyondRemovedRow(int removedRow) {
		for (TrashedEntity entity : trash2Row.keySet()) {
			if (trash2Row.get(entity) > removedRow) {
				trash2Row.put(entity, trash2Row.get(entity) - 1);
			}
		}
	}
}
