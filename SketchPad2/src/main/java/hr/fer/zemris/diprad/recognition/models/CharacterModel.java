package hr.fer.zemris.diprad.recognition.models;

import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.PointDouble;
import hr.fer.zemris.diprad.util.Rectangle;

public class CharacterModel {
	private String character;
	private Rectangle boundingBox;
	private BasicMovementWrapper[] bmws;
	private CircularObject co;

	public CharacterModel(String s, BasicMovementWrapper... bmws) {
		if (bmws.length == 0) {
			throw new RuntimeException("Empty bmw array given");
		}

		this.character = s;

		double minX = bmws[0].getBm().getBoundingBox().getIp1().x;
		double maxX = bmws[0].getBm().getBoundingBox().getIp2().x;
		double minY = bmws[0].getBm().getBoundingBox().getIp1().y;
		double maxY = bmws[0].getBm().getBoundingBox().getIp2().y;

		for (int i = 1; i < bmws.length; i++) {
			minX = Math.min(minX, bmws[i].getBm().getBoundingBox().getIp1().x);
			maxX = Math.max(maxX, bmws[i].getBm().getBoundingBox().getIp2().x);
			minY = Math.min(minY, bmws[i].getBm().getBoundingBox().getIp1().y);
			maxY = Math.max(maxY, bmws[i].getBm().getBoundingBox().getIp2().y);
		}

		this.boundingBox = new Rectangle(new PointDouble(minX, minY), new PointDouble(maxX, maxY));
		this.bmws = bmws;
	}

	public CharacterModel(String string, CircularObject co, BasicMovementWrapper bmw) {
		this.character = string;
		this.co = co;
		this.bmws = new BasicMovementWrapper[1];
		this.bmws[0] = bmw;
		this.boundingBox = bmws[0].getBm().getBoundingBox().copyOf();
	}

	public String getCharacter() {
		return character;
	}

	public Rectangle getBoundingBox() {
		return boundingBox;
	}

	public BasicMovementWrapper[] getBmws() {
		return bmws;
	}

	public CircularObject getCo() {
		return co;
	}

	public void setBmsToUsed() {
		for (BasicMovementWrapper bmw : bmws) {
			bmw.incTotalHandeledFragments();
		}
	}

	public void decBmsFragments() {
		for (BasicMovementWrapper bmw : bmws) {
			bmw.decTotalHandeledFragments();
		}
	}
	
	public void setBmsToDiscarded() {
		for (BasicMovementWrapper bmw : bmws) {
			bmw.setDiscarded(true);
		}
	}
}
