package hr.fer.zemris.diprad.recognition.models.letters;

import java.awt.Point;
import java.util.List;

import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.JShape;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.PointDouble;

public class JModel {
	public static JShape recognize(BasicMovementWrapper bmw) {
		System.out.println("Ulaz");
		double totalNorm = CircularModel.calculateTotalNorm(bmw.getBm().getPoints(), 0,
				bmw.getBm().getPoints().size() - 1);
		int k = bmw.getBm().getPoints().size();
		List<PointDouble> sampledPoints = CircularModel.samplePoints(bmw.getBm().getPoints(), 0, k - 1, totalNorm, k);
		Point topRight, botLeft;
		int topRightIndex, botLeftIndex;

		if ((sampledPoints.get(0).y > sampledPoints.get(sampledPoints.size() - 1).y)
				&& (sampledPoints.get(0).x < sampledPoints.get(sampledPoints.size() - 1).x)) {
			topRight = bmw.getBm().getPoints().get(bmw.getBm().getPoints().size() - 1);
			topRightIndex = bmw.getBm().getPoints().size() - 1;
			botLeft = bmw.getBm().getPoints().get(0);
			botLeftIndex = 0;
		} else if ((sampledPoints.get(sampledPoints.size() - 1).y > sampledPoints.get(0).y)
				&& (sampledPoints.get(sampledPoints.size() - 1).x < sampledPoints.get(0).x)) {
			botLeft = bmw.getBm().getPoints().get(bmw.getBm().getPoints().size() - 1);
			botLeftIndex = bmw.getBm().getPoints().size() - 1;
			topRight = bmw.getBm().getPoints().get(0);
			topRightIndex = 0;
		} else {
			return null;
		}

		if (topRight.y - bmw.getBm().getBoundingBox().getP1().y > bmw.getBm().getBoundingBox().getP2().y - botLeft.y) {
			// f..j as in f
			CircularObject co = null;
			Line l = null;
			if (topRightIndex > botLeftIndex) {
				for (int i = topRightIndex / 3; i < topRightIndex; i++) {
					if (sampledPoints.get(i).y < sampledPoints.get(topRightIndex).y) {
						if (sampledPoints.get(i - 1).y >= sampledPoints.get(topRightIndex).y) {
							co = CircularModel.recognize(i - 1, topRightIndex, bmw, totalNorm, k, sampledPoints);
							l = LinearModel.recognize(sampledPoints, botLeftIndex, i - 1, bmw);
						}

						break;
					}
				}
			} else {
				for (int i = (2 * botLeftIndex) / 3; i > 0; i--) {
					if (sampledPoints.get(i).y < sampledPoints.get(topRightIndex).y) {
						if (sampledPoints.get(i + 1).y >= sampledPoints.get(topRightIndex).y) {
							co = CircularModel.recognize(0, i + 1, bmw, totalNorm, k, sampledPoints);
							l = LinearModel.recognize(sampledPoints, i + 1, botLeftIndex, bmw);
						}

						break;
					}
				}
			}

			if (co == null || l == null) {
				// System.out.println(co + " " + l);
				return null;
			}
			if (co.getMinMaxRatio() > 0.55 || co.getMinMaxRatio() < 0.15) {
				// System.out.println("co minMax: " + co.getMinMaxRatio());
				return null;
			}

			if (l.getType() != LineType.VERTICAL) {
				// System.out.println("Line not vertical");
				return null;
			}

			if (co.getTotalAngle() < 220 || co.getTotalAngle() > 330) {
				// System.out.println("invalid angle: " + co.getTotalAngle());
				return null;
			}

			if (co.getTheta() > 125 || co.getTheta() < 55) {
				// System.out.println("Bad opening position: " + co.getTheta());
				return null;
			}

			return new JShape(co, l, true);
		} else {
			// g..J
			System.out.println("Bok");
			CircularObject co = null;
			Line l = null;
			if (topRightIndex > botLeftIndex) {
				for (int i = (2 * topRightIndex) / 3; i > 0; i--) {
					if (sampledPoints.get(i).y > sampledPoints.get(botLeftIndex).y) {
						if (sampledPoints.get(i + 1).y <= sampledPoints.get(botLeftIndex).y) {
							co = CircularModel.recognize(0, i + 1, bmw, totalNorm, k, sampledPoints);
							l = LinearModel.recognize(sampledPoints, i + 1, topRightIndex, bmw);
						}

						break;
					}
				}
			} else {
				for (int i = botLeftIndex / 3; i < botLeftIndex; i++) {
					if (sampledPoints.get(i).y > sampledPoints.get(botLeftIndex).y) {
						if (sampledPoints.get(i - 1).y <= sampledPoints.get(botLeftIndex).y) {
							co = CircularModel.recognize(i - 1, botLeftIndex, bmw, totalNorm, k, sampledPoints);
							l = LinearModel.recognize(sampledPoints, 0, i - 1, bmw);
						}

						break;
					}
				}
			}

			if (co == null || l == null) {
				return null;
			}
			if (co.getMinMaxRatio() > 0.55 || co.getMinMaxRatio() < 0.15) {
				// System.out.println("co minMax: " + co.getMinMaxRatio());
				return null;
			}

			if (l.getType() != LineType.VERTICAL) {
				// System.out.println("Line not vertical");
				return null;
			}

			if (co.getTotalAngle() < 220 || co.getTotalAngle() > 330) {
				// System.out.println("invalid angle: " + co.getTotalAngle());
				return null;
			}

			if (co.getTheta() < -125 || co.getTheta() > -55) {
				// System.out.println("Bad opening position: " + co.getTheta());
				return null;
			}

			return new JShape(co, l, false);
		}
	}
}
