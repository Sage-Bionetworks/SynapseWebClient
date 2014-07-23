package org.sagebionetworks.web.client.widget.table.v2;

import java.util.ArrayList;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDown;
import org.gwtbootstrap3.client.ui.PanelFooter;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.sagebionetworks.repo.model.table.ColumnModel;
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
	PanelHeader panelHeader;
	@UiField
	Table table;
	@UiField
	TBody tableBody;
	@UiField
	PanelFooter panelFooter;
	@UiField
	Button addColumnButton;
	@UiField
	Button upButton;
	@UiField
	Button downButton;
	@UiField
	Button deleteButton;
	@UiField
	DropDown selectDropDown;
	ViewType viewType;
	
	ArrayList<ColumnModelView> columnData;
	
	Presenter presenter;
	
	
	@Inject
	public ColumnModelsViewImpl(final Binder uiBinder){
		initWidget(uiBinder.createAndBindUi(this));
		columnData = new ArrayList<ColumnModelView>();
	}

	@Override
	public void setPresenter(Presenter setPresenter) {
		this.presenter = setPresenter;
		addColumnButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.addNewColumn();
			}
		});
	}

	@Override
	public void addColumn(ColumnModel model, boolean isEditable) {
		// Create a row
		ColumnModelView cf= new ColumnModelView(this.viewType, model, isEditable);
		tableBody.add(cf);
		columnData.add(cf);
		tableBody.setVisible(true);
	}


	@Override
	public void configure(ViewType type, boolean isEditable) {
		this.viewType = type;
		if(ViewType.VIEWER.equals(type)){
			panelHeader.setVisible(false);
			addColumnButton.setEnabled(false);
			selectDropDown.setVisible(false);
		}else{
			selectDropDown.setVisible(true);
			panelHeader.setVisible(true);
			addColumnButton.setEnabled(true);
		}
	}
}
