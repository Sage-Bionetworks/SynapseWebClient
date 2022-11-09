package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.jsinterop.EntityTypeIconProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class EntityTypeIcon extends ReactComponentSpan {

  public EntityTypeIcon() {}

  public EntityTypeIcon(EntityType type) {
    configure(type);
  }

  public void configure(EntityType type) {
    EntityTypeIconProps props = EntityTypeIconProps.create(type);
    ReactNode component = React.createElementWithThemeContext(
      SRC.SynapseComponents.EntityTypeIcon,
      props
    );
    this.render(component);
  }

  public void setType(EntityType type) {
    configure(type);
  }

  /**
   * This is required for XML setters to work. If you want to call this from Java, prefer {@link #EntityTypeIcon(EntityType)} for strong typing.
   * @param type
   */
  public EntityTypeIcon(String type) {
    setType(type);
  }

  /**
   * This is required for XML setters to work. If you want to call this from Java, prefer {@link #setType(EntityType)} for strong typing.
   * @param type
   */
  public void setType(String type) {
    EntityType enumValue = EntityType.file;
    try {
      enumValue = EntityType.valueOf(type);
    } catch (IllegalArgumentException e) {}
    setType(enumValue);
  }
}
