package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;

/**
 * A simple modal dialog for renaming an entity.
 *
 * @author John
 *
 */
public class RenameEntityModalWidgetImpl implements RenameEntityModalWidget {

  public static final String TITLE_PREFIX = "Rename ";

  public static final String NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER =
    "Name must include at least one character.";

  PromptForValuesModalView view;
  SynapseJavascriptClient jsClient;
  CookieProvider cookies;
  String parentId;
  Entity toRename;
  String startingName;
  String startingDescription;
  Callback handler;

  @Inject
  public RenameEntityModalWidgetImpl(
    PromptForValuesModalView view,
    SynapseJavascriptClient jsClient,
    CookieProvider cookieProvider
  ) {
    super();
    this.view = view;
    this.jsClient = jsClient;
    this.cookies = cookieProvider;
  }

  /**
   * Update entity with a new name.
   *
   * @param name
   */
  private void updateEntity(final String name, final String description) {
    view.setLoading(true);
    toRename.setName(name);
    toRename.setDescription(description);
    jsClient.updateEntity(
      toRename,
      null,
      null,
      new AsyncCallback<Entity>() {
        @Override
        public void onSuccess(Entity result) {
          view.hide();
          handler.invoke();
        }

        @Override
        public void onFailure(Throwable caught) {
          // put the name back.
          toRename.setName(startingName);
          toRename.setDescription(startingDescription);
          view.showError(caught.getMessage());
          view.setLoading(false);
        }
      }
    );
  }

  /**
   * Should be Called when the rename button is clicked on the dialog.
   */
  public void onRename(List<String> newValues) {
    String newName = null;
    String newDescription = null;
    if (newValues != null) {
      if (newValues.size() > 0) {
        newName = newValues.get(0);
      }
      if (newValues.size() > 1) {
        newDescription = newValues.get(1);
      }
    }
    String name = StringUtils.emptyAsNull(newName);
    String description = StringUtils.emptyAsNull(newDescription);
    if (name == null) {
      view.showError(NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
    } else if (
      this.startingName.equals(name) &&
      Objects.equals(this.startingDescription, description)
    ) {
      // just hide the view if the name has not changed.
      view.hide();
    } else {
      // Create the table
      updateEntity(name, description);
    }
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void onRename(Entity toRename, Callback handler) {
    this.handler = handler;
    String typeName = EntityTypeUtils.getDisplayName(
      EntityTypeUtils.getEntityTypeForClass(toRename.getClass())
    );
    this.toRename = toRename;
    this.startingName = toRename.getName();
    this.startingDescription = toRename.getDescription();
    this.view.clear();
    List<String> prompts = new ArrayList<>();
    prompts.add("Name");
    List<String> initialValues = new ArrayList<>();
    initialValues.add(toRename.getName());

    List<PromptForValuesModalView.InputType> inputTypes = new ArrayList<>();
    inputTypes.add(PromptForValuesModalView.InputType.TEXTBOX);

    // Only surfacing description for Table types (in experimental mode for now)
    if (toRename instanceof Table && DisplayUtils.isInTestWebsite(cookies)) {
      prompts.add("Description");
      initialValues.add(toRename.getDescription());
      inputTypes.add(PromptForValuesModalView.InputType.TEXTAREA);
    }

    this.view.configureAndShow(
        TITLE_PREFIX + typeName,
        prompts,
        initialValues,
        inputTypes,
        this::onRename
      );
  }
}
