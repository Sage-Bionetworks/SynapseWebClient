package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.widget.SelectionToolbar;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableColumnManagerViewImpl implements APITableColumnManagerView {
	public interface APITableColumnManagerViewImplUiBinder extends
			UiBinder<Widget, APITableColumnManagerViewImpl> {
	}

	@UiField
	Button addColumnButton;

	@UiField
	Table columnRenderersContainer;
	@UiField
	SelectionToolbar selectionToolbar;
	@UiField
	Span noColumnsUI;
	@UiField
	Table columnHeaders;
	
	private Presenter presenter;
	private Widget widget;
	IconsImageBundle iconsImageBundle;
	PortalGinInjector portalGinInjector;
	
	@Inject
	public APITableColumnManagerViewImpl(
			APITableColumnManagerViewImplUiBinder binder,
			IconsImageBundle iconsImageBundle,
			PortalGinInjector portalGinInjector) {
		widget = binder.createAndBindUi(this);
		this.iconsImageBundle = iconsImageBundle;
		this.portalGinInjector = portalGinInjector;
		addColumnButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.addColumnConfig();
			}
		});
		
		selectionToolbar.setDeleteClickedCallback(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.deleteSelected();
			}
		});
		selectionToolbar.setMovedownClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onMoveDown();
			}
		});
		
		selectionToolbar.setMoveupClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onMoveUp();
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
	public void clearColumns() {
		columnRenderersContainer.clear();
	}
	
	@Override
	public void addColumn(IsWidget widget) {
		columnRenderersContainer.add(widget);
	}
	@Override
	public void setNoColumnsUIVisible(boolean visible) {
		noColumnsUI.setVisible(visible);
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
	public void showLoading() {
	}

	@Override
	public void clear() {
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void setCanDelete(boolean canDelete) {
		selectionToolbar.setCanDelete(canDelete);
	}
	
	@Override
	public void setCanMoveDown(boolean canMoveDown) {
		selectionToolbar.setCanMoveDown(canMoveDown);
	}
	@Override
	public void setCanMoveUp(boolean canMoveUp) {
		selectionToolbar.setCanMoveUp(canMoveUp);
	}
	@Override
	public void setButtonToolbarVisible(boolean visible) {
		selectionToolbar.setVisible(visible);
	}
	@Override
	public void setHeaderColumnsVisible(boolean visible) {
		columnHeaders.setVisible(visible);
	}
}
