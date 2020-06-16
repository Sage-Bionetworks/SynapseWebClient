package org.sagebionetworks.web.client.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.gwt.user.client.ui.Widget;

public class CommaSeparatedValuesParser implements CommaSeparatedValuesParserView.Presenter{

	CommaSeparatedValuesParserView view;
	private Consumer<List<String>> onAddCallback;
	private Consumer<CommaSeparatedValuesParser> onCancelCallback;

	@Override
	public void configure(Consumer<List<String>> onAddCallback, Consumer<CommaSeparatedValuesParser> onCancelCallback) {
		this.onAddCallback = onAddCallback;
		this.onCancelCallback = onCancelCallback;
	}

	@Override
	public void onCancel() {
		if(this.onCancelCallback != null){
			this.onCancelCallback.accept(this);
		}
	}

	@Override
	public void onAdd() {
		if(this.onAddCallback != null){
			this.onAddCallback.accept(parseToStringList());
		}
		//TODO: to clear or not to clear? should we delete the
		view.clearTextBox();
	}

	@Override
	public List<String> parseToStringList(){
		String text = view.getText();
		List<String> result = new ArrayList<>();
		//TODO: implement
		return result;
	}

	@Override
	public Widget asWidget(){
		return view.asWidget();
	}
}
