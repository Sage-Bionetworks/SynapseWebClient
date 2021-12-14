package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.IsWidget;


/**
 * A simple model dialog prompting for multiple string values
 *
 */
public interface PromptForValuesModalView extends IsWidget {

	interface Configuration {
		String getTitle();

		void setTitle(String title);

		String getBodyCopy();

		void setBodyCopy(String bodyCopy);

		List<String> getPrompts();

		void setPrompts(List<String> prompts);

		List<String> getInitialValues();

		void setInitialValues(List<String> initialValues);

		List<InputType> getInputTypes();

		void setInputTypes(List<InputType> inputTypes);

		CallbackP<List<String>> getNewValuesCallback();

		void setNewValuesCallback(CallbackP<List<String>> newValueCallbacks);

		String getHelpPopoverMarkdown();

		void setHelpPopoverMarkdown(String markdown);

		String getHelpPopoverHref();

		void setHelpPopoverHref(String href);

		interface Builder {

			Builder setTitle(String title);

			Builder setBodyCopy(String bodyCopy);

			Builder setCallback(CallbackP<List<String>> callback);

			Builder addPrompt(String prompt, String initialValue);

			Builder addPrompt(String prompt, String initialValue, InputType inputType);

			Builder addPrompts(List<String> prompts, List<String> initialValues);

			Builder addPrompts(List<String> prompts, List<String> initialValues, List<InputType> inputTypes);

			Builder addHelpWidget(String markdown, String href);

			Configuration buildConfiguration();
		}
	}


	/**
	 * Defining each possible input type we can use in this class
	 */
	enum InputType {
		TEXTBOX, TEXTAREA;
	}

	public void configureAndShow(String title, String prompt, String initialValue, CallbackP<String> newValueCallback);

	/**
	 * Configure this view with starting data.
	 * 
	 * @param title The modal title
	 * @param prompts Prompt values
	 * @param initialValues Initial values.
	 * @param newValuesCallback Call back containing new values
	 */
	public void configureAndShow(String title, List<String> prompts, List<String> initialValues, CallbackP<List<String>> newValuesCallback);


	public void configureAndShow(String title, List<String> prompts, List<String> initialValues, List<PromptForValuesModalViewImpl.InputType> inputType, CallbackP<List<String>> newValuesCallback);

	void configureAndShow(Configuration configuration);

	/**
	 * Show an error message..
	 * 
	 * @param error
	 */
	public void showError(String error);

	/**
	 * Clear name and errors.
	 */
	public void clear();

	/**
	 * Set loading state.
	 * 
	 * @param isLoading
	 */
	public void setLoading(boolean isLoading);

	public void hide();
}
