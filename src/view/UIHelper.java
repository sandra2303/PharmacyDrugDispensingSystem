package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class UIHelper {

    public static final Color PRIMARY   = new Color(0, 102, 153);
    public static final Color SUCCESS   = new Color(22, 160, 80);
    public static final Color DANGER    = new Color(210, 45, 45);
    public static final Color WARNING   = new Color(210, 120, 0);
    public static final Color DARK      = new Color(55, 65, 81);
    public static final Color WHITE     = Color.WHITE;
    public static final Color BG        = new Color(235, 242, 250);
    public static final Color TEXT_DARK = new Color(25, 25, 25);
    public static final Color TEXT_GRAY = new Color(110, 110, 120);

    // ── Colored Button (always visible on any OS) ──────────────────────────
    public static JButton button(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed()  ? bg.darker().darker()
                        : getModel().isRollover() ? bg.darker()
                        : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setUI(new BasicButtonUI());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        return btn;
    }

    // ── Style JTable with fully visible colored header ─────────────────────
    public static void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setGridColor(new Color(220, 228, 238));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionBackground(new Color(200, 225, 250));
        table.setSelectionForeground(TEXT_DARK);
        table.setBackground(WHITE);
        table.setForeground(TEXT_DARK);

        // Force header renderer so it always shows color
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                JLabel lbl = new JLabel(value != null ? value.toString() : "") {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(PRIMARY);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        // bottom separator line
                        g2.setColor(new Color(0, 80, 130));
                        g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                        // right separator
                        g2.setColor(new Color(0, 80, 130));
                        g2.fillRect(getWidth() - 1, 0, 1, getHeight());
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                lbl.setText(value != null ? value.toString() : "");
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(WHITE);
                lbl.setOpaque(false);
                lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        // Alternate row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? WHITE : new Color(245, 249, 253));
                    c.setForeground(TEXT_DARK);
                } else {
                    c.setBackground(new Color(200, 225, 250));
                    c.setForeground(TEXT_DARK);
                }
                ((JLabel) c).setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    // ── Styled text field ──────────────────────────────────────────────────
    public static JTextField styledField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 212, 224), 1, true),
            new EmptyBorder(7, 10, 7, 10)
        ));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                tf.setBorder(new CompoundBorder(new LineBorder(PRIMARY, 2, true), new EmptyBorder(6, 9, 6, 9)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                tf.setBorder(new CompoundBorder(new LineBorder(new Color(200, 212, 224), 1, true), new EmptyBorder(7, 10, 7, 10)));
            }
        });
        return tf;
    }

    public static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(70, 80, 95));
        return lbl;
    }

    public static JPanel card(Color bg) {
        JPanel p = new JPanel();
        p.setBackground(bg);
        p.setBorder(new CompoundBorder(
            new LineBorder(new Color(215, 225, 238), 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
        return p;
    }
}
