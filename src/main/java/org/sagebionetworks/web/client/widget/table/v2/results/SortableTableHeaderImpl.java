package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.widget.HelpWidget;
import org.sagebionetworks.web.client.widget.HelpWidget.Binder;

import com.google.gwt.core.shared.GWT;
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
	
	public interface Binder extends UiBinder<Widget, SortableTableHeaderImpl> {}
	
	@UiField
	Anchor tableHeaderLink;
	
	Widget widget;
	private static Binder uiBinder = GWT.create(Binder.class);
	
	@Inject
	public SortableTableHeaderImpl(){
		widget = uiBinder.createAndBindUi(this);
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
		if(handler != null){
			tableHeaderLink.addClickHandler(event -> {
				handler.onToggleSort(getText());
			});
		}
	}
	@Override
	public void setIcon(IconType icon) {
		tableHeaderLink.setIcon(icon);	
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
	public void setText(String text) {
		tableHeaderLink.setText(text);
	}
	public String getText() {
		return tableHeaderLink.getText();
	}
}
