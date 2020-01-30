package org.sagebionetworks.web.client.widget.upload;

/**
 * Basic metadata about a file.
 * 
 * @author John
 *
 */
public class FileMetadata {

	private String fileName;
	private String contentType;
	private double fileSize;

	public FileMetadata(String fileName, String contentType, double fileSize) {
		super();
		this.fileName = fileName;
		this.contentType = contentType;
		this.fileSize = fileSize;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setFileSize(double fileSize) {
		this.fileSize = fileSize;
	}

	public double getFileSize() {
		return fileSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		long temp;
		temp = Double.doubleToLongBits(fileSize);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileMetadata other = (FileMetadata) obj;
		if (contentType == null) {
			if (other.contentType != null)
				return false;
		} else if (!contentType.equals(other.contentType))
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (Double.doubleToLongBits(fileSize) != Double.doubleToLongBits(other.fileSize))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FileMetadata [fileName=" + fileName + ", contentType=" + contentType + ", fileSize=" + fileSize + "]";
	}



}
