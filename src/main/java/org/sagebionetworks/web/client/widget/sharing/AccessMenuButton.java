package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.shared.NodeType;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessMenuButton implements AccessMenuButtonView.Presenter {

	public static enum AccessLevel { PUBLIC, SHARED, PRIVATE }
	
	private AccessMenuButtonView view;
	private NodeType nodeType;
	private String nodeId;
	private AccessControlListEditor accessControlListEditor;	
	
	@Inject
	public AccessMenuButton(AccessMenuButtonView view, AccessControlListEditor accessControlListEditor) {
		this.view = view;
		this.accessControlListEditor = accessControlListEditor;
		view.setPresenter(this);
	}	
	
	public void createAccessButton(AccessLevel level, NodeType type, Entity entity) {
		nodeType = type;
		nodeId = entity.getId();
		accessControlListEditor.setResource(entity);		
		view.createAccessMenu(level, accessControlListEditor);
	}

	public Widget asWidget() {
		view.setPresenter(this);			
		return view.asWidget();
	}
		
}
