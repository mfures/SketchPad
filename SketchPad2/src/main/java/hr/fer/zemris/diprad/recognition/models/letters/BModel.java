package hr.fer.zemris.diprad.recognition.models.letters;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.recognition.models.CharacterModel;
import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class BModel {
	private static final String B = "B";

	public static CharacterModel recognize(BasicMovementWrapper bmw1, BasicMovementWrapper bmw2) {

		List<CircularObject> cos = initCOs(bmw1);
		if (cos == null) {
			cos = initCOs(bmw2);
			if (cos == null) {
				//System.out.println("1.Invalid circular objects");
				return null;
			}

			return testForCircularObjectsAndBMW(cos, bmw1, bmw2);
		}

		return testForCircularObjectsAndBMW(cos, bmw2, bmw1);
	}

	private static List<CircularObject> initCOs(BasicMovementWrapper bmw) {
		int breakPosition = CircularModel.generateBestBreakPoint(bmw);
		if (breakPosition == -1) {
			// System.out.println("Bad number of break points: ");
			return null;
		}

		CircularObject co1, co2;
		int deltaX;
		if (bmw.getBm().getPoints().get(0).y < bmw.getBm().getPoints().get(bmw.getBm().getPoints().size() - 1).y) {
			co1 = CircularModel.recognize(bmw.getBm().getPoints(), 0, breakPosition, bmw);
			co2 = CircularModel.recognize(bmw.getBm().getPoints(), breakPosition, bmw.getBm().getPoints().size() - 1, bmw);
			deltaX = bmw.getBm().getPoints().get(bmw.getBm().getPoints().size() - 1).x - bmw.getBm().getPoints().get(0).x;
		} else {
			co2 = CircularModel.recognize(bmw.getBm().getPoints(), 0, breakPosition, bmw);
			co1 = CircularModel.recognize(bmw.getBm().getPoints(), breakPosition, bmw.getBm().getPoints().size() - 1, bmw);
			deltaX = -(bmw.getBm().getPoints().get(bmw.getBm().getPoints().size() - 1).x - bmw.getBm().getPoints().get(0).x);
		}

		if (co1 == null || co2 == null) {
			//System.out.println("Some are not circular");
			return null;
		}

		double avgNorm = (co1.getTotalNorm() + co2.getTotalNorm()) / 2;
		// Jesu li prošli srednju točku s lijeve strane
		if (bmw.getBm().getPoints().get(0).x > bmw.getBm().getPoints().get(breakPosition).x + avgNorm * 0.1) {
			//System.out.println("1");
			return null;
		}
		if (bmw.getBm().getPoints().get(bmw.getBm().getPoints().size() - 1).x > bmw.getBm().getPoints().get(breakPosition).x
				+ avgNorm * 0.1) {
			//System.out.println("2");
			return null;
		}
		// Jesu li prošli srednju točku s desne strane
		if (co1.getBoundingBox().getP2().x < bmw.getBm().getPoints().get(breakPosition).x + avgNorm * 0.05) {
			//System.out.println("11");
			return null;
		}
		if (co2.getBoundingBox().getP2().x < bmw.getBm().getPoints().get(breakPosition).x + avgNorm * 0.05) {
			//System.out.println("22");
			return null;
		}

		// je li donja točka dalja od gornje, ili jesu li tu negdje
		if (deltaX - 0.15 * avgNorm > 0) {
			//System.out.println("3. 1 point to far left in comparioson to last");
			return null;
		}

		if (!(angleAndMinMaxTest(co1) && angleAndMinMaxTest(co2))) {
			//System.out.println("Wat");
			return null;
		}

		if (!(co1.getTheta() > 130 || co1.getTheta() < -145)) {
			//System.out.println("Bad opening position: " + co1.getTheta());
			return null;
		}

		if (!(co2.getTheta() > 145 || co2.getTheta() < -130)) {
			//System.out.println("Bad opening position: " + co2.getTheta());
			return null;
		}

		if (co2.getBoundingBox().getWidth() < co1.getBoundingBox().getWidth() * 0.8) {
			//System.out.println("Upper part too wide");
			return null;
		}
		if (co2.getBoundingBox().getWidth() > co1.getBoundingBox().getWidth() * 2.1) {
			System.out.println(co2.getBoundingBox().getWidth() / co1.getBoundingBox().getWidth());
			//System.out.println("Lower part too wide");
			return null;
		}
		if (co2.getBoundingBox().getHeight() < co1.getBoundingBox().getHeight() * 0.8) {
			//System.out.println("Upper part too high");
			return null;
		}
		if (co2.getBoundingBox().getHeight() > co1.getBoundingBox().getHeight() * 2.1) {
			//System.out.println("Lower part too high");
			return null;
		}
		if (co2.getBoundingBox().getP2().x < co1.getBoundingBox().getP2().x - 0.2 * avgNorm) {
			return null;
		}

		List<CircularObject> cos = new ArrayList<>();
		cos.add(co1);
		cos.add(co2);
		return cos;
	}

	private static boolean angleAndMinMaxTest(CircularObject co1) {
		if (co1.getTotalAngle() < 240 || co1.getTotalAngle() > 305) {
			//System.out.println("invalid angle: " + co1.getTotalAngle());
			return false;
		}
		if (co1.getMinMaxRatio() > 0.55 || co1.getMinMaxRatio() < 0.18) {
			//System.out.println("Bad min max:" + co1.getMinMaxRatio());
			return false;
		}

		return true;
	}

	private static CharacterModel testForCircularObjectsAndBMW(List<CircularObject> cos, BasicMovementWrapper bmw,
			BasicMovementWrapper bmw2) {
		Line l = LinearModel.recognize(bmw);
		if (l.getType() != LineType.VERTICAL) {
			//System.out.println("Line not vertical");
			return null;
		}

		Point p1 = cos.get(0).getBmw().getBm().getPoints().get(0);
		Point p2 = cos.get(1).getBmw().getBm().getPoints().get(0);
		Point p3 = cos.get(1).getBmw().getBm().getPoints().get(cos.get(1).getBmw().getBm().getPoints().size() - 1);

		double dist1 = l.distanceFromPointToLine(p1);
		double dist2 = l.distanceFromPointToLine(p2);
		double dist3 = l.distanceFromPointToLine(p3);

		if (l.length() * 0.9 > Math.abs(p1.y - p3.y)) {
			return null;
		}

		double totalNorm = cos.get(0).getTotalNorm() + cos.get(1).getTotalNorm();

		if (dist1 - 0.15 * totalNorm > 0 || dist2 - 0.15 * totalNorm > 0 || dist3 - 0.15 * totalNorm > 0) {
			return null;
		}

		if (p1.x - l.forY(p1.y) - 0.075 * totalNorm > 0 || p2.x - l.forY(p2.y) - 0.075 * totalNorm > 0
				|| p3.x - l.forY(p3.y) - 0.075 * totalNorm > 0) {
			return null;
		}

		return new CharacterModel(B, bmw, bmw2);
	}
}
