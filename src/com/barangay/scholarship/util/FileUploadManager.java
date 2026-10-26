package com.barangay.scholarship.util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * Handles persistent document file uploads, validation, storage organization,
 * and system file opening/preview for scholarship applicant attachments.
 */
public class FileUploadManager {

    public static final String UPLOAD_BASE_DIR = "data/uploads";
    public static final long MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024; // 20 MB

    private static final String[] ALLOWED_EXTENSIONS = {
            "pdf", "png", "jpg", "jpeg", "docx", "doc", "txt"
    };

    /**
     * Copies and persists an applicant's uploaded document into the standardized storage directory:
     * data/uploads/{applicantId}/{documentType}_{sanitizedFilename}
     *
     * @param applicantId  The unique applicant reference ID (e.g., APP-2026-001)
     * @param documentType The document type identifier (e.g., "Indigency", "Grades", "Enrollment", "ID")
     * @param sourceFile   The original file selected by the applicant
     * @return The relative path to the saved file
     * @throws IOException If file copying fails
     */
    public static String saveUploadedFile(String applicantId, String documentType, File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
            throw new IllegalArgumentException("Source file does not exist or is not a valid file.");
        }

        // Sanitize applicant ID and document type to prevent directory traversal
        String safeAppId = applicantId.replaceAll("[^a-zA-Z0-9_-]", "_");
        String safeDocType = documentType.replaceAll("[^a-zA-Z0-9_-]", "_");

        Path targetDir = Paths.get(UPLOAD_BASE_DIR, safeAppId);
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        String originalName = sourceFile.getName();
        String safeOriginalName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String targetFileName = safeDocType + "_" + safeOriginalName;

        Path targetPath = targetDir.resolve(targetFileName);
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString().replace('\\', '/');
    }

    /**
     * Checks if a selected file has an allowed extension and is within the size limit.
     */
    public static boolean isValidDocumentFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        if (file.length() > MAX_FILE_SIZE_BYTES) {
            return false;
        }

        String name = file.getName().toLowerCase(Locale.ENGLISH);
        for (String ext : ALLOWED_EXTENSIONS) {
            if (name.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a human-friendly error message if a file is invalid, or null if valid.
     */
    public static String getValidationError(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return "Selected file does not exist or cannot be read.";
        }
        if (file.length() > MAX_FILE_SIZE_BYTES) {
            return "File exceeds the 20 MB size limit (" + formatFileSize(file.length()) + ").";
        }
        String name = file.getName().toLowerCase(Locale.ENGLISH);
        boolean extMatch = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (name.endsWith("." + ext)) {
                extMatch = true;
                break;
            }
        }
        if (!extMatch) {
            return "Invalid file format. Supported formats: PDF, PNG, JPG, JPEG, DOCX, DOC, TXT.";
        }
        return null;
    }

    /**
     * Creates a standardized JFileChooser configured with filters for documents and images.
     */
    public static JFileChooser createFileChooser(String dialogTitle) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(dialogTitle);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Document & Image Files (*.pdf, *.png, *.jpg, *.jpeg, *.docx, *.doc, *.txt)",
                ALLOWED_EXTENSIONS
        );
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);

        return chooser;
    }

    /**
     * Opens the document in the operating system's default viewer (e.g. Preview, Adobe, Photos).
     * If system opening fails or is unsupported, falls back to an embedded preview dialog for images.
     */
    public static void openFile(Component parent, String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "No document file is attached to this requirement.",
                    "File Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(parent,
                    "The attached file could not be found at path:\n" + file.getAbsolutePath(),
                    "File Missing", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Try Desktop API first
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                try {
                    desktop.open(file);
                    return;
                } catch (Exception ex) {
                    System.err.println("Desktop.open failed, trying image fallback: " + ex.getMessage());
                }
            }
        }

        // Fallback: Check if image and show preview dialog
        String lowerName = file.getName().toLowerCase(Locale.ENGLISH);
        if (lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif")) {
            showImagePreviewDialog(parent, file);
        } else {
            JOptionPane.showMessageDialog(parent,
                    "File is stored at:\n" + file.getAbsolutePath() +
                            "\n\nUnable to launch default OS viewer automatically. You may open it directly from the file system.",
                    "Document File Stored", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void showImagePreviewDialog(Component parent, File imageFile) {
        try {
            BufferedImage img = ImageIO.read(imageFile);
            if (img == null) {
                JOptionPane.showMessageDialog(parent, "Unable to render image preview for: " + imageFile.getName(),
                        "Preview Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int origW = img.getWidth();
            int origH = img.getHeight();

            // Scale to fit within 750x550
            int maxW = 750;
            int maxH = 550;
            double scale = Math.min((double) maxW / origW, (double) maxH / origH);
            if (scale > 1.0) scale = 1.0;

            int newW = (int) (origW * scale);
            int newH = (int) (origH * scale);

            Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(scaled));

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Document Preview: " + imageFile.getName(), true);
            dialog.setLayout(new BorderLayout());
            dialog.add(new JScrollPane(imgLabel), BorderLayout.CENTER);

            JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JLabel info = new JLabel(String.format("%s | %dx%d px | %s",
                    imageFile.getName(), origW, origH, formatFileSize(imageFile.length())));
            info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            info.setForeground(new Color(100, 116, 139));

            JButton btnClose = new JButton("Close");
            btnClose.addActionListener(e -> dialog.dispose());

            bot.add(info);
            bot.add(Box.createHorizontalStrut(16));
            bot.add(btnClose);
            dialog.add(bot, BorderLayout.SOUTH);

            dialog.pack();
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Error reading image: " + ex.getMessage(),
                    "Image Preview Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Formats raw byte count into readable KB or MB format.
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format(Locale.ENGLISH, "%.1f KB", bytes / 1024.0);
        } else {
            return String.format(Locale.ENGLISH, "%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }
}

