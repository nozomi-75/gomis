/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
    private ImageCropper parent;         // Reference to parent for status updates
    private BufferedImage originalImage; // Current working image
    private BufferedImage scaledImage;   // Zoomed image for display
    private double scale = 1.0;          // Zoom level
    private Double aspectRatio = null;   // Aspect ratio for cropping (null = free)
    private Point startPoint;            // Selection start point
    private Point currentPoint;          // Current mouse position during drag
    private Rectangle selectionRect;     // Selected crop area
    private boolean isSelecting = false; // Selection in progress

    public ImagePanel(ImageCropper parent) {
        this.parent = parent;
        setBackground(Color.LIGHT_GRAY);
        setPreferredSize(new Dimension(400, 300));

        // Mouse listeners for selection
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                isSelecting = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isSelecting = false;
                if (selectionRect != null) {
                    int originalWidth = (int) Math.round(selectionRect.width / scale);
                    int originalHeight = (int) Math.round(selectionRect.height / scale);
                    parent.updateStatus("Selection: " + originalWidth + " x " + originalHeight + " pixels. Click Crop to apply.");
                } else {
                    parent.updateStatus("");
                }
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                currentPoint = e.getPoint();
                updateSelection();
                repaint();
            }
        });
    }

    /** Set a new image, reset scale and selection */
    public void setImage(BufferedImage image) {
        originalImage = image;
        scale = 1.0;
        clearSelection();
        updateScaledImage();
    }

    /** Get the current working image */
    public BufferedImage getOriginalImage() {
        return originalImage;
    }

    /** Get the current zoom scale */
    public double getScale() {
        return scale;
    }

    /** Zoom in */
    public void zoomIn() {
        setScale(scale * 1.2);
    }

    /** Zoom out */
    public void zoomOut() {
        setScale(scale / 1.2);
    }

    /** Set zoom scale with limits */
    private void setScale(double newScale) {
        scale = Math.max(0.1, Math.min(10.0, newScale));
        updateScaledImage();
    }

    /** Reset zoom and selection */
    public void resetZoomAndSelection() {
        scale = 1.0;
        clearSelection();
        updateScaledImage();
    }

    /** Legacy method (replaced by setImage for reset) */
    public void resetToOriginal() {
        scale = 1.0;
        clearSelection();
        updateScaledImage();
    }

    /** Set cropping aspect ratio */
    public void setAspectRatio(Double ratio) {
        aspectRatio = ratio;
        if (isSelecting) updateSelection();
        repaint();
    }

    /** Get the selection rectangle */
    public Rectangle getSelectionRect() {
        return selectionRect;
    }

    /** Clear the selection */
    public void clearSelection() {
        selectionRect = null;
        startPoint = null;
        currentPoint = null;
        repaint();
    }

    /** Update the scaled image based on zoom */
    private void updateScaledImage() {
        if (originalImage == null) {
            scaledImage = null;
            setPreferredSize(new Dimension(400, 300));
        } else {
            int width = (int) (originalImage.getWidth() * scale);
            int height = (int) (originalImage.getHeight() * scale);
            scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, width, height, null);
            g2d.dispose();
            setPreferredSize(new Dimension(width, height));
        }
        revalidate();
        repaint();
    }

    /** Update selection rectangle and status during drag */
    private void updateSelection() {
        if (startPoint == null || currentPoint == null) return;
        double x1 = startPoint.x;
        double y1 = startPoint.y;
        double x2 = currentPoint.x;
        double y2 = currentPoint.y;

        if (aspectRatio == null) {
            selectionRect = new Rectangle(
                    (int) Math.min(x1, x2),
                    (int) Math.min(y1, y2),
                    (int) Math.abs(x2 - x1),
                    (int) Math.abs(y2 - y1)
            );
        } else {
            double dx = x2 - x1;
            double dy = y2 - y1;
            double selectionWidth, selectionHeight;
            if (Math.abs(dx) / Math.abs(dy) > aspectRatio) {
                selectionHeight = Math.abs(dy);
                selectionWidth = selectionHeight * aspectRatio * Math.signum(dx);
            } else {
                selectionWidth = Math.abs(dx);
                selectionHeight = selectionWidth / aspectRatio * Math.signum(dy);
            }
            double rectX = Math.min(x1, x1 + selectionWidth);
            double rectY = Math.min(y1, y1 + selectionHeight);
            selectionRect = new Rectangle(
                    (int) rectX,
                    (int) rectY,
                    (int) Math.abs(selectionWidth),
                    (int) Math.abs(selectionHeight)
            );
        }
        // Keep selection within image bounds
        if (scaledImage != null) {
            selectionRect.x = Math.max(0, selectionRect.x);
            selectionRect.y = Math.max(0, selectionRect.y);
            selectionRect.width = Math.min(scaledImage.getWidth() - selectionRect.x, selectionRect.width);
            selectionRect.height = Math.min(scaledImage.getHeight() - selectionRect.y, selectionRect.height);
        }
        // Update status with selection size
        if (selectionRect != null && originalImage != null) {

            int originalWidth = (int) Math.round(selectionRect.width / scale);
            int originalHeight = (int) Math.round(selectionRect.height / scale);
            parent.updateStatus("Selecting: " + originalWidth + " x " + originalHeight + " pixels");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (scaledImage != null) {
            g.drawImage(scaledImage, 0, 0, this);
        }
        if (selectionRect != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(255, 0, 0, 100));
            g2d.fillRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
            g2d.setColor(Color.RED);
            g2d.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
        }
    }
}
