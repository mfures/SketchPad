package hr.fer.zemris.diprad.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import hr.fer.zemris.diprad.SketchPad2;

public class NewDocumentAction extends AbstractAction {
	private static final long serialVersionUID = -834818366420513L;
	private SketchPad2 sP;

	public NewDocumentAction(SketchPad2 sP) {
		this.putValue(Action.NAME, "New");
		this.putValue(Action.SHORT_DESCRIPTION, "Create new document");
		this.sP = sP;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (sP.getModel().isModified()) {
			// TODO Pitaj za spremanje
		}

		new AppendAction(sP).actionPerformed(e);

		sP.getModel().clear();
		sP.getCanvas().repaint();
	}
}
