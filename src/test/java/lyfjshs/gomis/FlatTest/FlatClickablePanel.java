package lyfjshs.gomis.FlatTest;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

public class FlatClickablePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public FlatClickablePanel(String title, String description, String iconPath) {
        this.setLayout(new MigLayout("fillx", "[][][grow]", "[]"));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding
        this.setBackground(new Color(40, 40, 40));
        this.setOpaque(true);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Apply FlatLaf rounded border
        this.putClientProperty("JComponent.roundRect", true);
        this.putClientProperty("JComponent.arc", 20);

        JLabel iconLabel = new JLabel(new FlatSVGIcon(iconPath, 20, 20));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel descLabel = new JLabel("<html><i>" + description + "</i></html>");

        this.add(iconLabel, "gapright 10");
        this.add(titleLabel, "wrap");
        this.add(descLabel, "skip, wrap");

        // Hover Effect (Fix: Use FlatClickablePanel.this instead of this)
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                FlatClickablePanel.this.setBackground(new Color(50, 50, 50)); // Use FlatClickablePanel.this
            }

            @Override
            public void mouseExited(MouseEvent e) {
                FlatClickablePanel.this.setBackground(new Color(40, 40, 40)); // Use FlatClickablePanel.this
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, title + " clicked!");
            }
        });
    }
}
