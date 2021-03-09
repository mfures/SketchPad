package hr.fer.zemris.diprad.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.swing.AbstractAction;
import javax.swing.Action;

import hr.fer.zemris.diprad.SketchPad2;

public class AppendAction extends AbstractAction {
	private static final long serialVersionUID = -544906783035289058L;
	private SketchPad2 sP;

	public AppendAction(SketchPad2 sP) {
		this.putValue(Action.NAME, "Append");
		this.putValue(Action.SHORT_DESCRIPTION, "Append");
		this.sP = sP;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			StringBuilder sb = new StringBuilder();

			BufferedReader br = new BufferedReader(new FileReader("db2.spf"));
			String line = br.readLine();
			br.close();

			MyCounter mc = new MyCounter(Integer.parseInt(line));

			sb.append(sP.getModel().printLabeled(mc));
			Files.write(Paths.get("db.spf"), sb.toString().getBytes(), StandardOpenOption.APPEND);
			Files.write(Paths.get("db2.spf"), String.valueOf(mc.c).getBytes());
		} catch (IOException ex) {
			System.out.println("Nejde");
		}
	}

	public class MyCounter {
		public int c;

		private MyCounter(int c) {
			this.c = c;
		}
	}
}
