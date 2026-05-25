package view;

import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.*;
import model.User;
import service.UserService;

public class RegisterForm extends JFrame {

    private static final Color PRIMARY   = new Color(0, 102, 153);
    private static final Color ACCENT    = new Color(0, 168, 204);
    private static final Color BG        = new Color(240, 245, 250);
    private static final Color WHITE     = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 30, 30);
    private static final Color TEXT_GRAY = new Color(120, 120, 120);
    private static final Color SUCCESS   = new Color(34, 139, 34);
    private static final Color DANGER    = new Color(200, 50, 50);

    private JTextField fullNameTxt, usernameTxt, emailTxt;
    private JPasswordField passwordTxt, confirmPasswordTxt;
    private JComboBox<String> roleCombo;
    private JLabel statusLabel;
    private JButton registerBtn;

    public RegisterForm() {
        initComponents();
    }

    private void initComponents() {
        setTitle("PharmaCare - Create Account");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildRightPanel(), BorderLayout.CENTER);
    }

    private JPanel buildLeftPanel() {
        JPanel left = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 80, 130), 0, getHeight(), new Color(0, 30, 70));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // decorative circles
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillOval(-60, -60, 250, 250);
                g2.fillOval(150, 400, 200, 200);
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fillOval(50, 200, 300, 300);
            }
        };
        left.setPreferredSize(new Dimension(380, 0));
        left.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("💊", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("PharmaCare", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Drug Dispensing System", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(160, 210, 235));
        sub.setAlignmentX(CENTER_ALIGNMENT);

        JPanel divider = new JPanel();
        divider.setOpaque(false);
        divider.setMaximumSize(new Dimension(180, 2));
        divider.setBackground(new Color(255, 255, 255, 60));
        divider.setPreferredSize(new Dimension(180, 1));
        divider.setBorder(new MatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 60)));
        divider.setAlignmentX(CENTER_ALIGNMENT);

        JLabel joinLabel = new JLabel("Join Our System", SwingConstants.CENTER);
        joinLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        joinLabel.setForeground(new Color(200, 230, 245));
        joinLabel.setAlignmentX(CENTER_ALIGNMENT);

        String[] steps = {
            "1.  Fill in your details",
            "2.  Choose your role",
            "3.  Submit & login"
        };

        content.add(icon);
        content.add(Box.createVerticalStrut(10));
        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(sub);
        content.add(Box.createVerticalStrut(24));
        content.add(divider);
        content.add(Box.createVerticalStrut(20));
        content.add(joinLabel);
        content.add(Box.createVerticalStrut(16));

        for (String step : steps) {
            JLabel lbl = new JLabel(step);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setForeground(new Color(190, 220, 240));
            lbl.setAlignmentX(CENTER_ALIGNMENT);
            content.add(lbl);
            content.add(Box.createVerticalStrut(10));
        }

        left.add(content);
        return left;
    }

    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(BG);

        JPanel card = new JPanel();
        card.setBackground(WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 230, 240), 1, true),
            new EmptyBorder(30, 40, 30, 40)
        ));
        card.setPreferredSize(new Dimension(460, 540));

        JLabel heading = new JLabel("Create Account");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setForeground(TEXT_DARK);
        heading.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subHeading = new JLabel("Fill in the form below to register");
        subHeading.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subHeading.setForeground(TEXT_GRAY);
        subHeading.setAlignmentX(CENTER_ALIGNMENT);

        // form fields in 2 columns
        JPanel formGrid = new JPanel(new GridLayout(0, 2, 14, 10));
        formGrid.setOpaque(false);
        formGrid.setAlignmentX(LEFT_ALIGNMENT);

        fullNameTxt        = new JTextField();
        usernameTxt        = new JTextField();
        emailTxt           = new JTextField();
        passwordTxt        = new JPasswordField();
        confirmPasswordTxt = new JPasswordField();
        roleCombo          = new JComboBox<>(new String[]{"PHARMACIST", "ADMIN"});

        addFormGroup(formGrid, "Full Name *",        fullNameTxt);
        addFormGroup(formGrid, "Username *",         usernameTxt);
        addFormGroup(formGrid, "Email *",            emailTxt);
        addFormGroup(formGrid, "Role *",             roleCombo);
        addFormGroup(formGrid, "Password *",         passwordTxt);
        addFormGroup(formGrid, "Confirm Password *", confirmPasswordTxt);

        // register button
        registerBtn = new JButton("CREATE ACCOUNT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? new Color(0, 100, 50) : getModel().isRollover() ? new Color(0, 180, 90) : SUCCESS;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerBtn.setForeground(WHITE);
        registerBtn.setOpaque(false);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        registerBtn.setAlignmentX(LEFT_ALIGNMENT);
        registerBtn.addActionListener(e -> handleRegister());

        // back to login link
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        backPanel.setOpaque(false);
        backPanel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel backLabel = new JLabel("Already have an account?");
        backLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backLabel.setForeground(TEXT_GRAY);
        JLabel loginLink = new JLabel("Sign In");
        loginLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginLink.setForeground(PRIMARY);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                new LoginForm().setVisible(true);
                dispose();
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { loginLink.setForeground(ACCENT); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { loginLink.setForeground(PRIMARY); }
        });
        backPanel.add(backLabel);
        backPanel.add(loginLink);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        card.add(heading);
        card.add(Box.createVerticalStrut(4));
        card.add(subHeading);
        card.add(Box.createVerticalStrut(20));
        card.add(formGrid);
        card.add(Box.createVerticalStrut(16));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(backPanel);

        right.add(card);
        return right;
    }

    private void addFormGroup(JPanel parent, String label, JComponent field) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(80, 80, 80));

        if (field instanceof JTextField || field instanceof JPasswordField) {
            ((JTextField) field).setFont(new Font("Segoe UI", Font.PLAIN, 13));
            field.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 210, 220), 1, true),
                new EmptyBorder(8, 10, 8, 10)
            ));
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        } else if (field instanceof JComboBox) {
            ((JComboBox) field).setFont(new Font("Segoe UI", Font.PLAIN, 13));
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        }

        group.add(lbl);
        group.add(Box.createVerticalStrut(4));
        group.add(field);
        parent.add(group);
    }

    private void handleRegister() {
        String fullName  = fullNameTxt.getText().trim();
        String username  = usernameTxt.getText().trim();
        String email     = emailTxt.getText().trim();
        String password  = new String(passwordTxt.getPassword()).trim();
        String confirm   = new String(confirmPasswordTxt.getPassword()).trim();
        String role      = (String) roleCombo.getSelectedItem();

        // Validations
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showStatus("All fields are required.", DANGER); return;
        }
        if (fullName.length() < 3) {
            showStatus("Full name must be at least 3 characters.", DANGER); return;
        }
        if (username.length() < 4) {
            showStatus("Username must be at least 4 characters.", DANGER); return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showStatus("Invalid email format.", DANGER); return;
        }
        if (password.length() < 6) {
            showStatus("Password must be at least 6 characters.", DANGER); return;
        }
        if (!password.equals(confirm)) {
            showStatus("Passwords do not match.", DANGER); return;
        }

        registerBtn.setEnabled(false);
        registerBtn.setText("Creating account...");

        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                UserService svc = (UserService) reg.lookup("user-service");
                User existing = svc.findByUsername(username);
                if (!Objects.isNull(existing)) throw new Exception("Username already taken!");
                User user = new User();
                user.setFullName(fullName);
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(password);
                user.setRole(role);
                user.setActive(true);
                return svc.save(user);
            }
            @Override
            protected void done() {
                try {
                    User saved = get();
                    if (Objects.isNull(saved)) {
                        showStatus("Registration failed. Try again.", DANGER);
                    } else {
                        JOptionPane.showMessageDialog(RegisterForm.this,
                            "Account created successfully!\nYou can now sign in.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        new LoginForm().setVisible(true);
                        dispose();
                    }
                } catch (Exception ex) {
                    showStatus(ex.getMessage(), DANGER);
                } finally {
                    registerBtn.setEnabled(true);
                    registerBtn.setText("CREATE ACCOUNT");
                }
            }
        };
        worker.execute();
    }

    private void showStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
