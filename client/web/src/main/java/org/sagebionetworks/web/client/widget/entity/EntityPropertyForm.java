package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.web.client.widget.entity.row.EntityRow;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.Element;
import com.google.inject.Inject;

/**
 * This is a form for editing entity properties.
 * @author jmhill
 *
 */
public class EntityPropertyForm extends LayoutContainer {

	private VerticalPanel vp;
	private FormData formData;
	List<Field<?>> formFields;
	FormFieldFactory formFactory;

	@Inject
	public EntityPropertyForm(FormFieldFactory formFactory) {
		this.formFactory = formFactory;
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		rebuild();
	}

	public void rebuild() {
		// Nothing to do if this is not being rendered.
		if(!this.isRendered()) return;
		this.clearState();
		this.removeAll();
		formData = new FormData("-20");
		vp = new VerticalPanel();
		vp.setSpacing(10);
		if (formFields != null) {

			// Build up the form
			FormPanel simple = new FormPanel();
			simple.setHeading("Simple Form");
			simple.setFrame(true);
			simple.setWidth(350);
			// Add them to the form
			for (Field<?> formField : formFields) {
				simple.add(formField, formData);
			}
			vp.add(simple);
		}
		add(vp);
	}

	/**
	 * 
	 * @param entity
	 */
	public void setList(List<EntityRow<?>> rows) {
		// Create the list of fields
		formFields = formFactory.createFormFields(rows);
		rebuild();
	}

}
