package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListEditor;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

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

public class AdministerEvaluationsList implements SynapseWidgetPresenter {
	
	private SynapseClientAsync synapseClient;
	private EvaluationLinksList evalList;
	private AdapterFactory adapterFactory;
	private EvaluationAccessControlListEditor aclEditor;
	
	@Inject
	public AdministerEvaluationsList(SynapseClientAsync synapseClient, EvaluationLinksList evalList, EvaluationAccessControlListEditor aclEditor, AdapterFactory adapterFactory) {
		this.synapseClient = synapseClient;
		this.evalList = evalList;
		this.aclEditor = aclEditor;
		this.adapterFactory = adapterFactory;
	}

	/**
	 * 
	 * @param evaluations List of evaluations to display
	 * @param evaluationCallback call back with the evaluation if it is selected
	 */
	public void configure(String entityId) {
		synapseClient.getSharableEvaluations(entityId, new AsyncCallback<ArrayList<String>>() {
			
			@Override
			public void onSuccess(ArrayList<String> results) {					
				try {	
					List<Evaluation> evaluations = new ArrayList<Evaluation>();
					for(String eh : results) {
						evaluations.add(new Evaluation(adapterFactory.createNew(eh)));
					}
					evalList.configure(evaluations, getEvaluationClicked(), "Evaluation Administration");
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				evalList.showErrorMessage(caught.getMessage());
			}
		});
		
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
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}
	
	@Override
	public Widget asWidget() {
		return evalList.asWidget();
	}
	

		/*
	 * Private Methods
	 */
}
