package org.sagebionetworks.web.client.widget.table.v2;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonToolBar;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
	
	private int idSequence;
	List<ColumnModelTableRow> tableRows;
	ViewType viewType;
	Presenter presenter;
	
	
	@Inject
	public ColumnModelsViewImpl(final Binder uiBinder){
		initWidget(uiBinder.createAndBindUi(this));
		tableRows = new ArrayList<ColumnModelTableRow>();
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
	public void addColumn(ColumnModel model, boolean isEditable) {
		// Create a row
		final String columnId = "c"+idSequence++;
		ColumnModelTableRow cf= new ColumnModelTableRow(columnId, this.viewType, model, isEditable);
		cf.addSelectionListener(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				presenter.columnSelectionChanged(columnId, event.getValue());
				
			}
		});
		tableRows.add(cf);
		tableBody.add(cf);
		tableBody.setVisible(true);
	}


	@Override
	public void configure(ViewType type, boolean isEditable) {
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
			tableBody.addAttachHandler(new Handler() {
				@Override
				public void onAttachOrDetach(AttachEvent event) {
					// This should enable jqueryui to allow reordering with a mouse drag.
					enableDragging(tbodyId);
				}
			});
		}
	}
	
	private static native void enableDragging(String targetId)/*-{
		$wnd.jQuery('#' + targetId).sortable();
		$wnd.jQuery('#' + targetId).disableSelection();
	}-*/;

	@Override
	public List<ColumnModel> getCurrentColumnModels() {
		List<ColumnModel> list = new ArrayList<ColumnModel>(tableRows.size());
		// Get the column from each view
		for(ColumnModelTableRow cmtr: tableRows){
			list.add(cmtr.getColumnModel());
		}
		return list;
	}

	@Override
	public void showError(String message) {
		// TODO Auto-generated method stub
		
	}
	
}
