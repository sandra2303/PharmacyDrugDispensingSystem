package view;

import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import model.Drug;
import model.User;
import service.DrugService;

public class DrugModule extends JPanel {

    private static final Color PRIMARY   = new Color(0, 102, 153);
    private static final Color SUCCESS   = new Color(34, 139, 34);
    private static final Color DANGER    = new Color(200, 50, 50);
    private static final Color WARNING   = new Color(200, 120, 0);
    private static final Color BG        = new Color(240, 245, 250);
    private static final Color WHITE     = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 30, 30);
    private static final Color TEXT_GRAY = new Color(120, 120, 120);

    private final User currentUser;
    private DefaultTableModel tableModel;
    private JTable drugTable;
    private JTextField searchTxt, nameTxt, codeTxt, manufacturerTxt, strengthTxt, priceTxt, stockTxt, reorderTxt, expiryTxt;
    private JComboBox<String> categoryCombo, dosageCombo, prescriptionCombo;
    private JTextArea sideEffectsTxt, interactionsTxt;
    private int selectedDrugId = -1;

    public DrugModule(User user) {
        this.currentUser = user;
        initComponents();
        loadDrugs();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildFormPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(WHITE);
        toolbar.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 230, 240), 1),
            new EmptyBorder(10, 16, 10, 16)
        ));

        JLabel title = new JLabel("💊 Drug Inventory Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_DARK);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        searchTxt = new JTextField(18);
        searchTxt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchTxt.setBorder(new CompoundBorder(new LineBorder(new Color(200,210,220),1,true), new EmptyBorder(6,10,6,10)));
        searchTxt.putClientProperty("JTextField.placeholderText", "Search drugs...");

        JButton searchBtn = UIHelper.button("🔍 Search", UIHelper.PRIMARY);
        JButton refreshBtn = UIHelper.button("↻ Refresh", UIHelper.DARK);
        searchBtn.addActionListener(e -> searchDrugs());
        refreshBtn.addActionListener(e -> loadDrugs());

        searchPanel.add(searchTxt);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);

        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.EAST);
        return toolbar;
    }

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(WHITE);
        wrapper.setPreferredSize(new Dimension(300, 0));
        wrapper.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 230, 240), 1),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel formTitle = new JLabel("Drug Details");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setForeground(PRIMARY);
        formTitle.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 230, 240)));

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 0, 10, 0));

        nameTxt        = formField(form, "Drug Name *");
        codeTxt        = formField(form, "Drug Code *");
        manufacturerTxt= formField(form, "Manufacturer *");
        strengthTxt    = formField(form, "Strength (e.g. 500mg) *");
        priceTxt       = formField(form, "Unit Price (RWF) *");
        stockTxt       = formField(form, "Stock Quantity *");
        reorderTxt     = formField(form, "Reorder Level *");
        expiryTxt      = formField(form, "Expiry Date (yyyy-MM-dd) *");

        form.add(fieldLabel("Category *"));
        categoryCombo = new JComboBox<>(new String[]{"Antibiotic", "Analgesic", "Antiviral", "Antifungal", "Antiparasitic", "Cardiovascular", "Diabetes", "Vitamins", "Other"});
        styleCombo(categoryCombo);
        form.add(categoryCombo);

        form.add(fieldLabel("Dosage Form *"));
        dosageCombo = new JComboBox<>(new String[]{"Tablet", "Capsule", "Syrup", "Injection", "Cream", "Drops", "Inhaler"});
        styleCombo(dosageCombo);
        form.add(dosageCombo);

        form.add(fieldLabel("Requires Prescription"));
        prescriptionCombo = new JComboBox<>(new String[]{"Yes", "No"});
        styleCombo(prescriptionCombo);
        form.add(prescriptionCombo);

        form.add(fieldLabel("Side Effects"));
        sideEffectsTxt = new JTextArea(2, 10);
        sideEffectsTxt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sideEffectsTxt.setBorder(new LineBorder(new Color(200, 210, 220), 1));
        form.add(new JScrollPane(sideEffectsTxt));

        form.add(fieldLabel("Drug Interactions (codes)"));
        interactionsTxt = new JTextArea(2, 10);
        interactionsTxt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        interactionsTxt.setBorder(new LineBorder(new Color(200, 210, 220), 1));
        form.add(new JScrollPane(interactionsTxt));

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(0, 90));
        JButton saveBtn   = UIHelper.button("💾 Save",   UIHelper.SUCCESS);
        JButton updateBtn = UIHelper.button("✏ Update",  UIHelper.PRIMARY);
        JButton deleteBtn = UIHelper.button("🗑 Delete",  UIHelper.DANGER);
        JButton clearBtn  = UIHelper.button("✖ Clear",   UIHelper.DARK);

        saveBtn.addActionListener(e -> saveDrug());
        updateBtn.addActionListener(e -> updateDrug());
        deleteBtn.addActionListener(e -> deleteDrug());
        clearBtn.addActionListener(e -> clearForm());

        btnPanel.add(saveBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        wrapper.add(formTitle, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(form), BorderLayout.CENTER);
        wrapper.add(btnPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);
        panel.setBorder(new LineBorder(new Color(220, 230, 240), 1));

        String[] cols = {"ID", "Name", "Code", "Category", "Dosage Form", "Strength", "Stock", "Reorder", "Expiry", "Price (RWF)", "Rx Required"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        drugTable = new JTable(tableModel);
        UIHelper.styleTable(drugTable);
        drugTable.getColumnModel().getColumn(0).setPreferredWidth(40);

        drugTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && drugTable.getSelectedRow() != -1) populateForm();
        });

        panel.add(new JScrollPane(drugTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadDrugs() {
        SwingWorker<List<Drug>, Void> worker = new SwingWorker<List<Drug>, Void>() {
            @Override protected List<Drug> doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                DrugService svc = (DrugService) reg.lookup("drug-service");
                return svc.findAll();
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Drug d : get()) {
                        tableModel.addRow(new Object[]{
                            d.getId(), d.getName(), d.getDrugCode(), d.getCategory(),
                            d.getDosageForm(), d.getStrength(), d.getStockQuantity(),
                            d.getReorderLevel(), d.getExpiryDate(),
                            String.format("%.0f", d.getUnitPrice()),
                            d.isRequiresPrescription() ? "Yes" : "No"
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DrugModule.this, "Failed to load drugs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void searchDrugs() {
        String kw = searchTxt.getText().trim();
        if (kw.isEmpty()) { loadDrugs(); return; }
        SwingWorker<List<Drug>, Void> worker = new SwingWorker<List<Drug>, Void>() {
            @Override protected List<Drug> doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                DrugService svc = (DrugService) reg.lookup("drug-service");
                return svc.search(kw);
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Drug d : get()) {
                        tableModel.addRow(new Object[]{
                            d.getId(), d.getName(), d.getDrugCode(), d.getCategory(),
                            d.getDosageForm(), d.getStrength(), d.getStockQuantity(),
                            d.getReorderLevel(), d.getExpiryDate(),
                            String.format("%.0f", d.getUnitPrice()),
                            d.isRequiresPrescription() ? "Yes" : "No"
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void saveDrug() {
        if (!validateForm()) return;
        Drug drug = buildDrugFromForm();
        SwingWorker<Drug, Void> worker = new SwingWorker<Drug, Void>() {
            @Override protected Drug doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                DrugService svc = (DrugService) reg.lookup("drug-service");
                // Business validation: check duplicate drug code
                Drug existing = svc.findByDrugCode(drug.getDrugCode());
                if (!Objects.isNull(existing)) throw new Exception("Drug code already exists!");
                return svc.save(drug);
            }
            @Override protected void done() {
                try {
                    Drug saved = get();
                    if (Objects.isNull(saved)) {
                        JOptionPane.showMessageDialog(DrugModule.this, "Failed to save drug.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(DrugModule.this, "Drug saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm(); loadDrugs();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DrugModule.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateDrug() {
        if (selectedDrugId == -1) { JOptionPane.showMessageDialog(this, "Please select a drug to update.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        if (!validateForm()) return;
        Drug drug = buildDrugFromForm();
        drug.setId(selectedDrugId);
        SwingWorker<Drug, Void> worker = new SwingWorker<Drug, Void>() {
            @Override protected Drug doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((DrugService) reg.lookup("drug-service")).update(drug);
            }
            @Override protected void done() {
                try {
                    if (Objects.isNull(get())) {
                        JOptionPane.showMessageDialog(DrugModule.this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(DrugModule.this, "Drug updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm(); loadDrugs();
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void deleteDrug() {
        if (selectedDrugId == -1) { JOptionPane.showMessageDialog(this, "Please select a drug to delete.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this drug?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        Drug drug = new Drug();
        drug.setId(selectedDrugId);
        SwingWorker<Drug, Void> worker = new SwingWorker<Drug, Void>() {
            @Override protected Drug doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((DrugService) reg.lookup("drug-service")).delete(drug);
            }
            @Override protected void done() {
                try {
                    JOptionPane.showMessageDialog(DrugModule.this, "Drug deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm(); loadDrugs();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void populateForm() {
        int row = drugTable.getSelectedRow();
        selectedDrugId = (int) tableModel.getValueAt(row, 0);
        nameTxt.setText((String) tableModel.getValueAt(row, 1));
        codeTxt.setText((String) tableModel.getValueAt(row, 2));
        categoryCombo.setSelectedItem(tableModel.getValueAt(row, 3));
        dosageCombo.setSelectedItem(tableModel.getValueAt(row, 4));
        strengthTxt.setText((String) tableModel.getValueAt(row, 5));
        stockTxt.setText(String.valueOf(tableModel.getValueAt(row, 6)));
        reorderTxt.setText(String.valueOf(tableModel.getValueAt(row, 7)));
        expiryTxt.setText((String) tableModel.getValueAt(row, 8));
        priceTxt.setText(String.valueOf(tableModel.getValueAt(row, 9)));
        prescriptionCombo.setSelectedItem(tableModel.getValueAt(row, 10));
    }

    private boolean validateForm() {
        // Technical validations
        if (nameTxt.getText().trim().isEmpty()) { showError("Drug name is required."); return false; }
        if (codeTxt.getText().trim().isEmpty()) { showError("Drug code is required."); return false; }
        if (manufacturerTxt.getText().trim().isEmpty()) { showError("Manufacturer is required."); return false; }
        if (strengthTxt.getText().trim().isEmpty()) { showError("Strength is required."); return false; }
        if (expiryTxt.getText().trim().isEmpty()) { showError("Expiry date is required."); return false; }
        try { Integer.parseInt(stockTxt.getText().trim()); } catch (NumberFormatException e) { showError("Stock quantity must be a valid number."); return false; }
        try { Integer.parseInt(reorderTxt.getText().trim()); } catch (NumberFormatException e) { showError("Reorder level must be a valid number."); return false; }
        try { Double.parseDouble(priceTxt.getText().trim()); } catch (NumberFormatException e) { showError("Unit price must be a valid number."); return false; }
        if (Integer.parseInt(stockTxt.getText().trim()) < 0) { showError("Stock quantity cannot be negative."); return false; }
        if (Double.parseDouble(priceTxt.getText().trim()) <= 0) { showError("Unit price must be greater than zero."); return false; }
        // Business validation: expiry date must be in future
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            java.util.Date expiry = sdf.parse(expiryTxt.getText().trim());
            if (expiry.before(new java.util.Date())) { showError("Expiry date must be in the future."); return false; }
        } catch (Exception e) { showError("Expiry date format must be yyyy-MM-dd."); return false; }
        return true;
    }

    private Drug buildDrugFromForm() {
        Drug drug = new Drug();
        drug.setName(nameTxt.getText().trim());
        drug.setDrugCode(codeTxt.getText().trim().toUpperCase());
        drug.setManufacturer(manufacturerTxt.getText().trim());
        drug.setStrength(strengthTxt.getText().trim());
        drug.setCategory((String) categoryCombo.getSelectedItem());
        drug.setDosageForm((String) dosageCombo.getSelectedItem());
        drug.setStockQuantity(Integer.parseInt(stockTxt.getText().trim()));
        drug.setReorderLevel(Integer.parseInt(reorderTxt.getText().trim()));
        drug.setExpiryDate(expiryTxt.getText().trim());
        drug.setUnitPrice(Double.parseDouble(priceTxt.getText().trim()));
        drug.setSideEffects(sideEffectsTxt.getText().trim());
        drug.setInteractions(interactionsTxt.getText().trim());
        drug.setRequiresPrescription(prescriptionCombo.getSelectedItem().equals("Yes"));
        return drug;
    }

    private void clearForm() {
        selectedDrugId = -1;
        nameTxt.setText(""); codeTxt.setText(""); manufacturerTxt.setText("");
        strengthTxt.setText(""); priceTxt.setText(""); stockTxt.setText("");
        reorderTxt.setText(""); expiryTxt.setText("");
        sideEffectsTxt.setText(""); interactionsTxt.setText("");
        categoryCombo.setSelectedIndex(0); dosageCombo.setSelectedIndex(0);
        prescriptionCombo.setSelectedIndex(0);
        drugTable.clearSelection();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---- Helpers ----
    private JTextField formField(JPanel parent, String label) {
        parent.add(fieldLabel(label));
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBorder(new CompoundBorder(new LineBorder(new Color(200,210,220),1,true), new EmptyBorder(5,8,5,8)));
        parent.add(tf);
        return tf;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(WHITE);
    }

    private JButton styledButton(String text, Color color) {
        return UIHelper.button(text, color);
    }
}
