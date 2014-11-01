package org.sagebionetworks.web.client.widget.table;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.LinkedGroup;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View of a widget that lists table entities.  
 * 
 * @author jmhill
 *
 */
public class TableListWidgetViewImpl implements TableListWidgetView {
	
	public interface Binder extends UiBinder<HTMLPanel, TableListWidgetViewImpl> {}
	

	@UiField
	LinkedGroup tablesList;
	@UiField
	Button addTable;
	@UiField
	Button uploadTable;
	@UiField
	SimplePanel createTableModalPanel;
	@UiField
	SimplePanel uploadTableModalPanel;
	@UiField
	SimplePanel paginationPanel;
	
	HTMLPanel panel;
	
	@Inject
	public TableListWidgetViewImpl(Binder binder) {
		this.panel = binder.createAndBindUi(this);
	}

	@Override
	public void configure(List<EntityQueryResult> tables) {
		tablesList.clear();
		for(EntityQueryResult header: tables){
			tablesList.add(new EntityLinkedGroupItem(HeadingSize.H3, header));
		}
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.addTable.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onAddTable();
			}
		});
		this.uploadTable.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onUploadTable();
			}
		});
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
	public Widget asWidget() {
		return panel;
	}

	@Override
	public void clear() {
		tablesList.clear();
	}

	@Override
	public void setAddTableVisible(boolean visibile) {
		this.addTable.setVisible(visibile);
	}

	@Override
	public void setUploadTableVisible(boolean visibile) {
		this.uploadTable.setVisible(visibile);
	}

	@Override
	public void addCreateTableModal(IsWidget createTableModal) {
		this.createTableModalPanel.add(createTableModal);
	}

	@Override
	public void addPaginationWidget(PaginationWidget paginationWidget) {
		paginationPanel.add(paginationWidget);
	}

	@Override
	public void showPaginationVisible(boolean visible) {
		paginationPanel.setVisible(visible);
	}

	@Override
	public void addUploadTableModal(IsWidget uploadTableModalWidget) {
		this.uploadTableModalPanel.add(uploadTableModalWidget);
	}

}
