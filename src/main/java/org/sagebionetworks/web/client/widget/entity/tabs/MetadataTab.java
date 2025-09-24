package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.inject.Inject;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;

public class MetadataTab {

  private final Tab tab;
  private final MetadataTabView view;

  @Inject
  public MetadataTab(Tab tab, MetadataTabView view) {
    this.tab = tab;
    this.view = view;

    tab.configure(
      "Metadata",
      "label",
      "The metadata tab organizes curation tasks by data type. Each task can be used to guide the process of providing additional metadata to the project.",
      null,
      EntityArea.METADATA
    );
    tab.setContent(this.view.asWidget());
  }

  public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
    tab.addTabClickedCallback(onClickCallback);
  }

  public void configure(EntityBundle entityBundle) {
    tab.showTab();
    view.configure(entityBundle.getEntity().getId());
  }

  public Tab asTab() {
    return tab;
  }
}
