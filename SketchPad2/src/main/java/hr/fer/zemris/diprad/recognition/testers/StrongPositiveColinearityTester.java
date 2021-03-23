package hr.fer.zemris.diprad.recognition.testers;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.util.MyVector;

public class StrongPositiveColinearityTester implements Tester<MyVector> {

	@Override
	public boolean test(MyVector v1, MyVector v2) {
		return MyVector.scalarProduct(v1, v2) / (v1.norm() * v2.norm()) < 0.8;
	}

}
