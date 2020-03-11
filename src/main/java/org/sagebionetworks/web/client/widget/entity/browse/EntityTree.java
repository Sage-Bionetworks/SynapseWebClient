package org.sagebionetworks.web.client.widget.entity.browse;

import com.google.gwt.user.client.ui.Tree;

public class EntityTree extends Tree {
	
	public EntityTree(Resources resources) {
		super(resources);
	}
  @Override
  public void setFocus(boolean focus) {
    if (!focus) {
    	super.setFocus(focus);
    }
  }
}
