package org.sagebionetworks.web.client.widget.table;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.LinkedGroup;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View of a widget that lists table entites.
 * 
 * @author jmhill
 *
 */
public class TableListWidgetViewImpl implements TableListWidgetView {
	
	public interface Binder extends UiBinder<Panel, TableListWidgetViewImpl> {}
	

	@UiField
	LinkedGroup tablesList;
	@UiField
	Button addTable;
	@UiField
	Button uploadTable;
	
	private Presenter presenter;
	Panel panel;
	GlobalApplicationState globalApplicationState;
	
	@Inject
	public TableListWidgetViewImpl(Binder binder, GlobalApplicationState globalApplicationState, IconsImageBundle iconsImageBundle) {
		this.panel = binder.createAndBindUi(this);
		this.globalApplicationState = globalApplicationState;	
	}

	@Override
	public void configure(List<EntityHeader> tables) {
		tablesList.clear();
		for(EntityHeader header: tables){
			tablesList.add(new EntityLinkedGroupItem(HeadingSize.H3, header));
		}
	}

	@Override
	public void addTable(final EntityHeader table) {

	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
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

}
