package hr.fer.zemris.diprad.recognition.models.letters;

import java.awt.Point;
import java.util.List;

import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class BModel {
	public static boolean recognize(BasicMovementWrapper bmw1, BasicMovementWrapper bmw2) {
		return false;
	}
//		List<CircularObject> cos = initCOs(bmw1);
//		if (cos.size() != 2) {
//			cos = initCOs(bmw2);
//			if (cos.size() != 2) {
//				// System.out.println("1.Invalid circular objects");
//				return false;
//			}
//
//			return testForCircularObjectsAndBMW(cos, bmw1);
//		}
//
//		return testForCircularObjectsAndBMW(cos, bmw2);
//	}
//
//	private static List<CircularObject> initCOs(BasicMovementWrapper bmw) {
//		List<CircularObject> cos = CircularModel.recognize(bmw);
//		if (co == null) {
//			System.out.println("co was null");
//			return null;
//		}
//
//		if (co.getMinMaxRatio() > 0.55 || co.getMinMaxRatio() < 0.15) {
//			System.out.println("co minMax: " + co.getMinMaxRatio());
//			return null;
//		}
//
//		return cos;
//	}
//
//	private static boolean angleAndMinMaxTest(CircularObject co1) {
//		if (co1.getTotalAngle() < 240 || co1.getTotalAngle() > 305) {
//			System.out.println("invalid angle: " + co1.getTotalAngle());
//			return false;
//		}
//		if (co1.getMinMaxRatio() > 0.55 || co1.getMinMaxRatio() < 0.18) {
//			System.out.println("Bad min max:" + co1.getMinMaxRatio());
//			return false;
//		}
//
//		return true;
//	}
//
//	private static boolean testForCircularObjectsAndBMW(List<CircularObject> cos, BasicMovementWrapper bmw) {
//		Line l = LinearModel.recognize(bmw);
//		if (l.getType() != LineType.VERTICAL) {
//			System.out.println("Line not vertical");
//			return false;
//		}
//
//		if (co.getTotalAngle() < 220 || co.getTotalAngle() > 305) {
//			System.out.println("invalid angle: " + co.getTotalAngle());
//			return false;
//		}
//
//		if (!(co.getTheta() > 145 || co.getTheta() < -145)) {
//			System.out.println("Bad opening position: " + co.getTheta());
//			return false;
//		}
//
//		Point p1 = co.getBmw().getBm().getPoints().get(0);
//		Point p2 = co.getBmw().getBm().getPoints().get(co.getBmw().getBm().getPoints().size() - 1);
//
//		double dist1 = l.distanceFromPointToLine(p1);
//		double dist2 = l.distanceFromPointToLine(p2);
//		if (l.length() * 0.9 > Math.abs(p1.y - p2.y)) {
//			return false;
//		}
//
//		System.out.println(dist1 + " " + dist2);
//		System.out.println(co.getTotalNorm());
//
//		if (dist1 - 0.15 * co.getTotalNorm() > 0 || dist2 - 0.15 * co.getTotalNorm() > 0) {
//			return false;
//		}
//
//		System.out.println(p1.x + " " + co.getTotalNorm() + " " + l.forY(p1.y));
//		System.out.println(p2.x + " " + co.getTotalNorm() + " " + l.forY(p2.y));
//
//		if (p1.x - l.forY(p1.y) - 0.075 * co.getTotalNorm() > 0
//				|| p2.x - l.forY(p2.y) - 0.075 * co.getTotalNorm() > 0) {
//			return false;
//		}
//
//		return true;
//	}
}
