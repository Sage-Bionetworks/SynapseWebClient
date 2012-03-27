package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.web.client.ClientLoggerImpl;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.UrlCache;
import org.sagebionetworks.web.client.ontology.AdapterModelData;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowConcept;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowEnum;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowList;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowScalar;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.NumberPropertyEditor;
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

	private static final String KEY_CHILDREN_TRANSITIVE = "/childrenTransitive/";
	private static final String KEY_CONCEPT = "/concept/";
	IconsImageBundle iconBundle;
	ClientLoggerImpl log;
	UrlCache urlCache;

	@Inject
	public FormFieldFactory(IconsImageBundle iconBundle, ClientLoggerImpl log, UrlCache urlCache) {
		this.iconBundle = iconBundle;
		this.log = log;
		this.urlCache = urlCache;
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
			// An enumeration editor.
			field = createEnumEditor((EntityRowEnum) row);
		}else if(row instanceof EntityRowConcept){
			// An enumeration editor.
			field = createConceptEditor((EntityRowConcept) row);
		}else if(row instanceof EntityRowScalar){
			final EntityRowScalar scalar = (EntityRowScalar) row;
			Class clazz = scalar.getTypeClass();
			if(clazz == null) throw new IllegalArgumentException("Clazz cannot be null");
			if(String.class == clazz){
				field = createTextEditor(scalar);
			}else if(Date.class == clazz){
				field = createDateEditor(scalar);
			}else if(Long.class == clazz){
				field = createLongEditor(scalar);
			}else if(Double.class == clazz){
				field = createDoubleEditor(scalar);
			}else{
				throw new IllegalArgumentException("Unknown type: "	+ clazz.getName());
			}
		}else if(row instanceof EntityRowList){
			EntityRowList list = (EntityRowList) row;
			Class clazz = list.getListClass();
			if(clazz == null) throw new IllegalArgumentException("Clazz cannot be null");
			if (clazz == String.class) {
				field = createStringListEditor(list);
			}else if(clazz  == Date.class){
				field = createDateListEditor(list);
			}else if(clazz  == Long.class){
				field = createLongListEditor(list);
			}else if(clazz  == Double.class){
				field = createDoubleListEditor(list);
			}else{
				throw new IllegalArgumentException("Unknown list type: "+clazz.getName());
			}
		}else{
			throw new IllegalArgumentException("Unknown EntityRow: "+row.getClass().getName());
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
	 * Editor for a list of doubles.
	 * @param list
	 * @return
	 */
	public Field<?> createDoubleListEditor(EntityRowList<Number> rowList) {
		final List<Number> values = rowList.getValue();
		ListFieldEditor<Number> listEditor = new ListFieldEditor<Number>(rowList, createDoubleTextField(), iconBundle, log);
		listEditor.setList(values);
		return listEditor;
	}

	/**
	 * Editor for a list of longs.
	 * @param list
	 * @return
	 */
	public Field<?> createLongListEditor(EntityRowList list) {
		Field<?> field;
		EntityRowList<Number> rowList = list;
		final List<Number> values = rowList.getValue();
		ListFieldEditor<Number> listEditor = new ListFieldEditor<Number>(rowList, createLongTextField(), iconBundle, log);
		listEditor.setList(values);
		field = listEditor;
		return field;
	}

	/**
	 * Editor for a list of Dates.
	 * @param list
	 * @return
	 */
	public Field<?> createDateListEditor(EntityRowList<Date> rowList) {
		final List<Date> values = rowList.getValue();
		ListFieldEditor<Date> listEditor = new ListFieldEditor<Date>(rowList, createDateField(), iconBundle, log);
		listEditor.setList(values);
		return listEditor;
	}

	/**
	 * Editor for a list of strings.
	 * @param list
	 * @return
	 */
	public Field<?> createStringListEditor(EntityRowList<String> rowList) {
		final List<String> values = rowList.getValue();
		ListFieldEditor<String> listEditor = new ListFieldEditor<String>(rowList, new TextField<String>(), iconBundle, log);
		listEditor.setList(values);
		return listEditor;
	}

	/**
	 * Editor for a single string.
	 * @param scalar
	 * @return
	 */
	public Field<Number> createDoubleEditor(final EntityRowScalar<Double> scalar) {
		TextField<Number> textField = createDoubleTextField();
		textField.setValue((Double) scalar.getValue());
		textField.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				scalar.setValue((Double) be.getValue());
			}
		});
		return textField;
	}

	/**
	 * Create a text field that can be used to edit doubles
	 * @return
	 */
	public TextField<Number> createDoubleTextField() {
		TextField<Number> textField = new TextField<Number>();
		textField.setPropertyEditor(new NumberPropertyEditor(Double.class));
		textField.setAutoValidate(true);
		textField.setValidator(new Validator() {
			@Override
			public String validate(Field<?> field, String value) {
				try{
					// If it can be parsed it is double.
					Double.parseDouble(value);
					return null;
				}catch(NumberFormatException e){
					return e.getMessage();
				}
			}
		});
		return textField;
	}
	
	/**
	 * Create a text field that can be used to edit longs.
	 * @return
	 */
	public TextField<Number> createLongTextField() {
		TextField<Number> textField = new TextField<Number>();
		textField.setPropertyEditor(new NumberPropertyEditor(Long.class));
		textField.setAutoValidate(true);
		textField.setValidator(new Validator() {
			@Override
			public String validate(Field<?> field, String value) {
				try{
					// If it can be parsed it is long.
					Long.parseLong(value);
					return null;
				}catch(NumberFormatException e){
					return e.getMessage();
				}
			}
		});
		return textField;
	}

	/**
	 * Editor for a Long.
	 * @param scalar
	 * @return
	 */
	public Field<Number> createLongEditor(final EntityRowScalar<Long> scalar) {
		TextField<Number> textField = createLongTextField();
		textField.setValue((Long) scalar.getValue());
		textField.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				scalar.setValue((Long) be.getValue());
			}
		});
		return textField;
	}

	/**
	 * Editor for a Date
	 * @param scalar
	 * @return
	 */
	public Field<Date> createDateEditor(final EntityRowScalar<Date> scalar) {
		DateField dateField = createDateField();
		dateField.setValue((Date) scalar.getValue());
		dateField.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				scalar.setValue((Date) be.getValue());
			}
		});
		return dateField;
	}

	/**
	 * Basic date field.
	 * @return
	 */
	public DateField createDateField() {
		DateField dateField = new DateField();
		// We do not want the user directly editing the text field
		dateField.setEditable(false);
		return dateField;
	}

	/**
	 * Editor for a string.
	 * @param scalar
	 * @return
	 */
	public Field<String> createTextEditor(final EntityRowScalar<String> scalar) {
		TextField<String> textField = new TextField<String>();
		textField.setValue((String) scalar.getValue());
		textField.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				scalar.setValue((String) be.getValue());
			}
		});
		return textField;
	}

	/**
	 * Create an enumeration editor.
	 * @param row
	 * @return
	 */
	public Field<ComboValue> createEnumEditor(final EntityRowEnum row) {
		// build the list store from the enum
		ListStore<ComboValue> store = new ListStore<ComboValue>();
		ComboValue current = null;
		for(String value: row.getEnumValues()){
			ComboValue comboValue = new ComboValue(value);
			store.add(comboValue);
			if(value.equals(row.getValue())){
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

		combo.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if(combo.getValue() != null){
					row.setValue(combo.getValue().getValue());
				}
			}
		});
		return combo;
	}
	
	/**
	 * Create an enumeration editor.
	 * @param row
	 * @return
	 */
	public Field<?> createConceptEditor(final EntityRowConcept row) {
		// build the list store from the enum
		// We will need the rep url for this one
		if(this.urlCache.getRepositoryServiceUrl() == null){
			IllegalArgumentException e = new IllegalArgumentException("Failed to get the URL for the repository service.");
			log.error(e.getMessage(), IllegalArgumentException.class.getName(), FormFieldFactory.class.getName(), "createConceptEditor", 354);
			throw e;
		}
		// Build up the URL for this concept
		StringBuilder builder = new StringBuilder();
		builder.append(this.urlCache.getRepositoryServiceUrl());
		builder.append(KEY_CONCEPT);
		builder.append(row.getConceptId());
		builder.append(KEY_CHILDREN_TRANSITIVE);
		log.info("Using concept URL: "+builder.toString());
		final ComboBox<AdapterModelData> field = ConceptAutoCompleteEditorFactory.createConceptAutoCompleteEditor(builder.toString());
		AdapterModelData model = new AdapterModelData();
		model.set(ConceptAutoCompleteEditorFactory.KEY_PREFERRED_LABEL, row.getValue());
		field.setValue(model);
		field.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if(field.getValue() != null){
					ModelData model = field.getValue();
					row.setValue((String)model.get(ConceptAutoCompleteEditorFactory.KEY_PREFERRED_LABEL));
				}
			}
		});
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
