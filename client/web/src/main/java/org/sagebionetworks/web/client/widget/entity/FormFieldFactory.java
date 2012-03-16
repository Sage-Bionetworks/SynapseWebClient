package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.widget.entity.row.AbstractEntityRowDate;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowDouble;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowList;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowLong;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowString;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.TextArea;  
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.inject.Inject;

/**
 * Create a list of form fields from a list of entity rows.
 * 
 * @author John
 * 
 */
public class FormFieldFactory {

	IconsImageBundle iconBundle;
	ClientLogger log;

	@Inject
	public FormFieldFactory(IconsImageBundle iconBundle, ClientLogger log) {
		this.iconBundle = iconBundle;
		this.log = log;
	}

	/**
	 * Given a list of List<EntityRow<?>> create the form fields
	 * 
	 * @param rows
	 * @return
	 */
	public List<Field<?>> createFormFields(List<EntityRow<?>> rows) {
		List<Field<?>> results = new ArrayList<Field<?>>();
		for (EntityRow<?> row : rows) {
			Field<?> field = createField(row);

			results.add(field);
		}
		return results;
	}

	/**
	 * Given an EntityRo create a form field for it.
	 * 
	 * @param row
	 * @return
	 */
	public Field<?> createField(EntityRow<?> row) {
		Field<?> field = null;
		if (row instanceof EntityRowString) {
			// Create an editor for a string.
			final EntityRowString er = (EntityRowString) row;
			TextField<String> textField = new TextField<String>();
			textField.setValue(er.getValue());
			field = textField;
			field.addListener(Events.Change, new Listener<FieldEvent>() {
				@Override
				public void handleEvent(FieldEvent be) {
					er.setValue((String) be.getValue());
				}
			});
		} else if (row instanceof AbstractEntityRowDate) {
			final AbstractEntityRowDate er = (AbstractEntityRowDate) row;
			DateField dateField = new DateField();
			field = dateField;
			dateField.setValue(er.getValue());
			field.addListener(Events.Change, new Listener<FieldEvent>() {
				@Override
				public void handleEvent(FieldEvent be) {
					er.setValue((Date) be.getValue());
				}
			});
		} else if (row instanceof EntityRowLong) {
			final EntityRowLong er = (EntityRowLong) row;
			TextField<String> textField = new TextField<String>();
			Long value = er.getValue();
			String stringValue = null;
			if (value != null) {
				stringValue = value.toString();
			}
			// Only allow longs
			textField.setValidator(new Validator() {
				@Override
				public String validate(Field<?> field, String value) {
					try {
						Long.parseLong(value);
						return null;
					} catch (NumberFormatException e) {
						return e.getMessage();
					}
				}
			});
			textField.setValue(stringValue);
			field = textField;
			field.addListener(Events.Change, new Listener<FieldEvent>() {
				@Override
				public void handleEvent(FieldEvent be) {
					er.setValue((Long) be.getValue());
				}
			});
		} else if (row instanceof EntityRowDouble) {
			final EntityRowDouble er = (EntityRowDouble) row;
			TextField<String> textField = new TextField<String>();
			Double value = er.getValue();
			String stringValue = null;
			if (value != null) {
				stringValue = value.toString();
			}
			// Only allow doubles
			textField.setValidator(new Validator() {
				@Override
				public String validate(Field<?> field, String value) {
					try {
						Double.parseDouble(value);
						return null;
					} catch (NumberFormatException e) {
						return e.getMessage();
					}
				}
			});
			textField.setValue(stringValue);
			field = textField;
			field.addListener(Events.Change, new Listener<FieldEvent>() {
				@Override
				public void handleEvent(FieldEvent be) {
					er.setValue((Double) be.getValue());
				}
			});
		} else if (row instanceof EntityRowList) {
			EntityRowList er = (EntityRowList) row;
			Class clazz = er.getListClass();
			if (clazz == String.class) {
				EntityRowList<String> rowList = er;
				final List<String> values = rowList.getValue();
				ListFieldEditor testing = new ListFieldEditor(rowList, iconBundle, log);
//				testing.setValue(values);
				testing.setList(values);
				field = testing;
			}else{
				field = new TextField<String>();
			}
		} else {
			throw new IllegalArgumentException("Unknown type: "
					+ row.getClass().getName());
		}
		// Add all of the basic stuff
		field.setBorders(false);
		field.setEnabled(true);
		field.setShadow(false);
		field.setFireChangeEventOnSetValue(true);
		field.setFieldLabel(row.getLabel());
		field.setToolTip(row.getDescription());
		return field;
	}
	
	/**
	 * Create a text area field for large strings.
	 * @param row
	 * @return
	 */
	public TextArea createTextAreaField(final EntityRowString row) {
		final TextArea field = new TextArea();
		field.setValue(row.getValue());
		field.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				row.setValue(field.getValue());
			}
		});
		field.setBorders(false);
		field.setEnabled(true);
		field.setShadow(false);
		field.setFireChangeEventOnSetValue(true);
		field.setFieldLabel(row.getLabel());
		field.setToolTip(row.getDescription());
		return field;
	}
}
