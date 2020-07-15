package org.sagebionetworks.web.client.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.resources.WebResource;
import org.sagebionetworks.web.client.widget.csv.PapaCSVParser;
import org.sagebionetworks.web.client.widget.csv.PapaParseResult;

public class CommaSeparatedValuesParser implements CommaSeparatedValuesParserView.Presenter{
	CommaSeparatedValuesParserView view;
	ResourceLoader resourceLoader;
	private Consumer<List<String>> onAddCallback;
	PapaCSVParser papaCSVParser;

	@Inject
	public CommaSeparatedValuesParser(CommaSeparatedValuesParserView view, ResourceLoader resourceLoader, PapaCSVParser papaCSVParser) {
		this.view = view;
		this.resourceLoader = resourceLoader;
		this.papaCSVParser = papaCSVParser;
		view.setPresenter(this);
	}

	@Override
	public void configure(Consumer<List<String>> onAddCallback) {
		this.onAddCallback = onAddCallback;
		this.resourceLoader.requires(new WebResource("https://cdn.jsdelivr.net/npm/papaparse@5.2.0/papaparse.min.js"),
				new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {

					}

					@Override
					public void onSuccess(Void result) {

					}
				}
		);

	}

	@Override
	public void onCancel() {
		view.hide();
	}

	@Override
	public void onAdd() {
		if(this.onAddCallback != null){
			this.onAddCallback.accept(parseToStringList());
		}
		view.clearTextBox();
		view.hide();
	}

	@Override
	public List<String> parseToStringList(){
		String text = view.getText();
		PapaParseResult parsed = papaCSVParser.parse(text.trim());
		List<String> result = new ArrayList<>();
		for(String[] row : parsed.data){
			Collections.addAll(result, row);
		}
		return result;
	}

	@Override
	public void show(){
		view.show();
	}

	@Override
	public Widget asWidget(){
		return view.asWidget();
	}
}
