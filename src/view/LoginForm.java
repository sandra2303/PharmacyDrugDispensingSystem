package view;

import java.awt.*;
import java.awt.event.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.Timer;
import model.User;
import service.UserService;

public class LoginForm extends JFrame {

    private static final Color PRIMARY    = new Color(0, 102, 153);
    private static final Color ACCENT     = new Color(0, 168, 204);
    private static final Color BG         = new Color(235, 242, 250);
    private static final Color WHITE      = Color.WHITE;
    private static final Color TEXT_DARK  = new Color(25, 25, 25);
    private static final Color TEXT_GRAY  = new Color(120, 120, 120);
    private static final Color DANGER     = new Color(200, 50, 50);
    private static final Color SUCCESS    = new Color(34, 139, 34);

    private JTextField usernameTxt;
    private JPasswordField passwordTxt;
    private JButton loginBtn;
    private JLabel statusLabel;
    private JCheckBox showPasswordCheck;

    public LoginForm() {
        initComponents();
    }

    private void initComponents() {
        setTitle("PharmaCare - Drug Dispensing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildRightPanel(), BorderLayout.CENTER);
    }

    // ─── LEFT BRANDING PANEL ────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel left = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 80, 130), 0, getHeight(), new Color(0, 25, 60));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // decorative circles
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(-80, -80, 280, 280);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(100, 350, 260, 260);
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fillOval(200, 100, 180, 180);
            }
        };
        left.setPreferredSize(new Dimension(400, 0));
        left.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(0, 30, 0, 30));

        // icon
        JLabel icon = new JLabel("💊", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 68));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        // title
        JLabel title = new JLabel("PharmaCare", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);

        // subtitle
        JLabel subtitle = new JLabel("Drug Dispensing System", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(160, 210, 235));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        // divider
        JPanel divider = new JPanel();
        divider.setOpaque(false);
        divider.setAlignmentX(CENTER_ALIGNMENT);
        divider.setMaximumSize(new Dimension(200, 1));
        divider.setBorder(new MatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 50)));

        // features
        String[][] features = {
            {"✔", "Prescription Verification"},
            {"✔", "Drug Interaction Alerts"},
            {"✔", "Real-time Stock Management"},
            {"✔", "Secure Drug Dispensing"},
            {"✔", "Reports & Analytics"}
        };

        content.add(icon);
        content.add(Box.createVerticalStrut(10));
        content.add(title);
        content.add(Box.createVerticalStrut(5));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(22));
        content.add(divider);
        content.add(Box.createVerticalStrut(22));

        for (String[] f : features) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            row.setOpaque(false);
            row.setAlignmentX(CENTER_ALIGNMENT);
            row.setMaximumSize(new Dimension(300, 28));

            JLabel check = new JLabel(f[0]);
            check.setFont(new Font("Segoe UI", Font.BOLD, 13));
            check.setForeground(new Color(100, 220, 150));

            JLabel text = new JLabel(f[1]);
            text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            text.setForeground(new Color(190, 220, 240));

            row.add(check);
            row.add(text);
            content.add(row);
            content.add(Box.createVerticalStrut(6));
        }

        // version badge
        content.add(Box.createVerticalStrut(20));
        JLabel version = new JLabel("v1.0.0  |  Student ID: 27399", SwingConstants.CENTER);
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(new Color(120, 170, 200));
        version.setAlignmentX(CENTER_ALIGNMENT);
        content.add(version);

        left.add(content);
        return left;
    }

    // ─── RIGHT LOGIN PANEL ──────────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(BG);

        JPanel card = new JPanel();
        card.setBackground(WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(215, 228, 242), 1, true),
            new EmptyBorder(44, 48, 36, 48)
        ));
        card.setPreferredSize(new Dimension(420, 500));

        // top accent bar
        JPanel accentBar = new JPanel();
        accentBar.setBackground(PRIMARY);
        accentBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        accentBar.setAlignmentX(LEFT_ALIGNMENT);

        // heading
        JLabel heading = new JLabel("Welcome Back!");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setForeground(TEXT_DARK);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subHeading = new JLabel("Sign in to access your pharmacy dashboard");
        subHeading.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subHeading.setForeground(TEXT_GRAY);
        subHeading.setAlignmentX(LEFT_ALIGNMENT);

        // username
        JLabel userLabel = buildFieldLabel("Username");
        usernameTxt = new JTextField();
        styleTextField(usernameTxt);

        // password
        JLabel passLabel = buildFieldLabel("Password");
        passwordTxt = new JPasswordField();
        styleTextField(passwordTxt);

        // show password checkbox
        showPasswordCheck = new JCheckBox("Show password");
        showPasswordCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPasswordCheck.setForeground(TEXT_GRAY);
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.setAlignmentX(LEFT_ALIGNMENT);
        showPasswordCheck.addActionListener(e -> {
            if (showPasswordCheck.isSelected()) {
                passwordTxt.setEchoChar((char) 0);
            } else {
                passwordTxt.setEchoChar('•');
            }
        });

        // login button
        loginBtn = new JButton("SIGN IN  →") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? new Color(0, 70, 120) : getModel().isRollover() ? ACCENT : PRIMARY;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setForeground(WHITE);
        loginBtn.setOpaque(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        loginBtn.setAlignmentX(LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> handleLogin());

        // press Enter to login
        usernameTxt.addActionListener(e -> handleLogin());
        passwordTxt.addActionListener(e -> handleLogin());

        // status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        // divider
        JPanel orDivider = new JPanel(new GridBagLayout());
        orDivider.setOpaque(false);
        orDivider.setAlignmentX(LEFT_ALIGNMENT);
        orDivider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        JSeparator s1 = new JSeparator(); s1.setForeground(new Color(220, 228, 238));
        JSeparator s2 = new JSeparator(); s2.setForeground(new Color(220, 228, 238));
        JLabel orLabel = new JLabel("  OR  ");
        orLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        orLabel.setForeground(TEXT_GRAY);
        orDivider.add(s1, gc);
        gc.weightx = 0;
        orDivider.add(orLabel, gc);
        gc.weightx = 1;
        orDivider.add(s2, gc);

        // register link
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        registerPanel.setOpaque(false);
        registerPanel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel noAccount = new JLabel("Don't have an account?");
        noAccount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        noAccount.setForeground(TEXT_GRAY);
        JLabel registerLink = new JLabel("Create Account");
        registerLink.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerLink.setForeground(PRIMARY);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                new RegisterForm().setVisible(true);
                dispose();
            }
            @Override public void mouseEntered(MouseEvent e) { registerLink.setForeground(ACCENT); }
            @Override public void mouseExited(MouseEvent e)  { registerLink.setForeground(PRIMARY); }
        });
        registerPanel.add(noAccount);
        registerPanel.add(registerLink);

        // footer
        JLabel footer = new JLabel("© 2026 PharmaCare Drug Dispensing System", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(180, 190, 200));
        footer.setAlignmentX(CENTER_ALIGNMENT);

        card.add(accentBar);
        card.add(Box.createVerticalStrut(28));
        card.add(heading);
        card.add(Box.createVerticalStrut(4));
        card.add(subHeading);
        card.add(Box.createVerticalStrut(28));
        card.add(userLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameTxt);
        card.add(Box.createVerticalStrut(14));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordTxt);
        card.add(Box.createVerticalStrut(8));
        card.add(showPasswordCheck);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(orDivider);
        card.add(Box.createVerticalStrut(14));
        card.add(registerPanel);
        card.add(Box.createVerticalStrut(16));
        card.add(footer);

        right.add(card);
        return right;
    }

    // ─── HELPERS ────────────────────────────────────────────────────────────
    private JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 212, 224), 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                tf.setBorder(new CompoundBorder(
                    new LineBorder(PRIMARY, 2, true),
                    new EmptyBorder(9, 11, 9, 11)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                tf.setBorder(new CompoundBorder(
                    new LineBorder(new Color(200, 212, 224), 1, true),
                    new EmptyBorder(10, 12, 10, 12)
                ));
            }
        });
    }

    // ─── LOGIN LOGIC ────────────────────────────────────────────────────────
    private void handleLogin() {
        String username = usernameTxt.getText().trim();
        String password = new String(passwordTxt.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter username and password.", DANGER);
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in...");

        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                Registry registry = LocateRegistry.getRegistry("127.0.0.1", 5000);
                UserService service = (UserService) registry.lookup("user-service");
                return service.login(username, password);
            }
            @Override
            protected void done() {
                try {
                    User user = get();
                    if (Objects.isNull(user)) {
                        showStatus("Invalid username or password.", DANGER);
                    } else {
                        showOtpVerification(user);
                    }
                } catch (Exception ex) {
                    showStatus("Cannot connect to server. Is it running?", DANGER);
                } finally {
                    loginBtn.setEnabled(true);
                    loginBtn.setText("SIGN IN  →");
                }
            }
        };
        worker.execute();
    }

    private void showOtpVerification(User user) {
        // Simulate sending OTP to email
        final String[] currentOtp = { generateOtp() };
        simulateSendOtp(user.getEmail(), currentOtp[0]);

        // ── Build OTP Dialog ────────────────────────────────────────────
        JDialog dialog = new JDialog(this, "OTP Verification", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(30, 44, 30, 44));

        // Top accent bar
        JPanel topBar = new JPanel();
        topBar.setBackground(PRIMARY);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        topBar.setAlignmentX(CENTER_ALIGNMENT);

        // Header
        JLabel iconLbl = new JLabel("📧", SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel("Email Verification", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(new Color(25, 25, 25));
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);

        // Mask email for privacy: show only first 3 chars + ***@domain
        String email = user.getEmail();
        String maskedEmail = email.length() > 3
            ? email.substring(0, 3) + "***@" + email.substring(email.indexOf('@') + 1)
            : email;

        JLabel infoLbl = new JLabel(
            "<html><div style='text-align:center; width:360px'>A 6-digit OTP has been sent to<br>"
            + "<b>" + maskedEmail + "</b><br><br>"
            + "<span style='color:#888'>(Simulation: check NetBeans Output window for OTP)</span></div></html>",
            SwingConstants.CENTER);
        infoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoLbl.setAlignmentX(CENTER_ALIGNMENT);

        // Enter OTP label
        JLabel enterLbl = new JLabel("Enter 6-digit OTP below:", SwingConstants.CENTER);
        enterLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        enterLbl.setForeground(new Color(60, 60, 60));
        enterLbl.setAlignmentX(CENTER_ALIGNMENT);

        // OTP input field
        JTextField otpField = new JTextField();
        otpField.setFont(new Font("Segoe UI", Font.BOLD, 32));
        otpField.setHorizontalAlignment(JTextField.CENTER);
        otpField.setBorder(new CompoundBorder(
            new LineBorder(PRIMARY, 2, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        otpField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        otpField.setAlignmentX(CENTER_ALIGNMENT);

        // Countdown timer label
        JLabel timerLbl = new JLabel("OTP expires in: 60s", SwingConstants.CENTER);
        timerLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        timerLbl.setForeground(new Color(200, 100, 0));
        timerLbl.setAlignmentX(CENTER_ALIGNMENT);

        // Attempts label
        JLabel attemptsLbl = new JLabel("Attempts remaining: 3", SwingConstants.CENTER);
        attemptsLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        attemptsLbl.setForeground(new Color(130, 130, 130));
        attemptsLbl.setAlignmentX(CENTER_ALIGNMENT);

        // Buttons
        JButton verifyBtn = UIHelper.button("✔ Verify OTP", PRIMARY);
        verifyBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        verifyBtn.setAlignmentX(CENTER_ALIGNMENT);

        JButton resendBtn = new JButton("↻ Resend OTP");
        resendBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resendBtn.setForeground(PRIMARY);
        resendBtn.setBorderPainted(false);
        resendBtn.setContentAreaFilled(false);
        resendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resendBtn.setAlignmentX(CENTER_ALIGNMENT);
        resendBtn.setEnabled(false);

        // ── Countdown Timer (60 seconds) ────────────────────────────────
        final int[] secondsLeft = { 60 };
        final int[] attemptsLeft = { 3 };
        final boolean[] verified = { false };

        Timer countdown = new Timer(1000, null);
        countdown.addActionListener(e -> {
            secondsLeft[0]--;
            if (secondsLeft[0] > 0) {
                timerLbl.setText("OTP expires in: " + secondsLeft[0] + "s");
            } else {
                countdown.stop();
                timerLbl.setText("OTP has expired.");
                timerLbl.setForeground(new Color(200, 50, 50));
                verifyBtn.setEnabled(false);
                resendBtn.setEnabled(true);
                otpField.setEnabled(false);
            }
        });
        countdown.start();

        // ── Resend OTP ──────────────────────────────────────────────────
        resendBtn.addActionListener(e -> {
            currentOtp[0] = generateOtp();
            simulateSendOtp(user.getEmail(), currentOtp[0]);
            secondsLeft[0] = 60;
            attemptsLeft[0] = 3;
            timerLbl.setText("OTP expires in: 60s");
            timerLbl.setForeground(new Color(200, 100, 0));
            attemptsLbl.setText("Attempts remaining: 3");
            otpField.setText("");
            otpField.setEnabled(true);
            verifyBtn.setEnabled(true);
            resendBtn.setEnabled(false);
            countdown.restart();
            JOptionPane.showMessageDialog(dialog,
                "A new OTP has been sent to " + maskedEmail,
                "OTP Resent", JOptionPane.INFORMATION_MESSAGE);
        });

        // ── Verify Button ───────────────────────────────────────────────
        verifyBtn.addActionListener(e -> {
            String entered = otpField.getText().trim();
            if (entered.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter the OTP.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (entered.equals(currentOtp[0])) {
                verified[0] = true;
                countdown.stop();
                dialog.dispose();
            } else {
                attemptsLeft[0]--;
                if (attemptsLeft[0] > 0) {
                    attemptsLbl.setText("Attempts remaining: " + attemptsLeft[0]);
                    attemptsLbl.setForeground(new Color(200, 50, 50));
                    JOptionPane.showMessageDialog(dialog,
                        "Incorrect OTP. " + attemptsLeft[0] + " attempt(s) remaining.",
                        "Invalid OTP", JOptionPane.ERROR_MESSAGE);
                    otpField.setText("");
                } else {
                    countdown.stop();
                    JOptionPane.showMessageDialog(dialog,
                        "Too many failed attempts. Access denied.",
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                    dialog.dispose();
                }
            }
        });

        // Allow Enter key to trigger verify
        otpField.addActionListener(e -> verifyBtn.doClick());

        // Cancel closes dialog without login
        dialog.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                countdown.stop();
                dialog.dispose();
            }
        });

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220, 230, 240));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        main.add(topBar);
        main.add(Box.createVerticalStrut(22));
        main.add(iconLbl);
        main.add(Box.createVerticalStrut(10));
        main.add(titleLbl);
        main.add(Box.createVerticalStrut(14));
        main.add(infoLbl);
        main.add(Box.createVerticalStrut(16));
        main.add(sep);
        main.add(Box.createVerticalStrut(14));
        main.add(enterLbl);
        main.add(Box.createVerticalStrut(10));
        main.add(otpField);
        main.add(Box.createVerticalStrut(12));
        main.add(timerLbl);
        main.add(Box.createVerticalStrut(6));
        main.add(attemptsLbl);
        main.add(Box.createVerticalStrut(20));
        main.add(verifyBtn);
        main.add(Box.createVerticalStrut(12));
        main.add(resendBtn);

        dialog.setContentPane(main);
        dialog.setVisible(true);

        // After dialog closes, check if verified
        if (verified[0]) {
            new MainDashboard(user).setVisible(true);
            dispose();
        }
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void simulateSendOtp(String email, String otp) {
        // ── Change this to lecturer's email before demo ──────────────────
        final String RECEIVER_EMAIL = email; // uses logged-in user's email
        // To override with lecturer's email, replace above line with:
        // final String RECEIVER_EMAIL = "lecturer@gmail.com";

        final String SENDER_EMAIL   = "mbabazisandrine2003@gmail.com";
        final String SENDER_PASSWORD = "vhoq vsup ugdy ihzb";

        System.out.println("[OTP] Sending OTP " + otp + " to " + RECEIVER_EMAIL);

        new Thread(() -> {
            try {
                java.util.Properties props = new java.util.Properties();
                props.put("mail.smtp.auth",            "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host",            "smtp.gmail.com");
                props.put("mail.smtp.port",            "587");
                props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

                javax.mail.Session session = javax.mail.Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                            return new javax.mail.PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                        }
                    });

                javax.mail.Message message = new javax.mail.internet.MimeMessage(session);
                message.setFrom(new javax.mail.internet.InternetAddress(SENDER_EMAIL, "PharmaCare System"));
                message.setRecipients(javax.mail.Message.RecipientType.TO,
                    javax.mail.internet.InternetAddress.parse(RECEIVER_EMAIL));
                message.setSubject("PharmaCare - Your Login OTP");
                message.setText(
                    "Dear User,\n\n" +
                    "Your PharmaCare login OTP is:\n\n" +
                    "        " + otp + "\n\n" +
                    "This OTP is valid for 60 seconds.\n" +
                    "Do not share it with anyone.\n\n" +
                    "Regards,\n" +
                    "PharmaCare Drug Dispensing System"
                );

                javax.mail.Transport.send(message);
                System.out.println("[OTP] Email sent successfully to " + RECEIVER_EMAIL);

            } catch (Exception ex) {
                System.err.println("[OTP] Failed to send email: " + ex.getMessage());
            }
        }).start();
    }

    private void showStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        java.awt.EventQueue.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
