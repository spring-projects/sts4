package ui;

import java.awt.BorderLayout;
import java.util.concurrent.CompletableFuture;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class ProgressBar extends JPanel {

	private JProgressBar progressBar;

	public ProgressBar(BoundedRangeModel model) {
		super(new BorderLayout());

		progressBar = new JProgressBar(model);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		JPanel panel = new JPanel();
		panel.add(progressBar);

		add(panel, BorderLayout.CENTER);
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	}
	
	public static ProgressApi create(int totalWork) {
		BoundedRangeModel model = new DefaultBoundedRangeModel(0, 0, 0, totalWork);
		CompletableFuture<JFrame> window = new CompletableFuture<>();
		final ProgressApi api = new ProgressApi() {
			
			@Override
			public void worked(int amount) {
				model.setValue(model.getValue()+amount);
			}

			@Override
			public void done() {
				window.thenAccept(w -> SwingUtilities.invokeLater(() -> {
					w.setVisible(false);
					w.dispose();
				}));
			}
			
		};

		SwingUtilities.invokeLater(() -> {
			try {
				//Create and set up the window.
				JFrame frame = new JFrame("Unpacking...");
				frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	
				//Create and set up the content pane.
				JComponent newContentPane = new ProgressBar(model);
				newContentPane.setOpaque(true); //content panes must be opaque
				frame.setContentPane(newContentPane);
	
				//Display the window.
				frame.pack();
				frame.setVisible(true);
				window.complete(frame);
			} catch (Throwable e) {
				window.completeExceptionally(e);
			}
		});
		return api;
	}
}