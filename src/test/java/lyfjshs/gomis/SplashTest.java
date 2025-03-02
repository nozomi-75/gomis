package lyfjshs.gomis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;
import com.formdev.flatlaf.FlatDarkLaf;

public class SplashTest extends JFrame {
	private static final int WIDTH = 500;
	private static final int HEIGHT = 400;
	private static final Color BACKGROUND_COLOR = new Color(26, 26, 46);
	private static final Color ACCENT_COLOR_1 = new Color(79, 172, 254);
	private static final Color ACCENT_COLOR_2 = new Color(0, 242, 254);

	private JPanel mainPanel;
	private JPanel logoPanel;
	private ProgressPanel progressPanel;
	private JLabel loadingLabel;
	private BackgroundCirclesPanel circlesPanel;
	private Timer dotAnimationTimer;
	private int dotState = 0;

	public SplashTest() {
		setTitle("Loading...");
		setSize(WIDTH, HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setUndecorated(true);

		initComponents();
		startAnimations();
	}

	private void initComponents() {
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(BACKGROUND_COLOR);
		mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

		// Load and resize the image to 150x150
		ImageIcon originalIcon = new ImageIcon(getClass().getResource("/LYFJSHS_Logo.png"));
		Image scaledImage = originalIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
		ImageIcon logoIcon = new ImageIcon(scaledImage);

		// Create JLabel with resized image
		JLabel logoLabel = new JLabel(logoIcon);

		// Logo panel
		logoPanel = new JPanel();
		logoPanel.setOpaque(false);
		logoPanel.add(logoLabel);

		// Progress panel
		progressPanel = new ProgressPanel();
		progressPanel.setPreferredSize(new Dimension(300, 6));

		// Loading text
		loadingLabel = new JLabel("LOADING");
		loadingLabel.setForeground(Color.WHITE);
		loadingLabel.setFont(new Font("Arial", Font.PLAIN, 24));
		loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Center panel for logo and loading components
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setOpaque(false);
		centerPanel.add(Box.createVerticalGlue());

		JPanel logoContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
		logoContainer.setOpaque(false);
		logoContainer.add(logoPanel);
		centerPanel.add(logoContainer);

		centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));

		JPanel progressContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
		progressContainer.setOpaque(false);
		progressContainer.add(progressPanel);
		centerPanel.add(progressContainer);

		centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		JPanel textContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
		textContainer.setOpaque(false);
		textContainer.add(loadingLabel);
		centerPanel.add(textContainer);

		centerPanel.add(Box.createVerticalGlue());

		// Background circles
		circlesPanel = new BackgroundCirclesPanel();

		// Add components to main panel
		mainPanel.setLayout(new OverlayLayout(mainPanel));
		mainPanel.add(centerPanel);
		mainPanel.add(circlesPanel);

		setContentPane(mainPanel);
	}

	private void startAnimations() {
		// Animate dots
		dotAnimationTimer = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StringBuilder dots = new StringBuilder();
				for (int i = 0; i < 3; i++) {
					dots.append((i <= dotState) ? "." : " ");
				}
				loadingLabel.setText("LOADING" + dots.toString());
				dotState = (dotState + 1) % 4;
			}
		});
		dotAnimationTimer.start();
	}

	// Logo Panel with animations
	class LogoPanel extends JPanel {
		private Timer rotationTimer;
		private Timer pulseTimer;
		private double rotation = 0;
		private double scale = 1.0;
		private boolean growing = true;
		private double outerDashOffset = 0;
		private double innerDashOffset = 0;

		public LogoPanel() {
			setOpaque(false);

			rotationTimer = new Timer(50, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					rotation += 2;
					if (rotation >= 360) {
						rotation = 0;
					}

					outerDashOffset += 2;
					if (outerDashOffset >= 314) {
						outerDashOffset = 0;
					}

					innerDashOffset += 2;
					if (innerDashOffset >= 251) {
						innerDashOffset = 0;
					}

					repaint();
				}
			});

			pulseTimer = new Timer(50, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (growing) {
						scale += 0.005;
						if (scale >= 1.05) {
							growing = false;
						}
					} else {
						scale -= 0.005;
						if (scale <= 0.95) {
							growing = true;
						}
					}
					repaint();
				}
			});

			rotationTimer.start();
			pulseTimer.start();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int w = getWidth();
			int h = getHeight();
			int centerX = w / 2;
			int centerY = h / 2;

			// Apply scaling
			g2d.translate(centerX, centerY);
			g2d.scale(scale, scale);
			g2d.translate(-centerX, -centerY);

			// Outer circle
			float[] dashPattern1 = { 5, 5 };
			BasicStroke stroke1 = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dashPattern1,
					(float) outerDashOffset);
			g2d.setStroke(stroke1);
			g2d.setColor(ACCENT_COLOR_1);
			g2d.drawOval(centerX - 50, centerY - 50, 100, 100);

			// Inner circle
			float[] dashPattern2 = { 4, 4 };
			BasicStroke stroke2 = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dashPattern2,
					(float) innerDashOffset);
			g2d.setStroke(stroke2);
			g2d.setColor(ACCENT_COLOR_2);
			g2d.drawOval(centerX - 40, centerY - 40, 80, 80);

			// Center shape
			g2d.rotate(Math.toRadians(rotation), centerX, centerY);

			Path2D diamond = new Path2D.Double();
			diamond.moveTo(centerX, centerY - 30);
			diamond.lineTo(centerX + 25, centerY + 10);
			diamond.lineTo(centerX, centerY + 30);
			diamond.lineTo(centerX - 25, centerY + 10);
			diamond.closePath();

			g2d.setColor(BACKGROUND_COLOR);
			g2d.fill(diamond);
			g2d.setColor(Color.WHITE);
			g2d.setStroke(new BasicStroke(2));
			g2d.draw(diamond);

			g2d.dispose();
		}
	}

	// Progress bar with animation
	class ProgressPanel extends JPanel {
		private Timer progressTimer;
		private double progress = 0;
		private boolean increasing = true;

		public ProgressPanel() {
			setOpaque(false);

			progressTimer = new Timer(30, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (increasing) {
						progress += 0.01;
						if (progress >= 1.0) {
							increasing = false;
						}
					} else {
						progress -= 0.01;
						if (progress <= 0) {
							increasing = true;
						}
					}
					repaint();
				}
			});

			progressTimer.start();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int w = getWidth();
			int h = getHeight();

			// Background
			g2d.setColor(new Color(255, 255, 255, 25));
			g2d.fillRoundRect(0, 0, w, h, h, h);

			// Progress
			int progressWidth = (int) (w * progress);
			if (progressWidth > 0) {
				GradientPaint gradient = new GradientPaint(0, 0, ACCENT_COLOR_1, w, 0, ACCENT_COLOR_2);
				g2d.setPaint(gradient);
				g2d.fillRoundRect(0, 0, progressWidth, h, h, h);
			}

			g2d.dispose();
		}
	}

	// Background animated circles
	class BackgroundCirclesPanel extends JPanel {
		private ArrayList<Circle> circles = new ArrayList<>();
		private Timer animationTimer;
		private Random random = new Random();

		public BackgroundCirclesPanel() {
			setOpaque(false);

			// Create circles
			for (int i = 0; i < 15; i++) {
				circles.add(new Circle());
			}

			animationTimer = new Timer(50, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					for (Circle circle : circles) {
						circle.update();
					}
					repaint();
				}
			});

			animationTimer.start();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			for (Circle circle : circles) {
				circle.draw(g2d);
			}

			g2d.dispose();
		}

		class Circle {
			private double x, y;
			private double size;
			private double opacity = 0;
			private double opacityStep;
			private boolean increasing = true;

			public Circle() {
				size = random.nextDouble() * 200 + 50;
				x = random.nextDouble() * getWidth() - size / 2;
				y = random.nextDouble() * getHeight() - size / 2;
				opacityStep = 0.005 + random.nextDouble() * 0.01;
			}

			public void update() {
				if (increasing) {
					opacity += opacityStep;
					if (opacity >= 0.15) {
						increasing = false;
					}
				} else {
					opacity -= opacityStep;
					if (opacity <= 0) {
						increasing = true;
						// Reposition when completely faded out
						x = random.nextDouble() * getWidth() - size / 2;
						y = random.nextDouble() * getHeight() - size / 2;
					}
				}
			}

			public void draw(Graphics2D g2d) {
				if (opacity <= 0)
					return;

				GradientPaint gradient = new GradientPaint((float) (x), (float) (y),
						new Color(ACCENT_COLOR_1.getRed(), ACCENT_COLOR_1.getGreen(), ACCENT_COLOR_1.getBlue(),
								(int) (opacity * 255)),
						(float) (x + size), (float) (y + size), new Color(ACCENT_COLOR_2.getRed(),
								ACCENT_COLOR_2.getGreen(), ACCENT_COLOR_2.getBlue(), (int) (opacity * 255)));

				g2d.setPaint(gradient);
				g2d.fill(new Ellipse2D.Double(x, y, size, size));
			}
		}
	}

	public static void main(String[] args) {
		try {
			// Set FlatLaf look and feel
			UIManager.setLookAndFeel(new FlatDarkLaf());
		} catch (Exception ex) {
			System.err.println("Failed to initialize FlatLaf");
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SplashTest splash = new SplashTest();
				splash.setVisible(true);

				// For demo purposes, close after 10 seconds
				// Remove this in production and call splash.dispose() when your app is ready
				Timer closeTimer = new Timer(10000, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						splash.dispose();
						System.exit(0);
					}
				});
				closeTimer.setRepeats(false);
				closeTimer.start();
			}
		});
	}
}