package view;

import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import model.*;
import service.*;

public class DispensingModule extends JPanel {

    private static final Color PRIMARY   = new Color(0, 102, 153);
    private static final Color SUCCESS   = new Color(34, 139, 34);
    private static final Color DANGER    = new Color(200, 50, 50);
    private static final Color WARNING   = new Color(200, 120, 0);
    private static final Color BG        = new Color(240, 245, 250);
    private static final Color WHITE     = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 30, 30);

    private final User currentUser;
    private DefaultTableModel tableModel;
    private JTable dispensingTable;
    private JComboBox<String> prescriptionCombo, paymentCombo;
    private JTextField quantityTxt, insuranceProviderTxt, insurancePolicyTxt;
    private JTextArea remarksTxt;
    private JLabel totalCostLabel;
    private int selectedDispensingId = -1;
    private List<Prescription> pendingPrescriptions;

    public DispensingModule(User user) {
        this.currentUser = user;
        initComponents();
        loadData();
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
        toolbar.setBorder(new CompoundBorder(new LineBorder(new Color(220,230,240),1), new EmptyBorder(10,16,10,16)));

        JLabel title = new JLabel("💉 Drug Dispensing");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_DARK);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton refreshBtn = UIHelper.button("↻ Refresh", UIHelper.DARK);
        refreshBtn.addActionListener(e -> loadData());
        btnPanel.add(refreshBtn);

        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(btnPanel, BorderLayout.EAST);
        return toolbar;
    }

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(WHITE);
        wrapper.setPreferredSize(new Dimension(300, 0));
        wrapper.setBorder(new CompoundBorder(new LineBorder(new Color(220,230,240),1), new EmptyBorder(16,16,16,16)));

        JLabel formTitle = new JLabel("Dispense Drug");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setForeground(PRIMARY);
        formTitle.setBorder(new MatteBorder(0,0,1,0,new Color(220,230,240)));

        JPanel form = new JPanel(new GridLayout(0,1,4,4));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10,0,10,0));

        form.add(fieldLabel("Pending Prescription *"));
        prescriptionCombo = new JComboBox<>();
        prescriptionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        prescriptionCombo.addActionListener(e -> calculateCost());
        form.add(prescriptionCombo);

        quantityTxt = formField(form, "Quantity to Dispense *");
        quantityTxt.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculateCost(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculateCost(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculateCost(); }
        });

        form.add(fieldLabel("Payment Method *"));
        paymentCombo = new JComboBox<>(new String[]{"CASH","INSURANCE","MOBILE_MONEY"});
        paymentCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        paymentCombo.addActionListener(e -> toggleInsuranceFields());
        form.add(paymentCombo);

        insuranceProviderTxt = formField(form, "Insurance Provider");
        insurancePolicyTxt   = formField(form, "Insurance Policy No");

        form.add(fieldLabel("Remarks"));
        remarksTxt = new JTextArea(2, 10);
        remarksTxt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        remarksTxt.setBorder(new LineBorder(new Color(200,210,220),1));
        form.add(new JScrollPane(remarksTxt));

        // Total cost display
        JPanel costPanel = new JPanel(new BorderLayout());
        costPanel.setBackground(new Color(230, 245, 255));
        costPanel.setBorder(new CompoundBorder(new LineBorder(PRIMARY,1,true), new EmptyBorder(8,12,8,12)));
        totalCostLabel = new JLabel("Total Cost: RWF 0.00");
        totalCostLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalCostLabel.setForeground(PRIMARY);
        costPanel.add(totalCostLabel);
        form.add(costPanel);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 6, 6));
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(0, 46));
        JButton dispenseBtn = UIHelper.button("💉 Dispense", UIHelper.SUCCESS);
        JButton clearBtn    = UIHelper.button("✖ Clear",     UIHelper.DARK);
        dispenseBtn.addActionListener(e -> dispense());
        clearBtn.addActionListener(e -> clearForm());
        btnPanel.add(dispenseBtn); btnPanel.add(clearBtn);

        wrapper.add(formTitle, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(form), BorderLayout.CENTER);
        wrapper.add(btnPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);
        panel.setBorder(new LineBorder(new Color(220,230,240),1));

        String[] cols = {"ID","Prescription No","Patient","Pharmacist","Date","Qty","Total (RWF)","Payment","Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        dispensingTable = new JTable(tableModel);
        UIHelper.styleTable(dispensingTable);

        panel.add(new JScrollPane(dispensingTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                pendingPrescriptions = ((PrescriptionService) reg.lookup("prescription-service")).findPending();
                return null;
            }
            @Override protected void done() {
                prescriptionCombo.removeAllItems();
                if (pendingPrescriptions != null) {
                    for (Prescription rx : pendingPrescriptions) {
                        String patientName = rx.getPatient() != null ? rx.getPatient().getFullName() : "N/A";
                        prescriptionCombo.addItem(rx.getId() + " | " + rx.getPrescriptionNumber() + " | " + patientName);
                    }
                }
                loadDispensings();
            }
        };
        worker.execute();
    }

    private void loadDispensings() {
        SwingWorker<List<Dispensing>, Void> worker = new SwingWorker<List<Dispensing>, Void>() {
            @Override protected List<Dispensing> doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((DispensingService) reg.lookup("dispensing-service")).findAll();
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Dispensing d : get()) {
                        String rxNo      = d.getPrescription() != null ? d.getPrescription().getPrescriptionNumber() : "N/A";
                        String patient   = d.getPrescription() != null && d.getPrescription().getPatient() != null ? d.getPrescription().getPatient().getFullName() : "N/A";
                        String pharmacist= d.getDispensedBy() != null ? d.getDispensedBy().getFullName() : "N/A";
                        tableModel.addRow(new Object[]{d.getId(), rxNo, patient, pharmacist,
                            d.getDispensingDate(), d.getQuantityDispensed(),
                            String.format("%.0f", d.getTotalCost()), d.getPaymentMethod(), d.getStatus()});
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void calculateCost() {
        try {
            int idx = prescriptionCombo.getSelectedIndex();
            if (idx < 0 || pendingPrescriptions == null || idx >= pendingPrescriptions.size()) return;
            Prescription rx = pendingPrescriptions.get(idx);
            int qty = Integer.parseInt(quantityTxt.getText().trim());
            if (rx.getDrugs() != null && !rx.getDrugs().isEmpty()) {
                double unitPrice = rx.getDrugs().get(0).getUnitPrice();
                double total = unitPrice * qty;
                totalCostLabel.setText(String.format("Total Cost: RWF %.0f", total));
            }
        } catch (NumberFormatException ignored) {
            totalCostLabel.setText("Total Cost: RWF 0.00");
        }
    }

    private void toggleInsuranceFields() {
        boolean isInsurance = "INSURANCE".equals(paymentCombo.getSelectedItem());
        insuranceProviderTxt.setEnabled(isInsurance);
        insurancePolicyTxt.setEnabled(isInsurance);
    }

    private void dispense() {
        if (!validateForm()) return;

        int idx = prescriptionCombo.getSelectedIndex();
        Prescription rx = pendingPrescriptions.get(idx);
        int qty = Integer.parseInt(quantityTxt.getText().trim());

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                DrugService drugSvc = (DrugService) reg.lookup("drug-service");
                DispensingService disSvc = (DispensingService) reg.lookup("dispensing-service");
                PrescriptionService rxSvc = (PrescriptionService) reg.lookup("prescription-service");

                if (rx.getDrugs() == null || rx.getDrugs().isEmpty())
                    throw new Exception("No drugs found in this prescription!");

                Drug drug = rx.getDrugs().get(0);
                Drug freshDrug = drugSvc.findById(drug.getId());

                // Business Validation 1: Check drug is not expired
                String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                if (freshDrug.getExpiryDate().compareTo(today) < 0)
                    throw new Exception("Cannot dispense: Drug '" + freshDrug.getName() + "' is EXPIRED!");

                // Business Validation 2: Check sufficient stock
                if (freshDrug.getStockQuantity() < qty)
                    throw new Exception("Insufficient stock! Available: " + freshDrug.getStockQuantity() + " units.");

                // Business Validation 3: Check prescription is still PENDING
                if (!"PENDING".equals(rx.getStatus()))
                    throw new Exception("Prescription is already " + rx.getStatus() + "!");

                // Business Validation 4: Check prescription not expired
                if (rx.getExpiryDate().compareTo(today) < 0)
                    throw new Exception("Prescription has EXPIRED and cannot be dispensed!");

                // Business Validation 5: Check patient allergies vs drug
                String allergies = rx.getPatient() != null ? rx.getPatient().getAllergies() : "";
                if (allergies != null && !allergies.isEmpty()) {
                    for (String allergy : allergies.split(",")) {
                        if (freshDrug.getName().toLowerCase().contains(allergy.trim().toLowerCase()))
                            throw new Exception("⚠ ALLERGY ALERT: Patient is allergic to " + allergy.trim() + "!");
                    }
                }

                // Create dispensing record
                Dispensing dispensing = new Dispensing();
                dispensing.setPrescription(rx);
                dispensing.setDispensedBy(currentUser);
                dispensing.setQuantityDispensed(qty);
                dispensing.setDispensingDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                dispensing.setTotalCost(freshDrug.getUnitPrice() * qty);
                dispensing.setPaymentMethod((String) paymentCombo.getSelectedItem());
                dispensing.setInsuranceProvider(insuranceProviderTxt.getText().trim());
                dispensing.setInsurancePolicyNo(insurancePolicyTxt.getText().trim());
                dispensing.setStatus("COMPLETED");
                dispensing.setRemarks(remarksTxt.getText().trim());
                disSvc.save(dispensing);

                // Update stock
                freshDrug.setStockQuantity(freshDrug.getStockQuantity() - qty);
                drugSvc.update(freshDrug);

                // Update prescription status
                rx.setStatus("DISPENSED");
                rxSvc.update(rx);

                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(DispensingModule.this,
                        "Drug dispensed successfully! Stock updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm(); loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DispensingModule.this,
                        ex.getMessage(), "Dispensing Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private boolean validateForm() {
        if (prescriptionCombo.getSelectedIndex() < 0) { showError("Please select a prescription."); return false; }
        if (quantityTxt.getText().trim().isEmpty()) { showError("Quantity is required."); return false; }
        try {
            int qty = Integer.parseInt(quantityTxt.getText().trim());
            if (qty <= 0) { showError("Quantity must be greater than zero."); return false; }
        } catch (NumberFormatException e) { showError("Quantity must be a valid number."); return false; }
        if ("INSURANCE".equals(paymentCombo.getSelectedItem())) {
            if (insuranceProviderTxt.getText().trim().isEmpty()) { showError("Insurance provider is required."); return false; }
            if (insurancePolicyTxt.getText().trim().isEmpty()) { showError("Insurance policy number is required."); return false; }
        }
        return true;
    }

    private void clearForm() {
        selectedDispensingId = -1;
        quantityTxt.setText(""); remarksTxt.setText("");
        insuranceProviderTxt.setText(""); insurancePolicyTxt.setText("");
        paymentCombo.setSelectedIndex(0);
        totalCostLabel.setText("Total Cost: RWF 0.00");
        dispensingTable.clearSelection();
    }

    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE); }

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
        lbl.setForeground(new Color(80,80,80));
        return lbl;
    }

    private JButton styledButton(String text, Color color) {
        return UIHelper.button(text, color);
    }
}
