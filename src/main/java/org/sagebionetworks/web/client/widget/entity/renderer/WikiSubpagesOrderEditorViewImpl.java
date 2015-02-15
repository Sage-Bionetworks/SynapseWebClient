package org.sagebionetworks.web.client.widget.entity.renderer;


import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor.HasChangesHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesOrderEditorViewImpl extends Composite implements WikiSubpagesOrderEditorView {
	public interface Binder extends UiBinder<Widget, WikiSubpagesOrderEditorViewImpl> {}
	
	
	@UiField
	SimplePanel treePanel;
	@UiField
	SimplePanel instructionPanel;
	@UiField
	Button upButton;
	@UiField
	Button downButton;
	
	private Presenter presenter;
	private WikiSubpageOrderEditorTree tree;
	private HasChangesHandler hasChangesHandler;

	@Inject
	public WikiSubpagesOrderEditorViewImpl(Binder binder) {
		initWidget(binder.createAndBindUi(this));
		addUpDownButtonHandlers();
	}
	
	public void disableUpDownButtons() {
		upButton.setEnabled(false);
		downButton.setEnabled(false);
	}
	
	@Override
	public void configure(WikiSubpageOrderEditorTree subpageTree, HasChangesHandler hasChangesHandler) {
		this.tree = subpageTree;
		this.hasChangesHandler = hasChangesHandler;
		treePanel.setWidget(tree.asWidget());
		subpageTree.setMovabilityCallback(getTreeItemMovabilityCallback());
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	private void addUpDownButtonHandlers() {
		upButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tree.moveSelectedItem(true);
			}
		});
		
		downButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tree.moveSelectedItem(false);
			}
		});
	}
	
	@Override
	public void initializeState() {
		disableUpDownButtons();
	}
	
	public TreeItemMovabilityCallback getTreeItemMovabilityCallback() {
		return new TreeItemMovabilityCallback() {
			@Override
			public void invoke(boolean canMoveUp, boolean canMoveDown) {
				upButton.setEnabled(canMoveUp);
				downButton.setEnabled(canMoveDown);
			}
		};
	}
	
	public interface TreeItemMovabilityCallback {
		public void invoke(boolean canMoveUp, boolean canMoveDown);
	}
	
	@Override
	public void clear() {
		// TODO Auto-generated method stub
	}

	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
