package com.mastercard.pclo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MergeInterval {

	List<IntervalList> newIntervalList= null;
	List<IntervalList> removeIntervalList = null;

	public List<IntervalList> mergeInterval(List<IntervalList> allintervalList) {

		newIntervalList = new ArrayList<IntervalList>();
		removeIntervalList = new ArrayList<IntervalList>();

		for (int i = 0; i < allintervalList.size() - 1; i++) {

			if (hasAdd(allintervalList.get(i), allintervalList.get(i + 1))) {
				addInterval(allintervalList.get(i));

				if (hasRemove(allintervalList.get(i + 1)))
					removeInterval(allintervalList.get(i + 1));

				allintervalList.get(i).setEnd(allintervalList.get(i + 1).getEnd());
			}
		}
		modifyAndSort(allintervalList);
		System.out.println("==========================================");
		System.out.println("After Merge");
		for (IntervalList list : allintervalList) {
			System.out.println(list.getStart() + " :: " + list.getEnd());
		}
		System.out.println("==========================================");
		return allintervalList;
	}

	public List<IntervalList> modifyAndSort(List<IntervalList> updateIntervalList) {

		updateIntervalList.addAll(newIntervalList);
		updateIntervalList.removeAll(removeIntervalList);
		Collections.sort(updateIntervalList);
		return updateIntervalList;
	}

	public boolean hasRemove(IntervalList removeIntervalList) {

		if (removeIntervalList.getStart() == removeIntervalList.getEnd())
			return true;
		return false;
	}

	public boolean hasAdd(IntervalList currentIndex, IntervalList nextIndex) {

		if (currentIndex.getEnd() > nextIndex.getEnd() && currentIndex.getEnd() != nextIndex.getStart())
			return true;
		return false;
	}

	public void addInterval(IntervalList addInterval) {
		newIntervalList.add(new IntervalList(addInterval.getEnd(), addInterval.getEnd()));
	}

	public void removeInterval(IntervalList removeInterval) {
		removeIntervalList.add(new IntervalList(removeInterval.getEnd(), removeInterval.getEnd()));
	}

}