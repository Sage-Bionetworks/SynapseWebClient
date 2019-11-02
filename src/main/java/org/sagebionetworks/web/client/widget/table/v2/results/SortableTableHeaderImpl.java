package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.table.SortDirection;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This is a view only component that contains zero business logic.
 * 
 * @author jhill
 *
 */

public class SortableTableHeaderImpl implements SortableTableHeader {
	public static final String UNSORTED_STYLES = "synapse-blue";
	public static final String SORTED_STYLES = "synapse-blue-bg color-white";

	public interface Binder extends UiBinder<Widget, SortableTableHeaderImpl> {
	}

	@UiField
	Anchor tableHeaderLink;
	@UiField
	Icon sortIcon;
	Widget widget;
	private static Binder uiBinder = GWT.create(Binder.class);

	@Inject
	public SortableTableHeaderImpl() {
		widget = uiBinder.createAndBindUi(this);
		setSortDirection(null);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void configure(final String text, final SortingListener handler) {
		tableHeaderLink.setText(text);
		setSortingListener(handler);
	}

	public void setSortingListener(final SortingListener handler) {
		if (handler != null) {
			ClickHandler onClick = event -> {
				handler.onToggleSort(getText());
			};
			tableHeaderLink.addClickHandler(onClick);
			sortIcon.addClickHandler(onClick);
		}
	}

	@Override
	public void setSortDirection(SortDirection direction) {
		IconType icon = IconType.SYN_SORT_DESC;
		if (direction == null) {
			sortIcon.removeStyleName(SORTED_STYLES);
			sortIcon.addStyleName(UNSORTED_STYLES);
		} else {
			sortIcon.removeStyleName(UNSORTED_STYLES);
			sortIcon.addStyleName(SORTED_STYLES);
			if (SortDirection.ASC.equals(direction)) {
				icon = IconType.SYN_SORT_ASC;
			}
		}
		sortIcon.setType(icon);
	}

	public void setWidth(String width) {
		widget.setWidth(width);
	}

	public void setHeight(String height) {
		widget.setHeight(height);
	}

	public void setAddStyleNames(String styles) {
		widget.addStyleName(styles);
	}

	public void setStyleName(String styles) {
		widget.setStyleName(styles);
	}

	public void setText(String text) {
		tableHeaderLink.setText(text);
	}

	public String getText() {
		return tableHeaderLink.getText();
	}

	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}
}
