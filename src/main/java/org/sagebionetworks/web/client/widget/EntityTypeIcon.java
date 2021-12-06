package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.jsinterop.EntityTypeIconProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class EntityTypeIcon extends Span {

	public EntityTypeIcon(EntityType type) {
		configure(type);
	}

	public void configure(EntityType type) {
		EntityTypeIconProps props = EntityTypeIconProps.create(type);
		ReactElement component = React.createElement(SRC.SynapseComponents.EntityTypeIcon, props);
		ReactDOM.render(component, getElement());
	}

	public void setType(EntityType type) {
		configure(type);
	}

	/**
	 * Prefer {@link #EntityTypeIcon(EntityType)} for strong typing.
	 * @param type
	 */
	public EntityTypeIcon(String type) {
		configure(type);
	}

	/**
	 * Prefer {@link #configure(EntityType)} for strong typing.
	 * @param type
	 */
	public void configure(String type) {
		EntityTypeIconProps props = EntityTypeIconProps.create(type);
		ReactElement component = React.createElement(SRC.SynapseComponents.EntityTypeIcon, props);
		ReactDOM.render(component, getElement());
	}

	/**
	 * Prefer {@link #setType(EntityType)} for strong typing.
	 * @param type
	 */
	public void setType(String type) {
		configure(type);
	}

}
