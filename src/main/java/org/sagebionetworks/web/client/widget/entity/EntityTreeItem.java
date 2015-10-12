package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsTreeItem;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityTreeItem implements IsTreeItem, SynapseWidgetPresenter {

	private TreeItem treeItem;
	private EntityBadge entityBadge;
	private boolean isExpandable;
	
	@Inject
	public EntityTreeItem(EntityBadge entityBadge) { 
		this.entityBadge = entityBadge;
	}
	
	public void configure(EntityQueryResult header,  boolean isRootItem, boolean isExpandable) {
		entityBadge.configure(header);
		entityBadge.asWidget().addStyleName("padding-2 light-border-bottom");
		treeItem = new TreeItem(asWidget());
		this.isExpandable = isExpandable;
		if (isRootItem)
			treeItem.addStyleName("entityTreeItem padding-left-0-imp");
		else
			treeItem.addStyleName("entityTreeItem");
	}
	
	public boolean isExpandable() {
		return isExpandable;
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
	
	public void setEntityClickedHandler(CallbackP<String> callback) {
		entityBadge.setEntityClickedHandler(callback);
	}
	
	public EntityQueryResult getHeader() {
		return entityBadge.getHeader();
	}
	
	public void setClickHandler(ClickHandler handler) {
		entityBadge.setClickHandler(handler);
	}

}
