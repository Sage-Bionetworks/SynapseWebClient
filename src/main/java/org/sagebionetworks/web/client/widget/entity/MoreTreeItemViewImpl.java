package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Button;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MoreTreeItemViewImpl implements MoreTreeItemView {

	public interface Binder extends UiBinder<Widget, MoreTreeItemViewImpl> {
	}

	@UiField
	TreeItem treeItemWrapper;

	@UiField
	Button moreButton;

	Widget widget;

	@Inject
	public MoreTreeItemViewImpl(final Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public Button asButton() {
		return moreButton;
	}

	@Override
	public TreeItem asTreeItem() {
		return treeItemWrapper;
	}

	@Override
	public void setButtonClickHandler(ClickHandler handler) {
		moreButton.addClickHandler(handler);
	}

	@Override
	public void setVisible(boolean b) {
		treeItemWrapper.setVisible(b);
	}

}
