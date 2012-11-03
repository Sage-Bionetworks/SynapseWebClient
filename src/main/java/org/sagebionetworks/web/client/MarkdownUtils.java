package org.sagebionetworks.web.client;




public class MarkdownUtils {

	/**
	 * Generate the Synapse web client markdown that we use to recognize an attachment
	 * @return
	 */
	public static String getAttachmentLinkMarkdown(String text, String entityId, String tokenId, String previewId, String tooltip){
		StringBuilder sb = new StringBuilder();
		sb.append("![");
		sb.append(text);
		//put in everything we need in the url
		sb.append("](");
		sb.append(DisplayConstants.ENTITY_DESCRIPTION_ATTACHMENT_PREFIX);
		sb.append(entityId);
		sb.append("/tokenId/");
		sb.append(tokenId);
		sb.append("/previewTokenId/");
		sb.append(previewId);
		sb.append(" \"");
		sb.append(tooltip);
		sb.append("\")");
		return sb.toString();
	}

}
