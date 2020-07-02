package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.CommaSeparatedValuesParser;

public class EditJSONModal implements EditJSONListModalView.Presenter{
	public static final String SEE_THE_ERRORS_ABOVE = "See the error(s) above.";

	private final PortalGinInjector ginInjector;
	private final EditJSONListModalView view;
	private boolean commaSeparatedValuesParserExists;

	private List<CellEditor> cellEditors;

//	private ColumnModel columnModel;
	private Consumer<List<String>> onSaveCallback;

	@Inject
	public EditJSONModal(EditJSONListModalView view, PortalGinInjector ginInjector){
		this.ginInjector = ginInjector;
		this.view = view;

		view.setPresenter(this);
	}

	void configure(String jsonString, Consumer<List<String>> onSaveCallback){
		view.clearEditors();
		//TODO: enforce list size , string length limits and generate appropriately typed cell editor
//		this.columnModel = columnModel;
		this.onSaveCallback = onSaveCallback;
		this.cellEditors = new ArrayList<>();
		GWT.debugger();
		if (jsonString != null && !jsonString.isEmpty()) {
			try {
				JSONArray jsonArray = JSONParser.parseStrict(jsonString).isArray();
				if (jsonArray == null) {
					view.showError("Not a valid JSON Array");
				} else {
					//replace currently tracked editors with new list
					for (int i = 0; i < jsonArray.size(); i++) {
						JSONValue maybeString = jsonArray.get(i);

						// if the value is a json string , we want unquoted vesion, else we want its string representation;
						String strVal = maybeString.isString() != null ?
								maybeString.isString().stringValue() : maybeString.toString();

						addNewValue(strVal);

					}
				}
			} catch (JSONException e){
				view.showError("Not a valid JSON Array");
			}
		}

		// if no values, then add a single editor (allows edit or delete of annotation)
		if (cellEditors.isEmpty()) {
			view.addNewEditor(createNewEditor());
		}
		view.showEditor();
	}

	public CellEditor createNewEditor() {
		//todo: replace with factory
		CellEditor editor = ginInjector.createStringEditorCell();
		editor.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				// on enter, add a new field (empty fields are ignored on save)
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					onAddNewEmptyValue();
				}
			}
		});
		cellEditors.add(editor);
		return editor;
	}


	@Override
	public void onSave() {
		// check all annotation editor validity
		boolean isValid = cellEditors.stream().allMatch(CellEditor::isValid);
		if (!isValid) {
			view.showError(SEE_THE_ERRORS_ABOVE);
		}

		List<String> values = cellEditors.stream()
				.map(CellEditor::getValue)
				.filter(DisplayUtils::isDefined)
				.collect(Collectors.toList());

		if(this.onSaveCallback != null){
			this.onSaveCallback.accept(values);
		}
		view.hideEditor();
	}


	@Override
	public void onClickPasteNewValues(){
		//do not add another parser if one is already active
		if(this.commaSeparatedValuesParserExists){
			return;
		}
		CommaSeparatedValuesParser parser = ginInjector.getCommaSeparatedValuesParser();

		parser.configure(this::addNewValues, this::onCancelPasteNewValues);
		view.addCommaSeparatedValuesParser(parser.asWidget());
		this.commaSeparatedValuesParserExists = true;
	}

	@Override
	public void onCancelPasteNewValues(CommaSeparatedValuesParser commaSeparatedValuesParser){
		//TODO: do we want to enforce singleton? if so we can avoid passing in the parser as an arg.
		view.removeCommaSeparatedValuesParser(commaSeparatedValuesParser.asWidget());
		this.commaSeparatedValuesParserExists = false;
	}


	@Override
	public void onAddNewEmptyValue() {
		CellEditor editor = createNewEditor();
		view.addNewEditor(editor);
		// after attaching, set focus to the new editor
		editor.setFocus(true);
	}

	@Override
	public void addNewValues(Iterable<String> values){
		for(String val: values){
			addNewValue(val);
		}
	}

	@Override
	public void addNewValue(String value){
		CellEditor editor =  createNewEditor();
		editor.setValue(value);
		view.addNewEditor(editor);
	}

	@Override
	public void onValueDeleted(CellEditor editor) {

		int editorIndex = cellEditors.indexOf(editor);
		boolean editorAtEndOfList = editorIndex == cellEditors.size() - 1;

		cellEditors.remove(editorIndex);
		if (cellEditors.size() == 0) {
			view.addNewEditor(createNewEditor());
		} else if (editorAtEndOfList){
			//if last row removed, we need to move the addValues button
			view.moveAddNewAnnotationValueButtonToRowToLastRow();
		}
	}

	@Override
	public Widget asWidget(){
		return view.asWidget();
	}

}
