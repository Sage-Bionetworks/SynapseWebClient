package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Button;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MoreTreeItem implements MoreTreeItemView.Presenter {

	MoreTreeItemView view;
	ClickHandler handler;

	@Inject
	public MoreTreeItem(MoreTreeItemView view) {
		this.view = view;
	}

	public Button asButton() {
		return view.asButton();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public TreeItem asTreeItem() {
		return view.asTreeItem();
	}

	public void setClickHandler(ClickHandler handler) {
		view.setButtonClickHandler(handler);
	}

	public void setVisible(boolean b) {
		view.setVisible(b);
	}
}
