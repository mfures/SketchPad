package hr.fer.zemris.diprad.actions;

import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import hr.fer.zemris.diprad.SketchPad2;

public class SaveAsAction extends AbstractAction {
	private static final long serialVersionUID = -2541945667821846566L;
	private SketchPad2 sP;

	public SaveAsAction(SketchPad2 sP) {
		this.putValue(Action.NAME, "Save As");
		this.putValue(Action.SHORT_DESCRIPTION, "Save As file to disc");
		this.sP = sP;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser jfc = new JFileChooser();
		FileNameExtensionFilter fnef = new FileNameExtensionFilter("SketchPad file", "spf");
		jfc.setFileFilter(fnef);

		if (jfc.showSaveDialog(sP) == JFileChooser.APPROVE_OPTION) {
			Path selected = jfc.getSelectedFile().toPath();
			if (!selected.toString().endsWith(".spf")) {
				selected = Paths.get(selected.toString() + ".spf");
			}

			if (Files.exists(selected)) {
				if (JOptionPane.showConfirmDialog(sP, "Do you want to overwrite?", "Overwrite",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
					return;
				}
			}

			sP.setPath(selected);

			try {
				Files.write(sP.getPath(), sP.getModel().print().getBytes(StandardCharsets.UTF_8));
			} catch (Exception e2) {
				JOptionPane.showConfirmDialog(sP, "Couldn't save");
				return;
			}
		}
	}
}
