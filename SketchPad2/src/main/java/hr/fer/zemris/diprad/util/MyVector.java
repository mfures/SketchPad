package hr.fer.zemris.diprad.util;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.recognition.models.KTableModel;

public class MyVector {
	public Point v;
	/**
	 * First point that makes the vector
	 */
	public Point p1;

	/**
	 * Second point that makes the vector
	 */
	public Point p2;

	/**
	 * First vectors identifier, usually index in the list
	 */
	public int i1;

	/**
	 * First vectors identifier, usually index in the list
	 */
	public int i2;

	public MyVector() {
		v = new Point();
	}

	public double norm() {
		return Math.sqrt(v.x * v.x + v.y * v.y);
	}

	public void add(MyVector v1) {
		v.x += v1.v.x;
		v.y += v1.v.y;
	}

	public void setToVector(MyVector v1) {
		v.x = v1.v.x;
		v.y = v1.v.y;
	}

	public boolean semiSameDirection(MyVector v1) {
		return (v1.v.y * v.y >= 0) && (v1.v.x * v.x >= 0);
	}

	@Override
	public String toString() {
		return "x:" + v.x + " y:" + v.y + p1.toString() + " " + p2.toString();
	}

	public static MyVector initNewVector(Point point, Point point2, int i1, int i2) {
		MyVector v = new MyVector();
		v.v.x = point2.x - point.x;
		v.v.y = point2.y - point.y;
		v.p1 = point;
		v.p2 = point2;
		v.i1 = i1;
		v.i2 = i2;
		return v;
	}

	public static double scalarProduct(MyVector v1, MyVector v2) {
		return v1.v.x * v2.v.x + v1.v.y * v2.v.y;
	}

	public static List<MyVector> listOfPointsToListOfVectors(List<Point> points) {
		List<MyVector> vectors = new ArrayList<MyVector>();

		for (int i = 0; i < points.size() - 1; i++) {
			vectors.add(MyVector.initNewVector(points.get(i), points.get(i + 1), i, i + 1));
		}

		List<MyVector> vectorsAcumulated = new ArrayList<MyVector>();
		vectorsAcumulated.add(vectors.get(0));

		for (int i = 1, j = 0; i < vectors.size(); i++) {
			if (vectorsAcumulated.get(j).norm() < KTableModel.MIN_VECTOR_NORM) {
				if (vectorsAcumulated.get(j).semiSameDirection(vectors.get(i))) {
					vectorsAcumulated.get(j).add(vectors.get(i));
					vectorsAcumulated.get(j).p2 = vectors.get(i).p2;
					vectorsAcumulated.get(j).i2 = vectors.get(i).i2;
				} else {
					j++;
					vectorsAcumulated.add(vectors.get(i));
				}
			} else {
				j++;
				vectorsAcumulated.add(vectors.get(i));
			}
		}

		return vectorsAcumulated;
	}
}
