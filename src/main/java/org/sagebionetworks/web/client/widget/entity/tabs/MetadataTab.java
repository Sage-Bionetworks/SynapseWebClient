package org.sagebionetworks.web.client.widget.entity.tabs;

import javax.inject.Inject;
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

    tab.configure("Tasks and Actions", EntityArea.METADATA);
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
