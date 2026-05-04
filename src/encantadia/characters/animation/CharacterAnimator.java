package encantadia.characters.animation;

import encantadia.audio.SFXManager;
import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * CharacterAnimator
 * ENHANCED: Integrated Kinetic Feedback (Knockback) & Elemental Audio Sync system.
 */
public class CharacterAnimator {

    public enum AnimState { IDLE, SKILL }

    private final String   idlePath;
    private final String[] skillPaths;
    private final int[]    skillDurationsMs;
    private String         sfxPath; // Holds the elemental audio path

    private ImageIcon currentIcon;
    private AnimState currentState = AnimState.IDLE;
    private Timer     revertTimer;
    private Timer     knockbackTimer;

    private int       knockbackOffset = 0;
    private static final int REVERT_PADDING_MS = 120;

    public CharacterAnimator(String idlePath, String[] skillPaths, int[] skillDurationsMs) {
        this.idlePath          = idlePath;
        this.skillPaths        = skillPaths;
        this.skillDurationsMs  = skillDurationsMs;
        switchIcon(idlePath);
    }

    public void toIdle() {
        cancelRevert();
        currentState = AnimState.IDLE;
        switchIcon(idlePath);

        // Exact audio termination synchronized with the return to IDLE state
        SFXManager.stopSkillSFX();
    }

    public void toSkill(int skillIndex) {
        cancelRevert();
        String path = (skillPaths != null && skillIndex < skillPaths.length) ? skillPaths[skillIndex] : null;
        if (path == null) return;

        currentState = AnimState.SKILL;
        switchIcon(path);

        // Fire audio trigger simultaneously with the GIF execution
        SFXManager.playSkillSFX(sfxPath);

        int displayMs = (skillDurationsMs != null && skillIndex < skillDurationsMs.length)
                ? skillDurationsMs[skillIndex] + REVERT_PADDING_MS : 1600;

        revertTimer = new Timer(displayMs, e -> {
            ((Timer) e.getSource()).stop();
            toIdle(); // This drops the GIF and cuts the audio simultaneously
        });
        revertTimer.setRepeats(false);
        revertTimer.start();
    }

    public void triggerHit() {
        if (knockbackTimer != null) knockbackTimer.stop();
        knockbackOffset = 30; // Max pushback distance in pixels

        knockbackTimer = new Timer(16, e -> {
            knockbackOffset = Math.max(0, knockbackOffset - 3); // Fast decay
            if (knockbackOffset <= 0) ((Timer)e.getSource()).stop();
        });
        knockbackTimer.start();
    }

    public int getKnockbackOffset() {
        return knockbackOffset;
    }

    public void draw(Graphics2D g2, int x, int y, int w, int h, Component observer) {
        if (currentIcon == null) return;
        Image img = currentIcon.getImage();
        if (img == null) return;

        int iw = img.getWidth(observer);
        int ih = img.getHeight(observer);
        if (iw <= 0 || ih <= 0) return;

        if (currentState == AnimState.SKILL) {
            double scale = (double) h / ih;
            int dw = (int) (iw * scale);
            g2.drawImage(img, x, y, x + dw, y + h, 0, 0, iw, ih, observer);
        } else {
            double scale = Math.min((double) w / iw, (double) h / ih);
            int dw = (int) (iw * scale);
            int dh = (int) (ih * scale);
            g2.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, observer);
        }
    }

    public void dispose() {
        cancelRevert();
        if (knockbackTimer != null) knockbackTimer.stop();
        if (currentIcon != null) {
            currentIcon.setImageObserver(null);
            currentIcon = null;
        }
        SFXManager.stopSkillSFX();
    }

    public AnimState getState()  { return currentState; }
    public boolean   isIdle()    { return currentState == AnimState.IDLE; }
    public boolean   isSkill()   { return currentState == AnimState.SKILL; }

    private void switchIcon(String path) {
        if (currentIcon != null) currentIcon.setImageObserver(null);
        if (path == null) { currentIcon = null; return; }
        URL url = getClass().getResource(path);
        if (url == null) { currentIcon = null; return; }
        currentIcon = new ImageIcon(url);
    }

    private void cancelRevert() {
        if (revertTimer != null) { revertTimer.stop(); revertTimer = null; }
    }

    /**
     * Factory method handling Elemental Audio Asset Mapping and Memory Caching
     */
    public static CharacterAnimator forCharacter(encantadia.characters.Character ch) {
        if (ch == null) return null;

        String   idle      = ch.getIdleAnimationPath();
        String[] skills    = ch.getSkillAnimationPaths();
        int[]    durations = ch.getSkillAnimationDurations();
        if (idle == null && (skills == null || skills.length == 0)) return null;

        CharacterAnimator animator = new CharacterAnimator(idle, skills, durations);

        // Dynamically map elemental audio files
        switch (ch.getElement()) {
            case FIRE:  animator.sfxPath = "/resources/fireSkill.wav";  break;
            case WATER: animator.sfxPath = "/resources/waterSkill.wav"; break;
            case EARTH: animator.sfxPath = "/resources/earthSkill.wav"; break;
            case AIR:   animator.sfxPath = "/resources/airSkill.wav";   break;
            default:    animator.sfxPath = null;
        }

        // Offload preload to a background thread to prevent latency spikes during battle initialization
        SFXManager.preload(animator.sfxPath);

        return animator;
    }
}