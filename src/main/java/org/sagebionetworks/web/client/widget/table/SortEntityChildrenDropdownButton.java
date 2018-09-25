package org.sagebionetworks.web.client.widget.table;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class SortEntityChildrenDropdownButton implements IsWidget {
	
	public interface Binder extends UiBinder<Widget, SortEntityChildrenDropdownButton> {}
	
	@UiField
	AnchorListItem createdOnDesc;
	@UiField
	AnchorListItem createdOnAsc;
	@UiField
	AnchorListItem nameAsc;
	@UiField
	AnchorListItem nameDesc;
	@UiField
	Button sortButton;
	
	IsWidget widget;
	SortEntityChildrenDropdownButtonListener presenter;
	
	@Inject
	public SortEntityChildrenDropdownButton(Binder binder) {
		widget = binder.createAndBindUi(this);
		createdOnDesc.addClickHandler(event -> {
			sortButton.setText(createdOnDesc.getText());
			presenter.onSort(SortBy.CREATED_ON, Direction.DESC);
		});
		createdOnAsc.addClickHandler(event -> {
			sortButton.setText(createdOnAsc.getText());
			presenter.onSort(SortBy.CREATED_ON, Direction.ASC);
		});
		nameDesc.addClickHandler(event -> {
			sortButton.setText(nameDesc.getText());
			presenter.onSort(SortBy.NAME, Direction.DESC);
		});

		nameAsc.addClickHandler(event -> {
			sortButton.setText(nameAsc.getText());
			presenter.onSort(SortBy.NAME, Direction.ASC);
		});
	}
	public void setSortUI(SortBy sortBy, Direction dir) {
		String sortButtonText;
		if (SortBy.CREATED_ON.equals(sortBy)) {
			sortButtonText = Direction.ASC.equals(dir) ? createdOnAsc.getText() : createdOnDesc.getText();
		} else {
			sortButtonText = Direction.ASC.equals(dir) ? nameAsc.getText() : nameDesc.getText();
		}
		sortButton.setText(sortButtonText);	
	}
	
	public void setListener(final SortEntityChildrenDropdownButtonListener presenter) {
		this.presenter = presenter;
	}
	
	public Widget asWidget() {
		return widget.asWidget();
	}
}
