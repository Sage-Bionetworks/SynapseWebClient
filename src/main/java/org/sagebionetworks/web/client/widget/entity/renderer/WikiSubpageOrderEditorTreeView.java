package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree.SubpageOrderEditorTreeNode;

public interface WikiSubpageOrderEditorTreeView extends IsWidget {
  void configure(SubpageOrderEditorTreeNode overallRoot, String ownerObjectId);

  void selectTreeItem(SubpageOrderEditorTreeNode toSelect);

  void moveTreeItem(SubpageOrderEditorTreeNode node, boolean moveUp);

  void setPresenter(Presenter presenter);

  void setSynAlert(IsWidget w);

  void clear();

  public interface Presenter {
    void selectTreeItem(SubpageOrderEditorTreeNode node);

    SubpageOrderEditorTreeNode getSelectedTreeItem();

    SubpageOrderEditorTreeNode getParent(SubpageOrderEditorTreeNode child);
  }
}
