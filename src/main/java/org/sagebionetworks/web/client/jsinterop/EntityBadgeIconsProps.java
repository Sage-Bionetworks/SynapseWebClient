package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityBadgeIconsProps extends ReactComponentProps {

	@JsFunction
	public interface OnUnlinkSuccess {
		void onUnlinkSuccess(String entityId);
	}

	@JsFunction
	public interface OnUnlinkError {
		void onUnlinkError(SynapseClientError error);
	}


	String entityId;
	OnUnlinkSuccess onUnlink;
	OnUnlinkError onUnlinkError;

	boolean renderTooltipComponent;

	@JsOverlay
	public OnUnlinkSuccess getOnUnlinkSuccess() {
		return this.onUnlink;
	}

	@JsOverlay
	public OnUnlinkError getOnUnlinkError() {
		return this.onUnlinkError;
	}

//
//	versionNumber?: number
//	flexWrap?: // possible settings for flex-wrap
//			| 'wrap'
//			| 'nowrap'
//			| '-moz-initial'
//			| 'inherit'
//			| 'initial'
//			| 'revert'
//			| 'unset'
//			| 'wrap-reverse'
//	justifyContent?: 'flex-start' | 'flex-end' | string
//	/** Shows an icon indicating if the entity is 'public' or 'private'. Default true  */
//	showIsPublicPrivate?: boolean
//	/** Shows an icon if the entity has sharing settings set on itself. Default true  */
//	showHasLocalSharingSettings?: boolean
//	/** Shows an icon if the entity has annotations, or if it has a validation schema (in experimental mode only). Default true  */
//	showHasAnnotations?: boolean
//	/** Shows an icon if the entity a wiki. Default true  */
//	showHasWiki?: boolean
//	/** Shows an icon if the entity has been mentioned in discussion threads. Default true  */
//	showHasDiscussionThread?: boolean
//	/* Shows an 'unlink' button if the entity is a link and the user has permission to delete it. Default true */
//	showUnlink?: boolean
//	/* Invoked after the entity is unlinked/deleted in case there is cleanup to do. Returns the entityId */
//	onUnlink?: (entityId: string) => void
//	onUnlinkError?: (error: unknown) => void
//	/** Whether or not the badges (e.g. Annotations) can trigger opening a modal on click */
//	canOpenModal: boolean
//	/** Whether this component should render a ReactTooltip or if an external component is managing it */
//	renderTooltipComponent: boolean

	@JsOverlay
	public static EntityBadgeIconsProps create(String entityId, OnUnlinkSuccess onUnlink, OnUnlinkError onUnlinkError) {
		EntityBadgeIconsProps props = new EntityBadgeIconsProps();
		props.entityId = entityId;
		props.onUnlink = onUnlink;
		props.onUnlinkError = onUnlinkError;
		props.renderTooltipComponent = true;
		return props;
	}
}
