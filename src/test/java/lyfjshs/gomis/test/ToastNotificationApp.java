package lyfjshs.gomis.test;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import net.miginfocom.swing.MigLayout;

public class ToastNotificationApp {
    private JFrame frame;
    private JLayeredPane layeredPane;
    private JPanel mainPanel;
    private JComboBox<String> positionSelect, animationInSelect, animationOutSelect;
    private JSpinner durationInput, entranceSpeedInput, exitSpeedInput;
    private boolean isDarkMode = false;

    // Colors and constants matching CSS variables
    private final Color SUCCESS_COLOR = new Color(0x4CAF50);
    private final Color ERROR_COLOR = new Color(0xF44336);
    private final Color INFO_COLOR = new Color(0x2196F3);
    private final Color WARNING_COLOR = new Color(0xFF9800);
    private final Color NEUTRAL_COLOR = new Color(0x757575);
    private final int TOAST_WIDTH = 350;
    private final int TOAST_RADIUS = 10;

    // Track toasts by position for proper stacking
    private final Map<String, List<ToastPanel>> toastsByPosition = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToastNotificationApp::new);
    }

    public ToastNotificationApp() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }

        frame = new JFrame("Enhanced Toast Notifications");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        frame.setContentPane(layeredPane);

        mainPanel = new JPanel(new MigLayout("wrap 1", "[center]", "[]20[]20[]"));
        mainPanel.setBounds(0, 0, frame.getWidth(), frame.getHeight());
        mainPanel.setOpaque(false);
        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);

        JPanel header = new JPanel(new MigLayout("ins 0", "[grow, fill][pref!]"));
        header.setOpaque(false);
        JLabel title = new JLabel("Enhanced Toast Notifications");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(isDarkMode ? Color.WHITE : new Color(0x333333));
        header.add(title, "growx");

        JPanel themeToggle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDarkMode ? new Color(0x555555) : new Color(0xE0E0E0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
                g2.setColor(Color.WHITE);
                int circleDiameter = getHeight() - 4;
                int circleX = isDarkMode ? getWidth() - circleDiameter - 2 : 2;
                g2.fillOval(circleX, 2, circleDiameter, circleDiameter);
                g2.dispose();
            }
        };
        themeToggle.setPreferredSize(new Dimension(50, 26));
        themeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        themeToggle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleTheme();
            }
        });
        header.add(themeToggle, "wrap");
        mainPanel.add(header, "growx");

        JPanel buttonPanel = new JPanel(new MigLayout("wrap 5", "[]10[]10[]10[]10[]", ""));
        buttonPanel.setOpaque(false);
        // Use FlatSVGIcon for button icons
        buttonPanel.add(createToastButton("Success", SUCCESS_COLOR, "icons/success.svg", "Success!",
                "Your action has been completed successfully."));
        buttonPanel.add(createToastButton("Error", ERROR_COLOR, "icons/error.svg", "Error!",
                "Oops! Something went wrong."));
        buttonPanel.add(createToastButton("Info", INFO_COLOR, "icons/info.svg", "Information",
                "Here is some information you should know."));
        buttonPanel.add(createToastButton("Warning", WARNING_COLOR, "icons/warning.svg", "Warning",
                "Please be cautious with your next action."));
        buttonPanel.add(createToastButton("Neutral", NEUTRAL_COLOR, "icons/neutral.svg", "Notification",
                "This is a neutral notification message."));
        mainPanel.add(buttonPanel, "center");

        JPanel optionsPanel = new JPanel(new MigLayout("wrap 4", "[][grow, fill][][grow, fill]", "[]10[]"));
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Customize Toasts"));

        optionsPanel.add(new JLabel("Position:"));
        positionSelect = new JComboBox<>(
                new String[]{"top-right", "top-left", "top-center", "bottom-right", "bottom-left", "bottom-center"});
        optionsPanel.add(positionSelect, "span 3");

        optionsPanel.add(new JLabel("Entrance Animation:"));
        animationInSelect = new JComboBox<>(
                new String[]{"slideInRight", "slideInLeft", "slideInUp", "slideInDown", "bounceIn", "fadeIn"});
        optionsPanel.add(animationInSelect, "span 3");

        optionsPanel.add(new JLabel("Exit Animation:"));
        animationOutSelect = new JComboBox<>(
                new String[]{"slideOutRight", "slideOutLeft", "slideOutUp", "slideOutDown", "bounceOut", "fadeOut"});
        optionsPanel.add(animationOutSelect, "span 3");

        optionsPanel.add(new JLabel("Duration (seconds):"));
        durationInput = new JSpinner(new SpinnerNumberModel(5.0, 1.0, 20.0, 0.5));
        optionsPanel.add(durationInput, "span 3");

        optionsPanel.add(new JLabel("Entrance Speed (ms):"));
        entranceSpeedInput = new JSpinner(new SpinnerNumberModel(500, 100, 2000, 100));
        optionsPanel.add(entranceSpeedInput, "span 3");

        optionsPanel.add(new JLabel("Exit Speed (ms):"));
        exitSpeedInput = new JSpinner(new SpinnerNumberModel(500, 100, 2000, 100));
        optionsPanel.add(exitSpeedInput, "span 3");

        mainPanel.add(optionsPanel, "growx");

        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                mainPanel.setBounds(0, 0, frame.getWidth(), frame.getHeight());
                // Reposition all toasts on resize
                for (String position : toastsByPosition.keySet()) {
                    List<ToastPanel> toasts = new ArrayList<>(toastsByPosition.get(position));
                    toastsByPosition.get(position).clear();
                    for (ToastPanel toast : toasts) {
                        Point pos = calculateToastPosition(position, toast);
                        toast.setBounds(pos.x, pos.y, toast.getWidth(), toast.getHeight());
                        toastsByPosition.get(position).add(toast);
                    }
                }
            }
        });

        frame.setVisible(true);
    }

    private JButton createToastButton(String text, Color color, String iconPath, String title, String message) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        // Set the button icon using FlatSVGIcon (size 16x16)
        button.setIcon(new FlatSVGIcon(iconPath, 16, 16));
        button.addActionListener(e -> showCustomToast(text.toLowerCase()));
        return button;
    }

    private ToastData getToastData(String type) {
        switch (type) {
            case "success":
                return new ToastData("Success!", "Your action has been completed successfully.", SUCCESS_COLOR);
            case "error":
                return new ToastData("Error!", "Oops! Something went wrong. Please try again.", ERROR_COLOR);
            case "info":
                return new ToastData("Information", "Here is some information you should know.", INFO_COLOR);
            case "warning":
                return new ToastData("Warning", "Please be cautious with your next action.", WARNING_COLOR);
            case "neutral":
                return new ToastData("Notification", "This is a neutral notification message.", NEUTRAL_COLOR);
            default:
                return new ToastData("Notification", "This is a neutral notification message.", NEUTRAL_COLOR);
        }
    }

    private void showCustomToast(String type) {
        ToastData data = getToastData(type);
        double durationSec = (Double) durationInput.getValue();
        int durationMs = (int) (durationSec * 1000);
        String animIn = (String) animationInSelect.getSelectedItem();
        String animOut = (String) animationOutSelect.getSelectedItem();
        int entranceSpeed = (Integer) entranceSpeedInput.getValue();
        int exitSpeed = (Integer) exitSpeedInput.getValue();
        String position = (String) positionSelect.getSelectedItem();

        ToastPanel toast = new ToastPanel(data, durationMs, animIn, animOut, entranceSpeed, exitSpeed, position);
        Point pos = calculateToastPosition(position, toast);
        toast.setBounds(pos.x, pos.y, TOAST_WIDTH, toast.getPreferredSize().height);
        layeredPane.add(toast, JLayeredPane.POPUP_LAYER);

        // Track the toast by position
        toastsByPosition.computeIfAbsent(position, k -> new ArrayList<>()).add(toast);

        layeredPane.revalidate();
        layeredPane.repaint();

        toast.animateEntrance();
    }

    private Point calculateToastPosition(String position, ToastPanel toast) {
        int margin = 16;
        int x = margin, y = margin;
        int containerWidth = frame.getWidth();
        int containerHeight = frame.getHeight();
        int toastHeight = toast.getPreferredSize().height;

        switch (position) {
            case "top-right":
                x = containerWidth - TOAST_WIDTH - margin;
                y = margin;
                break;
            case "top-left":
                x = margin;
                y = margin;
                break;
            case "top-center":
                x = (containerWidth - TOAST_WIDTH) / 2;
                y = margin;
                break;
            case "bottom-right":
                x = containerWidth - TOAST_WIDTH - margin;
                y = containerHeight - toastHeight - margin;
                break;
            case "bottom-left":
                x = margin;
                y = containerHeight - toastHeight - margin;
                break;
            case "bottom-center":
                x = (containerWidth - TOAST_WIDTH) / 2;
                y = containerHeight - toastHeight - margin;
                break;
        }

        // Stack toasts vertically in the same position
        List<ToastPanel> toasts = toastsByPosition.getOrDefault(position, new ArrayList<>());
        for (ToastPanel existingToast : toasts) {
            if (existingToast == toast)
                continue;
            Rectangle bounds = existingToast.getBounds();
            if (Math.abs(bounds.x - x) < 10) { // Same x-position, stack vertically
                if (position.contains("top")) {
                    if (bounds.y >= y) {
                        y = bounds.y + bounds.height + margin;
                    }
                } else { // bottom positions
                    if (bounds.y <= y) {
                        y = bounds.y - toastHeight - margin;
                    }
                }
            }
        }

        return new Point(x, y);
    }

    private void removeToast(ToastPanel toast, String position) {
        layeredPane.remove(toast);
        toastsByPosition.get(position).remove(toast);
        layeredPane.revalidate();
        layeredPane.repaint();

        // Reposition remaining toasts
        List<ToastPanel> toasts = new ArrayList<>(toastsByPosition.get(position));
        toastsByPosition.get(position).clear();
        for (ToastPanel remainingToast : toasts) {
            Point pos = calculateToastPosition(position, remainingToast);
            remainingToast.setBounds(pos.x, pos.y, remainingToast.getWidth(), remainingToast.getHeight());
            toastsByPosition.get(position).add(remainingToast);
        }
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        try {
            UIManager.setLookAndFeel(isDarkMode ? new FlatDarkLaf() : new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(frame);
            mainPanel.repaint();
            layeredPane.repaint();
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
    }

    private static class ToastData {
        String title, message;
        Color color;

        public ToastData(String title, String message, Color color) {
            this.title = title;
            this.message = message;
            this.color = color;
        }
    }

    private class ToastPanel extends JPanel {
        private ToastData data;
        private int duration;
        private String animIn, animOut;
        private String position;
        private float alpha = 0f;
        private int entranceSpeed;
        private int exitSpeed;
        private static final int ANIMATION_STEPS = 20;

        public ToastPanel(ToastData data, int duration, String animIn, String animOut, int entranceSpeed, int exitSpeed, String position) {
            this.data = data;
            this.duration = duration;
            this.animIn = animIn;
            this.animOut = animOut;
            this.entranceSpeed = entranceSpeed;
            this.exitSpeed = exitSpeed;
            this.position = position;
            setOpaque(false);
            setLayout(new MigLayout("insets 16 10 16 10, fillx", "[pref!][grow][pref!]", "[][]5[]"));

            // Use FlatSVGIcon for toast icon based on toast type.
            String iconPath;
            switch (data.title.toLowerCase()) {
                case "success!":
                    iconPath = "icons/success.svg";
                    break;
                case "error!":
                    iconPath = "icons/error.svg";
                    break;
                case "information":
                    iconPath = "icons/info.svg";
                    break;
                case "warning":
                    iconPath = "icons/warning.svg";
                    break;
                default:
                    iconPath = "icons/neutral.svg";
                    break;
            }
            // Create a label with the SVG icon (size 56x56)
            JLabel iconLabel = new JLabel(new FlatSVGIcon(iconPath, 56, 56));
            iconLabel.setPreferredSize(new Dimension(56, 56));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(iconLabel, "cell 0 0");

            JPanel contentPanel = new JPanel(new MigLayout("wrap 1, insets 0", "[grow]", "[]5[]"));
            contentPanel.setOpaque(false);
            JLabel titleLabel = new JLabel(data.title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setForeground(isDarkMode ? Color.WHITE : new Color(0x333333));
            contentPanel.add(titleLabel);
            JLabel messageLabel = new JLabel(data.message);
            messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            messageLabel.setForeground(isDarkMode ? Color.LIGHT_GRAY : Color.GRAY);
            contentPanel.add(messageLabel);
            add(contentPanel, "cell 1 0, growx, gapleft 10");

            JButton closeButton = new JButton("×");
            closeButton.setBorderPainted(false);
            closeButton.setFocusPainted(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setForeground(isDarkMode ? Color.LIGHT_GRAY : Color.GRAY);
            closeButton.addActionListener(e -> animateExit());
            add(closeButton, "cell 2 0, aligny top");

            JProgressBar progressBar = new JProgressBar();
            progressBar.setForeground(data.color);
            progressBar.setPreferredSize(new Dimension(TOAST_WIDTH - 20, 4));
            progressBar.setMaximum(duration);
            progressBar.setValue(duration);
            add(progressBar, "cell 0 1, span 3, growx");

            javax.swing.Timer progressTimer = new javax.swing.Timer(100, e -> {
                int timeLeft = progressBar.getValue() - 100;
                progressBar.setValue(timeLeft);
                if (timeLeft <= 0) {
                    ((javax.swing.Timer) e.getSource()).stop();
                    animateExit();
                }
            });
            progressTimer.start();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    progressTimer.stop();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    progressTimer.start();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    animateExit();
                }
            });

            // Set border and ensure proper rendering
            setBorder(new LineBorder(data.color, 5, true) {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    g.setColor(getLineColor());
                    g.fillRect(x, y, getThickness(), height);
                }
            });

            // Calculate preferred size based on content
            doLayout();
            setPreferredSize(new Dimension(TOAST_WIDTH, getPreferredSize().height));
        }

        public void animateEntrance() {
            int animationDelay = entranceSpeed / ANIMATION_STEPS;
            javax.swing.Timer timer = new javax.swing.Timer(animationDelay, null);
            Point target = getLocation();
            Point start = new Point(target);
            if (animIn.equals("slideInRight"))
                start.x += 100;
            else if (animIn.equals("slideInLeft"))
                start.x -= 100;
            else if (animIn.equals("slideInUp"))
                start.y += 100;
            else if (animIn.equals("slideInDown"))
                start.y -= 100;
            else if (animIn.equals("bounceIn"))
                start.x += 150;
            setLocation(start);
            final int deltaX = (target.x - start.x) / ANIMATION_STEPS;
            final int deltaY = (target.y - start.y) / ANIMATION_STEPS;

            timer.addActionListener(e -> {
                int count = timer.getActionListeners().length > 1 ? 0
                        : ((javax.swing.Timer) e.getSource()).getActionCommand() != null
                                ? Integer.parseInt(((javax.swing.Timer) e.getSource()).getActionCommand()) + 1
                                : 0;
                ((javax.swing.Timer) e.getSource()).setActionCommand(String.valueOf(count));

                if (animIn.equals("bounceIn") && count == ANIMATION_STEPS / 2) {
                    setLocation(target.x - deltaX * 2, target.y - deltaY * 2);
                } else {
                    setLocation(getX() + deltaX, getY() + deltaY);
                }
                alpha = Math.min(1f, alpha + (1f / ANIMATION_STEPS));
                repaint();

                if (count >= ANIMATION_STEPS) {
                    timer.stop();
                    setLocation(target);
                    alpha = 1f;
                    repaint();
                }
            });
            timer.start();
        }

        public void animateExit() {
            int animationDelay = exitSpeed / ANIMATION_STEPS;
            javax.swing.Timer timer = new javax.swing.Timer(animationDelay, null);
            Point start = getLocation();
            Point target = new Point(start);
            if (animOut.equals("slideOutRight"))
                target.x += 100;
            else if (animOut.equals("slideOutLeft"))
                target.x -= 100;
            else if (animOut.equals("slideOutUp"))
                target.y -= 100;
            else if (animOut.equals("slideOutDown"))
                target.y += 100;
            else if (animOut.equals("bounceOut"))
                target.x += 150;
            final int deltaX = (target.x - start.x) / ANIMATION_STEPS;
            final int deltaY = (target.y - start.y) / ANIMATION_STEPS;

            timer.addActionListener(e -> {
                int count = timer.getActionListeners().length > 1 ? 0
                        : ((javax.swing.Timer) e.getSource()).getActionCommand() != null
                                ? Integer.parseInt(((javax.swing.Timer) e.getSource()).getActionCommand()) + 1
                                : 0;
                ((javax.swing.Timer) e.getSource()).setActionCommand(String.valueOf(count));

                if (animOut.equals("bounceOut") && count == ANIMATION_STEPS / 2) {
                    setLocation(start.x + deltaX * 2, start.y + deltaY * 2);
                } else {
                    setLocation(getX() + deltaX, getY() + deltaY);
                }
                alpha = Math.max(0f, alpha - (1f / ANIMATION_STEPS));
                repaint();

                if (count >= ANIMATION_STEPS) {
                    timer.stop();
                    removeToast(ToastPanel.this, position);
                }
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(new Color(0, 0, 0, 80));
            for (int i = 1; i <= 5; i++) {
                g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, TOAST_RADIUS, TOAST_RADIUS);
            }
            g2.setColor(isDarkMode ? new Color(0x2D2D2D) : Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, TOAST_RADIUS, TOAST_RADIUS);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
