import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Game {

    private static final Color SKY      = new Color(146, 144, 255);
    private static final Color MENU_DK  = new Color(100,  50,   0);
    private static final Color MENU_LT  = new Color(153,  78,   0);
    private static final Color MENU_TXT = new Color(255, 204, 197);
    private static final Color HUD_BG   = new Color( 18,  18,  70);
    private static final Color WHITE    = Color.WHITE;
    private static final Color GOLD     = new Color(255, 183,   0);
    private static final Color RED_TXT  = new Color(255,  80,  80);
    private static final Color END_BG   = new Color( 18,  18,  80);

    private final ArrayList<Level> levels;
    private final Map              map;

    private int     pauseDuration = 25;
    private boolean cameraMode   = false;
    private boolean cWasPressed  = false;

    public Game(ArrayList<Level> levels, Map map) {
        this.levels = levels;
        this.map    = map;
    }

    public void run() {
        Mario menuMario = new Mario(400, 140);
        startScreen(menuMario);

        boolean playing = true;
        while (playing) {
            playing = runGame();
        }
        System.exit(0);
    }

    // start screen with fps calibration
    private void startScreen(Mario mario) {
        mario.x      = 400;
        mario.y      = 140;
        mario.yExact = 140;
        mario.speedY = 0;
        final int GROUND_Y = 120 + mario.getSize() / 2;
        int frame = 0;

        while (true) {
            StdDraw.clear(SKY);

            StdDraw.setPenColor(MENU_DK);
            StdDraw.filledRectangle(400, 550, 254, 104);
            StdDraw.setPenColor(MENU_LT);
            StdDraw.filledRectangle(400, 550, 250, 100);
            StdDraw.setPenColor(MENU_TXT);
            StdDraw.setFont(new Font("Arial", Font.BOLD, 46));
            StdDraw.text(400, 582, "SUPER");
            StdDraw.text(400, 522, "MARIO BROS.");

            int fps = pauseDuration > 0 ? 1000 / pauseDuration : 999;
            StdDraw.setPenColor(WHITE);
            StdDraw.setFont(new Font("Arial", Font.PLAIN, 22));
            StdDraw.text(400, 425, "Press [Space] to start");
            StdDraw.text(400, 398, "Move: [A] [D] [W]");
            StdDraw.text(400, 371, "FPS: ~" + fps + "   Adjust: [←] [→]");

            if (StdDraw.isKeyPressed(KeyEvent.VK_LEFT)  && pauseDuration < 100) pauseDuration++;
            if (StdDraw.isKeyPressed(KeyEvent.VK_RIGHT) && pauseDuration > 10)  pauseDuration--;

            for (int row = 0; row < 3; row++)
                for (int i = 0; i < 20; i++)
                    StdDraw.picture(20 + i * 40, 20 + row * 40, "assets/block.png", 40, 40);

            applyGravity(mario);
            if (mario.y <= GROUND_Y) {
                mario.y = GROUND_Y; mario.yExact = GROUND_Y; mario.speedY = 0;
            }
            int sprite = mario.handleInput(frame, mario.y <= GROUND_Y);

            int hs = mario.getSize() / 2;
            if (mario.x < hs)       mario.x = hs;
            if (mario.x > 800 - hs) mario.x = 800 - hs;

            mario.draw(sprite);

            if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
                while (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) StdDraw.pause(10);
                return;
            }

            StdDraw.show();
            StdDraw.pause(pauseDuration);
            frame++;
        }
    }

    // main game loop, returns true if we should restart
    private boolean runGame() {
        Mario mario = new Mario(Mario.SPAWN_X, Mario.SPAWN_Y);
        mario.respawn();
        map.resetPortalState();

        int   frame        = 0;
        int   currentIdx   = 0;
        Level currentLevel = levels.get(currentIdx);

        long gameStartMs = System.currentTimeMillis();
        long bannersMs   = 0;

        final int SPAWN_GUARD = 90; // ignore exit detection right after spawning
        int spawnFrames = 0;

        boolean rWasPressed       = false;
        boolean wWasPressedPortal = false;
        boolean sWasPressedPortal = false;

        while (true) {
            boolean rNow = StdDraw.isKeyPressed(KeyEvent.VK_R);
            if (rNow && !rWasPressed) return true;
            rWasPressed = rNow;

            boolean cNow = StdDraw.isKeyPressed(KeyEvent.VK_C);
            if (cNow && !cWasPressed) cameraMode = !cameraMode;
            cWasPressed = cNow;

            // edge detection for portal keys
            boolean wNow  = StdDraw.isKeyPressed(KeyEvent.VK_W);
            boolean sNow  = StdDraw.isKeyPressed(KeyEvent.VK_S);
            boolean wEdge = wNow && !wWasPressedPortal;
            boolean sEdge = sNow && !sWasPressedPortal;
            wWasPressedPortal = wNow;
            sWasPressedPortal = sNow;

            spawnFrames++;
            String timeStr = getTime(gameStartMs + bannersMs);
            int sprite;

            if (mario.isDead) {
                sprite = 9;
                boolean done = mario.updateDeath();
                if (done) {
                    mario.respawn();
                    map.resetPortalState();
                    spawnFrames = 0;
                }
            } else {
                if (mario.isEnteringPipe) {
                    map.handleTeleport(mario, wEdge, sEdge);
                    sprite = mario.isCrouching ? 10 : 5;
                } else {
                    applyGravity(mario);

                    if (map.isOnCeiling(mario.x, mario.y, mario.getSize() / 2, mario.speedY)) {
                        mario.y      = map.snapToCeiling(mario.x, mario.y, mario.getSize() / 2);
                        mario.yExact = mario.y;
                        mario.speedY = 0;
                    }

                    boolean onGround = map.isOnGround(mario.x, mario.y, mario.getSize() / 2, mario.speedY);
                    if (onGround) {
                        mario.y      = map.snapToGround(mario.x, mario.y, mario.getSize() / 2);
                        mario.yExact = mario.y;
                        mario.speedY = 0;
                    }

                    int prevX = mario.x;
                    sprite    = mario.handleInput(frame, onGround);
                    mario.x   = map.resolveXCollision(mario.x, prevX, mario.y, mario.getSize() / 2);

                    // clamp to screen bounds
                    int hs = mario.getSize() / 2;
                    if (mario.x < hs)       mario.x = hs;
                    if (mario.x > 800 - hs) mario.x = 800 - hs;
                    if (mario.y > 800 - hs) {
                        mario.y = 800 - hs; mario.yExact = mario.y;
                        if (mario.speedY > 0) mario.speedY = 0;
                    }

                    map.handleTeleport(mario, wEdge, sEdge);
                    map.collectCoin(mario, currentLevel.getCoins());

                    for (Enemy e : currentLevel.getEnemies()) {
                        if (mario.checkEnemyCollision(e)) {
                            mario.die();
                            break;
                        }
                    }

                    if (spawnFrames > SPAWN_GUARD && map.isAtExit(mario)) {
                        boolean taskDone = true;
                        if (currentLevel.getId() == 3)
                            taskDone = map.allCoinsCollected(currentLevel.getCoins());

                        if (taskDone) {
                            long t0 = System.currentTimeMillis();

                            if (currentIdx < levels.size() - 1) {
                                levelBanner(currentLevel.getId());
                            }

                            bannersMs += (System.currentTimeMillis() - t0);
                            currentIdx++;

                            if (currentIdx >= levels.size()) {
                                String finalTime = getTime(gameStartMs + bannersMs);
                                int result = endScreen(finalTime, mario.getDeathCount());
                                return (result == 1);
                            }

                            currentLevel = levels.get(currentIdx);
                            mario.respawn();
                            map.resetPortalState();
                            currentLevel.resetEnemies();
                            spawnFrames = 0;
                            frame = 0;
                            continue;
                        }
                    }
                }
                currentLevel.update();
            }

            // draw everything
            StdDraw.clear(SKY);

            if (cameraMode) applyCameraFollow(mario);
            else            resetCamera();

            mario.draw(sprite);
            map.draw();
            currentLevel.draw();

            resetCamera(); // always reset before drawing hud so it stays fixed
            drawHUD(currentLevel, timeStr, mario.getDeathCount());

            StdDraw.show();
            StdDraw.pause(pauseDuration);
            frame++;
        }
    }

    private void applyGravity(Mario mario) {
        mario.speedY -= mario.gravity;
        mario.yExact += mario.speedY;
        mario.y       = (int) Math.round(mario.yExact);
    }

    private String getTime(long startMs) {
        long elapsed = System.currentTimeMillis() - startMs;
        if (elapsed < 0) elapsed = 0;
        long cs  = (elapsed % 1000) / 10;
        long sec = (elapsed / 1000) % 60;
        long min = elapsed / 60000;
        return String.format("%02d:%02d:%02d", min, sec, cs);
    }

    // level complete banner shown between levels
    private void levelBanner(int id) {
        StdDraw.setPenColor(HUD_BG);
        StdDraw.filledRectangle(400, 400, 400, 80);

        StdDraw.setPenColor(GOLD);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 42));
        StdDraw.text(400, 435, "Level " + id + " Complete!");

        StdDraw.setPenColor(WHITE);
        StdDraw.setFont(new Font("Arial", Font.PLAIN, 22));
        StdDraw.text(400, 382, "Press [Space] to Continue");

        StdDraw.show();

        while (true) {
            if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
                while (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) StdDraw.pause(10);
                return;
            }
            StdDraw.pause(16);
        }
    }

    // end screen, returns 0 for exit, 1 for restart
    private int endScreen(String elapsedTime, int deathCount) {
        final String[] OPTS = {"Exit", "Restart"};
        int sel = 1;
        boolean upWas = false, dnWas = false;

        while (true) {
            StdDraw.clear(END_BG);
            StdDraw.setPenColor(GOLD);
            StdDraw.setFont(new Font("Arial", Font.BOLD, 72));
            StdDraw.text(400, 570, "You Won!");
            StdDraw.setFont(new Font("Arial", Font.BOLD, 30));
            for (int i = 0; i < OPTS.length; i++) {
                boolean active = (i == sel);
                StdDraw.setPenColor(active ? GOLD : WHITE);
                StdDraw.text(400, 420 - i * 70,
                        (active ? "> " : "  ") + OPTS[i] + (active ? " <" : "  "));
            }
            StdDraw.setFont(new Font("Arial", Font.PLAIN, 22));
            StdDraw.setPenColor(WHITE);
            StdDraw.text(400, 230, "Time: "   + elapsedTime);
            StdDraw.text(400, 198, "Deaths: " + deathCount);

            boolean upNow = StdDraw.isKeyPressed(KeyEvent.VK_UP);
            boolean dnNow = StdDraw.isKeyPressed(KeyEvent.VK_DOWN);
            if (upNow && !upWas) sel = (sel - 1 + OPTS.length) % OPTS.length;
            if (dnNow && !dnWas) sel = (sel + 1) % OPTS.length;
            upWas = upNow; dnWas = dnNow;

            if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
                while (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) StdDraw.pause(10);
                return sel;
            }
            StdDraw.show();
            StdDraw.pause(16);
        }
    }

    private void drawHUD(Level level, String time, int deaths) {
        StdDraw.setPenColor(HUD_BG);
        StdDraw.filledRectangle(400, 60, 400, 60);

        Font f = new Font("Arial", Font.BOLD, 15);
        StdDraw.setFont(f);

        StdDraw.setPenColor(WHITE);
        StdDraw.textLeft(20, 95, "Move: [A] [D] [W]");
        StdDraw.textLeft(20, 60, "Restart: [R]");
        StdDraw.textLeft(20, 25, "Camera: [C]");

        StdDraw.setPenColor(GOLD);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 16));
        StdDraw.text(400, 60, level.getClue());

        StdDraw.setFont(f);
        StdDraw.setPenColor(WHITE);
        StdDraw.textRight(780, 95, "Level " + level.getId());
        StdDraw.textRight(780, 60, time);

        StdDraw.setPenColor(GOLD);
        StdDraw.textRight(780, 25, "Deaths: " + deaths);
    }

    // follow camera, viewport is 400x400 centered on mario
    private void applyCameraFollow(Mario mario) {
        final int HW = 200, HH = 200;
        final int MW = 800, MH = 800;
        int cx = Math.max(HW, Math.min(MW - HW, mario.x));
        int cy = Math.max(HH, Math.min(MH - HH, mario.y));
        StdDraw.setXscale(cx - HW, cx + HW);
        StdDraw.setYscale(cy - HH, cy + HH);
    }

    private void resetCamera() {
        StdDraw.setXscale(0, 800);
        StdDraw.setYscale(0, 800);
    }
}
