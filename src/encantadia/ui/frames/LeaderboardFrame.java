package encantadia.ui.frames;

import encantadia.ScreenManager;
import encantadia.battle.arcade.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.List;
import java.util.Vector;

public class LeaderboardFrame extends JFrame {

    // --- Asset Paths ---
    private static final String BG_PATH         = "/resources/background (4).png";
    private static final String TITLE_PATH      = "/resources/hallOfFame.png";
    private static final String FRAME_PATH      = "/resources/frame.png";
    private static final String TILE_PATH       = "/resources/pad.png";
    private static final String RETURN_BTN_PATH = "/resources/return.png";

    private static final String[] HEADER_PATHS = {
            "/resources/rank.png",
            "/resources/tag.png",
            "/resources/time.png",
            "/resources/dmgDealt.png",
            "/resources/dmgTaken.png",
            "/resources/status.png"
    };

    private static final Color TEXT_LIGHT      = new Color(0xFF, 0xF5, 0xDC);
    private static final Color BG_DARK_OVERLAY = new Color(0x18, 0x14, 0x0E, 230);
    private static final Color TRANSPARENT     = new Color(0, 0, 0, 0);

    // Sort button colours (wood-sign palette)
    private static final Color BTN_NORMAL_BG = new Color(0x6B, 0x43, 0x20);
    private static final Color BTN_NORMAL_FG = new Color(0xFF, 0xF0, 0xC0);
    private static final Color BTN_ACTIVE_BG = new Color(0xC8, 0xA0, 0x28);
    private static final Color BTN_ACTIVE_FG = new Color(0x18, 0x10, 0x00);
    private static final Color BTN_BORDER    = new Color(0x3B, 0x25, 0x10);

    private static final String[] SORT_OPTIONS = {"Speedrunner", "Aggressor", "Tank", "Alphabetical"};

    private JTable table;
    private DefaultTableModel tableModel;
    private String currentSort = "Speedrunner";
    private final JButton[] sortButtons = new JButton[SORT_OPTIONS.length];

    public LeaderboardFrame() {
        setTitle("Arcade Leaderboard");
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        buildUI();
        setVisible(true);
        ScreenManager.register(this);
        loadData("Speedrunner");
    }

    @Override
    public void dispose() {
        ScreenManager.unregister(this);
        super.dispose();
    }

    private void buildUI() {
        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        BgPanel bg = new BgPanel(BG_PATH);
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        lp.add(content, JLayeredPane.PALETTE_LAYER);

        // --- HEADER AREA ---
        JPanel headerPanel = new JPanel(new BorderLayout(0, 6));
        headerPanel.setOpaque(false);

        ImagePanel titleImage = new ImagePanel(TITLE_PATH);
        titleImage.setPreferredSize(new Dimension(800, 110));
        headerPanel.add(titleImage, BorderLayout.CENTER);

        // Sort buttons row
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sortPanel.setOpaque(false);

        JLabel sortLbl = new JLabel("Sort By:");
        sortLbl.setForeground(TEXT_LIGHT);
        sortLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        sortPanel.add(sortLbl);

        for (int i = 0; i < SORT_OPTIONS.length; i++) {
            final String option = SORT_OPTIONS[i];
            JButton btn = new JButton(option) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean active = option.equals(currentSort);
                    g2.setColor(active ? BTN_ACTIVE_BG : BTN_NORMAL_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.setColor(BTN_BORDER);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 6, 6);
                    g2.setColor(new Color(255, 255, 255, active ? 60 : 40));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawLine(4, 2, getWidth() - 4, 2);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                    int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.setColor(new Color(0, 0, 0, 120));
                    g2.drawString(getText(), tx + 1, ty + 1);
                    g2.setColor(active ? BTN_ACTIVE_FG : BTN_NORMAL_FG);
                    g2.drawString(getText(), tx, ty);
                    g2.dispose();
                }
            };
            btn.setFont(new Font("SansSerif", Font.BOLD, 12));
            btn.setPreferredSize(new Dimension(100, 28));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                currentSort = option;
                for (JButton b : sortButtons) b.repaint();
                loadData(option);
            });
            sortButtons[i] = btn;
            sortPanel.add(btn);
        }

        headerPanel.add(sortPanel, BorderLayout.SOUTH);
        content.add(headerPanel, BorderLayout.NORTH);

        // --- TABLE AREA ---
        Vector<String> columns = new Vector<>(List.of("Rank", "Tag", "Time", "Dmg Dealt", "Dmg Taken", "Status"));
        tableModel = new DefaultTableModel(new Vector<>(), columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setOpaque(false);
        table.setBackground(TRANSPARENT);
        table.setForeground(TEXT_LIGHT);
        table.setFont(new Font("Monospaced", Font.BOLD, 18));
        table.setRowHeight(50);
        table.setShowGrid(false);
        table.setFillsViewportHeight(false);

        // KEY FIX: remove the JTableHeader from the table entirely.
        // This kills the column-header viewport inside JScrollPane — the source
        // of the white strip — and lets us draw a fully static header panel instead.
        table.setTableHeader(null);

        // Transparent centred cell renderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                setBackground(TRANSPARENT);
                setForeground(TEXT_LIGHT);
                setOpaque(false);
                return this;
            }
        };
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Fully transparent scroll pane (no header viewport since header is null)
        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.setBackground(TRANSPARENT);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(TRANSPARENT);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // Static pixel-art header row — sits above the scroll pane, never moves
        StaticHeaderPanel staticHeader = new StaticHeaderPanel(HEADER_PATHS);
        staticHeader.setPreferredSize(new Dimension(0, 48));

        // Inner panel: static header on top, scrollable rows below
        JPanel tableInner = new JPanel(new BorderLayout());
        tableInner.setOpaque(false);
        tableInner.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        tableInner.add(staticHeader, BorderLayout.NORTH);
        tableInner.add(scroll, BorderLayout.CENTER);

        // Outer panel that draws the stone tiles + vine frame
        TableBackgroundPanel tableArea = new TableBackgroundPanel(FRAME_PATH, TILE_PATH);
        tableArea.setLayout(new BorderLayout());
        tableArea.add(tableInner, BorderLayout.CENTER);
        content.add(tableArea, BorderLayout.CENTER);

        // --- FOOTER ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        ImageButton exitBtn = new ImageButton(RETURN_BTN_PATH);
        exitBtn.setPreferredSize(new Dimension(240, 70));
        exitBtn.addActionListener(e -> { dispose(); new MainMenuFrame(); });
        footer.add(exitBtn);
        content.add(footer, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                bg.setBounds(0, 0, lp.getWidth(), lp.getHeight());
                content.setBounds(0, 0, lp.getWidth(), lp.getHeight());
            }
        });
        SwingUtilities.invokeLater(() -> {
            bg.setBounds(0, 0, lp.getWidth(), lp.getHeight());
            content.setBounds(0, 0, lp.getWidth(), lp.getHeight());
        });
    }

    private void loadData(String sortType) {
        Vector<Vector<Object>> data = DatabaseManager.getInstance().getLeaderboardData(sortType);
        tableModel.setRowCount(0);
        for (Vector<Object> row : data) tableModel.addRow(row);
    }

    // ──────────────────────────────────────────────────────────────────────
    // CUSTOM UI COMPONENTS
    // ──────────────────────────────────────────────────────────────────────

    /**
     * A plain JPanel that draws the six pixel-art column header images side by side.
     * It is completely static — no JTableHeader, no viewport, no white bleed.
     * Column widths are kept in sync with the JTable's column model via a
     * ComponentListener so they always line up perfectly.
     */
    private class StaticHeaderPanel extends JPanel {
        private final Image[] imgs;

        StaticHeaderPanel(String[] paths) {
            setOpaque(false);
            setBackground(TRANSPARENT);
            imgs = new Image[paths.length];
            for (int i = 0; i < paths.length; i++) {
                URL url = getClass().getResource(paths[i]);
                if (url != null) imgs[i] = new ImageIcon(url).getImage();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (table == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            int x = 0;
            for (int col = 0; col < table.getColumnCount() && col < imgs.length; col++) {
                int colW = table.getColumnModel().getColumn(col).getWidth();
                Image img = imgs[col];
                if (img != null) {
                    int iw = img.getWidth(null), ih = img.getHeight(null);
                    double scale = (double) getHeight() / ih;
                    int dw = (int) (iw * scale), dh = getHeight();
                    int dx = x + (colW - dw) / 2;
                    g2.drawImage(img, dx + 2, 1, dw - 4, dh - 2, null);
                }
                x += colW;
            }
            g2.dispose();
        }
    }

    private class TableBackgroundPanel extends JPanel {
        private final Image frameImg, tileImg;

        TableBackgroundPanel(String fPath, String tPath) {
            setOpaque(false);
            URL fUrl = getClass().getResource(fPath);
            URL tUrl = getClass().getResource(tPath);
            frameImg = (fUrl != null) ? new ImageIcon(fUrl).getImage() : null;
            tileImg  = (tUrl != null) ? new ImageIcon(tUrl).getImage() : null;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            if (tileImg != null) {
                g2.drawImage(tileImg, 20, 20, getWidth() - 40, getHeight() - 40, null);
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRect(20, 20, getWidth() - 40, getHeight() - 40);
            }
            if (frameImg != null) {
                g2.drawImage(frameImg, 0, 0, getWidth(), getHeight(), null);
            }
            g2.dispose();
        }
    }

    private class BgPanel extends JPanel {
        private final Image img;
        BgPanel(String path) {
            URL url = getClass().getResource(path);
            img = (url != null) ? new ImageIcon(url).getImage() : null;
            setOpaque(true);
            setBackground(Color.BLACK);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                int iw = img.getWidth(null), ih = img.getHeight(null);
                double scale = Math.max((double) getWidth() / iw, (double) getHeight() / ih);
                int dw = (int) (iw * scale), dh = (int) (ih * scale);
                g2.drawImage(img, (getWidth() - dw) / 2, (getHeight() - dh) / 2, dw, dh, null);
                g2.setColor(BG_DARK_OVERLAY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }

    private class ImagePanel extends JPanel {
        private final Image img;
        ImagePanel(String path) {
            URL url = getClass().getResource(path);
            img = (url != null) ? new ImageIcon(url).getImage() : null;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                int iw = img.getWidth(null), ih = img.getHeight(null);
                double scale = Math.min((double) getWidth() / iw, (double) getHeight() / ih);
                int dw = (int) (iw * scale), dh = (int) (ih * scale);
                g2.drawImage(img, (getWidth() - dw) / 2, (getHeight() - dh) / 2, dw, dh, null);
                g2.dispose();
            }
        }
    }

    private class ImageButton extends JButton {
        private final Image img;
        ImageButton(String path) {
            URL url = getClass().getResource(path);
            img = (url != null) ? new ImageIcon(url).getImage() : null;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                int iw = img.getWidth(null), ih = img.getHeight(null);
                double scale = Math.min((double) getWidth() / iw, (double) getHeight() / ih);
                int dw = (int) (iw * scale), dh = (int) (ih * scale);
                if (getModel().isRollover()) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                }
                g2.drawImage(img, (getWidth() - dw) / 2, (getHeight() - dh) / 2, dw, dh, null);
                g2.dispose();
            }
        }
    }
}