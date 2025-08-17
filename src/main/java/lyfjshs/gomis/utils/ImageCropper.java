/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.utils;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class ImageCropper extends JPanel {
    private JButton cropButton, zoomInButton, zoomOutButton, resetButton;
    private JLabel statusLabel;
    private ImagePanel imagePanel;
    private JScrollPane scrollPane;
    private BufferedImage initialImage;
    private Consumer<BufferedImage> onCropComplete;

    public ImageCropper(BufferedImage image, Consumer<BufferedImage> onCropComplete) {
        this.initialImage = image;
        this.onCropComplete = onCropComplete;
        setLayout(new BorderLayout());
        initializeComponents();
        loadImage(image);
    }

    private void initializeComponents() {
        // Create buttons
        cropButton = new JButton("Crop Image");
        zoomInButton = new JButton("Zoom In (+)");
        zoomOutButton = new JButton("Zoom Out (-)");
        resetButton = new JButton("Reset");
        statusLabel = new JLabel("Drag to adjust crop area", SwingConstants.CENTER);
        
        // Initialize image panel with 1:1 aspect ratio
        imagePanel = new ImagePanel(this);
        imagePanel.setAspectRatio(1.0); // Force 1:1 aspect ratio for profile pictures
        
        // Control panel setup
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);
        controlPanel.add(resetButton);
        controlPanel.add(cropButton);

        // Add components
        scrollPane = new JScrollPane(imagePanel);
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Add listeners
        cropButton.addActionListener(e -> cropImage());
        zoomInButton.addActionListener(e -> imagePanel.zoomIn());
        zoomOutButton.addActionListener(e -> imagePanel.zoomOut());
        resetButton.addActionListener(e -> resetImage());
    }

    private void loadImage(BufferedImage image) {
        if (image != null) {
            imagePanel.setImage(image);
            enableControls(true);
        }
    }

    private void cropImage() {
        Rectangle selection = imagePanel.getSelectionRect();
        if (selection == null) {
            showError("Please select an area to crop.");
            return;
        }

        try {
            double scale = imagePanel.getScale();
            int x = (int) Math.round(selection.x / scale);
            int y = (int) Math.round(selection.y / scale);
            int width = (int) Math.round(selection.width / scale);
            int height = (int) Math.round(selection.height / scale);

            BufferedImage originalImage = imagePanel.getOriginalImage();
            BufferedImage cropped = originalImage.getSubimage(x, y, width, height);
            
            // Create a clean copy of the cropped image
            BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = copy.createGraphics();
            g2d.drawImage(cropped, 0, 0, null);
            g2d.dispose();

            if (onCropComplete != null) {
                onCropComplete.accept(copy);
            }
        } catch (Exception ex) {
            showError("Failed to crop image: " + ex.getMessage());
        }
    }

    private void resetImage() {
        if (initialImage != null) {
            imagePanel.setImage(initialImage);
            statusLabel.setText("Image reset to original.");
        }
    }

    private void enableControls(boolean enabled) {
        cropButton.setEnabled(enabled);
        zoomInButton.setEnabled(enabled);
        zoomOutButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void updateStatus(String message) {
        statusLabel.setText(message);
    }

    // Static utility method for easy use with ModalDialog
    public static void showImageCropper(java.awt.Component parent, BufferedImage image, Consumer<BufferedImage> onCropComplete) {
        ImageCropper cropper = new ImageCropper(image, croppedImage -> {
            ModalDialog.closeModal("image_cropper");
            if (onCropComplete != null) {
                onCropComplete.accept(croppedImage);
            }
        });

        ModalDialog.showModal(parent,
            new SimpleModalBorder(cropper, "Crop Profile Picture", new SimpleModalBorder.Option[] {}, null),
            "image_cropper");
    }
}