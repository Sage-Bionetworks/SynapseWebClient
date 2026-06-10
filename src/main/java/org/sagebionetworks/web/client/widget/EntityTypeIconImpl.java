package org.sagebionetworks.web.client.widget;

import com.google.gwt.dom.client.SpanElement;
import javax.inject.Inject;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.jsinterop.EntityTypeIconProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class EntityTypeIconImpl
  extends ReactComponent
  implements EntityTypeIcon {

  @Inject
  public EntityTypeIconImpl() {
    super(SpanElement.TAG);
  }

  public EntityTypeIconImpl(EntityType type) {
    super(SpanElement.TAG);
    configure(type);
  }

  @Override
  public void configure(EntityType type) {
    EntityTypeIconProps props = EntityTypeIconProps.create(type);
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityTypeIcon,
      props
    );
    this.render(component);
  }

  public void setType(EntityType type) {
    configure(type);
  }

  /**
   * This is required for XML setters to work. If you want to call this from Java, prefer {@link #EntityTypeIconImpl(EntityType)} for strong typing.
   * @param type
   */
  public EntityTypeIconImpl(String type) {
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

  @Override
  public String getIconHTML() {
    // Force React to flush pending updates before reading the DOM
    ReactDOM.flushSync();
    return getElement().getInnerHTML();
  }
}
