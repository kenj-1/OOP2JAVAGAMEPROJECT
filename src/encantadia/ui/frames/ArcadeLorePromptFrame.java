package encantadia.ui.frames;

import encantadia.BackstoryShowcase;
import encantadia.ScreenManager;
import encantadia.gamemode.GameModeType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

/**
 * Styled prompt that appears when Arcade Mode is selected from the main menu.
 * Ensures aspect ratio preservation of backgrounds and interactive panels.
 */
public class ArcadeLorePromptFrame extends JFrame {

    private static final String BG_PATH      = "/resources/background.png";
    private static final String COLUMNS_PATH = "/resources/columns.png";
    private static final String HOLDER_PATH  = "/resources/mainMenuHolder.png";

    private static final String[] ARCADE_LORE = {
            "<p>The ancient arenas of Encantadia were built long before the Sang'gres — monuments to the belief that strength proved without witness means nothing.</p>",
            "<p>Now they serve a new purpose.</p>",
            "<p>Warriors who seek to understand every edge, every weakness, every rhythm of combat enter the trials — facing opponents one after another until the body gives out or the mind finds what it was looking for.</p>",
            "<p>Not glory. Readiness. Because the Gem Void will not wait for anyone to feel prepared.</p>"
    };

    public ArcadeLorePromptFrame() {
        setTitle("Encantadia: Echoes of the Gem — Arcade Mode");
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildUI();
        setVisible(true);
        ScreenManager.register(this);
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

        BackgroundPanel bg      = new BackgroundPanel(BG_PATH);
        ScaledPanel     columns = new ScaledPanel(COLUMNS_PATH);
        HolderPanel     holder  = new HolderPanel(HOLDER_PATH);

        lp.add(bg,      JLayeredPane.DEFAULT_LAYER);
        lp.add(columns, JLayeredPane.PALETTE_LAYER);
        lp.add(holder,  JLayeredPane.MODAL_LAYER);

        holder.setLayout(new GridBagLayout());
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        String html = "<html><div style='text-align:center; font-family:sans-serif; font-weight:bold; font-size:18px; color:#FFF5DC;'>"
                + "DO YOU WISH TO<br>UNVEIL THE<br>FORGOTTEN PAST?"
                + "</div></html>";
        JLabel question = new JLabel(html, SwingConstants.CENTER);
        question.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        btnRow.setOpaque(false);
        JButton yesBtn = makeHolderButton("YES");
        JButton noBtn  = makeHolderButton("NO");
        btnRow.add(yesBtn);
        btnRow.add(noBtn);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        yesBtn.addActionListener(e -> {
            dispose();
            new BackstoryShowcase(
                    ARCADE_LORE,
                    "The Ancient Arenas",
                    () -> new CharacterSelectionFrame(GameModeType.ARCADE),
                    () -> new MainMenuFrame()
            );
        });

        noBtn.addActionListener(e -> {
            dispose();
            new CharacterSelectionFrame(GameModeType.ARCADE);
        });

        inner.add(Box.createVerticalGlue());
        inner.add(question);
        inner.add(Box.createVerticalStrut(36));
        inner.add(btnRow);
        inner.add(Box.createVerticalGlue());

        holder.add(inner);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                layout(lp, bg, columns, holder);
            }
        });
        SwingUtilities.invokeLater(() -> layout(lp, bg, columns, holder));
    }

    private void layout(JLayeredPane lp, JPanel bg, JPanel col, JPanel holder) {
        int w = lp.getWidth(), h = lp.getHeight();
        if (w == 0 || h == 0) return;
        bg.setBounds(0, 0, w, h);
        col.setBounds(0, 0, w, h);

        // Improved math to stay responsive but limit maximum stretch
        int hw = Math.min(600, (int)(w * 0.55));
        int hh = Math.min(500, (int)(h * 0.70));
        holder.setBounds((w - hw) / 2, (h - hh) / 2, hw, hh);

        lp.revalidate();
        lp.repaint();
    }

    private JButton makeHolderButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean h = getModel().isRollover();
                g2.setPaint(new GradientPaint(0, 0, h ? new Color(170, 110, 40) : new Color(120, 70, 20), 0, getHeight(), h ? new Color(120, 80, 30) : new Color(80, 40, 10)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                if (h) {
                    g2.setColor(new Color(255, 215, 120, 80));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 16, 16);
                }
                g2.setColor(new Color(220, 180, 90));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 16, 16);
                g2.setFont(new Font("Serif", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.setColor(Color.BLACK); g2.drawString(getText(), tx+1, ty+1);
                g2.setColor(new Color(255, 230, 170)); g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 52));
        return btn;
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        return url != null ? new ImageIcon(url).getImage() : null;
    }

    protected void drawImageFill(Graphics2D g2, Image img, int x, int y, int w, int h) {
        if (img == null) return;
        int iw = img.getWidth(null), ih = img.getHeight(null);
        if (iw <= 0 || ih <= 0) return;
        double scale = Math.max((double) w / iw, (double) h / ih);
        int dw = (int) (iw * scale), dh = (int) (ih * scale);
        int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
        g2.drawImage(img, dx, dy, dw, dh, null);
    }

    protected void drawImageProportional(Graphics2D g2, Image img, int x, int y, int w, int h) {
        if (img == null) return;
        int iw = img.getWidth(null), ih = img.getHeight(null);
        if (iw <= 0 || ih <= 0) return;
        double scale = Math.min((double) w / iw, (double) h / ih);
        int dw = (int) (iw * scale), dh = (int) (ih * scale);
        int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
        g2.drawImage(img, dx, dy, dw, dh, null);
    }

    private class BackgroundPanel extends JPanel {
        private final Image img;
        BackgroundPanel(String p) { img = loadImage(p); setOpaque(true); setBackground(Color.BLACK); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                drawImageFill(g2, img, 0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }

    private class ScaledPanel extends JPanel {
        private final Image img;
        ScaledPanel(String p) { img = loadImage(p); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                drawImageFill(g2, img, 0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }

    private class HolderPanel extends JPanel {
        private final Image img;
        HolderPanel(String p) { img = loadImage(p); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                drawImageProportional(g2, img, 0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }
}