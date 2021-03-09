package hr.fer.zemris.diprad.recognition.objects;

import java.awt.Point;

public class X {
	private Point center;
	private double radius;

	public X(Point center, double radius) {
		this.center = center;
		this.radius = radius;
	}

	public Point getCenter() {
		return center;
	}

	public double getRadius() {
		return radius;
	}
}
