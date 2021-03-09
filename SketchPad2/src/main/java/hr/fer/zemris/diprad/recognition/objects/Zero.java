package hr.fer.zemris.diprad.recognition.objects;

import java.awt.Point;

public class Zero {
	private Point center;
	private double radius;
	public Zero(Point center, double radius) {
		super();
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
