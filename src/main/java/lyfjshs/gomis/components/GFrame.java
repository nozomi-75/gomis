package lyfjshs.gomis.components;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.LayoutManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

import lyfjshs.gomis.Database.DBConnection;

/**
 * The {@code GFrame} class extends {@code JFrame} and provides a customized
 * application window with additional utility methods for refreshing and
 * replacing the content panel.
 */
public class GFrame extends JFrame {

	/**
	 * Constructs a {@code GFrame} with a specified width, height, and visibility
	 * state, along with customizable title and icon.
	 * 
	 * @param width   The width of the frame.
	 * @param height  The height of the frame.
	 * @param visible {@code true} to make the frame visible, {@code false}
	 *                otherwise.
	 * @param title   The title of the frame.
	 * @param icon    The icon of the frame.
	 */
	public GFrame(int width, int height, boolean visible, String title, ImageIcon icon) {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(width, height);
		this.setLocationRelativeTo(null);
		this.setPreferredSize(new Dimension(1360, 768));
		this.setMinimumSize(new Dimension(800, 600));
		this.setTitle(title);
		// this.setIconImage(icon.getImage());
		this.setVisible(visible);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int confirm = JOptionPane.showConfirmDialog(null, 
					"Are you sure you want to exit?", "Exit Confirmation", 
					JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
		            DBConnection.closeAllConnections(); // Close the database connection
					System.exit(0);
				}
			}
		});
	}

	/**
	 * Sets the size of the frame along with a preferred size.
	 * 
	 * @param width      The actual width of the frame.
	 * @param height     The actual height of the frame.
	 * @param prefWidth  The preferred width of the frame.
	 * @param prefHeight The preferred height of the frame.
	 */
	public void setSize(int width, int height, int prefWidth, int prefHeight) {
		this.setPreferredSize(new Dimension(prefWidth, prefHeight));
		this.setSize(width, height);
	}

	/**
	 * Refreshes the frame by revalidating and repainting the content pane. This
	 * ensures that UI changes are reflected immediately.
	 */
	public void refresh() {
		FlatAnimatedLafChange.showSnapshot();
		this.getContentPane().revalidate();
		this.getContentPane().repaint();
		FlatAnimatedLafChange.hideSnapshotWithAnimation();
	}

	/**
	 * Replaces the current content panel with a new one, updating the layout and
	 * title of the frame.
	 * 
	 * @param contentPanel The new content panel to be displayed.
	 * @param manager      The layout manager to be applied to the content pane.
	 * @param title        The title to be set for the frame.
	 */
	public void replaceContentPanel(JComponent contentPanel, LayoutManager manager, String title) {
		EventQueue.invokeLater(() -> {
			FlatAnimatedLafChange.showSnapshot();
			this.getContentPane().removeAll();
			this.getContentPane().revalidate();
			this.getContentPane().repaint();
			this.getContentPane().setLayout(manager);

			this.setTitle(title);
			setContentPane(contentPanel);
			FlatAnimatedLafChange.hideSnapshotWithAnimation();
		});
	}
}
