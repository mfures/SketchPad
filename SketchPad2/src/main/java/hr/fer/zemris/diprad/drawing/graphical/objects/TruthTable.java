package hr.fer.zemris.diprad.drawing.graphical.objects;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import hr.fer.zemris.diprad.drawing.graphical.GraphicalObject;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObjectVisitor;
import hr.fer.zemris.diprad.drawing.graphical.objects.KTable.Value;
import hr.fer.zemris.diprad.util.ColRow;

public class TruthTable extends GraphicalObject {
	private KTable kTable;
	private Point p;
	private int varHeight = 50;
	private int valHeight = 20;

	public TruthTable(KTable kTable) {
		this.kTable = kTable;
		this.p = new Point(kTable.getP());
		this.p.x += kTable.getWidth() + 150;
	}

	public String getFunctionName() {
		return this.kTable.getFunctionName();
	}

	public Set<String> variables() {
		return this.kTable.getBoolTableVariables();
	}

	public int height() {
		return varHeight + valHeight * ((int) Math.pow(2, variables().size()));
	}

	public int width() {
		return varHeight * (variables().size() + 1);
	}

	public int getVarHeight() {
		return varHeight;
	}

	public int getValHeight() {
		return valHeight;
	}

	public int getValueAt(int... values) {
		int i = 0;

		List<Integer> rows = new ArrayList<>();
		List<Integer> cols = new ArrayList<>();
		int row = 0;
		int col = 0;

		for (String s : this.variables()) {
			List<ColRow> colRows = kTable.xRangeYrange(s, values[i]);
			for (ColRow colRow : colRows) {
				if (colRow.col) {
					if (cols.contains(colRow.i)) {
						col = colRow.i;
					} else {
						if (cols.isEmpty()) {
							col = colRow.i;
						}
						cols.add(colRow.i);
					}
				} else {
					if (rows.contains(colRow.i)) {
						row = colRow.i;
					} else {
						if (rows.isEmpty()) {
							row = colRow.i;
						}
						rows.add(colRow.i);
					}
				}
			}

			i++;
		}
		return kTable.getValues()[row][col].value;
	}

	public Value[][] getValues() {
		return kTable.getValues();
	}

	public Point getP() {
		return p;
	}

	@Override
	public void accept(GraphicalObjectVisitor v) {
		v.visit(this);
	}

	@Override
	public boolean isInRect(int minX, int maxX, int minY, int max) {
		return false;
	}

	@Override
	public String toString() {
		return "TruthTable:" + this.hashCode();
	}

	@Override
	public boolean youInterested(Point p) {
		if (p.x > (this.p.x + width() - varHeight) && p.y > (this.p.y + varHeight) && p.x < (this.p.x + width())
				&& p.y < (this.p.y + height()))
			return true;

		return false;
	}

	@Override
	public void handleIntrest(Point point) {
		int i = (point.y - p.y - varHeight) / valHeight;
		if (variables().size() == 4) {
			incValueAt(i / 8, (i / 4) % 2, (i / 2) % 2, i % 2);
		} else if (variables().size() == 3) {
			incValueAt((i / 4), (i / 2) % 2, i % 2);
		} else {
			incValueAt((i / 2), i % 2);
		}
	}

	@Override
	public String print() {
		return "";
	}

	private void incValueAt(int... values) {
		int i = 0;

		List<Integer> rows = new ArrayList<>();
		List<Integer> cols = new ArrayList<>();
		int row = 0;
		int col = 0;

		for (String s : this.variables()) {
			List<ColRow> colRows = kTable.xRangeYrange(s, values[i]);
			for (ColRow colRow : colRows) {
				if (colRow.col) {
					if (cols.contains(colRow.i)) {
						col = colRow.i;
					} else {
						if (cols.isEmpty()) {
							col = colRow.i;
						}
						cols.add(colRow.i);
					}
				} else {
					if (rows.contains(colRow.i)) {
						row = colRow.i;
					} else {
						if (rows.isEmpty()) {
							row = colRow.i;
						}
						rows.add(colRow.i);
					}
				}
			}

			i++;
		}

		kTable.getValues()[row][col].incValue();
		;
	}
}
