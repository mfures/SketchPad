package hr.fer.zemris.diprad.recognition.objects;

import hr.fer.zemris.diprad.recognition.models.tokens.VectorOrientationType;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.PointDouble;

public class CircularObject {
	private PointDouble averagePoint;
	private double minMaxRatio;
	private VectorOrientationType averagePointToOpeningOrientation;
	private boolean fullCircle;
	private BasicMovementWrapper bmw;

	public CircularObject(PointDouble averagePoint, double minMaxRatio,
			VectorOrientationType averagePointToOpeningOrientation, boolean fullCircle, BasicMovementWrapper bmw) {
		this.averagePoint = averagePoint;
		this.minMaxRatio = minMaxRatio;
		this.averagePointToOpeningOrientation = averagePointToOpeningOrientation;
		this.fullCircle = fullCircle;
		this.bmw = bmw;
	}

	public PointDouble getAveragePoint() {
		return averagePoint;
	}

	public double getMinMaxRatio() {
		return minMaxRatio;
	}

	public VectorOrientationType getAveragePointToOpeningOrientation() {
		return averagePointToOpeningOrientation;
	}

	public boolean isFullCircle() {
		return fullCircle;
	}

	public BasicMovementWrapper getBmw() {
		return bmw;
	}
}
