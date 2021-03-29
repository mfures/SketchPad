package hr.fer.zemris.diprad.recognition.testers;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.util.MyVector;

public class WeakNegativeColinearityTester implements Tester<MyVector> {
	private double minValue;

	public WeakNegativeColinearityTester() {
		minValue = -0.3;
	}

	public WeakNegativeColinearityTester(double minValue) {
		this.minValue = minValue;
	}

	@Override
	public boolean test(MyVector v1, MyVector v2) {
		// System.out.println(MyVector.scalarProduct(v1, v2) / (v1.norm() * v2.norm()));
		return MyVector.scalarProduct(v1, v2) / (v1.norm() * v2.norm()) < minValue;
	}

}
