package hr.fer.zemris.diprad.actions;

import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import hr.fer.zemris.diprad.SketchPad2;

public class SaveAction extends AbstractAction {
	private static final long serialVersionUID = -8011420591394148807L;
	private SketchPad2 sP;

	public SaveAction(SketchPad2 sP) {
		this.putValue(Action.NAME, "Save");
		this.putValue(Action.SHORT_DESCRIPTION, "Save file to disc");
		this.sP = sP;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (sP.getPath() == null) {
			sP.getSaveAs().actionPerformed(e);
			return;
		}

		try {
			Files.write(sP.getPath(), sP.getModel().print().getBytes(StandardCharsets.UTF_8));
		} catch (Exception e2) {
			JOptionPane.showConfirmDialog(sP, "Couldn't save");
			return;
		}
	}

}
