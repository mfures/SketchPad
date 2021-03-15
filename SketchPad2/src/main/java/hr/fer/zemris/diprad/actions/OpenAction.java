package hr.fer.zemris.diprad.actions;

import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import hr.fer.zemris.diprad.SketchPad2;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObject;
import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;
import hr.fer.zemris.diprad.drawing.graphical.objects.KTable;
import hr.fer.zemris.diprad.drawing.model.DrawingModel;

public class OpenAction extends AbstractAction {
	private static final long serialVersionUID = -5449067830352809058L;
	private SketchPad2 sP;

	public OpenAction(SketchPad2 sP) {
		this.putValue(Action.NAME, "Open");
		this.putValue(Action.SHORT_DESCRIPTION, "Open file from disc");
		this.sP = sP;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO check if user wants to save document

		JFileChooser jfc = new JFileChooser();
		FileNameExtensionFilter fnef = new FileNameExtensionFilter("SketchPad file", "spf");
		jfc.setFileFilter(fnef);
		if (jfc.showOpenDialog(sP) != JFileChooser.APPROVE_OPTION)
			return;

		Path selected = jfc.getSelectedFile().toPath();

		try {
			List<String> lines = Files.readAllLines(selected);
			List<GraphicalObject> tmp = new ArrayList<>();

			for (String line : lines) {
				if (line.startsWith("!")) {
					// skips, will be used for multidocument model
				} else if (line.startsWith("BM")) {
					tmp.add(BasicMovement.parseBasicMovement(line));
				} else if (line.startsWith("KT")) {
					tmp.add(KTable.parseKTable(line));
				} else {
					throw new RuntimeException();
				}
			}

			DrawingModel model = sP.getModel();
			model.clear();

			for (GraphicalObject go : tmp) {
				model.add(go);
			}

			sP.setPath(selected);
		} catch (Exception e2) {
			throw new RuntimeException(e2);
		}

	}

}
