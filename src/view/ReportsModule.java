package view;

import java.awt.*;
import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import model.*;
import service.*;

public class ReportsModule extends JPanel {

    private static final Color PRIMARY   = new Color(0, 102, 153);
    private static final Color SUCCESS   = new Color(34, 139, 34);
    private static final Color BG        = new Color(240, 245, 250);
    private static final Color WHITE     = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 30, 30);
    private static final Color TEXT_GRAY = new Color(120, 120, 120);

    private final User currentUser;
    private DefaultTableModel tableModel;
    private JTable reportTable;
    private JComboBox<String> reportTypeCombo;
    private JTextField startDateTxt, endDateTxt;
    private JLabel summaryLabel;

    public ReportsModule(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        add(buildControlPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildSummaryPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(WHITE);
        panel.setBorder(new CompoundBorder(new LineBorder(new Color(220,230,240),1), new EmptyBorder(10,14,10,14)));

        // ── Row 1: Title + Report type + Date range ──────────────────────
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row1.setOpaque(false);

        JLabel title = new JLabel("📊 Reports & Export");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_DARK);

        reportTypeCombo = new JComboBox<>(new String[]{
            "Dispensing History", "Drug Stock Report", "Low Stock Alert",
            "Expired Drugs", "Patient List", "Prescription Report"
        });
        reportTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reportTypeCombo.setPreferredSize(new Dimension(210, 34));

        JLabel fromLabel = new JLabel("From:");
        fromLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        startDateTxt = new JTextField("2024-01-01", 10);
        startDateTxt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        startDateTxt.setPreferredSize(new Dimension(110, 34));

        JLabel toLabel = new JLabel("To:");
        toLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        endDateTxt = new JTextField(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()), 10);
        endDateTxt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        endDateTxt.setPreferredSize(new Dimension(110, 34));

        row1.add(title);
        row1.add(Box.createHorizontalStrut(6));
        row1.add(reportTypeCombo);
        row1.add(fromLabel);
        row1.add(startDateTxt);
        row1.add(toLabel);
        row1.add(endDateTxt);

        // ── Row 2: Action buttons ─────────────────────────────────────────
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row2.setOpaque(false);

        JButton generateBtn  = UIHelper.button("▶  Generate Report", UIHelper.PRIMARY);
        JButton exportCsvBtn = UIHelper.button("📄  Export CSV",      UIHelper.SUCCESS);
        JButton exportPdfBtn = UIHelper.button("📕  Export PDF",      UIHelper.DANGER);

        generateBtn.setPreferredSize(new Dimension(170, 36));
        exportCsvBtn.setPreferredSize(new Dimension(140, 36));
        exportPdfBtn.setPreferredSize(new Dimension(140, 36));

        generateBtn.addActionListener(e -> generateReport());
        exportCsvBtn.addActionListener(e -> exportCSV());
        exportPdfBtn.addActionListener(e -> exportPDF());

        row2.add(generateBtn);
        row2.add(exportCsvBtn);
        row2.add(exportPdfBtn);

        panel.add(row1, BorderLayout.NORTH);
        panel.add(row2, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);
        panel.setBorder(new LineBorder(new Color(220,230,240),1));

        tableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        reportTable = new JTable(tableModel);
        UIHelper.styleTable(reportTable);

        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(230, 245, 255));
        panel.setBorder(new CompoundBorder(new LineBorder(PRIMARY,1), new EmptyBorder(10,16,10,16)));
        summaryLabel = new JLabel("Generate a report to see summary.");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        summaryLabel.setForeground(PRIMARY);
        panel.add(summaryLabel);
        return panel;
    }

    private void generateReport() {
        String type = (String) reportTypeCombo.getSelectedItem();
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                switch (type) {
                    case "Dispensing History":   loadDispensingReport(reg); break;
                    case "Drug Stock Report":    loadDrugStockReport(reg); break;
                    case "Low Stock Alert":      loadLowStockReport(reg); break;
                    case "Expired Drugs":        loadExpiredDrugsReport(reg); break;
                    case "Patient List":         loadPatientReport(reg); break;
                    case "Prescription Report":  loadPrescriptionReport(reg); break;
                }
                return null;
            }
            @Override protected void done() {
                try { get(); } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ReportsModule.this, "Failed to generate report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadDispensingReport(Registry reg) throws Exception {
        DispensingService svc = (DispensingService) reg.lookup("dispensing-service");
        List<Dispensing> list = svc.findByDateRange(startDateTxt.getText().trim(), endDateTxt.getText().trim());
        SwingUtilities.invokeLater(() -> {
            tableModel.setColumnIdentifiers(new String[]{"ID","Prescription No","Patient","Pharmacist","Date","Qty","Total (RWF)","Payment","Status"});
            tableModel.setRowCount(0);
            double total = 0;
            for (Dispensing d : list) {
                String rxNo    = d.getPrescription() != null ? d.getPrescription().getPrescriptionNumber() : "N/A";
                String patient = d.getPrescription() != null && d.getPrescription().getPatient() != null ? d.getPrescription().getPatient().getFullName() : "N/A";
                String pharm   = d.getDispensedBy() != null ? d.getDispensedBy().getFullName() : "N/A";
                tableModel.addRow(new Object[]{d.getId(), rxNo, patient, pharm, d.getDispensingDate(), d.getQuantityDispensed(), String.format("%.0f", d.getTotalCost()), d.getPaymentMethod(), d.getStatus()});
                total += d.getTotalCost();
            }
            summaryLabel.setText("Total Records: " + list.size() + "  |  Total Revenue: RWF " + String.format("%.0f", total));
        });
    }

    private void loadDrugStockReport(Registry reg) throws Exception {
        List<Drug> list = ((DrugService) reg.lookup("drug-service")).findAll();
        SwingUtilities.invokeLater(() -> {
            tableModel.setColumnIdentifiers(new String[]{"ID","Name","Code","Category","Stock","Reorder Level","Expiry","Price (RWF)","Status"});
            tableModel.setRowCount(0);
            for (Drug d : list) {
                String status = d.getStockQuantity() <= d.getReorderLevel() ? "⚠ LOW STOCK" : "✔ OK";
                tableModel.addRow(new Object[]{d.getId(), d.getName(), d.getDrugCode(), d.getCategory(), d.getStockQuantity(), d.getReorderLevel(), d.getExpiryDate(), String.format("%.0f", d.getUnitPrice()), status});
            }
            summaryLabel.setText("Total Drugs: " + list.size());
        });
    }

    private void loadLowStockReport(Registry reg) throws Exception {
        List<Drug> list = ((DrugService) reg.lookup("drug-service")).findLowStock();
        SwingUtilities.invokeLater(() -> {
            tableModel.setColumnIdentifiers(new String[]{"ID","Name","Code","Category","Stock","Reorder Level","Expiry"});
            tableModel.setRowCount(0);
            for (Drug d : list) tableModel.addRow(new Object[]{d.getId(), d.getName(), d.getDrugCode(), d.getCategory(), d.getStockQuantity(), d.getReorderLevel(), d.getExpiryDate()});
            summaryLabel.setText("⚠ " + list.size() + " drug(s) below reorder level - Restock required!");
        });
    }

    private void loadExpiredDrugsReport(Registry reg) throws Exception {
        List<Drug> list = ((DrugService) reg.lookup("drug-service")).findExpiredDrugs();
        SwingUtilities.invokeLater(() -> {
            tableModel.setColumnIdentifiers(new String[]{"ID","Name","Code","Category","Stock","Expiry Date"});
            tableModel.setRowCount(0);
            for (Drug d : list) tableModel.addRow(new Object[]{d.getId(), d.getName(), d.getDrugCode(), d.getCategory(), d.getStockQuantity(), d.getExpiryDate()});
            summaryLabel.setText("🗓 " + list.size() + " expired drug(s) found - Remove from stock immediately!");
        });
    }

    private void loadPatientReport(Registry reg) throws Exception {
        List<Patient> list = ((PatientService) reg.lookup("patient-service")).findAll();
        SwingUtilities.invokeLater(() -> {
            tableModel.setColumnIdentifiers(new String[]{"ID","Full Name","National ID","Phone","Email","Gender","Date of Birth"});
            tableModel.setRowCount(0);
            for (Patient p : list) tableModel.addRow(new Object[]{p.getId(), p.getFullName(), p.getNationalId(), p.getPhone(), p.getEmail(), p.getGender(), p.getDateOfBirth()});
            summaryLabel.setText("Total Patients: " + list.size());
        });
    }

    private void loadPrescriptionReport(Registry reg) throws Exception {
        List<Prescription> list = ((PrescriptionService) reg.lookup("prescription-service")).findAll();
        SwingUtilities.invokeLater(() -> {
            tableModel.setColumnIdentifiers(new String[]{"ID","Rx Number","Patient","Doctor","Issued","Expiry","Status"});
            tableModel.setRowCount(0);
            for (Prescription rx : list) {
                String patient = rx.getPatient() != null ? rx.getPatient().getFullName() : "N/A";
                tableModel.addRow(new Object[]{rx.getId(), rx.getPrescriptionNumber(), patient, rx.getDoctorName(), rx.getIssuedDate(), rx.getExpiryDate(), rx.getStatus()});
            }
            summaryLabel.setText("Total Prescriptions: " + list.size());
        });
    }

    private void exportCSV() {
        if (tableModel.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "Generate a report first.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("pharmacy_report.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
            // Header
            StringBuilder header = new StringBuilder();
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                header.append(tableModel.getColumnName(i));
                if (i < tableModel.getColumnCount() - 1) header.append(",");
            }
            pw.println(header);
            // Rows
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                StringBuilder row = new StringBuilder();
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    Object val = tableModel.getValueAt(r, c);
                    row.append(val != null ? val.toString().replace(",", ";") : "");
                    if (c < tableModel.getColumnCount() - 1) row.append(",");
                }
                pw.println(row);
            }
            JOptionPane.showMessageDialog(this, "Report exported to CSV successfully!\n" + fc.getSelectedFile().getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPDF() {
        if (tableModel.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "Generate a report first.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("pharmacy_report.txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
            pw.println("=======================================================");
            pw.println("       PHARMACARE - DRUG DISPENSING SYSTEM");
            pw.println("       Report: " + reportTypeCombo.getSelectedItem());
            pw.println("       Generated: " + new java.util.Date());
            pw.println("       Generated By: " + currentUser.getFullName());
            pw.println("=======================================================");
            pw.println();
            // Header
            StringBuilder header = new StringBuilder();
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                header.append(String.format("%-20s", tableModel.getColumnName(i)));
            }
            pw.println(header);
            StringBuilder separator = new StringBuilder();
            for (int i = 0; i < tableModel.getColumnCount() * 20; i++) separator.append("-");
            pw.println(separator.toString());
            // Rows
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                StringBuilder row = new StringBuilder();
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    Object val = tableModel.getValueAt(r, c);
                    row.append(String.format("%-20s", val != null ? val.toString() : ""));
                }
                pw.println(row);
            }
            pw.println();
            pw.println("Total Records: " + tableModel.getRowCount());
            pw.println("=======================================================");
            JOptionPane.showMessageDialog(this, "Report exported successfully!\n" + fc.getSelectedFile().getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton styledButton(String text, Color color) {
        return UIHelper.button(text, color);
    }
}
