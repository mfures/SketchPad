package hr.fer.zemris.diprad.recognition.models;

import hr.fer.zemris.diprad.util.Rectangle;

public class VariableModel {
	private String variable;
	private CharacterModel[] cms;

	public VariableModel(CharacterModel... cms) {

		this.cms = cms;

		if (cms.length == 1) {
			this.variable = cms[0].getCharacter();
		} else if (cms.length == 2) {
			this.variable = cms[0].getCharacter() + cms[1].getCharacter();

			Rectangle bb1 = cms[0].getBmws()[0].getBm().getBoundingBox();
			Rectangle bb2 = cms[1].getBmws()[0].getBm().getBoundingBox();
			int overlapLength = (bb1.getIp2().x - bb2.getIp1().x);

			if (overlapLength > 0) {
				if ((overlapLength * 1.0) / (bb1.getIp2().x - bb1.getIp1().x) > 0.35
						|| (overlapLength * 1.0) / (bb2.getIp2().x - bb2.getIp1().x) > 0.35) {
					throw new RuntimeException("Too much variable and index overlap");

				}
			} else {
				double distance = -overlapLength;
				double minDistanceRatio = Math.min((distance) / (bb1.getIp2().x - bb1.getIp1().x),
						(distance) / (bb2.getIp2().x - bb2.getIp1().x));
				if (minDistanceRatio > 0.7) {
					throw new RuntimeException("Variable and index to far away (ratio):" + minDistanceRatio);
				}
			}

			double height1 = bb1.getIp2().y - bb1.getIp1().y;
			double height2 = bb2.getIp2().y - bb2.getIp1().y;

			if (height2 / height1 < 0.35 || height2 / height1 > 1.3) {
				throw new RuntimeException("Bad variable height ration" + height2 / height1);
			}

			if (bb1.getIp1().y > bb2.getIp1().y) {
				throw new RuntimeException("Index heigher start than char");
			}
			if (bb2.getIp1().y > bb1.getIp2().y - 0.2 * height1) {
				throw new RuntimeException("Index starts too low");
			}
			if (bb1.getIp2().y > bb2.getIp2().y) {
				throw new RuntimeException("Index heigher end than char");
			}
		} else {
			throw new RuntimeException(
					"Too much carachter models given. Expected no more than 2. This should never happen");
		}
	}

	public String getVariable() {
		return variable;
	}

	public CharacterModel[] getCms() {
		return cms;
	}

	public boolean hasIndex() {
		return this.cms.length == 2;
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
}
