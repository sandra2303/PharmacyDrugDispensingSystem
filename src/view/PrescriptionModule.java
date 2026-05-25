package view;

import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import model.*;
import service.*;

public class PrescriptionModule extends JPanel {

    private static final Color PRIMARY   = new Color(0, 102, 153);
    private static final Color SUCCESS   = new Color(34, 139, 34);
    private static final Color DANGER    = new Color(200, 50, 50);
    private static final Color BG        = new Color(240, 245, 250);
    private static final Color WHITE     = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 30, 30);

    private final User currentUser;
    private DefaultTableModel tableModel;
    private JTable prescriptionTable;
    private JTextField rxNumberTxt, doctorNameTxt, doctorLicenseTxt, issuedDateTxt, expiryDateTxt;
    private JComboBox<String> patientCombo, statusCombo;
    private JList<String> drugList;
    private DefaultListModel<String> drugListModel;
    private JTextArea notesTxt;
    private int selectedRxId = -1;
    private List<Patient> patients;
    private List<Drug> drugs;

    public PrescriptionModule(User user) {
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

        JLabel title = new JLabel("📋 Prescription Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_DARK);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton pendingBtn = UIHelper.button("⏳ Show Pending", UIHelper.WARNING);
        JButton allBtn     = UIHelper.button("📋 Show All",     UIHelper.PRIMARY);
        JButton refreshBtn = UIHelper.button("↻ Refresh",       UIHelper.DARK);
        pendingBtn.addActionListener(e -> loadPending());
        allBtn.addActionListener(e -> loadPrescriptions());
        refreshBtn.addActionListener(e -> loadData());

        btnPanel.add(pendingBtn); btnPanel.add(allBtn); btnPanel.add(refreshBtn);
        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(btnPanel, BorderLayout.EAST);
        return toolbar;
    }

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(WHITE);
        wrapper.setPreferredSize(new Dimension(300, 0));
        wrapper.setBorder(new CompoundBorder(new LineBorder(new Color(220,230,240),1), new EmptyBorder(16,16,16,16)));

        JLabel formTitle = new JLabel("Prescription Details");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setForeground(PRIMARY);
        formTitle.setBorder(new MatteBorder(0,0,1,0,new Color(220,230,240)));

        JPanel form = new JPanel(new GridLayout(0,1,4,4));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10,0,10,0));

        form.add(fieldLabel("Rx Number (auto-generated)"));
        rxNumberTxt = new JTextField();
        rxNumberTxt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rxNumberTxt.setEditable(false);
        rxNumberTxt.setBackground(new Color(245,245,245));
        rxNumberTxt.setBorder(new CompoundBorder(new LineBorder(new Color(200,210,220),1,true), new EmptyBorder(5,8,5,8)));
        form.add(rxNumberTxt);

        form.add(fieldLabel("Patient *"));
        patientCombo = new JComboBox<>();
        patientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(patientCombo);

        doctorNameTxt    = formField(form, "Doctor Name *");
        doctorLicenseTxt = formField(form, "Doctor License No *");
        issuedDateTxt    = formField(form, "Issued Date (yyyy-MM-dd) *");
        expiryDateTxt    = formField(form, "Expiry Date (yyyy-MM-dd) *");

        form.add(fieldLabel("Status"));
        statusCombo = new JComboBox<>(new String[]{"PENDING","DISPENSED","CANCELLED","EXPIRED"});
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(statusCombo);

        form.add(fieldLabel("Drugs Prescribed (select multiple)"));
        drugListModel = new DefaultListModel<>();
        drugList = new JList<>(drugListModel);
        drugList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        drugList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane drugScroll = new JScrollPane(drugList);
        drugScroll.setPreferredSize(new Dimension(0, 80));
        form.add(drugScroll);

        form.add(fieldLabel("Notes"));
        notesTxt = new JTextArea(2, 10);
        notesTxt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notesTxt.setBorder(new LineBorder(new Color(200,210,220),1));
        form.add(new JScrollPane(notesTxt));

        JButton genRxBtn = UIHelper.button("🔄 Generate Rx No", UIHelper.DARK);
        genRxBtn.addActionListener(e -> rxNumberTxt.setText("RX-" + UUID.randomUUID().toString().substring(0,8).toUpperCase()));
        form.add(genRxBtn);

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(0, 90));
        JButton saveBtn   = UIHelper.button("💾 Save",   UIHelper.SUCCESS);
        JButton updateBtn = UIHelper.button("✏ Update",  UIHelper.PRIMARY);
        JButton deleteBtn = UIHelper.button("🗑 Delete",  UIHelper.DANGER);
        JButton clearBtn  = UIHelper.button("✖ Clear",   UIHelper.DARK);

        saveBtn.addActionListener(e -> savePrescription());
        updateBtn.addActionListener(e -> updatePrescription());
        deleteBtn.addActionListener(e -> deletePrescription());
        clearBtn.addActionListener(e -> clearForm());

        btnPanel.add(saveBtn); btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn); btnPanel.add(clearBtn);

        wrapper.add(formTitle, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(form), BorderLayout.CENTER);
        wrapper.add(btnPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);
        panel.setBorder(new LineBorder(new Color(220,230,240),1));

        String[] cols = {"ID","Rx Number","Patient","Doctor","License No","Issued","Expiry","Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        prescriptionTable = new JTable(tableModel);
        UIHelper.styleTable(prescriptionTable);

        prescriptionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && prescriptionTable.getSelectedRow() != -1) populateForm();
        });

        panel.add(new JScrollPane(prescriptionTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                patients = ((PatientService) reg.lookup("patient-service")).findAll();
                drugs    = ((DrugService)   reg.lookup("drug-service")).findAll();
                return null;
            }
            @Override protected void done() {
                patientCombo.removeAllItems();
                drugListModel.clear();
                if (patients != null) for (Patient p : patients) patientCombo.addItem(p.getId() + " - " + p.getFullName());
                if (drugs != null)    for (Drug d : drugs)       drugListModel.addElement(d.getId() + " - " + d.getName() + " (" + d.getStrength() + ")");
                loadPrescriptions();
            }
        };
        worker.execute();
    }

    private void loadPrescriptions() {
        SwingWorker<List<Prescription>, Void> worker = new SwingWorker<List<Prescription>, Void>() {
            @Override protected List<Prescription> doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((PrescriptionService) reg.lookup("prescription-service")).findAll();
            }
            @Override protected void done() {
                try { populateTable(get()); } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void loadPending() {
        SwingWorker<List<Prescription>, Void> worker = new SwingWorker<List<Prescription>, Void>() {
            @Override protected List<Prescription> doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((PrescriptionService) reg.lookup("prescription-service")).findPending();
            }
            @Override protected void done() {
                try { populateTable(get()); } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void populateTable(List<Prescription> list) {
        tableModel.setRowCount(0);
        for (Prescription rx : list) {
            String patientName = rx.getPatient() != null ? rx.getPatient().getFullName() : "N/A";
            tableModel.addRow(new Object[]{rx.getId(), rx.getPrescriptionNumber(), patientName,
                rx.getDoctorName(), rx.getDoctorLicenseNo(), rx.getIssuedDate(), rx.getExpiryDate(), rx.getStatus()});
        }
    }

    private void savePrescription() {
        if (!validateForm()) return;
        Prescription rx = buildFromForm();
        SwingWorker<Prescription, Void> worker = new SwingWorker<Prescription, Void>() {
            @Override protected Prescription doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                PrescriptionService svc = (PrescriptionService) reg.lookup("prescription-service");
                // Business validation: check duplicate Rx number
                Prescription existing = svc.findByPrescriptionNumber(rx.getPrescriptionNumber());
                if (!Objects.isNull(existing)) throw new Exception("Prescription number already exists!");
                // Business validation: prescription expiry must be after issued date
                if (rx.getExpiryDate().compareTo(rx.getIssuedDate()) <= 0) throw new Exception("Expiry date must be after issued date!");
                return svc.save(rx);
            }
            @Override protected void done() {
                try {
                    if (Objects.isNull(get())) {
                        JOptionPane.showMessageDialog(PrescriptionModule.this, "Failed to save prescription.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(PrescriptionModule.this, "Prescription saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm(); loadPrescriptions();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PrescriptionModule.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updatePrescription() {
        if (selectedRxId == -1) { JOptionPane.showMessageDialog(this, "Select a prescription to update.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        if (!validateForm()) return;
        Prescription rx = buildFromForm();
        rx.setId(selectedRxId);
        SwingWorker<Prescription, Void> worker = new SwingWorker<Prescription, Void>() {
            @Override protected Prescription doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((PrescriptionService) reg.lookup("prescription-service")).update(rx);
            }
            @Override protected void done() {
                try {
                    JOptionPane.showMessageDialog(PrescriptionModule.this, "Prescription updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm(); loadPrescriptions();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void deletePrescription() {
        if (selectedRxId == -1) { JOptionPane.showMessageDialog(this, "Select a prescription to delete.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this prescription?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        Prescription rx = new Prescription();
        rx.setId(selectedRxId);
        SwingWorker<Prescription, Void> worker = new SwingWorker<Prescription, Void>() {
            @Override protected Prescription doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((PrescriptionService) reg.lookup("prescription-service")).delete(rx);
            }
            @Override protected void done() {
                try {
                    JOptionPane.showMessageDialog(PrescriptionModule.this, "Prescription deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm(); loadPrescriptions();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void populateForm() {
        int row = prescriptionTable.getSelectedRow();
        selectedRxId = (int) tableModel.getValueAt(row, 0);
        rxNumberTxt.setText((String) tableModel.getValueAt(row, 1));
        doctorNameTxt.setText((String) tableModel.getValueAt(row, 3));
        doctorLicenseTxt.setText((String) tableModel.getValueAt(row, 4));
        issuedDateTxt.setText((String) tableModel.getValueAt(row, 5));
        expiryDateTxt.setText((String) tableModel.getValueAt(row, 6));
        statusCombo.setSelectedItem(tableModel.getValueAt(row, 7));
    }

    private boolean validateForm() {
        if (rxNumberTxt.getText().trim().isEmpty()) { showError("Please generate a Rx number."); return false; }
        if (patientCombo.getSelectedIndex() == -1)  { showError("Please select a patient."); return false; }
        if (doctorNameTxt.getText().trim().isEmpty()) { showError("Doctor name is required."); return false; }
        if (doctorLicenseTxt.getText().trim().isEmpty()) { showError("Doctor license number is required."); return false; }
        if (issuedDateTxt.getText().trim().isEmpty()) { showError("Issued date is required."); return false; }
        if (expiryDateTxt.getText().trim().isEmpty()) { showError("Expiry date is required."); return false; }
        if (drugList.getSelectedIndices().length == 0) { showError("Please select at least one drug."); return false; }
        return true;
    }

    private Prescription buildFromForm() {
        Prescription rx = new Prescription();
        rx.setPrescriptionNumber(rxNumberTxt.getText().trim());
        rx.setDoctorName(doctorNameTxt.getText().trim());
        rx.setDoctorLicenseNo(doctorLicenseTxt.getText().trim());
        rx.setIssuedDate(issuedDateTxt.getText().trim());
        rx.setExpiryDate(expiryDateTxt.getText().trim());
        rx.setStatus((String) statusCombo.getSelectedItem());
        rx.setNotes(notesTxt.getText().trim());

        if (patients != null && patientCombo.getSelectedIndex() >= 0) {
            rx.setPatient(patients.get(patientCombo.getSelectedIndex()));
        }
        if (drugs != null) {
            java.util.List<Drug> selected = new java.util.ArrayList<>();
            for (int i : drugList.getSelectedIndices()) selected.add(drugs.get(i));
            rx.setDrugs(selected);
        }
        return rx;
    }

    private void clearForm() {
        selectedRxId = -1;
        rxNumberTxt.setText(""); doctorNameTxt.setText(""); doctorLicenseTxt.setText("");
        issuedDateTxt.setText(""); expiryDateTxt.setText(""); notesTxt.setText("");
        statusCombo.setSelectedIndex(0); drugList.clearSelection();
        prescriptionTable.clearSelection();
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
