package org.sagebionetworks.web.client.widget.upload;

public class ByteRange {
	private long start, end;

	public ByteRange(long start, long end) {
		this.start = start;
		this.end = end;
	}

	public ByteRange(int partNumber, Long fileSize, long partSize) {
		long startByte = (partNumber - 1) * partSize;
		long endByte = partNumber * partSize - 1;
		if (endByte >= fileSize)
			endByte = fileSize - 1;
		this.start = startByte;
		this.end = endByte;
	}

	public long getEnd() {
		return end;
	}

	public long getStart() {
		return start;
	}
}
