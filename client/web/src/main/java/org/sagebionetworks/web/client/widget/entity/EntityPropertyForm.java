package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.web.client.widget.entity.row.EntityFormModel;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowString;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.Element;
import com.google.inject.Inject;

/**
 * This is a form for editing entity properties.
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

	@Inject
	public EntityPropertyForm(FormFieldFactory formFactory) {
		this.formFactory = formFactory;
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		this.setLayout(new AnchorLayout());
		this.setScrollMode(Scroll.AUTO);
		// Build up the form
		formPanel = new FormPanel();
		add(formPanel);
		rebuild();
	}

	public void rebuild() {
		// Nothing to do if this is not being rendered.
		if(!this.isRendered()) return;
		this.remove(formPanel);
		// Build up a new form
		formPanel = new FormPanel();
		formPanel.setHeading("Simple Form");
		formPanel.setHeaderVisible(false);
		formPanel.setFrame(true);
		formPanel.setBorders(false);
		formPanel.setLabelAlign(LabelAlign.RIGHT);
//		formPanel.setSize("100%", "100%");
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
//			FormData thisData = new FormData("-100");
			formPanel.add(formField, basicFormData);
		}
		this.add(formPanel);
	}

	/**
	 * 
	 * @param entity
	 */
	public void setList(EntityFormModel model) {
		// The name field is just a text field that cannot be null
		nameField = formFactory.createField(model.getName());
		descriptionField = formFactory.createTextAreaField((EntityRowString) model.getDescription());
		
		// Create the list of fields
		propertyFields = formFactory.createFormFields(model.getProperties());
		rebuild();
	}

}
