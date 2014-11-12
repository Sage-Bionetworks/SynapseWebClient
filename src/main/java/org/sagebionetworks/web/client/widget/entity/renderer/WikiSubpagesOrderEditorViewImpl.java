package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
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
	private Tree tree;

	@Inject
	public WikiSubpagesOrderEditorViewImpl(Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}
	
	@Override
	public void configure(Tree subpageTree) {
		this.tree = subpageTree;
		tree.getElement().getStyle().setZIndex(1000);
		treePanel.setWidget(tree);	// TODO: initView method?
		instructionPanel.setWidget(new Label("Supp"));
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	
	/********************* TODO *********************/
	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showInfo(String title, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showErrorMessage(String message) {
		// TODO Auto-generated method stub
		
	}
}
