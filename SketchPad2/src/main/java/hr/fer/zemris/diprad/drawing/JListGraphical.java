package hr.fer.zemris.diprad.drawing;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import hr.fer.zemris.diprad.SketchPad2;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObject;
import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;
import hr.fer.zemris.diprad.drawing.model.DrawingModel;

public class JListGraphical extends JList<GraphicalObject> {
	private static final long serialVersionUID = -3670416929897656280L;
	private SketchPad2 sP;
	private int selected = -1;

	public JListGraphical(ListModel<GraphicalObject> list, SketchPad2 sP) {
		super(list);
		this.sP = sP;
		init();
	}

	private void init() {
		this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				handleSelected(JListGraphical.this.getSelectedIndex());
			}
		});

		JListGraphical jList = this;
		jList.setVisible(true);
		jList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {

				DrawingModel model = sP.getModel();
				if (!(jList.getModel().getSize() > 0)) {
					return;
				}

				GraphicalObject sel = jList.getSelectedValue();
				int cIndex = jList.getSelectedIndex();
				int code = e.getKeyCode();
				if (sel == null)
					return;

				if (code == KeyEvent.VK_DELETE) {
					if (cIndex == selected) {
						selected = -1;
					} else if (cIndex < selected) {
						selected--;
					}

					model.remove(sel);
				}

				if (code == KeyEvent.VK_PLUS || code == KeyEvent.VK_ADD) {
					if (cIndex > 0 && selected >= 0) {
						if (selected == cIndex) {
							selected--;
						} else if (selected + 1 == cIndex) {
							selected++;
						}
					}

					model.changeOrder(sel, -1);
				}

				if (code == KeyEvent.VK_MINUS || code == KeyEvent.VK_SUBTRACT) {
					if (cIndex < jList.getModel().getSize() - 1 && selected >= 0) {
						if (selected == cIndex) {
							selected++;
						} else if (selected - 1 == cIndex) {
							selected--;
						}
					}

					model.changeOrder(sel, 1);
				}
			}

		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int index = jList.getSelectedIndex();
				if (index == -1)
					return;

				handleSelected(index);

				if (e.getClickCount() == 2) {
					GraphicalObject go = sP.getModel().getObject(index);

					if (go instanceof BasicMovement) {
						String result = (String) JOptionPane.showInputDialog(sP, "Select one of the color",
								"Swing Tester", JOptionPane.PLAIN_MESSAGE, null, null, "");

						if (result != null) {
							((BasicMovement) go).setLabel(result);
						}
					}
				}
			}

		});
	}

	private void handleSelected(int index) {
		DrawingModel model = sP.getModel();

		if (index < 0) {
			if (selected >= 0) {
				model.getObject(selected).setColor(Colors.DEFAULT);
				selected = -1;
				sP.getCanvas().repaint();
			}
		} else if (index != selected) {
			if (selected >= 0) {
				model.getObject(selected).setColor(Colors.DEFAULT);
			}
			selected = index;
			model.getObject(index).setColor(Colors.SELECTED);
			sP.getCanvas().repaint();
		}
	}
}
