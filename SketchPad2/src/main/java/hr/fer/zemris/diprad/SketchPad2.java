package hr.fer.zemris.diprad;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import hr.fer.zemris.diprad.actions.ExitAction;
import hr.fer.zemris.diprad.actions.OpenAction;
import hr.fer.zemris.diprad.actions.SaveAction;
import hr.fer.zemris.diprad.actions.SaveAsAction;
import hr.fer.zemris.diprad.drawing.JListGraphical;
import hr.fer.zemris.diprad.drawing.Tool;
import hr.fer.zemris.diprad.drawing.model.DrawingModel;
import hr.fer.zemris.diprad.drawing.model.DrawingModelImpl;
import hr.fer.zemris.diprad.drawing.model.DrawingObjectsListModel;
import hr.fer.zemris.diprad.drawing.model.JDrawingCanvas;
import hr.fer.zemris.diprad.drawing.tools.PencilTool;
import hr.fer.zemris.diprad.drawing.tools.SelectorTool;
import hr.fer.zemris.diprad.drawing.tools.ToolManager;

public class SketchPad2 extends JFrame {
	private static final long serialVersionUID = -4039070322155680266L;
	private DrawingModel model;
	private JDrawingCanvas canvas;
	private DrawingObjectsListModel list;
	private Tool pencilTool;
	private Tool selectorTool;
	private ToolManager toolManager;
	private ExitAction exit;
	private boolean altPressed;
	private Path path;
	private SaveAsAction saveAs;
	private SaveAction save;
	private OpenAction open;

	public SketchPad2() {
		setSize(1000, 600);
		setTitle("SketchPad");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit.actionPerformed(null);
			}
		});

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		model = new DrawingModelImpl();
		toolManager = new ToolManager(model);
		canvas = new JDrawingCanvas(model, toolManager);
		toolManager.setCanvas(canvas);

		pencilTool = new PencilTool(canvas, model);
		selectorTool = new SelectorTool(this);
		toolManager.init(pencilTool);

		list = new DrawingObjectsListModel(model);

		cp.add(canvas, BorderLayout.CENTER);

		JListGraphical jlist = new JListGraphical(list, this);
		cp.add(new JScrollPane(jlist), BorderLayout.EAST);

		initActions();

		JMenu file = new JMenu("File");
		file.add(open);
		file.add(save);
		file.add(saveAs);
		file.addSeparator();
		file.add(exit);

		JMenuBar menubar = new JMenuBar();
		menubar.add(file);

		setJMenuBar(menubar);

		altPressed = false;
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				switch (e.getID()) {
				case KeyEvent.KEY_PRESSED:
					if (e.getKeyCode() == 18) {
						if (!altPressed) {
							altPressed = true;
							toolManager.setActiveTool(selectorTool);
						}
					}
					break;
				case KeyEvent.KEY_RELEASED:
					if (e.getKeyCode() == 18) {
						altPressed = false;
						toolManager.setActiveTool(pencilTool);
					}
					break;

				}
				return false;
			}
		});
	}

	private void initActions() {
		saveAs = new SaveAsAction(this);
		save = new SaveAction(this);
		open = new OpenAction(this);
		exit = new ExitAction(this);
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public DrawingModel getModel() {
		return model;
	}

	public JDrawingCanvas getCanvas() {
		return canvas;
	}

	public SaveAsAction getSaveAs() {
		return saveAs;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new SketchPad2().setVisible(true));
	}
}
