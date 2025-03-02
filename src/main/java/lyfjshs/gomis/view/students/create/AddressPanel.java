package lyfjshs.gomis.view.students.create;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import net.miginfocom.swing.MigLayout;

public class AddressPanel extends JPanel {
    private JTextField houseNoField;
    private JTextField streetField;
    private JTextField regionField;
    private JTextField provinceField;
    private JTextField municipalityField;
    private JTextField barangayField;
    private JTextField zipCodeField;

    public AddressPanel() {
        setBorder(BorderFactory.createTitledBorder("RESIDENTIAL ADDRESS"));
        setLayout(new MigLayout("wrap 4", "[][grow,fill]15[][grow,fill]", "[]10[]10[]10[]"));

        houseNoField = new JTextField(15);
        streetField = new JTextField(25);
        regionField = new JTextField(15);
        provinceField = new JTextField(15);
        municipalityField = new JTextField(15);
        barangayField = new JTextField(15);
        zipCodeField = new JTextField(8);

        // Restrict ZIP code to digits only
        zipCodeField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });

        // Arrange components in a structured layout
        add(new JLabel("House No:"));
        add(houseNoField);
        add(new JLabel("Street/Subdivision:"), "gapleft 15");
        add(streetField);

        add(new JLabel("Region:"));
        add(regionField);
        add(new JLabel("Province:"), "gapleft 15");
        add(provinceField);

        add(new JLabel("Municipality:"));
        add(municipalityField);
        add(new JLabel("Barangay:"), "gapleft 15");
        add(barangayField);

        add(new JLabel("Zip Code:"));
        add(zipCodeField, "span, width 120"); // Spans across layout to keep ZIP centered
    }

    // Getters for form fields
    public String getHouseNo() { return houseNoField.getText(); }
    public String getStreet() { return streetField.getText(); }
    public String getRegion() { return regionField.getText(); }
    public String getProvince() { return provinceField.getText(); }
    public String getMunicipality() { return municipalityField.getText(); }
    public String getBarangay() { return barangayField.getText(); }
    public String getZipCode() { return zipCodeField.getText(); }
}
