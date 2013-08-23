package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAnnotationDialog;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogUtils;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

/**
 * A widget that renders entity properties.
 * 
 * @author jmhill
 *
 */
public class PropertyWidgetViewImpl extends FlowPanel implements PropertyWidgetView, IsWidget {
	
	private IconsImageBundle iconsImageBundle;
	private SynapseJSNIUtils synapseJSNIUtils;
	private Presenter presenter;
	private FormFieldFactory formFieldFactory;
	
	@Inject
	public PropertyWidgetViewImpl(IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils, FormFieldFactory formFieldFactory) {
		this.addStyleName("span-24 notopmargin last");
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.formFieldFactory = formFieldFactory;
	}
	
	private Image getNewButton(ImageResource resource, ClickHandler handler, String tooltipText) {
		Image addAnnotationButton = new Image(resource);
		addAnnotationButton.addStyleName("imageButton vertical-align-middle margin-left-5");
		addAnnotationButton.addClickHandler(handler);
		DisplayUtils.addTooltip(synapseJSNIUtils, addAnnotationButton, tooltipText, TOOLTIP_POSITION.BOTTOM);
		return addAnnotationButton;
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	/**
	 * The rows of data to render.
	 * 
	 * @param rows
	 */
	@Override
	public void configure(List<EntityRow<?>> rows, boolean canEdit) {
		this.clear();
		if (!rows.isEmpty() || canEdit) {
			Label title = new Label(DisplayConstants.ANNOTATIONS + ":");
			title.addStyleName("inline-block boldText");
			add(title);
			//include Add Annotation button if user can edit
			if (canEdit) {
				ClickHandler handler = new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						// Show a form for adding an Annotations
						AddAnnotationDialog.showAddAnnotation(new AddAnnotationDialog.Callback(){

							@Override
							public void addAnnotation(String name, ANNOTATION_TYPE type) {
								presenter.addAnnotation(name, type);
							}
						});
					}
				};
				add(getNewButton(iconsImageBundle.addSquareGrey16(), handler, DisplayConstants.TEXT_ADD_ANNOTATION));
			}
			add(new HTML());
		}
			
		//now add a button for every row
		for (final EntityRow<?> row : rows) {
			FlowPanel container = new FlowPanel();
			container.addStyleName("inline-block light-border margin-right-5 margin-top-5");
			String value = SafeHtmlUtils.htmlEscapeAllowEntities(row.getDislplayValue());
			String label = row.getLabel();
			String delimiter = label != null && label.trim().length() > 0 && value != null && value.trim().length() > 0 ? ":" : "";
			Label l1 = new Label(label + delimiter);
			l1.addStyleName("inline-block greyText-imp margin-right-5");
			HTML l2 = new HTML(value);
			l2.addStyleName("inline-block blackText-imp");
			DisplayUtils.addTooltip(synapseJSNIUtils, l2, row.getToolTipsBody(), TOOLTIP_POSITION.BOTTOM);
			
			container.add(l1);
			container.add(l2);
			
			//if user can edit values, then make it clickable
			if (canEdit) {
				//edit annotation handler
				ClickHandler editHandler = new ClickHandler() {
					public void onClick(ClickEvent event) {
						handleEditClick(row);
					};
				};
				l2.addStyleName("link");
				l2.addClickHandler(editHandler);
				l1.addStyleName("link");
				l1.addClickHandler(editHandler);
				//container.add(getNewButton(iconsImageBundle.editGrey16(), editHandler, DisplayConstants.BUTTON_EDIT));
				
				//delete annotation handler
				ClickHandler deleteHandler = new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						//delete this row
						handleDeleteClick(row);
					}
				};
				container.add(getNewButton(iconsImageBundle.deleteButtonGrey16(), deleteHandler, DisplayConstants.LABEL_DELETE));
			}
			else {
				l2.addStyleName("blackText");
			}
			
			
			this.add(container);
			
		}
	}
	
	private void handleEditClick(final EntityRow row) {
		//add the field to a popup
		showEditAnnotation(row, new org.sagebionetworks.web.client.utils.Callback() {
			@Override
			public void invoke() {
				presenter.updateAnnotation(row);
			}
		});
	}
	
	private void handleDeleteClick(final EntityRow row) {
		MessageBox.confirm(DisplayConstants.LABEL_DELETE +" \"" + row.getLabel()+"\"", DisplayConstants.PROMPT_SURE_DELETE + " annotation?", new Listener<MessageBoxEvent>() {					
			@Override
			public void handleEvent(MessageBoxEvent be) { 					
				Button btn = be.getButtonClicked();
				if(Dialog.YES.equals(btn.getItemId())) {
					presenter.deleteAnnotation(row);
				}
			}
		});
	}
	
	/**
	 * Show the Edit Annotation dialog.
	 * @param callback If the user chooses to edit an annotation, this callback will be called.
	 */
	public void showEditAnnotation(final EntityRow row, final org.sagebionetworks.web.client.utils.Callback callback){
		Field field = formFieldFactory.createField(row);
		final Object originalValue = row.getValue();
		Dialog dialog = new Dialog();
		dialog.setMaximizable(false);
		dialog.setSize(345, 125);
		dialog.setPlain(true);  
		dialog.setModal(true);  
		dialog.setHeading("Edit Annotation");  
		dialog.setLayout(new FitLayout());
	    dialog.okText = "Save";
		dialog.setButtons(Dialog.OKCANCEL);
		dialog.setHideOnButtonClick(true);
		FormPanel form = DialogUtils.createNewFormPanel();
		// Add them to the form
		form.add(field);
		dialog.add(form);
		dialog.show();
		Button addButton = dialog.getButtonById(Dialog.OK);
		Button cancelButton = dialog.getButtonById(Dialog.CANCEL);
		
		FormButtonBinding binding = new FormButtonBinding(form);  
	    binding.addButton(addButton);
	    binding.addButton(cancelButton);
	    
	    addButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				//fire the change event to update the entity
				callback.invoke();
			}
	    });

	    cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				//set it back to the original value
				row.setValue(originalValue);
			}
	    });
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	@Override
	public void showLoading() {
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
}
