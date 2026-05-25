package view;

import javax.jms.*;
import javax.swing.*;
import javax.swing.border.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import java.awt.*;

public class NotificationListener {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "pharmacy.notifications";

    private Connection connection;
    private Session session;

    public void start(JFrame parentFrame) {
        new Thread(() -> {
            try {
                ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
                connection = factory.createConnection();
                connection.start();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createQueue(QUEUE_NAME);
                MessageConsumer consumer = session.createConsumer(destination);

                System.out.println("[ActiveMQ] Notification listener started.");

                consumer.setMessageListener(message -> {
                    if (message instanceof TextMessage) {
                        try {
                            String text = ((TextMessage) message).getText();
                            SwingUtilities.invokeLater(() -> showNotification(parentFrame, text));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

            } catch (Exception ex) {
                System.err.println("[ActiveMQ] Listener failed: " + ex.getMessage());
            }
        }).start();
    }

    private void showNotification(JFrame parent, String message) {
        // Create a non-blocking toast notification in bottom-right corner
        JWindow toast = new JWindow(parent);

        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(30, 40, 55));
        panel.setBorder(new CompoundBorder(
            new LineBorder(new Color(0, 102, 153), 2, true),
            new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel iconLbl = new JLabel("\uD83D\uDD14");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        iconLbl.setForeground(Color.WHITE);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("PharmaCare Notification");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(0, 180, 255));

        JLabel msgLbl = new JLabel("<html><div style='width:260px'>" + message + "</div></html>");
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        msgLbl.setForeground(Color.WHITE);

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(msgLbl);

        panel.add(iconLbl, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);

        toast.setContentPane(panel);
        toast.pack();

        // Position at bottom-right of screen
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        toast.setLocation(screen.width - toast.getWidth() - 20,
                          screen.height - toast.getHeight() - 60);

        toast.setOpacity(0.95f);
        toast.setVisible(true);

        // Auto-close after 5 seconds
        new Timer(5000, e -> toast.dispose()).start();
    }

    public void stop() {
        try {
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
