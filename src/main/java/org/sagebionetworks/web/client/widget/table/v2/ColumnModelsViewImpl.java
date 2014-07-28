package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonToolBar;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A table view of a list of ColumnModels
 * @author jmhill
 *
 */
public class ColumnModelsViewImpl extends Composite implements ColumnModelsView {
	
	public interface Binder extends UiBinder<Widget, ColumnModelsViewImpl> {	}

	@UiField
	ButtonToolBar buttonToolbar;
	@UiField
	Table table;
	@UiField
	TBody tableBody;
	@UiField
	Button addColumnButton;
	@UiField
	Button editColumnsButton;
	
	ViewType viewType;
	Presenter presenter;
	
	
	@Inject
	public ColumnModelsViewImpl(final Binder uiBinder){
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(Presenter setPresenter) {
		this.presenter = setPresenter;
		// Edit clicks
		this.editColumnsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEditColumns();
			}
		});
		// Add clicks
		this.addColumnButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.addNewColumn();
			}
		});
	}


	@Override
	public void configure(ViewType type, boolean isEditable) {
		// Clear any rows
		tableBody.clear();
		this.viewType = type;
		if(ViewType.VIEWER.equals(type)){
			editColumnsButton.setVisible(isEditable);
			addColumnButton.setVisible(false);
			buttonToolbar.setVisible(false);
		}else{
			editColumnsButton.setVisible(false);
			addColumnButton.setVisible(true);
			buttonToolbar.setVisible(true);
			final String tbodyId = "tableBodyId";
			tableBody.setId(tbodyId);
		}
	}
	
	private static native void enableDragging(String targetId)/*-{
		$wnd.jQuery('#' + targetId).sortable();
		$wnd.jQuery('#' + targetId).disableSelection();
	}-*/;

	@Override
	public void addColumn(ColumnModelTableRow row) {
		tableBody.add(row);
	}
	
}
