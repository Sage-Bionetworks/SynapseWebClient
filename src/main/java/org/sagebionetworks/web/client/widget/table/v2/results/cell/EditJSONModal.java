package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.CommaSeparatedValuesParser;

public class EditJSONModal implements EditJSONListModalView.Presenter{
	public static final String SEE_THE_ERRORS_ABOVE = "See the error(s) above.";

	private final PortalGinInjector ginInjector;
	private final EditJSONListModalView view;
	private final JSONArrayAdapter jsonArrayAdapter;
	private boolean commaSeparatedValuesParserExists;

	private List<CellEditor> cellEditors;

//	private ColumnModel columnModel;
	private Consumer<List<String>> onSaveCallback;

	@Inject
	public EditJSONModal(EditJSONListModalView view, PortalGinInjector ginInjector, JSONArrayAdapter jsonArrayAdapter){
		this.ginInjector = ginInjector;
		this.view = view;
		this.jsonArrayAdapter = jsonArrayAdapter;
	}

	void configure(String jsonString, Consumer<List<String>> onSaveCallback){
		//TODO: enforce list size , string length limits and generate appropriately typed cell editor
//		this.columnModel = columnModel;
		this.onSaveCallback = onSaveCallback;
		try {
			JSONArrayAdapter parsed = jsonArrayAdapter.createNewArray(jsonString);
			List<String> values = new ArrayList<>(parsed.length());
			for(int i = 0; i <parsed.length(); i++){
				values.add(parsed.getString(i));
			}
			addNewValues(values);
		} catch (JSONObjectAdapterException e) {
			//TODO: handle errors but maybe on the parent side? or we can display an empty editor?
		}
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
		GWT.debugger();
		this.commaSeparatedValuesParserExists = true;
	}

	@Override
	public void onCancelPasteNewValues(CommaSeparatedValuesParser commaSeparatedValuesParser){
		//TODO: do we want to enforce singleton? if so we can avoid passing in the parser as an arg.
		view.removeCommaSeparatedValuesParser(commaSeparatedValuesParser.asWidget());
		this.commaSeparatedValuesParserExists = false;
	}


	@Override
	public void addNewValues(Iterable<String> values){
		for(String val: values){
			CellEditor editor = ginInjector.createStringEditorCell();
			editor.setValue(val);
			InputGroup editorWithDeleteButton = CellFactory.appendDeleteButton(editor, this::onDeleteValue);
		}

	}

	@Override
	public void onDeleteValue(CellEditor editor){
		cellEditors.remove(editor);
	}
}
