package org.sagebionetworks.web.client.widget.table.modal.upload;

/**
 * Enumerates possible CSV escape characters.
 *
 */
public enum EscapeCharacter {

	BACKSLASH("\\"), OTHER(null);

	private String character;

	EscapeCharacter(String character) {
		this.character = character;
	}

	/**
	 * Find a character from the given string.
	 * 
	 * @param character
	 * @return
	 */
	public static EscapeCharacter findCharacter(String character) {
		if (character == null) {
			// default to BACKSLASH
			return BACKSLASH;
		}
		for (EscapeCharacter del : values()) {
			if (character.equals(del.character)) {
				return del;
			}
		}
		return OTHER;
	}

	public String getCharacter() {
		return character;
	}
}
