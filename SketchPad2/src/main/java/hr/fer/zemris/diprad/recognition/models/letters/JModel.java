package hr.fer.zemris.diprad.recognition.models.letters;

import java.util.List;

import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.PointDouble;

public class JModel {
	public static boolean recognize(BasicMovementWrapper bmw) {
		double totalNorm = CircularModel.calculateTotalNorm(bmw.getBm().getPoints(), 0,
				bmw.getBm().getPoints().size() - 1);
		int k = bmw.getBm().getPoints().size();
		List<PointDouble> sampledPoints = CircularModel.samplePoints(bmw.getBm().getPoints(), 0, k - 1, totalNorm, k);
		return false;
	}
}
