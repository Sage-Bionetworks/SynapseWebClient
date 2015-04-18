package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.utils.COLUMN_SORT_TYPE;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableColumnManagerViewImpl implements APITableColumnManagerView {
	public interface APITableColumnManagerViewImplUiBinder extends
			UiBinder<Widget, APITableColumnManagerViewImpl> {
	}

	@UiField
	Button addColumnButton;

	// new column modal
	@UiField
	Modal newColumnModal;
	@UiField
	ListBox rendererField;
	@UiField
	ListBox sortField;
	@UiField
	TextBox inputColumnNamesField;
	@UiField
	TextBox displayColumnNamesField;
	@UiField
	Button newColumnOkButton;
	@UiField
	Button newColumnCancelButton;
	@UiField
	Div columnRenderersTable;

	private Presenter presenter;
	private boolean isEmpty;
	private Widget widget;
	IconsImageBundle iconsImageBundle;

	@Inject
	public APITableColumnManagerViewImpl(
			APITableColumnManagerViewImplUiBinder binder,
			IconsImageBundle iconsImageBundle) {
		widget = binder.createAndBindUi(this);
		this.iconsImageBundle = iconsImageBundle;
		addColumnButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				resetColumnModal();
				newColumnModal.show();
			}
		});
		KeyDownHandler newColumn = new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					newColumnOkButton.click();
				}
			}
		};
		displayColumnNamesField.addKeyDownHandler(newColumn);
		inputColumnNamesField.addKeyDownHandler(newColumn);
		newColumnOkButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.addColumnConfig(rendererField.getValue(rendererField
						.getSelectedIndex()), inputColumnNamesField.getValue(),
						displayColumnNamesField.getValue(), COLUMN_SORT_TYPE
								.valueOf(sortField.getValue(sortField
										.getSelectedIndex())));
				newColumnModal.hide();
			}
		});
		newColumnCancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				newColumnModal.hide();
			}
		});
	}

	private void resetColumnModal() {
		rendererField.setSelectedIndex(0);
		sortField.setSelectedIndex(0);
		inputColumnNamesField.setValue("");
		displayColumnNamesField.setValue("");
	}

	@Override
	public void configure(List<APITableColumnConfig> configs) {
		isEmpty = configs == null || configs.size() == 0;
		if (isEmpty) {
			addNoConfigRow();
		} else {
			populateTable(configs);
		}
	}

	private void addNoConfigRow() {
		columnRenderersTable.clear();
		columnRenderersTable.add(new HTML(DisplayConstants.TEXT_NO_COLUMNS));
	}

	private void populateTable(List<APITableColumnConfig> configs) {
		columnRenderersTable.clear();
		for (final APITableColumnConfig data : configs) {
			Div row = new Div();
			row.add(new InlineHTML(data.getDisplayColumnName()));

			AbstractImagePrototype img = AbstractImagePrototype
					.create(iconsImageBundle.deleteButtonGrey16());
			Anchor deleteColumnButton = DisplayUtils.createIconLink(img,
					new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							presenter.deleteColumnConfig(data);
						}
					});
			deleteColumnButton.addStyleName("margin-left-5");
			row.add(deleteColumnButton);
			columnRenderersTable.add(row);
		}
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
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
