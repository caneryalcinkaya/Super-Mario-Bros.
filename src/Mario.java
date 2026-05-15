// CanerYalcinkaya
// 2024400273

import java.awt.event.KeyEvent;

public class Mario {

    public static final int SPAWN_X = 60;
    public static final int SPAWN_Y = 770;
    private static final int SIZE   = 40;

    public int    x, y;
    public double speedY  = 0;
    public double gravity = 0.5;

    public boolean isEnteringPipe = false;
    public boolean isCrouching    = false;
    public boolean isSpawning     = false; // locks input until mario lands after respawn

    private int     deathCount    = 0;
    public  boolean isDead        = false;
    private boolean isFacingRight = true;
    private boolean wWasPressed   = false;

    public double yExact;
    private double deathY;
    private double deathVY;

    public Mario(int x, int y) {
        this.x      = x;
        this.y      = y;
        this.yExact = y;
    }

    public int getSize()       { return SIZE; }
    public int getDeathCount() { return deathCount; }

    public int handleInput(int frame, boolean onGround) {
        // wait until mario hits the ground before accepting input
        if (isSpawning) {
            if (onGround) {
                isSpawning = false;
            } else {
                return isFacingRight ? 8 : 7;
            }
        }

        if (isEnteringPipe) return isCrouching ? 10 : (isFacingRight ? 5 : 6);

        boolean sPressed = StdDraw.isKeyPressed(KeyEvent.VK_S);
        boolean wNow = StdDraw.isKeyPressed(KeyEvent.VK_W);
        if (wNow && !wWasPressed && onGround) jump();
        wWasPressed = wNow;

        boolean left  = StdDraw.isKeyPressed(KeyEvent.VK_A);
        boolean right = StdDraw.isKeyPressed(KeyEvent.VK_D);

        // crouching
        if (onGround && sPressed) {
            if (left)  isFacingRight = false;
            if (right) isFacingRight = true;
            return 10;
        }

        // airborne movement
        if (!onGround) {
            if (left)  { x -= 3; isFacingRight = false; }
            if (right) { x += 3; isFacingRight = true;  }
            return isFacingRight ? 7 : 8;
        }

        // walking left/right, alternate sprites every 8 frames
        if (left) {
            isFacingRight = false;
            x -= 3;
            return (frame % 16 < 8) ? 1 : 2;
        }
        if (right) {
            isFacingRight = true;
            x += 3;
            return (frame % 16 < 8) ? 3 : 4;
        }

        return isFacingRight ? 5 : 6;
    }

    public void jump() {
        speedY = 12;
    }

    public boolean checkEnemyCollision(Enemy enemy) {
        int hs  = SIZE / 2;
        int ehs = enemy.getSize() / 2;

        if (Math.abs(x - enemy.getX()) < hs + ehs && Math.abs(y - enemy.getY()) < hs + ehs) {
            return true;
        }

        // also check bullets for type 1 enemies
        if (enemy.getType() == 1) {
            for (Enemy.Bullet b : enemy.getBullets()) {
                if (Math.abs(x - b.x) < hs + 12 && Math.abs(y - b.y) < hs + 12) {
                    return true;
                }
            }
        }
        return false;
    }

    public void die() {
        if (isDead) return;
        isDead         = true;
        isEnteringPipe = false;
        isCrouching    = false;
        isSpawning     = false;
        deathCount++;
        deathY  = y;
        deathVY = 9;
        speedY  = 0;
    }

    // returns true when mario has fully fallen off screen
    public boolean updateDeath() {
        deathVY -= gravity;
        deathY  += deathVY;
        y        = (int) deathY;
        return y < -(SIZE * 3);
    }

    public void respawn() {
        x              = SPAWN_X;
        y              = SPAWN_Y;
        yExact         = SPAWN_Y;
        speedY         = -10; // push down so he drops out of the start pipe
        deathVY        = 0;
        isDead         = false;
        isEnteringPipe = false;
        isCrouching    = false;
        isSpawning     = true;
    }

    public void draw(int sprite) {
        String img;
        switch (sprite) {
            case 1:  img = "assets/walkLeft1.png";  break;
            case 2:  img = "assets/walkLeft2.png";  break;
            case 3:  img = "assets/walkRight1.png"; break;
            case 4:  img = "assets/walkRight2.png"; break;
            case 6:  img = "assets/standLeft.png";  break;
            case 7:  img = "assets/jumpRight.png";  break;
            case 8:  img = "assets/jumpLeft.png";   break;
            case 9:  img = "assets/dead.png";       break;
            case 10: img = "assets/crouch.png";     break;
            default: img = "assets/standRight.png"; break;
        }
        try {
            StdDraw.picture(x, y, img, SIZE, SIZE);
        } catch (IllegalArgumentException e) {
            try { StdDraw.picture(x, y, "assets/standRight.png", SIZE, SIZE); }
            catch (IllegalArgumentException ignored) { }
        }
    }
}