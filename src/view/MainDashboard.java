package view;

import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import model.User;
import service.*;

public class MainDashboard extends JFrame {

    private static final Color PRIMARY   = new Color(0, 102, 153);
    private static final Color SIDEBAR   = new Color(18, 30, 50);
    private static final Color SIDEBAR_H = new Color(0, 102, 153);
    private static final Color BG        = new Color(240, 245, 250);
    private static final Color WHITE     = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 30, 30);
    private static final Color TEXT_GRAY = new Color(120, 120, 120);

    private final User currentUser;
    private JPanel contentPanel;
    private JLabel pageTitle;
    private final NotificationListener notificationListener = new NotificationListener();

    public MainDashboard(User user) {
        this.currentUser = user;
        initComponents();
        loadDashboardStats();
        notificationListener.start(this);
    }

    private void initComponents() {
        setTitle("PharmaCare - Drug Dispensing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(WHITE);
        topBar.setPreferredSize(new Dimension(0, 55));
        topBar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(220, 230, 240)),
            new EmptyBorder(0, 20, 0, 20)
        ));

        pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pageTitle.setForeground(TEXT_DARK);

        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userInfo.setOpaque(false);

        JLabel userIcon = new JLabel("👤 " + currentUser.getFullName() + "  |  " + currentUser.getRole());
        userIcon.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userIcon.setForeground(TEXT_GRAY);

        JButton logoutBtn = UIHelper.button("⏻ Logout", new Color(200, 40, 40));
        logoutBtn.setPreferredSize(new Dimension(90, 34));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                notificationListener.stop();
                new LoginForm().setVisible(true);
                dispose();
            }
        });

        userInfo.add(userIcon);
        userInfo.add(logoutBtn);

        topBar.add(pageTitle, BorderLayout.WEST);
        topBar.add(userInfo, BorderLayout.EAST);
        return topBar;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Logo area
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        logoPanel.setMaximumSize(new Dimension(220, 80));

        JLabel logo = new JLabel("💊 PharmaCare", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logo.setForeground(WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 40));
        sep.setMaximumSize(new Dimension(200, 1));

        logoPanel.add(logo);
        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(sep);
        sidebar.add(logoPanel);

        // Nav items
        String[][] navItems = {
            {"🏠", "Dashboard"},
            {"💊", "Drugs"},
            {"👤", "Patients"},
            {"📋", "Prescriptions"},
            {"💉", "Dispensing"},
            {"📊", "Reports"},
            {"👥", "Users"}
        };

        for (String[] item : navItems) {
            if (item[1].equals("Users") && !currentUser.getRole().equals("ADMIN")) continue;
            sidebar.add(buildNavButton(item[0], item[1]));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JButton buildNavButton(String icon, String label) {
        JButton btn = new JButton(icon + "  " + label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() || getModel().isPressed() ? SIDEBAR_H : SIDEBAR;
                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(new Color(0, 200, 255));
                    g2.fillRect(0, 0, 4, getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(new Color(180, 200, 220));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 24, 12, 12));
        btn.setMaximumSize(new Dimension(220, 48));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setForeground(WHITE); btn.repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { btn.setForeground(new Color(180, 200, 220)); btn.repaint(); }
        });
        btn.addActionListener(e -> navigate(label));
        return btn;
    }

    private void navigate(String page) {
        pageTitle.setText(page);
        contentPanel.removeAll();
        switch (page) {
            case "Dashboard":   loadDashboardStats(); break;
            case "Drugs":       contentPanel.add(new DrugModule(currentUser)); break;
            case "Patients":    contentPanel.add(new PatientModule(currentUser)); break;
            case "Prescriptions": contentPanel.add(new PrescriptionModule(currentUser)); break;
            case "Dispensing":  contentPanel.add(new DispensingModule(currentUser)); break;
            case "Reports":     contentPanel.add(new ReportsModule(currentUser)); break;
            case "Users":       contentPanel.add(new UserModule(currentUser)); break;
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void loadDashboardStats() {
        contentPanel.removeAll();
        JPanel dashboard = new JPanel(new BorderLayout(0, 20));
        dashboard.setOpaque(false);

        // Stats cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);

        // Load stats from server
        SwingWorker<int[], Void> worker = new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                Registry reg = LocateRegistry.getRegistry("127.0.0.1", 5000);
                DrugService drugSvc = (DrugService) reg.lookup("drug-service");
                PatientService patSvc = (PatientService) reg.lookup("patient-service");
                PrescriptionService preSvc = (PrescriptionService) reg.lookup("prescription-service");
                DispensingService disSvc = (DispensingService) reg.lookup("dispensing-service");

                int drugs = drugSvc.findAll().size();
                int patients = patSvc.findAll().size();
                int pending = preSvc.findPending().size();
                int dispensed = disSvc.findAll().size();
                int lowStock = drugSvc.findLowStock().size();
                int expired = drugSvc.findExpiredDrugs().size();
                return new int[]{drugs, patients, pending, dispensed, lowStock, expired};
            }

            @Override
            protected void done() {
                try {
                    int[] stats = get();
                    statsRow.add(buildStatCard("💊 Total Drugs",     String.valueOf(stats[0]), new Color(0, 102, 153)));
                    statsRow.add(buildStatCard("👤 Total Patients",  String.valueOf(stats[1]), new Color(0, 150, 100)));
                    statsRow.add(buildStatCard("📋 Pending Rx",      String.valueOf(stats[2]), new Color(200, 120, 0)));
                    statsRow.add(buildStatCard("💉 Dispensed Today", String.valueOf(stats[3]), new Color(100, 50, 180)));
                } catch (Exception ex) {
                    statsRow.add(buildStatCard("💊 Total Drugs",     "--", new Color(0, 102, 153)));
                    statsRow.add(buildStatCard("👤 Total Patients",  "--", new Color(0, 150, 100)));
                    statsRow.add(buildStatCard("📋 Pending Rx",      "--", new Color(200, 120, 0)));
                    statsRow.add(buildStatCard("💉 Dispensed Today", "--", new Color(100, 50, 180)));
                }
                statsRow.revalidate();
                statsRow.repaint();
            }
        };
        worker.execute();

        // Alert panel
        JPanel alertPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        alertPanel.setOpaque(false);
        alertPanel.add(buildAlertCard("⚠ Low Stock Alerts", "Drugs below reorder level will appear here.", new Color(255, 243, 205)));
        alertPanel.add(buildAlertCard("🗓 Expiry Alerts", "Drugs nearing expiry will appear here.", new Color(255, 220, 220)));

        // Welcome banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(PRIMARY);
        banner.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel bannerText = new JLabel("Welcome back, " + currentUser.getFullName() + "! Have a productive day. 🌟");
        bannerText.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bannerText.setForeground(WHITE);
        banner.add(bannerText);

        dashboard.add(banner, BorderLayout.NORTH);
        dashboard.add(statsRow, BorderLayout.CENTER);
        dashboard.add(alertPanel, BorderLayout.SOUTH);

        contentPanel.add(dashboard);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel buildStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 230, 240), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel topStripe = new JPanel();
        topStripe.setBackground(color);
        topStripe.setPreferredSize(new Dimension(0, 5));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(TEXT_GRAY);

        card.add(topStripe, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildAlertCard(String title, String message, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(bgColor);
        card.setBorder(new CompoundBorder(
            new LineBorder(bgColor.darker(), 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(TEXT_DARK);

        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        msgLabel.setForeground(TEXT_GRAY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(msgLabel, BorderLayout.CENTER);
        return card;
    }
}
