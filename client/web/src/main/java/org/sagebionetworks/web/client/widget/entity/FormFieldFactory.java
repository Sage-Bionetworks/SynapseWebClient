package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowEnum;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowList;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowScalar;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
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
		// There are three types
		if(row instanceof EntityRowEnum){
			final EntityRowEnum rowEnum = (EntityRowEnum) row;
			// build the list store from the enum
			ListStore<ComboValue> store = new ListStore<ComboValue>();
			ComboValue current = null;
			for(String value: rowEnum.getEnumValues()){
				ComboValue comboValue = new ComboValue(value);
				store.add(comboValue);
				if(value.equals(rowEnum.getValue())){
					current = comboValue;
				}
			}
			final ComboBox<ComboValue> combo = new ComboBox<ComboValue>();
			combo.setEmptyText("Select a value...");
			combo.setDisplayField(ComboValue.VALUE_KEY);
			combo.setStore(store);
			// setting this to false prevents the user from typing anything they want in the box.
			combo.setEditable(false);
			if(current != null){
				combo.setValue(current);
			}
		    combo.setTriggerAction(TriggerAction.ALL);

			field = combo;
			field.addListener(Events.Change, new Listener<FieldEvent>() {
				@Override
				public void handleEvent(FieldEvent be) {
					if(combo.getValue() != null){
						rowEnum.setValue(combo.getValue().getValue());
					}
				}
			});
			
		}else if(row instanceof EntityRowScalar){
			final EntityRowScalar scalar = (EntityRowScalar) row;
			Class clazz = scalar.getTypeClass();
			if(String.class == clazz){
				TextField<String> textField = new TextField<String>();
				textField.setValue((String) scalar.getValue());
				field = textField;
				field.addListener(Events.Change, new Listener<FieldEvent>() {
					@Override
					public void handleEvent(FieldEvent be) {
						scalar.setValue((String) be.getValue());
					}
				});
			}else if(Date.class == clazz){
				DateField dateField = new DateField();
				field = dateField;
				dateField.setValue((Date) scalar.getValue());
				field.addListener(Events.Change, new Listener<FieldEvent>() {
					@Override
					public void handleEvent(FieldEvent be) {
						scalar.setValue((Date) be.getValue());
					}
				});
			}else if(Long.class == clazz){
				TextField<Long> textField = new TextField<Long>();
				textField.setValue((Long) scalar.getValue());
				field = textField;
				field.addListener(Events.Change, new Listener<FieldEvent>() {
					@Override
					public void handleEvent(FieldEvent be) {
						scalar.setValue((Long) be.getValue());
					}
				});
			}else if(Double.class == clazz){
				TextField<Double> textField = new TextField<Double>();
				textField.setValue((Double) scalar.getValue());
				field = textField;
				field.addListener(Events.Change, new Listener<FieldEvent>() {
					@Override
					public void handleEvent(FieldEvent be) {
						scalar.setValue((Double) be.getValue());
					}
				});
			}else{
				throw new IllegalArgumentException("Unknown type: "	+ row.getClass().getName());
			}
			
		}else if(row instanceof EntityRowList){
			EntityRowList list = (EntityRowList) row;
			Class clazz = list.getListClass();
			if (clazz == String.class) {
				EntityRowList<String> rowList = list;
				final List<String> values = rowList.getValue();
				ListFieldEditor testing = new ListFieldEditor(rowList, new TextField<String>(), iconBundle, log);
//				testing.setValue(values);
				testing.setList(values);
				field = testing;
			}else if(clazz  == Date.class){
				EntityRowList<Date> rowList = list;
				final List<Date> values = rowList.getValue();
				ListFieldEditor testing = new ListFieldEditor(rowList, new DateField(), iconBundle, log);
//				testing.setValue(values);
				testing.setList(values);
				field = testing;
			}else{
				field = new TextField<String>();
			}
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
	public TextArea createTextAreaField(final EntityRowScalar<String> row) {
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
