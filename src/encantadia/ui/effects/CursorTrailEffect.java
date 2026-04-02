package encantadia.ui.effects;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.LinkedList;
import java.util.Random;

public class CursorTrailEffect extends JComponent {
    private long lastMoveTime = System.currentTimeMillis();
    private static final int IDLE_DELAY = 120; // ms before "idle mode"
    private Point lastMousePoint = new Point(0, 0);
    private static class Particle {
        float x, y;
        float dx, dy;
        float life;
        float size;
        Color color;

        Particle(float x, float y, Color color) {
            Random r = new Random();
            this.x = x;
            this.y = y;
            this.dx = (r.nextFloat() - 0.5f) * 1.5f;
            this.dy = (r.nextFloat() * 1.5f) + 0.5f; // falling effect
            this.life = 1.0f;
            this.size = 4 + r.nextFloat() * 6;
            this.color = color;
        }

        void update() {
            x += dx;
            y += dy;
            life -= 0.04f;
        }

        boolean isAlive() {
            return life > 0;
        }
    }

    private final LinkedList<Particle> particles = new LinkedList<>();
    private final Random rand = new Random();

    private final Color[] palette = {
            new Color(0x99, 0x66, 0xCC),
            new Color(0x88, 0x4D, 0xC4),
            new Color(0x77, 0x3C, 0xB3),
            new Color(0x66, 0x33, 0x99)
    };

    public CursorTrailEffect() {
        setOpaque(false);

        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (e instanceof java.awt.event.MouseEvent me) {
                if (me.getID() == java.awt.event.MouseEvent.MOUSE_MOVED ||
                        me.getID() == java.awt.event.MouseEvent.MOUSE_DRAGGED) {

                    Point p = SwingUtilities.convertPoint(
                            me.getComponent(),
                            me.getPoint(),
                            this
                    );

                    lastMousePoint = p;

                    // ✨ spawn multiple particles per move
                    lastMoveTime = System.currentTimeMillis();

                    for (int i = 0; i < 3; i++) {
                        Color c = palette[rand.nextInt(palette.length)];
                        particles.add(new Particle(p.x, p.y, c));
                    }
                }
            }
        }, AWTEvent.MOUSE_MOTION_EVENT_MASK);

        Timer timer = new Timer(30, e -> {

            long now = System.currentTimeMillis();
            boolean idle = (now - lastMoveTime) > IDLE_DELAY;

            // 🌌 IDLE MODE → spawn falling pixie dust from top
            if (idle) {
                for (int i = 0; i < 2; i++) {

                    float x = lastMousePoint.x + (rand.nextFloat() - 0.5f) * 10;
                    float y = lastMousePoint.y + (rand.nextFloat() - 0.5f) * 5;

                    Color c = palette[rand.nextInt(palette.length)];
                    Particle p = new Particle(x, y, c);

                    p.dx *= 0.2f;
                    p.dy = 1.5f + rand.nextFloat(); // falling

                    particles.add(p);
                }
            }

            // Update all particles
            particles.removeIf(p -> {
                p.update();
                return !p.isAlive();
            });

            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        for (Particle p : particles) {

            float alpha = Math.max(0, p.life);

            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, alpha));

            // 🌟 Glow
            g2.setColor(new Color(
                    p.color.getRed(),
                    p.color.getGreen(),
                    p.color.getBlue(),
                    (int)(alpha * 80)
            ));
            g2.fillOval((int)(p.x - p.size), (int)(p.y - p.size),
                    (int)p.size * 2, (int)p.size * 2);

            // ✨ Star shape
            g2.setColor(p.color);
            drawStar(g2, p.x, p.y, p.size / 2);
        }

        g2.dispose();
    }

    // ⭐ Draw a simple 5-point star
    private void drawStar(Graphics2D g2, float x, float y, float size) {
        GeneralPath star = new GeneralPath();

        int points = 5;
        double angle = Math.PI / points;

        for (int i = 0; i < 2 * points; i++) {
            double r = (i % 2 == 0) ? size : size / 2;
            double theta = i * angle;

            double px = x + Math.cos(theta) * r;
            double py = y + Math.sin(theta) * r;

            if (i == 0) star.moveTo(px, py);
            else star.lineTo(px, py);
        }

        star.closePath();
        g2.fill(star);
    }
}