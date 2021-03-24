package hr.fer.zemris.diprad.recognition.objects;

import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.PointDouble;

public class CircularObject {
	private PointDouble averagePoint;
	private double minMaxRatio;
	private double theta;
	private double thetaMaxDistance;

	private boolean fullCircle;
	double totalAngle;
	double totalNorm;
	private BasicMovementWrapper bmw;

	public CircularObject(PointDouble averagePoint, double minMaxRatio, double theta, double thetaMaxDistance,
			boolean fullCircle, double totalAngle, double totalNorm, BasicMovementWrapper bmw) {
		this.averagePoint = averagePoint;
		this.minMaxRatio = minMaxRatio;
		this.theta = theta;
		this.thetaMaxDistance = thetaMaxDistance;
		this.fullCircle = fullCircle;
		this.totalAngle = totalAngle;
		this.totalNorm = totalNorm;
		this.bmw = bmw;
	}

	public PointDouble getAveragePoint() {
		return averagePoint;
	}

	public double getMinMaxRatio() {
		return minMaxRatio;
	}

	public double getTheta() {
		return theta;
	}

	public boolean isFullCircle() {
		return fullCircle;
	}

	public BasicMovementWrapper getBmw() {
		return bmw;
	}

	public double getThetaMaxDistance() {
		return thetaMaxDistance;
	}

	public double getTotalAngle() {
		return totalAngle;
	}

	public double getTotalNorm() {
		return totalNorm;
	}
}
