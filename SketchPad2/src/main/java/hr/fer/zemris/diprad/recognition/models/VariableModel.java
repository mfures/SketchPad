package hr.fer.zemris.diprad.recognition.models;

import java.util.List;

import hr.fer.zemris.diprad.util.PointDouble;
import hr.fer.zemris.diprad.util.Rectangle;

public class VariableModel {
	private String variable;
	private CharacterModel[] cms;
	private Rectangle boundingBox;
	private boolean center = false;

	public VariableModel(CharacterModel... cms) {
		if (cms.length > 2 || cms.length < 1) {
			throw new RuntimeException(
					"Incorrect number of carachter models given. Expected no more than 2 and no less than 1. This should never happen. Number of: "
							+ cms.length);
		}

		this.cms = cms;

		if (cms.length == 1) {
			this.variable = cms[0].getCharacter();
			this.boundingBox = cms[0].getBoundingBox().copyOf();
		} else {
			this.variable = cms[0].getCharacter() + cms[1].getCharacter();

			Rectangle bb1 = cms[0].getBoundingBox();
			Rectangle bb2 = cms[1].getBoundingBox();
			// KTableModel.debugDrawRectangleStatic(bb1);
			// KTableModel.debugDrawRectangleStatic(bb2);
			int overlapLength = (bb1.getIp2().x - bb2.getIp1().x);

			if (overlapLength > 0) {
				if ((overlapLength * 1.0) / (bb1.getIp2().x - bb1.getIp1().x) > 0.8
						|| (overlapLength * 1.0) / (bb2.getIp2().x - bb2.getIp1().x) > 0.8) {
					if (cms[0].getCharacter() != "f" && cms[0].getCharacter() != "g" && cms[0].getCharacter() != "W"
							&& cms[0].getCharacter() != "Y" && cms[0].getCharacter() != "X") {
						System.out.println((overlapLength * 1.0) / (bb1.getIp2().x - bb1.getIp1().x));
						System.out.println((overlapLength * 1.0) / (bb2.getIp2().x - bb2.getIp1().x));
						throw new RuntimeException(
								"Too much variable and index overlap (character): " + cms[0].getCharacter());
					}
				}
			} else {
				double distance = -overlapLength;
				double minDistanceRatio = Math.min((distance) / (bb1.getIp2().x - bb1.getIp1().x),
						(distance) / (bb2.getIp2().x - bb2.getIp1().x));
				if (minDistanceRatio > 1) {
					throw new RuntimeException("Variable and index to far away (ratio):" + minDistanceRatio);
				}
			}

			double height1 = bb1.getIp2().y - bb1.getIp1().y;

			double height2 = bb2.getIp2().y - bb2.getIp1().y;

			if (height2 / height1 < 0.15 || height2 / height1 > 2.1) {
				throw new RuntimeException("Bad variable height ration" + height2 / height1);
			}

			if (bb1.getIp1().y > bb2.getIp1().y) {
				throw new RuntimeException("Index heigher start than char");
			}
			if (bb2.getIp1().y > bb1.getIp2().y + 0.05 * height1) {
				throw new RuntimeException("Index starts too low");
			}
			if (bb1.getIp2().y > bb2.getIp2().y) {
				throw new RuntimeException("Index heigher end than char");
			}

			initBoundingBox();
		}
	}

	private void initBoundingBox() {
		double minX = cms[0].getBoundingBox().getIp1().x;
		double maxX = cms[0].getBoundingBox().getIp2().x;
		double minY = cms[0].getBoundingBox().getIp1().y;
		double maxY = cms[0].getBoundingBox().getIp2().y;

		for (int i = 1; i < cms.length; i++) {
			minX = Math.min(minX, cms[i].getBoundingBox().getIp1().x);
			maxX = Math.max(maxX, cms[i].getBoundingBox().getIp2().x);
			minY = Math.min(minY, cms[i].getBoundingBox().getIp1().y);
			maxY = Math.max(maxY, cms[i].getBoundingBox().getIp2().y);
		}

		this.boundingBox = new Rectangle(new PointDouble(minX, minY), new PointDouble(maxX, maxY));
	}

	public String getVariable() {
		return variable;
	}

	public CharacterModel[] getCms() {
		return cms;
	}

	public Rectangle getBoundingBox() {
		return boundingBox;
	}

	public boolean hasIndex() {
		return this.cms.length == 2;
	}

	public double getCharacherHeight() {
		Rectangle bb1 = cms[0].getBmws()[0].getBm().getBoundingBox();
		return bb1.getIp2().y - bb1.getIp1().y;
	}

	public boolean isCenter() {
		return center;
	}

	public void setCenter(boolean center) {
		this.center = center;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((variable == null) ? 0 : variable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableModel other = (VariableModel) obj;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}

	public static void validateVariableListOrFail(List<VariableModel> vms) {
		if (vms.size() > 1) {
			for (int i = 0; i < vms.size() - 1; i++) {
				validOrFail(vms.get(i), vms.get(i + 1));
			}
		}
	}

	public static void validOrFail(VariableModel vm1, VariableModel vm2) {
		Rectangle bb1 = vm1.getBoundingBox();
		Rectangle bb2 = vm2.getBoundingBox();

		int overlapLength = (bb1.getIp2().x - bb2.getIp1().x);

		if (overlapLength > 0) {
			if ((overlapLength * 1.0) / (bb1.getIp2().x - bb1.getIp1().x) > 0.4
					|| (overlapLength * 1.0) / (bb2.getIp2().x - bb2.getIp1().x) > 0.4) {
				throw new RuntimeException("Too overlap beetwen variables");

			}
		} else {
			double distance = -overlapLength;
			double minDistanceRatio = Math.min((distance) / (bb1.getIp2().x - bb1.getIp1().x),
					(distance) / (bb2.getIp2().x - bb2.getIp1().x));
			if (minDistanceRatio > 1.5) {
				throw new RuntimeException("Variables to far away (ratio):" + minDistanceRatio);
			}
		}

		double height1 = vm1.getCharacherHeight();
		double height2 = vm2.getCharacherHeight();

		if (height2 / height1 < 0.45 || height2 / height1 > 1.0 / 0.45) {
			throw new RuntimeException("Bad character variable height ration" + height2 / height1 + " " + vm1.variable
					+ " " + vm2.variable);
		}

		if (bb1.getIp1().y + 0.35 * height1 > bb2.getIp2().y) {
			throw new RuntimeException("Variable to high: " + vm2.getVariable());
		}
		if (bb2.getIp1().y + 0.35 * height1 > bb1.getIp2().y) {
			throw new RuntimeException("Variable to high: " + vm1.getVariable());
		}

	}

	public void setBmwsToUsed() {
		for (CharacterModel cm : cms) {
			cm.setBmsToUsed();
		}
	}
}
