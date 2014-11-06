package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsTreeItem;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityTreeItem implements IsTreeItem, SynapseWidgetPresenter {

	private TreeItem treeItem;
	private EntityBadge entityBadge;
	
	@Inject
	public EntityTreeItem(EntityBadge entityBadge) { 
		this.entityBadge = entityBadge;
	}
	
	public void configure(EntityHeader header, boolean isRootItem) {
		entityBadge.configure(header);
		entityBadge.asWidget().addStyleName("padding-bottom-4-imp");
		treeItem = new TreeItem(asWidget());
		if (isRootItem)
			treeItem.addStyleName("entityTreeItem padding-left-0-imp");
		else
			treeItem.addStyleName("entityTreeItem");
	}

	@Override
	public Widget asWidget() {
		return entityBadge.asWidget();
	}

	@Override
	public TreeItem asTreeItem() {
		return treeItem;
	}
	
	public void showLoadingIcon() {
		entityBadge.showLoadingIcon();
	}
	
	public void showTypeIcon() {
		entityBadge.hideLoadingIcon();
	}
	
	public EntityHeader getHeader() {
		return entityBadge.getHeader();
	}
	
	public void setClickHandler(ClickHandler handler) {
		entityBadge.setClickHandler(handler);
	}

}
