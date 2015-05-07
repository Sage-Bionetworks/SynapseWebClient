package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconPosition;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsTreeItem;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MoreTreeItem implements IsTreeItem, SynapseWidgetPresenter {

	public Button widget;
	public TreeItem treeItem;
	public MORE_TYPE type;
	ClickHandler handler;
	
	public enum MORE_TYPE {
		FOLDER("Show more folders"), FILE("Show more files");
		
		String innerText;
		
		MORE_TYPE(String moreText) {
			innerText = moreText;
		}
	}
	
	@Inject
	public MoreTreeItem() { 
		this.widget = new Button();
		treeItem = new TreeItem(asWidget());
		widget.setSize(ButtonSize.SMALL);
		widget.setType(ButtonType.INFO);
		widget.setIcon(IconType.ELLIPSIS_H);
		widget.setIconSize(IconSize.LARGE);
		widget.setIconPosition(IconPosition.RIGHT);
	}
	
	public void configure(MORE_TYPE type) {
		this.type = type;
		this.widget.setText(type.innerText);
	}

	public Button asButton() {
		return widget;
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public TreeItem asTreeItem() {
		return treeItem;
	}
	
	public void setClickHandler(ClickHandler handler) {
		this.widget.addClickHandler(handler);
	}
	
	public void setVisible(boolean b) {
		treeItem.setVisible(b);
	}	
}
