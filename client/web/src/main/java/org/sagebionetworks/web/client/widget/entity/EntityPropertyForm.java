package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.widget.entity.row.EntityFormModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.inject.Inject;

/**
 * This is a form for editing entity properties.
 * 
 * @author jmhill
 * 
 */
public class EntityPropertyForm extends LayoutContainer {

	Field<?> nameField;
	Field<?> descriptionField;
	List<Field<?>> propertyFields;
	List<Field<?>> annotationFields;
	FormFieldFactory formFactory;
	FormPanel formPanel;
	FormPanel annotationFormPanel;
	ContentPanel annoPanel;
	ContentPanel propPanel;
	VerticalPanel vp;
	IconsImageBundle iconsImageBundle;
	// ContentPanel annotationsPanel;

	@Inject
	public EntityPropertyForm(FormFieldFactory formFactory, IconsImageBundle sageImageBundle) {
		this.formFactory = formFactory;
		this.iconsImageBundle = sageImageBundle;
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		this.setLayout(new AnchorLayout());
		this.setScrollMode(Scroll.AUTO);
		this.vp = new VerticalPanel();
		this.add(vp);
		// This is the property panel
		propPanel = new ContentPanel();
		propPanel.setCollapsible(true);
		propPanel.setFrame(false);
		propPanel.setHeading("Properties");
		propPanel.setLayout(new AnchorLayout());
		// Add a place holder form panel
		formPanel = new FormPanel();
		propPanel.add(formPanel);


		ToolBar toolBar = new ToolBar();
		Button addButton = new Button("Add Annotation");
		addButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.addSquare16()));
		toolBar.add(addButton);
		toolBar.setAlignment(HorizontalAlignment.CENTER);

		annoPanel = new ContentPanel();
		annoPanel.setCollapsible(true);
		annoPanel.setFrame(false);
		annoPanel.setHeading("Annotations");
		annoPanel.setLayout(new AnchorLayout());
		annoPanel.setBottomComponent(toolBar);
		// Add a place holder form panel
		annotationFormPanel = new FormPanel();
		annoPanel.add(annotationFormPanel);
		
		vp.add(propPanel);
		vp.add(annoPanel);
		
		rebuild();
	}
	
	/**
	 * Build a new empty from panel
	 * @return
	 */
	private FormPanel createNewFormPanel(){
		FormPanel form = new FormPanel();
		form.setHeading("Simple Form");
		form.setHeaderVisible(false);
		form.setFrame(true);
		form.setBorders(false);
		form.setLabelAlign(LabelAlign.RIGHT);
		return form;
	}

	public void rebuild() {
		// Nothing to do if this is not being rendered.
		if (!this.isRendered())
			return;
		this.propPanel.remove(formPanel);
		this.annoPanel.remove(annotationFormPanel);
		// Build up a new form
		formPanel = createNewFormPanel();
		annotationFormPanel = createNewFormPanel();
		
		// formPanel.setSize("100%", "100%");
		// Basic form data
		Margins margins = new Margins(10, 10, 0, 10);
		FormData basicFormData = new FormData("-100");
		basicFormData.setMargins(margins);

		// Name is the first
		FormData formData = new FormData(200, 20);
		formPanel.add(nameField, basicFormData);
		// followed by description.
		FormData descriptionData = new FormData("-20 50%");
		descriptionData.setMargins(margins);
		formPanel.add(descriptionField, descriptionData);

		// Add them to the form
		for (Field<?> formField : propertyFields) {
			// FormData thisData = new FormData("-100");
			formPanel.add(formField, basicFormData);
		}
		// Add them to the form
		for (Field<?> formField : annotationFields) {
			// FormData thisData = new FormData("-100");
			annotationFormPanel.add(formField, basicFormData);
		}
		// Add both panels back.
		this.propPanel.add(formPanel);
		this.annoPanel.add(annotationFormPanel);
	}

	/**
	 * 
	 * @param entity
	 */
	public void setList(EntityFormModel model) {
		// The name field is just a text field that cannot be null
		nameField = formFactory.createField(model.getName());
		descriptionField = formFactory.createTextAreaField(model
				.getDescription());

		// Create the list of fields
		propertyFields = formFactory.createFormFields(model.getProperties());
		annotationFields = formFactory.createFormFields(model.getAnnotations());
		rebuild();
	}

}
