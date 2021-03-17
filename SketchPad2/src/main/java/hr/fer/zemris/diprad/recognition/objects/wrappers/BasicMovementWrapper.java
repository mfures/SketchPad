package hr.fer.zemris.diprad.recognition.objects.wrappers;

import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;

public class BasicMovementWrapper {
	private BasicMovement bm;
	private int index;
	private int totalFragments = 1;
	private int totalHandeledFragments = 0;

	public BasicMovementWrapper(BasicMovement bm, int index) {
		this.bm = bm;
		this.index = index;
	}

	public BasicMovement getBm() {
		return bm;
	}

	public int getIndex() {
		return index;
	}

	public int getTotalFragments() {
		return totalFragments;
	}

	public void setTotalFragments(int totalFragments) {
		this.totalFragments = totalFragments;
	}

	public int getTotalHandeledFragments() {
		return totalHandeledFragments;
	}

	public void incTotalHandeledFragments() {
		this.totalHandeledFragments++;
	}

	public void resetTotalHandeledFragments() {
		this.totalHandeledFragments = 0;
	}

	public boolean isUnused() {
		return totalFragments != totalHandeledFragments;
	}
}
