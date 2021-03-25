package hr.fer.zemris.diprad.recognition.objects;

import java.awt.Point;
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
	double totalAngle;
	double totalNorm;
	private BasicMovementWrapper bmw;
	private int startIndex, endIndex;

	public CircularObject(PointDouble averagePoint, double minMaxRatio, double theta, double thetaMaxDistance,
			boolean fullCircle, double totalAngle, double totalNorm, BasicMovementWrapper bmw, int startIndex,
			int endIndex) {
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

		initBoundingBox();
	}

	public void initBoundingBox() {
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;

		List<Point> points = bmw.getBm().getPoints();
		for (int i = startIndex; i <= endIndex; i++) {
			Point p = points.get(i);
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

		this.boundingBox = new Rectangle(new Point(minX, minY), new Point(maxX, maxY));
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
}
