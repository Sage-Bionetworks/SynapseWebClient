package org.sagebionetworks.web.client.widget.upload;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.widget.CheckBoxState;
import org.sagebionetworks.web.client.widget.SelectionToolbar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleListViewImpl implements FileHandleListView {
	public interface Binder extends UiBinder<Widget, FileHandleListViewImpl> {
	}

	Widget widget;
	Presenter presenter;

	@UiField
	SelectionToolbar selectionToolbar;
	@UiField
	TBody fileLinksContainer;
	@UiField
	Div fileUploadContainer;

	@Inject
	public FileHandleListViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);

		selectionToolbar.hideReordering();

		selectionToolbar.setDeleteClickedCallback(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.deleteSelected();
			}
		});
		selectionToolbar.setSelectAllClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.selectAll();
			}
		});
		selectionToolbar.setSelectNoneClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.selectNone();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}


	@Override
	public void setUploadWidget(Widget widget) {
		fileUploadContainer.clear();
		fileUploadContainer.add(widget);
	}

	@Override
	public void setUploadWidgetVisible(boolean visible) {
		fileUploadContainer.setVisible(visible);
	}

	@Override
	public void addFileLink(Widget fileLinkWidget) {
		fileLinksContainer.add(fileLinkWidget);
	}

	@Override
	public void clearFileLinks() {
		fileLinksContainer.clear();
	}

	@Override
	public void setToolbarVisible(boolean visible) {
		selectionToolbar.setVisible(visible);
	}

	@Override
	public void setCanDelete(boolean canDelete) {
		selectionToolbar.setCanDelete(canDelete);
	}

	@Override
	public void setSelectionState(CheckBoxState selectionState) {
		selectionToolbar.setSelectionState(selectionState);
	}
}
