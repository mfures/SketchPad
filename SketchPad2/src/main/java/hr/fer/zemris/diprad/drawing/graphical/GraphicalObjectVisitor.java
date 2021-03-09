package hr.fer.zemris.diprad.drawing.graphical;

import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;
import hr.fer.zemris.diprad.drawing.graphical.objects.KTable;
import hr.fer.zemris.diprad.drawing.graphical.objects.SelectionRectangle;

public interface GraphicalObjectVisitor {

	void visit(BasicMovement basicMovement);

	void visit(SelectionRectangle selectionRectangle);

	void visit(KTable kTable);
}
