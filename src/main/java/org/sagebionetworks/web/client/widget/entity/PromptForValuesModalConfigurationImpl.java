package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.client.widget.entity.PromptForValuesModalViewImpl.DEFAULT_INPUT_TYPE;

import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.sagebionetworks.web.client.utils.CallbackP;

/**
 * A simple model dialog prompting for multiple string values
 *
 */
public class PromptForValuesModalConfigurationImpl
  implements PromptForValuesModalView.Configuration {

  private String title;
  private String bodyCopy;
  private List<String> prompts;
  private List<String> initialValues;
  private List<PromptForValuesModalView.InputType> inputTypes;
  private CallbackP<List<String>> newValuesCallback;
  private String helpPopoverMarkdown;
  private String helpPopoverHref;

  @Override
  public String getHelpPopoverMarkdown() {
    return helpPopoverMarkdown;
  }

  @Override
  public void setHelpPopoverMarkdown(String helpPopoverMarkdown) {
    this.helpPopoverMarkdown = helpPopoverMarkdown;
  }

  @Override
  public String getHelpPopoverHref() {
    return helpPopoverHref;
  }

  @Override
  public void setHelpPopoverHref(String helpPopoverHref) {
    this.helpPopoverHref = helpPopoverHref;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getBodyCopy() {
    return bodyCopy;
  }

  @Override
  public void setBodyCopy(String bodyCopy) {
    this.bodyCopy = bodyCopy;
  }

  @Override
  public List<String> getPrompts() {
    return prompts;
  }

  @Override
  public void setPrompts(List<String> prompts) {
    this.prompts = prompts;
  }

  @Override
  public List<String> getInitialValues() {
    return initialValues;
  }

  @Override
  public void setInitialValues(List<String> initialValues) {
    this.initialValues = initialValues;
  }

  @Override
  public List<PromptForValuesModalView.InputType> getInputTypes() {
    return inputTypes;
  }

  @Override
  public void setInputTypes(
    List<PromptForValuesModalView.InputType> inputTypes
  ) {
    this.inputTypes = inputTypes;
  }

  @Override
  public CallbackP<List<String>> getNewValuesCallback() {
    return newValuesCallback;
  }

  @Override
  public void setNewValuesCallback(CallbackP<List<String>> newValueCallbacks) {
    this.newValuesCallback = newValueCallbacks;
  }

  public static class Builder
    implements PromptForValuesModalView.Configuration.Builder {

    String title;
    String bodyCopy;
    CallbackP<List<String>> newValuesCallback;
    List<String> prompts;
    List<String> initialValues;
    List<PromptForValuesModalView.InputType> promptTypes;
    String helpPopoverMarkdown;
    String helpPopoverHref;
    Placement helpPopoverPlacement;

    public Builder() {
      prompts = new ArrayList<>();
      initialValues = new ArrayList<>();
      promptTypes = new ArrayList<>();
    }

    @Override
    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    @Override
    public Builder setBodyCopy(String bodyCopy) {
      this.bodyCopy = bodyCopy;
      return this;
    }

    @Override
    public Builder setCallback(CallbackP<List<String>> callback) {
      this.newValuesCallback = callback;
      return this;
    }

    @Override
    public Builder addPrompt(String prompt, String initialValue) {
      return addPrompt(prompt, initialValue, DEFAULT_INPUT_TYPE);
    }

    @Override
    public Builder addPrompt(
      String prompt,
      String initialValue,
      PromptForValuesModalView.InputType inputType
    ) {
      this.prompts.add(prompt);
      this.initialValues.add(initialValue);
      this.promptTypes.add(inputType);
      return this;
    }

    @Override
    public Builder addPrompts(
      List<String> prompts,
      List<String> initialValues
    ) {
      for (int i = 0; i < prompts.size(); i++) {
        addPrompt(prompts.get(i), initialValues.get(i));
      }
      return this;
    }

    @Override
    public Builder addPrompts(
      List<String> prompts,
      List<String> initialValues,
      List<PromptForValuesModalView.InputType> inputTypes
    ) {
      for (int i = 0; i < prompts.size(); i++) {
        addPrompt(prompts.get(i), initialValues.get(i), inputTypes.get(i));
      }
      return this;
    }

    @Override
    public Builder addHelpWidget(String markdown, String href) {
      this.helpPopoverMarkdown = markdown;
      this.helpPopoverHref = href;
      return this;
    }

    @Override
    public PromptForValuesModalConfigurationImpl buildConfiguration() {
      PromptForValuesModalConfigurationImpl configuration = new PromptForValuesModalConfigurationImpl();
      configuration.setTitle(this.title);
      configuration.setBodyCopy(this.bodyCopy);
      configuration.setPrompts(this.prompts);
      configuration.setInitialValues(this.initialValues);
      configuration.setInputTypes(this.promptTypes);
      configuration.setNewValuesCallback(this.newValuesCallback);
      configuration.setHelpPopoverMarkdown(this.helpPopoverMarkdown);
      configuration.setHelpPopoverHref(this.helpPopoverHref);
      return configuration;
    }
  }
}
