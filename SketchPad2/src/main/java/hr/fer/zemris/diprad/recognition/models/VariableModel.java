package hr.fer.zemris.diprad.recognition.models;

public class VariableModel {
	private String variable;
	private CharacterModel[] cms;

	public VariableModel(CharacterModel... cms) {

		this.cms = cms;

		if (cms.length == 1) {
			this.variable = cms[0].getCharacter();
		} else if (cms.length == 2) {
			this.variable = cms[0].getCharacter() + cms[1].getCharacter();

			// TODO
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
