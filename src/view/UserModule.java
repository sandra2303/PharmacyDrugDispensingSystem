package view;

import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import model.User;
import service.UserService;

public class UserModule extends JPanel {

    private static final Color PRIMARY   = new Color(0, 102, 153);
    private static final Color SUCCESS   = new Color(34, 139, 34);
    private static final Color DANGER    = new Color(200, 50, 50);
    private static final Color BG        = new Color(240, 245, 250);
    private static final Color WHITE     = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 30, 30);

    private final User currentUser;
    private DefaultTableModel tableModel;
    private JTable userTable;
    private JTextField usernameTxt, fullNameTxt, emailTxt;
    private JPasswordField passwordTxt;
    private JComboBox<String> roleCombo, statusCombo;
    private int selectedUserId = -1;

    public UserModule(User user) {
        this.currentUser = user;
        initComponents();
        loadUsers();
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

        JLabel title = new JLabel("👥 User Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_DARK);

        JButton refreshBtn = UIHelper.button("↻ Refresh", UIHelper.DARK);
        refreshBtn.addActionListener(e -> loadUsers());

        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(refreshBtn, BorderLayout.EAST);
        return toolbar;
    }

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(WHITE);
        wrapper.setPreferredSize(new Dimension(280, 0));
        wrapper.setBorder(new CompoundBorder(new LineBorder(new Color(220,230,240),1), new EmptyBorder(16,16,16,16)));

        JLabel formTitle = new JLabel("User Details");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setForeground(PRIMARY);
        formTitle.setBorder(new MatteBorder(0,0,1,0,new Color(220,230,240)));

        JPanel form = new JPanel(new GridLayout(0,1,4,4));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10,0,10,0));

        fullNameTxt = formField(form, "Full Name *");
        usernameTxt = formField(form, "Username *");

        form.add(fieldLabel("Password *"));
        passwordTxt = new JPasswordField();
        passwordTxt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordTxt.setBorder(new CompoundBorder(new LineBorder(new Color(200,210,220),1,true), new EmptyBorder(5,8,5,8)));
        form.add(passwordTxt);

        emailTxt = formField(form, "Email *");

        form.add(fieldLabel("Role *"));
        roleCombo = new JComboBox<>(new String[]{"PHARMACIST","ADMIN"});
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(roleCombo);

        form.add(fieldLabel("Status"));
        statusCombo = new JComboBox<>(new String[]{"Active","Inactive"});
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(statusCombo);

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(0, 90));
        JButton saveBtn   = UIHelper.button("💾 Save",   UIHelper.SUCCESS);
        JButton updateBtn = UIHelper.button("✏ Update",  UIHelper.PRIMARY);
        JButton deleteBtn = UIHelper.button("🗑 Delete",  UIHelper.DANGER);
        JButton clearBtn  = UIHelper.button("✖ Clear",   UIHelper.DARK);

        saveBtn.addActionListener(e -> saveUser());
        updateBtn.addActionListener(e -> updateUser());
        deleteBtn.addActionListener(e -> deleteUser());
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

        String[] cols = {"ID","Full Name","Username","Email","Role","Active"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(tableModel);
        UIHelper.styleTable(userTable);
        userTable.getColumnModel().getColumn(0).setPreferredWidth(40);

        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userTable.getSelectedRow() != -1) populateForm();
        });

        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadUsers() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
            @Override protected List<User> doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((UserService) reg.lookup("user-service")).findAll();
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (User u : get()) {
                        tableModel.addRow(new Object[]{u.getId(), u.getFullName(), u.getUsername(), u.getEmail(), u.getRole(), u.isActive() ? "Yes" : "No"});
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserModule.this, "Failed to load users.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void saveUser() {
        if (!validateForm()) return;
        User user = buildUserFromForm();
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override protected User doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                UserService svc = (UserService) reg.lookup("user-service");
                User existing = svc.findByUsername(user.getUsername());
                if (!Objects.isNull(existing)) throw new Exception("Username already exists!");
                return svc.save(user);
            }
            @Override protected void done() {
                try {
                    if (Objects.isNull(get())) {
                        JOptionPane.showMessageDialog(UserModule.this, "Failed to save user.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(UserModule.this, "User created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm(); loadUsers();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserModule.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateUser() {
        if (selectedUserId == -1) { JOptionPane.showMessageDialog(this, "Select a user to update.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        if (fullNameTxt.getText().trim().isEmpty() || emailTxt.getText().trim().isEmpty()) { showError("Full name and email are required."); return; }
        User user = buildUserFromForm();
        user.setId(selectedUserId);
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override protected User doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((UserService) reg.lookup("user-service")).update(user);
            }
            @Override protected void done() {
                try {
                    JOptionPane.showMessageDialog(UserModule.this, "User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm(); loadUsers();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void deleteUser() {
        if (selectedUserId == -1) { JOptionPane.showMessageDialog(this, "Select a user to delete.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        if (selectedUserId == currentUser.getId()) { showError("You cannot delete your own account!"); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this user?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        User user = new User();
        user.setId(selectedUserId);
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override protected User doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                return ((UserService) reg.lookup("user-service")).delete(user);
            }
            @Override protected void done() {
                try {
                    JOptionPane.showMessageDialog(UserModule.this, "User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm(); loadUsers();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void populateForm() {
        int row = userTable.getSelectedRow();
        selectedUserId = (int) tableModel.getValueAt(row, 0);
        fullNameTxt.setText((String) tableModel.getValueAt(row, 1));
        usernameTxt.setText((String) tableModel.getValueAt(row, 2));
        emailTxt.setText((String) tableModel.getValueAt(row, 3));
        roleCombo.setSelectedItem(tableModel.getValueAt(row, 4));
        statusCombo.setSelectedItem("Yes".equals(tableModel.getValueAt(row, 5)) ? "Active" : "Inactive");
    }

    private boolean validateForm() {
        if (fullNameTxt.getText().trim().isEmpty()) { showError("Full name is required."); return false; }
        if (usernameTxt.getText().trim().isEmpty()) { showError("Username is required."); return false; }
        if (new String(passwordTxt.getPassword()).trim().isEmpty()) { showError("Password is required."); return false; }
        if (new String(passwordTxt.getPassword()).trim().length() < 6) { showError("Password must be at least 6 characters."); return false; }
        if (emailTxt.getText().trim().isEmpty()) { showError("Email is required."); return false; }
        if (!emailTxt.getText().trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) { showError("Invalid email format."); return false; }
        return true;
    }

    private User buildUserFromForm() {
        User user = new User();
        user.setFullName(fullNameTxt.getText().trim());
        user.setUsername(usernameTxt.getText().trim());
        user.setPassword(new String(passwordTxt.getPassword()).trim());
        user.setEmail(emailTxt.getText().trim());
        user.setRole((String) roleCombo.getSelectedItem());
        user.setActive("Active".equals(statusCombo.getSelectedItem()));
        return user;
    }

    private void clearForm() {
        selectedUserId = -1;
        fullNameTxt.setText(""); usernameTxt.setText(""); emailTxt.setText("");
        passwordTxt.setText(""); roleCombo.setSelectedIndex(0); statusCombo.setSelectedIndex(0);
        userTable.clearSelection();
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
