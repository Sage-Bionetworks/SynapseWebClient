package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget.SubPageTreeItem;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class WikiSubpagesTreeUtils {
	
	public static Tree copyTree(Tree tree) {
		Tree newTree = new Tree();
		SubPageTreeItem oldOverallRoot = getOverallRootTreeItem(tree);
		SubPageTreeItem newOverallRoot = new SubPageTreeItem(null, "Dummy Overall Root", null, false);	// Dummy
		buildTreeRecurse(oldOverallRoot, newOverallRoot);
		
		// Put old root items back into old tree.
		for (SubPageTreeItem child : oldOverallRoot.getChildren()) {
			tree.addItem(child);
		}
		
		// Put new root items into new tree.
		for (SubPageTreeItem child : newOverallRoot.getChildren()) {
			newTree.addItem(child);
		}
		
		return newTree;
	}
	
	public static void buildTreeRecurse(SubPageTreeItem item1, SubPageTreeItem item2) {
		for (int i = 0; i < item1.getChildCount(); i++) {
			SubPageTreeItem oldItem = (SubPageTreeItem) item1.getChild(i);
			SubPageTreeItem newChild = new SubPageTreeItem(oldItem.getHeader(), oldItem.getText(), oldItem.getTargetPlace(), oldItem.isCurrentPage());
			item2.addItem(newChild);
			Label label = new Label();
			if (oldItem.getWidget() instanceof Anchor) {
				label.setText(((Anchor)oldItem.getWidget()).getText());
			} else if (oldItem.getWidget() instanceof Label) {
				label.setText(((Label)oldItem.getWidget()).getText());
			}
			newChild.setWidget(label);
			item2.setState(true);	// Fully expand tree.
			buildTreeRecurse(oldItem, newChild);
		}
	}
	
	public static List<SubPageTreeItem> getTreeRootChildren(Tree tree) {
		List<SubPageTreeItem> result =  new ArrayList<SubPageTreeItem>();
		for (int i = 0; i < tree.getItemCount(); i++) {
			result.add((SubPageTreeItem) tree.getItem(i));
		}
		return result;
	}
	
	public static void sortHeadersByOrderHint(PaginatedResults<JSONEntity> wikiHeaders, V2WikiOrderHint orderHint) {
		List<JSONEntity> headerList = wikiHeaders.getResults();
		List<String> idList = orderHint.getIdList();
		if (idList == null) return;
		
		int insertIndex = 0;
		for (int i = 0; i < idList.size(); i++) {
			for (int j = 0; j < headerList.size(); j++) {
				if (((V2WikiHeader) headerList.get(j)).getId().equals(idList.get(i))) {
					// The header was in the order hint. Move that header towards the front.
					JSONEntity toMove = headerList.remove(j);
					headerList.add(insertIndex, toMove);
					insertIndex++;
				}
			}
		}
	}
	
	public static List<String> getCurrentOrderIdList(Tree tree) {
		List<String> idList = new LinkedList<String>();
		for (int i = 0; i < tree.getItemCount(); i++) {
			recurseAddIds(idList, tree.getItem(i));
		}
		return idList;
	}

	private static void recurseAddIds(List<String> idList, TreeItem root) {
		idList.add(((SubPageTreeItem) root).getHeader().getId());
		for (int i = 0; i < root.getChildCount(); i++) {
			recurseAddIds(idList, root.getChild(i));
		}
	}
	
	private static SubPageTreeItem getOverallRootTreeItem(Tree tree) {
		SubPageTreeItem overallRoot = new SubPageTreeItem(null, "Dummy Overall Root", null, false);	// Dummy
		for (int i = 0; i < tree.getItemCount(); i++) {
			overallRoot.addItem(tree.getItem(i));
		}
		return overallRoot;
	}
}