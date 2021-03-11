package hr.fer.zemris.diprad.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import hr.fer.zemris.diprad.SketchPad2;

public class ExitAction extends AbstractAction {
	private static final long serialVersionUID = -83481833466420513L;
	private SketchPad2 sP;

	public ExitAction(SketchPad2 sP) {
		this.putValue(Action.NAME, "Exit");
		this.putValue(Action.SHORT_DESCRIPTION, "Exit application");
		this.sP = sP;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (sP.getModel().isModified()) {
			// TODO Pitaj za zatvaranje
		}

		new AppendAction(sP).actionPerformed(e);
		sP.dispose();
	}
}
