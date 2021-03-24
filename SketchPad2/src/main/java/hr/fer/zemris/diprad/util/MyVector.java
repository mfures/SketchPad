package hr.fer.zemris.diprad.util;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.recognition.models.KTableModel;

/**
 * Defines vector as used in calculations
 * 
 * @author Matej
 *
 */
public class MyVector {
	/**
	 * This point defines values of the vector v.x=p2.x-p1.x and v.y=p2.y-p1.y
	 */
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
	 * First identifier, index of first point in the list of points
	 */
	public int i1;

	/**
	 * Second identifier, index of second point in the list of points
	 */
	public int i2;

	public MyVector() {
		v = new Point();
	}

	/**
	 * Returns vectors norm
	 * 
	 * @return Euclidean norm ->Math.sqrt(v.x * v.x + v.y * v.y)
	 */
	public double norm() {
		return Math.sqrt(v.x * v.x + v.y * v.y);
	}

	/**
	 * Adds vector to current vector
	 * 
	 * @param v1
	 */
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
		return "x:" + v.x + " y:" + v.y; // + p1.toString() + " " + p2.toString();
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
		return listOfPointsToListOfVectors(points, 0, points.size() - 1);
	}

	/**
	 * Creates list of vectors from list of points
	 * 
	 * @param points
	 * @return
	 */
	public static List<MyVector> listOfPointsToListOfVectors(List<Point> points, int startIndex, int endIndex) {
		List<MyVector> vectors = initVectorList(points, startIndex, endIndex);
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

		if (vectorsAcumulated.get(vectorsAcumulated.size() - 1).norm() < KTableModel.MIN_VECTOR_NORM) {
			if (vectorsAcumulated.size() > 1) {
				int last = vectorsAcumulated.size() - 1;
				if (vectorsAcumulated.get(last - 1).semiSameDirection(vectorsAcumulated.get(last))) {
					vectorsAcumulated.get(last - 1).add(vectors.get(last));
					vectorsAcumulated.get(last - 1).p2 = vectors.get(last).p2;
					vectorsAcumulated.get(last - 1).i2 = vectors.get(last).i2;
					vectorsAcumulated.remove(last);
				}
			}
		}

		return vectorsAcumulated;
	}

	public static List<MyVector> initVectorList(List<Point> points, int startIndex, int endIndex) {
		List<MyVector> vectors = new ArrayList<MyVector>();
		// double totalLength = 0 - 0;
		for (int i = startIndex; i < endIndex; i++) {
			MyVector v = MyVector.initNewVector(points.get(i), points.get(i + 1), i, i + 1);
			vectors.add(v);
			// System.out.print("(" + v + " norm:" + v.norm() + ") ");
			// totalLength += v.norm();
		}

		// System.out.println("\nTotal vektor norm: " + totalLength);
		return vectors;
	}

	public static double norm(Point point, Point point2) {
		double x = point.x - point2.x;
		double y = point.y - point2.y;

		return Math.sqrt(x * x + y * y);
	}

	public static double norm(PointDouble point, PointDouble point2) {
		double x = point.x - point2.x;
		double y = point.y - point2.y;

		return Math.sqrt(x * x + y * y);
	}

	public static double norm(PointDouble point, Point point2) {
		double x = point.x - point2.x;
		double y = point.y - point2.y;

		return Math.sqrt(x * x + y * y);
	}
}
