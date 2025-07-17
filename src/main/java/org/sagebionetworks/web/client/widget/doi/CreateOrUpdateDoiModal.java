package org.sagebionetworks.web.client.widget.doi;

import com.google.inject.Inject;
import javax.annotation.Nullable;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.jsinterop.CreateOrUpdateDoiModalProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class CreateOrUpdateDoiModal extends ReactComponent {

  @Inject
  public CreateOrUpdateDoiModal() {}

  private void renderComponent(CreateOrUpdateDoiModalProps props) {
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.CreateOrUpdateDoiModal,
      props
    );
    this.render(component);
  }

  /**
   * Configure the modal used to create or update a DOI for the given entity.
   * @param entity - the entity to create or update the DOI for
   * @param entityVersion - the optional version to use to populate the form
   * @param openModal - whether to open the modal or not
   */
  public void configure(
    Entity entity,
    @Nullable Long entityVersion,
    boolean openModal
  ) {
    CreateOrUpdateDoiModalProps props = CreateOrUpdateDoiModalProps.create(
      openModal,
      () -> this.configure(entity, entityVersion, false),
      ObjectType.ENTITY,
      entity.getId(),
      entityVersion
    );
    this.renderComponent(props);
  }
}
