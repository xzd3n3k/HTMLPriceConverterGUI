import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class UploadFile extends JFrame {
    private JPanel panelMain;
    private JButton uploadFileButton;
    private JButton convertFileButton;
    private JButton downloadFileButton;
    private JTextField czkValueInput;

    public String content;

    public UploadFile() {
        czkValueInput.setToolTipText("Use . as decimal separator");
        convertFileButton.setEnabled(false);
        downloadFileButton.setEnabled(false);
        uploadFileButton.setEnabled(false);

        czkValueInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // This is only called when the style of the text changes,
                // not the actual content. We don't need to respond to this.
            }

            private void update() {
                try {
                    Double.parseDouble(czkValueInput.getText());
                    uploadFileButton.setEnabled(true);
                    czkValueInput.setBorder(UIManager.getLookAndFeelDefaults().getBorder("TextField.border"));
                } catch (NumberFormatException ex) {
                    uploadFileButton.setEnabled(false);
                    czkValueInput.setBorder(BorderFactory.createLineBorder(Color.RED));
                }
            }
        });

        uploadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {

                if (event.getSource() == uploadFileButton) {
                    JFileChooser file_upload = new JFileChooser();

                    int result = file_upload.showOpenDialog(null);

                    if (result == JFileChooser.APPROVE_OPTION) {
                        File file = file_upload.getSelectedFile();
                        try {
                            content = new String(Files.readAllBytes(file.toPath()));
                            convertFileButton.setEnabled(true);
                        } catch (IOException exception) {
                            exception.printStackTrace();
                            JOptionPane.showMessageDialog(uploadFileButton, "Error reading the file", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        convertFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
//                Pattern pattern = Pattern.compile("(\\d+) Kč");
//                Matcher matcher = pattern.matcher(content);

                String patternSpaces = "&nbsp;";

                // Create a Pattern object
                Pattern p = Pattern.compile(patternSpaces);

                // Create a Matcher object
                Matcher m = p.matcher(content);

                StringBuffer result1 = new StringBuffer();

                // Find all occurrences
                while (m.find()) {
                    m.appendReplacement(result1, Matcher.quoteReplacement(" "));
                }

                m.appendTail(result1);
                content = result1.toString();

                Pattern pattern = Pattern.compile("(\\d[\\d ,]*)\\s*K&#269;");
                Matcher matcher = pattern.matcher(content);

                StringBuffer result = new StringBuffer();

                while (matcher.find()) {
                    String originalPrice = matcher.group(1);
                    originalPrice = originalPrice.replace(" ", "");
                    double czkPrice = Double.parseDouble(originalPrice.replace(',', '.'));
                    double eurPrice = czkPrice / Double.parseDouble(czkValueInput.getText());
                    String roundedNumberString = String.format("%.3f", eurPrice);
                    eurPrice = Double.parseDouble(roundedNumberString);
                    String replacement = eurPrice + " Eur";
                    matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                }
                matcher.appendTail(result);

                content = result.toString();
                downloadFileButton.setEnabled(true);
            }
        });
        downloadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileSave = new JFileChooser();
                int result = fileSave.showSaveDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileSave.getSelectedFile();
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(content);
                        JOptionPane.showMessageDialog(downloadFileButton, "File saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException exception) {
                        exception.printStackTrace();
                        JOptionPane.showMessageDialog(downloadFileButton, "Error saving the file", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        UploadFile frame = new UploadFile();
        frame.setContentPane(frame.panelMain);
        frame.setTitle("Price converter");
        frame.setSize(400, 300);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
