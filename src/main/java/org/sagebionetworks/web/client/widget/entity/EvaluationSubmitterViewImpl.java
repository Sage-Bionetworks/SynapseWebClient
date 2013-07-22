package org.sagebionetworks.web.client.widget.entity;

import java.util.HashSet;
import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class EvaluationSubmitterViewImpl implements EvaluationSubmitterView {
	
	public static final int DEFAULT_DIALOG_HEIGHT = 280;
	public static final int DEFAULT_DIALOG_WIDTH = 480;
	private Presenter presenter;
	private EvaluationList evaluationList;
	private SageImageBundle sageImageBundle;
	private  IconsImageBundle iconsImageBundle;
	private ComboBox<ComboValue> submitterCombo;
	private EntityFinder entityFinder;
	private Dialog window;
	private boolean showEntityFinder;
	private Reference selectedReference;
	private HTML selectedText;
	
	@Inject
	public EvaluationSubmitterViewImpl(EntityFinder entityFinder, EvaluationList evaluationList, SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle) {
		this.entityFinder = entityFinder;
		this.evaluationList = evaluationList;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		initializeWindow();
	}
	
	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}

	@Override
	public void showLoading() {
		window.removeAll();
		window.setSize(DEFAULT_DIALOG_WIDTH, DEFAULT_DIALOG_HEIGHT);
		window.add(DisplayUtils.createFullWidthLoadingPanel(sageImageBundle, "Loading Submission Options..."));
		window.show();
	}
	
	@Override
	public void clear() {
	}
	
	@Override
	public void showSubmissionAcceptedDialogs(HashSet<String> receiptMessages) {
		for (String message : receiptMessages) {
			Dialog d = new Dialog();
			d.setHeading(DisplayConstants.THANK_YOU_FOR_SUBMISSION);
			d.addText(message);
			d.setBodyStyle("padding:5px;");
			//render like html coming from markdown
			d.addStyleName("markdown");
			d.setWidth(370);
			d.setAutoHeight(true);
			d.setHideOnButtonClick(true);
			d.setButtons(Dialog.OK);
			d.show();	
		}
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void popupSelector(boolean showEntityFinder, List<Evaluation> evaluations, List<String> submitterAliases) {
		window.removeAll();
		selectedReference = null;
        evaluationList.configure(evaluations);
        submitterCombo = getSubmitterAliasComboBox(submitterAliases);
        this.showEntityFinder = showEntityFinder;
	    
        FlowPanel panel = new FlowPanel();
        panel.addStyleName("margin-left-10");
        if (showEntityFinder) {
        	panel.add(new HTML("<h6 class=\"margin-top-10\">Select the Entity that you would like to submit:</h6>"));
        	Button findEntityButton = new Button(DisplayConstants.FIND_ENTITY, AbstractImagePrototype.create(iconsImageBundle.magnify16()));
    		findEntityButton.addSelectionListener(new SelectionListener<ButtonEvent>() {			
    			@Override
    			public void componentSelected(ButtonEvent ce) {
    				entityFinder.configure(true);				
    				final Window window = new Window();
    				DisplayUtils.configureAndShowEntityFinderWindow(entityFinder, window, new SelectedHandler<Reference>() {					
    					@Override
    					public void onSelected(Reference selected) {
    						if(selected.getTargetId() != null) {					
    							selectedReference = selected;
    							selectedText.setHTML("&nbsp<h7>" + DisplayUtils.createEntityVersionString(selected) + "</h7>");
    							window.hide();
    						} else {
    							showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
    						}
    					}
    				});
    			}
    		});
    		HorizontalPanel findEntityHorizintalPanel = new HorizontalPanel();
    		findEntityHorizintalPanel.add(findEntityButton);
        	selectedText = new HTML("");
        	findEntityHorizintalPanel.add(selectedText);
        	panel.add(findEntityHorizintalPanel);
        	window.setSize(DEFAULT_DIALOG_WIDTH,DEFAULT_DIALOG_HEIGHT + 70);
        }
        else {
        	window.setSize(DEFAULT_DIALOG_WIDTH,DEFAULT_DIALOG_HEIGHT);
        }
        panel.add(new HTML("<h6 class=\"margin-top-10\">Select the challenge(s) below that you would like to submit to:</h6>"));
        panel.add(evaluationList.asWidget());
        panel.add(new HTML("<h6 class=\"margin-top-10\">Set "+DisplayConstants.SUBMITTER_ALIAS+" to be shown in the public leaderboard:</h6>"));
        panel.add(submitterCombo);
        window.add(panel);
        window.layout(true);
        window.center();
	}
	
	
	public void initializeWindow() {
		window = new Dialog();
        window.setMaximizable(false);
        
        window.setPlain(true); 
        window.setModal(true); 
        
        window.setHeading(DisplayConstants.LABEL_SUBMIT_TO_EVALUATION); 
        window.setButtons(Dialog.OKCANCEL);
        window.setHideOnButtonClick(false);

        window.setLayout(new FitLayout());
        
        //ok button submits if valid
        Button okButton = window.getButtonById(Dialog.OK);  
        okButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<Evaluation> evaluations = evaluationList.getSelectedEvaluations();
				if (evaluations.size() > 0) {
					if (submitterCombo.isValid()) {
						if (showEntityFinder) {
							if (selectedReference == null || selectedReference.getTargetId() == null) {
								//invalid, return.
								showErrorMessage(DisplayConstants.NO_ENTITY_SELECTED);
								return;
							}
						}
						presenter.submitToEvaluations(selectedReference, evaluations, submitterCombo.getRawValue());
					} else {
						showErrorMessage(submitterCombo.getErrorMessage());
					}
				} else {
					showErrorMessage(DisplayConstants.NO_EVALUATION_SELECTED);
				}
			}
	    });
        
        //cancel button simply hides
        Button cancelButton = window.getButtonById(Dialog.CANCEL);	    
	    cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				window.hide();
			}
	    });
	}
	public ComboBox<ComboValue> getSubmitterAliasComboBox(List<String> submitterAliases) {
		// build the list store from the enum
		ListStore<ComboValue> store = new ListStore<ComboValue>();
		for (String value : submitterAliases) {
			ComboValue comboValue = new ComboValue(value);
			store.add(comboValue);
		}
		final ComboBox<ComboValue> combo = new ComboBox<ComboValue>();
		combo.setDisplayField(ComboValue.VALUE_KEY);
		combo.setWidth(400);
		combo.getMessages().setBlankText("Please set " + DisplayConstants.SUBMITTER_ALIAS);
		combo.setStore(store);
		combo.setEditable(true);
		combo.setEmptyText("Set "+DisplayConstants.SUBMITTER_ALIAS+"...");
		combo.setAllowBlank(false);
		if(store.getCount() > 0)
			combo.setValue(store.getAt(0));
		combo.setTriggerAction(TriggerAction.ALL);
		return combo;
	}
	
	@Override
	public void hideWindow() {
		window.hide();	
	}
}
