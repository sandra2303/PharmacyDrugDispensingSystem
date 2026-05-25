package view;

import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import model.Patient;
import model.User;
import service.PatientService;

public class PatientModule extends JPanel {

    private static final Color PRIMARY   = new Color(0, 102, 153);
    private static final Color SUCCESS   = new Color(34, 139, 34);
    private static final Color DANGER    = new Color(200, 50, 50);
    private static final Color BG        = new Color(240, 245, 250);
    private static final Color WHITE     = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 30, 30);

    private final User currentUser;
    private DefaultTableModel tableModel;
    private JTable patientTable;
    private JTextField searchTxt, fullNameTxt, nationalIdTxt, phoneTxt, emailTxt, addressTxt, dobTxt;
    private JComboBox<String> genderCombo;
    private JTextArea allergiesTxt;
    private int selectedPatientId = -1;

    public PatientModule(User user) {
        this.currentUser = user;
        initComponents();
        loadPatients();
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

        JLabel title = new JLabel("👤 Patient Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_DARK);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        searchTxt = new JTextField(18);
        searchTxt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchTxt.setBorder(new CompoundBorder(new LineBorder(new Color(200,210,220),1,true), new EmptyBorder(6,10,6,10)));

        JButton searchBtn = UIHelper.button("🔍 Search", UIHelper.PRIMARY);
        JButton refreshBtn = UIHelper.button("↻ Refresh", UIHelper.DARK);
        searchBtn.addActionListener(e -> searchPatients());
        refreshBtn.addActionListener(e -> loadPatients());

        searchPanel.add(searchTxt); searchPanel.add(searchBtn); searchPanel.add(refreshBtn);
        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.EAST);
        return toolbar;
    }

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(WHITE);
        wrapper.setPreferredSize(new Dimension(280, 0));
        wrapper.setBorder(new CompoundBorder(new LineBorder(new Color(220,230,240),1), new EmptyBorder(16,16,16,16)));

        JLabel formTitle = new JLabel("Patient Details");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setForeground(PRIMARY);
        formTitle.setBorder(new MatteBorder(0,0,1,0,new Color(220,230,240)));

        JPanel form = new JPanel(new GridLayout(0,1,4,4));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10,0,10,0));

        fullNameTxt  = formField(form, "Full Name *");
        nationalIdTxt= formField(form, "National ID *");
        phoneTxt     = formField(form, "Phone *");
        emailTxt     = formField(form, "Email");
        addressTxt   = formField(form, "Address");
        dobTxt       = formField(form, "Date of Birth (yyyy-MM-dd)");

        form.add(fieldLabel("Gender"));
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(genderCombo);

        form.add(fieldLabel("Known Allergies"));
        allergiesTxt = new JTextArea(3, 10);
        allergiesTxt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        allergiesTxt.setBorder(new LineBorder(new Color(200,210,220),1));
        form.add(new JScrollPane(allergiesTxt));

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(0, 90));
        JButton saveBtn   = UIHelper.button("💾 Save",   UIHelper.SUCCESS);
        JButton updateBtn = UIHelper.button("✏ Update",  UIHelper.PRIMARY);
        JButton deleteBtn = UIHelper.button("🗑 Delete",  UIHelper.DANGER);
        JButton clearBtn  = UIHelper.button("✖ Clear",   UIHelper.DARK);

        saveBtn.addActionListener(e -> savePatient());
        updateBtn.addActionListener(e -> updatePatient());
        deleteBtn.addActionListener(e -> deletePatient());
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

        String[] cols = {"ID","Full Name","National ID","Phone","Email","Gender","Date of Birth","Allergies"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        patientTable = new JTable(tableModel);
        UIHelper.styleTable(patientTable);
        patientTable.getColumnModel().getColumn(0).setPreferredWidth(40);

        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && patientTable.getSelectedRow() != -1) populateForm();
        });

        panel.add(new JScrollPane(patientTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadPatients() {
        SwingWorker<List<Patient>, Void> worker = new SwingWorker<List<Patient>, Void>() {
            @Override protected List<Patient> doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((PatientService) reg.lookup("patient-service")).findAll();
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Patient p : get()) {
                        tableModel.addRow(new Object[]{p.getId(), p.getFullName(), p.getNationalId(),
                            p.getPhone(), p.getEmail(), p.getGender(), p.getDateOfBirth(), p.getAllergies()});
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PatientModule.this, "Failed to load patients.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void searchPatients() {
        String kw = searchTxt.getText().trim();
        if (kw.isEmpty()) { loadPatients(); return; }
        SwingWorker<List<Patient>, Void> worker = new SwingWorker<List<Patient>, Void>() {
            @Override protected List<Patient> doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((PatientService) reg.lookup("patient-service")).search(kw);
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Patient p : get()) {
                        tableModel.addRow(new Object[]{p.getId(), p.getFullName(), p.getNationalId(),
                            p.getPhone(), p.getEmail(), p.getGender(), p.getDateOfBirth(), p.getAllergies()});
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void savePatient() {
        if (!validateForm()) return;
        Patient patient = buildPatientFromForm();
        SwingWorker<Patient, Void> worker = new SwingWorker<Patient, Void>() {
            @Override protected Patient doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                PatientService svc = (PatientService) reg.lookup("patient-service");
                Patient existing = svc.findByNationalId(patient.getNationalId());
                if (!Objects.isNull(existing)) throw new Exception("Patient with this National ID already exists!");
                return svc.save(patient);
            }
            @Override protected void done() {
                try {
                    if (Objects.isNull(get())) {
                        JOptionPane.showMessageDialog(PatientModule.this, "Failed to save patient.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(PatientModule.this, "Patient registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm(); loadPatients();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PatientModule.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updatePatient() {
        if (selectedPatientId == -1) { JOptionPane.showMessageDialog(this, "Select a patient to update.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        if (!validateForm()) return;
        Patient patient = buildPatientFromForm();
        patient.setId(selectedPatientId);
        SwingWorker<Patient, Void> worker = new SwingWorker<Patient, Void>() {
            @Override protected Patient doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((PatientService) reg.lookup("patient-service")).update(patient);
            }
            @Override protected void done() {
                try {
                    JOptionPane.showMessageDialog(PatientModule.this, "Patient updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm(); loadPatients();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void deletePatient() {
        if (selectedPatientId == -1) { JOptionPane.showMessageDialog(this, "Select a patient to delete.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this patient?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        Patient patient = new Patient();
        patient.setId(selectedPatientId);
        SwingWorker<Patient, Void> worker = new SwingWorker<Patient, Void>() {
            @Override protected Patient doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((PatientService) reg.lookup("patient-service")).delete(patient);
            }
            @Override protected void done() {
                try {
                    JOptionPane.showMessageDialog(PatientModule.this, "Patient deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm(); loadPatients();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void populateForm() {
        int row = patientTable.getSelectedRow();
        selectedPatientId = (int) tableModel.getValueAt(row, 0);
        fullNameTxt.setText((String) tableModel.getValueAt(row, 1));
        nationalIdTxt.setText((String) tableModel.getValueAt(row, 2));
        phoneTxt.setText((String) tableModel.getValueAt(row, 3));
        emailTxt.setText(Objects.toString(tableModel.getValueAt(row, 4), ""));
        genderCombo.setSelectedItem(tableModel.getValueAt(row, 5));
        dobTxt.setText(Objects.toString(tableModel.getValueAt(row, 6), ""));
        allergiesTxt.setText(Objects.toString(tableModel.getValueAt(row, 7), ""));
    }

    private boolean validateForm() {
        if (fullNameTxt.getText().trim().isEmpty()) { showError("Full name is required."); return false; }

        // National ID: exactly 16 digits
        if (nationalIdTxt.getText().trim().isEmpty()) { showError("National ID is required."); return false; }
        if (!nationalIdTxt.getText().trim().matches("[0-9]{16}")) { showError("National ID must be exactly 16 digits."); return false; }

        // Phone: Rwandan format - starts with 07 and has exactly 10 digits
        if (phoneTxt.getText().trim().isEmpty()) { showError("Phone number is required."); return false; }
        if (!phoneTxt.getText().trim().matches("07[2389][0-9]{7}")) {
            showError("Phone number must be a valid Rwandan number (e.g. 0781234567)."); return false;
        }

        // Email: optional but must be valid if provided
        if (!emailTxt.getText().trim().isEmpty() && !emailTxt.getText().trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Email format is invalid."); return false;
        }

        // Date of Birth: optional but must be real date if provided
        if (!dobTxt.getText().trim().isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                java.util.Date dob = sdf.parse(dobTxt.getText().trim());
                // Must be in the past
                if (dob.after(new java.util.Date())) {
                    showError("Date of birth cannot be in the future."); return false;
                }
                // Must be realistic (not before 1900)
                java.util.Calendar minDate = java.util.Calendar.getInstance();
                minDate.set(1900, 0, 1);
                if (dob.before(minDate.getTime())) {
                    showError("Date of birth is not realistic (before 1900)."); return false;
                }
            } catch (Exception e) {
                showError("Date of birth must be a valid date in format yyyy-MM-dd (e.g. 1990-05-20)."); return false;
            }
        }
        return true;
    }

    private Patient buildPatientFromForm() {
        Patient p = new Patient();
        p.setFullName(fullNameTxt.getText().trim());
        p.setNationalId(nationalIdTxt.getText().trim());
        p.setPhone(phoneTxt.getText().trim());
        p.setEmail(emailTxt.getText().trim());
        p.setAddress(addressTxt.getText().trim());
        p.setDateOfBirth(dobTxt.getText().trim());
        p.setGender((String) genderCombo.getSelectedItem());
        p.setAllergies(allergiesTxt.getText().trim());
        return p;
    }

    private void clearForm() {
        selectedPatientId = -1;
        fullNameTxt.setText(""); nationalIdTxt.setText(""); phoneTxt.setText("");
        emailTxt.setText(""); addressTxt.setText(""); dobTxt.setText("");
        allergiesTxt.setText(""); genderCombo.setSelectedIndex(0);
        patientTable.clearSelection();
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
