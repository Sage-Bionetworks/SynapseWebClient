package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.HashSet;
import java.util.Set;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.utils.COLUMN_SORT_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableColumnConfigViewImpl implements APITableColumnConfigView {
	public interface Binder extends UiBinder<Widget, APITableColumnConfigViewImpl> {
	}

	Widget widget;
	@UiField
	TextBox columnName;
	@UiField
	TextBox displayName;
	@UiField
	ListBox sortField;
	@UiField
	ListBox rendererField;
	@UiField
	CheckBox select;
	APITableColumnConfig data;
	Callback selectionChangedCallback;

	@Inject
	public APITableColumnConfigViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void configure(APITableColumnConfig data) {
		this.data = data;
		if (data.getInputColumnNames() != null && data.getInputColumnNames().size() > 0) {
			setColumnName(data.getInputColumnNames().iterator().next());
		}
		setDisplayName(data.getDisplayColumnName());
		setSort(data.getSort());
		setRenderer(data.getRendererFriendlyName());
		select.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectionChangedCallback != null) {
					selectionChangedCallback.invoke();
				}
			}
		});
	}

	private void updateFromView() {
		data.setDisplayColumnName(displayName.getValue());
		String[] inputColNamesArray = columnName.getValue().split(",");
		Set<String> inputColumnNamesSet = new HashSet<String>();
		for (int i = 0; i < inputColNamesArray.length; i++) {
			inputColumnNamesSet.add(inputColNamesArray[i].trim());
		}
		data.setInputColumnNames(inputColumnNamesSet);
		data.setRendererFriendlyName(rendererField.getSelectedValue());
		data.setSort(COLUMN_SORT_TYPE.valueOf(sortField.getSelectedValue().toUpperCase()));
	}

	public void setColumnName(String text) {
		this.columnName.setValue(text);
	}

	public void setDisplayName(String text) {
		this.displayName.setValue(text);
	}

	public void setSort(COLUMN_SORT_TYPE sort) {
		int i = 0;
		if (sort != null) {
			for (; i < sortField.getItemCount(); i++) {
				if (sort.toString().equals(sortField.getValue(i))) {
					break;
				}
			}
		}
		sortField.setSelectedIndex(i);
	}

	public void setRenderer(String rendererName) {
		int i = 0;
		if (rendererName != null) {
			for (; i < rendererField.getItemCount(); i++) {
				if (rendererName.equals(rendererField.getValue(i))) {
					break;
				}
			}
		}
		rendererField.setSelectedIndex(i);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public boolean isSelected() {
		return select.getValue();
	}

	@Override
	public void setSelected(boolean selected) {
		select.setValue(selected);
	}

	@Override
	public APITableColumnConfig getConfig() {
		updateFromView();
		return data;
	}

	@Override
	public void setSelectionChangedCallback(Callback selectionChangedCallback) {
		this.selectionChangedCallback = selectionChangedCallback;
	}
}
