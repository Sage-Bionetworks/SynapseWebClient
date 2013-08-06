package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListEditor;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AdministerEvaluationsListViewImpl implements AdministerEvaluationsListView {

	private Presenter presenter;
	private EvaluationLinksList evalList;
	private EvaluationAccessControlListEditor aclEditor;
	
	@Inject
	public AdministerEvaluationsListViewImpl(EvaluationLinksList evalList, EvaluationAccessControlListEditor aclEditor) {
		this.evalList = evalList;
		this.aclEditor = aclEditor;
	}
	
	@Override
	public void configure(List<Evaluation> evaluations) {
		evalList.configure(evaluations, getEvaluationClicked(), null);
	}
	
	public CallbackP<Evaluation> getEvaluationClicked(){
		return new CallbackP<Evaluation>(){
			@Override
			public void invoke(Evaluation evaluation) {
				//evaluation clicked.
				//TODO: go to a new Evaluation Admin page (show submissions, and other info, with a share button)?
				//for now, pop up a share dialog for the selected Evaluation
				aclEditor.setResource(evaluation);
				
				final Dialog window = new Dialog();
				
				// configure layout
				window.setSize(560, 465);
				window.setPlain(true);
				window.setModal(true);
				window.setHeading(DisplayConstants.TITLE_SHARING_PANEL);
				window.setLayout(new FitLayout());
				window.add(aclEditor.asWidget(), new FitData(4));			    
			    
				// configure buttons
				window.okText = "Save";
				window.cancelText = "Cancel";
			    window.setButtons(Dialog.OKCANCEL);
			    window.setButtonAlign(HorizontalAlignment.RIGHT);
			    window.setHideOnButtonClick(false);
				window.setResizable(false);
				
				// "Apply" button
				// TODO: Disable the "Apply" button if ACLEditor has no unsaved changes
				Button applyButton = window.getButtonById(Dialog.OK);
				applyButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						// confirm close action if there are unsaved changes
						if (aclEditor.hasUnsavedChanges()) {
							aclEditor.pushChangesToSynapse(new AsyncCallback<Void>() {
								@Override
								public void onSuccess(Void result) {
									//acl editor view handled notification
								}
								@Override
								public void onFailure(Throwable caught) {
									//failure notification is handled by the acl editor view.
								}
							});
						}
						window.hide();
					}
			    });
				
				// "Close" button				
				Button closeButton = window.getButtonById(Dialog.CANCEL);
			    closeButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						window.hide();
					}
			    });
				
				window.show();
			}
		};
	}
	
	@Override
	public Widget asWidget() {
		return evalList.asWidget();
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void clear() {
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public void showLoading() {
	}
	
	/*
	 * Private Methods
	 */

}
