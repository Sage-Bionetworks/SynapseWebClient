package org.sagebionetworks.web.client.widget.entity.browse;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Tree;

public class EntityTree extends Tree {

  public EntityTree(Resources resources) {
    super(resources);
    Roles.getTreeRole().set(getElement());
    Element treeElement = getElement();
    Element treeContentElem = treeElement.getFirstChildElement();
    treeContentElem.removeAttribute("role");
  }

  @Override
  public void setFocus(boolean focus) {
    if (!focus) {
      super.setFocus(focus);
    }
  }
}
