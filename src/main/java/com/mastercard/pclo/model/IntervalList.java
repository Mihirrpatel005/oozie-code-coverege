package com.mastercard.pclo.model;


public class IntervalList implements Comparable<IntervalList> {

	private long start;
	private long end;
	private String tagName;

	public IntervalList() {
	}

	public IntervalList(long start, long end) {
		this.start = start;
		this.end = end;
	}

	public IntervalList(long start, long end, String tagName) {
		super();
		this.start = start;
		this.end = end;
		this.tagName = tagName;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (other == null)
			return false;
		if (other.getClass() != this.getClass())
			return false;
		IntervalList that = (IntervalList) other;
		return this.start == that.start && this.end == that.end;
	}

	/**
	 * Returns an integer hash code for this interval.
	 *
	 * @return an integer hash code for this interval
	 */
	public int hashCode() {
		long hash1 = start * 31;
		long hash2 = end * 31;
		return (int) (31 * hash1 + hash2);
	}

	@Override
	public int compareTo(IntervalList o) {

		return (this.start < o.start ? -1 : (this.start > o.start ? 1 : 0));
	}
}