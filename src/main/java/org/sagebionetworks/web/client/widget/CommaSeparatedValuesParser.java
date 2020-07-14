package org.sagebionetworks.web.client.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.resources.WebResource;
import org.sagebionetworks.web.client.widget.csv.PapaParseConfig;
import org.sagebionetworks.web.client.widget.csv.PapaParseWrapper;
import org.sagebionetworks.web.client.widget.csv.PapaParseResult;

public class CommaSeparatedValuesParser implements CommaSeparatedValuesParserView.Presenter{
	//TODO: onModuleLoad() implement here to inject the CSV parser, PapaParser?

	CommaSeparatedValuesParserView view;
	ResourceLoader resourceLoader;
	private Consumer<List<String>> onAddCallback;

	@Inject
	public CommaSeparatedValuesParser(CommaSeparatedValuesParserView view, ResourceLoader resourceLoader) {
		this.view = view;
		this.resourceLoader = resourceLoader;
		view.setPresenter(this);
	}

	@Override
	public void configure(Consumer<List<String>> onAddCallback) {
		this.onAddCallback = onAddCallback;
		this.resourceLoader.requires(new WebResource("https://cdn.jsdelivr.net/npm/papaparse@5.2.0/papaparse.min.js"), new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {

			}

			@Override
			public void onSuccess(Void result) {

			}
		});
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
		PapaParseConfig config = new PapaParseConfig();
		GWT.debugger();

		PapaParseResult parsed = PapaParseWrapper.parse(text.trim(),config);
		List<String> result = new ArrayList<>();
		for(String[] row : parsed.data){
			for(String element: row){
				result.add(element);
			}
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
