package hr.fer.zemris.diprad.recognition.objects;

import java.util.List;

import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.PointDouble;
import hr.fer.zemris.diprad.util.Rectangle;

public class CircularObject {
	private PointDouble averagePoint;
	private double minMaxRatio;
	private double theta;
	private double thetaMaxDistance;
	private Rectangle boundingBox;

	private boolean fullCircle;
	private double totalAngle;
	private double totalNorm;
	private double startEndDistanceToTotalLengthRatio;
	private BasicMovementWrapper bmw;
	private int startIndex, endIndex;
	private boolean deltaXdominant;

	public CircularObject(PointDouble averagePoint, double minMaxRatio, double theta, double thetaMaxDistance,
			boolean fullCircle, double totalAngle, double totalNorm, BasicMovementWrapper bmw, int startIndex,
			int endIndex, List<PointDouble> sampledPoints) {
		this.averagePoint = averagePoint;
		this.minMaxRatio = minMaxRatio;
		this.theta = theta;
		this.thetaMaxDistance = thetaMaxDistance;
		this.fullCircle = fullCircle;
		this.totalAngle = totalAngle;
		this.totalNorm = totalNorm;
		this.bmw = bmw;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.startEndDistanceToTotalLengthRatio = Math
				.sqrt(Math.pow(sampledPoints.get(startIndex).x - sampledPoints.get(endIndex).x, 2)
						+ Math.pow(sampledPoints.get(startIndex).y - sampledPoints.get(endIndex).y, 2))
				/ totalNorm;

		PointDouble p1 = sampledPoints.get(startIndex);
		PointDouble p2 = sampledPoints.get(endIndex);
		deltaXdominant = Math.abs(p1.x - p2.x) > Math.abs(p1.y - p2.y);

		initBoundingBox(sampledPoints);
	}

	public void initBoundingBox(List<PointDouble> sampledPoints) {
		double minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;

		for (int i = startIndex; i <= endIndex; i++) {
			PointDouble p = sampledPoints.get(i);
			if (p.x < minX) {
				minX = p.x;
			}
			if (p.y < minY) {
				minY = p.y;
			}
			if (p.x > maxX) {
				maxX = p.x;
			}
			if (p.y > maxY) {
				maxY = p.y;
			}
		}

		this.boundingBox = new Rectangle(new PointDouble(minX, minY), new PointDouble(maxX, maxY));
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

	public Rectangle getBoundingBox() {
		return boundingBox;
	}

	public double getStartEndDistanceToTotalLengthRatio() {
		return startEndDistanceToTotalLengthRatio;
	}

	public boolean getDeltaXdominant() {
		return this.deltaXdominant;
	}

}
