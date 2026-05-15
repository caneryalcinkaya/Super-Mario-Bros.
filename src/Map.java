// CanerYalcinkaya
// 2024400273

import java.awt.event.KeyEvent;

public class Map {

    private int[][] obstacles;
    private int[][] pipes;
    private int[][] portals;
    private int[][] coins;

    private int portalCooldown = 0;

    public Map(int[][] obstacles, int[][] pipes, int[][] portals, int[][] coins) {
        this.obstacles = obstacles;
        this.pipes     = pipes;
        this.portals   = portals;
        this.coins     = coins;
    }

    public void draw() {
        for (int[] o : obstacles)
            StdDraw.picture(o[0], o[1], "assets/block.png", o[2] * 2, o[3] * 2);

        StdDraw.setPenColor(StdDraw.YELLOW);
        for (int[] p : pipes)
            StdDraw.filledRectangle(p[0], p[1], p[2], p[3]);

        StdDraw.setPenColor(StdDraw.RED);
        if (portals != null)
            for (int[] p : portals)
                StdDraw.filledRectangle(p[0], p[1], p[2], p[3]);
    }

    // basic aabb overlap check
    private boolean aabb(int ax, int ay, int ahw, int ahh, int bx, int by, int bhw, int bhh) {
        return Math.abs(ax - bx) < ahw + bhw && Math.abs(ay - by) < ahh + bhh;
    }

    // pipe helpers

    // start pipe is the one with the highest y value
    private int[] getStartPipe() {
        if (pipes == null || pipes.length == 0) return null;
        int[] start = pipes[0];
        for (int[] p : pipes) if (p[1] > start[1]) start = p;
        return start;
    }

    private int[] getTopPortal() {
        if (portals == null || portals.length == 0) return null;
        int[] top = portals[0];
        for (int[] p : portals) if (p[1] > top[1]) top = p;
        return top;
    }

    private int[] getBottomPortal() {
        if (portals == null || portals.length == 0) return null;
        int[] bot = portals[0];
        for (int[] p : portals) if (p[1] < bot[1]) bot = p;
        return bot;
    }

    // mario needs to pass through the upper portal from below, so skip ceiling check there
    private boolean nearUpperPortalBottom(int x, int y, int halfSize) {
        int[] up = getTopPortal();
        if (up == null) return false;
        if (Math.abs(x - up[0]) >= up[2] + halfSize) return false;
        double portalBottom = up[1] - up[3];
        double head = y + halfSize;
        return head >= portalBottom - 5 && y < up[1] + up[3];
    }

    // physics / collision

    public boolean isOnGround(int x, int y, int halfSize, double speedY) {
        if (speedY > 0) return false;
        double feet = y - halfSize;
        double tol  = Math.max(Math.abs(speedY), 2.0);

        if (feet <= 1) return true;

        int[] startPipe = getStartPipe();

        for (int[] o : obstacles) {
            double top = o[1] + o[3];
            if (Math.abs(x - o[0]) < halfSize + o[2] - 1)
                if (feet >= top - tol && feet <= top + 1) return true;
        }
        for (int[] p : pipes) {
            if (p == startPipe) continue; // mario falls through the start pipe
            double top = p[1] + p[3];
            if (Math.abs(x - p[0]) < halfSize + p[2] - 1)
                if (feet >= top - tol && feet <= top + 1) return true;
        }
        if (portals != null) {
            for (int[] p : portals) {
                double top = p[1] + p[3];
                if (Math.abs(x - p[0]) < halfSize + p[2] - 1)
                    if (feet >= top - tol && feet <= top + 1) return true;
            }
        }
        return false;
    }

    public int snapToGround(int x, int y, int halfSize) {
        double feet = y - halfSize;
        double win  = Math.max(halfSize, 10.0);
        int highestGround = -1;

        int[] startPipe = getStartPipe();

        for (int[] o : obstacles) {
            double top = o[1] + o[3];
            if (Math.abs(x - o[0]) < halfSize + o[2] - 1 && Math.abs(feet - top) < win)
                if ((int) top > highestGround) highestGround = (int) top;
        }
        for (int[] p : pipes) {
            if (p == startPipe) continue;
            double top = p[1] + p[3];
            if (Math.abs(x - p[0]) < halfSize + p[2] - 1 && Math.abs(feet - top) < win)
                if ((int) top > highestGround) highestGround = (int) top;
        }
        if (portals != null) {
            for (int[] p : portals) {
                double top = p[1] + p[3];
                if (Math.abs(x - p[0]) < halfSize + p[2] - 1 && Math.abs(feet - top) < win)
                    if ((int) top > highestGround) highestGround = (int) top;
            }
        }

        if (highestGround != -1) return highestGround + halfSize;
        return halfSize;
    }

    public boolean isOnCeiling(int x, int y, int halfSize, double speedY) {
        if (speedY <= 0) return false;
        double head = y + halfSize;

        if (nearUpperPortalBottom(x, y, halfSize)) return false;

        int[] startPipe = getStartPipe();

        for (int[] o : obstacles) {
            double bottom = o[1] - o[3];
            if (Math.abs(x - o[0]) < halfSize + o[2] - 1)
                if (head >= bottom - 1 && head <= bottom + speedY + 1) return true;
        }
        for (int[] p : pipes) {
            if (p == startPipe) continue;
            double bottom = p[1] - p[3];
            if (Math.abs(x - p[0]) < halfSize + p[2] - 1)
                if (head >= bottom - 1 && head <= bottom + speedY + 1) return true;
        }
        return false;
    }

    public int snapToCeiling(int x, int y, int halfSize) {
        double head = y + halfSize;

        if (nearUpperPortalBottom(x, y, halfSize)) return y;

        int[] startPipe = getStartPipe();

        for (int[] o : obstacles) {
            double bottom = o[1] - o[3];
            if (Math.abs(x - o[0]) < halfSize + o[2] - 1 && head >= bottom - 2 && head <= bottom + 15)
                return (int) bottom - halfSize - 1;
        }
        for (int[] p : pipes) {
            if (p == startPipe) continue;
            double bottom = p[1] - p[3];
            if (Math.abs(x - p[0]) < halfSize + p[2] - 1 && head >= bottom - 2 && head <= bottom + 15)
                return (int) bottom - halfSize - 1;
        }
        return y;
    }

    public int resolveXCollision(int nextX, int currentX, int y, int halfSize) {
        if (nextX == currentX) return nextX;
        boolean goRight = nextX > currentX;
        int yHalf = halfSize - 2;

        for (int[] o : obstacles) {
            boolean wasInside    = aabb(currentX, y, halfSize, yHalf, o[0], o[1], o[2], o[3]);
            boolean willBeInside = aabb(nextX,    y, halfSize, yHalf, o[0], o[1], o[2], o[3]);
            if (!wasInside && willBeInside) {
                return goRight ? o[0] - o[2] - halfSize : o[0] + o[2] + halfSize;
            }
        }

        int[] startPipe = getStartPipe();
        for (int[] p : pipes) {
            if (p == startPipe) continue; // start pipe has no horizontal wall
            boolean wasInside    = aabb(currentX, y, halfSize, yHalf, p[0], p[1], p[2], p[3]);
            boolean willBeInside = aabb(nextX,    y, halfSize, yHalf, p[0], p[1], p[2], p[3]);
            if (!wasInside && willBeInside) {
                return goRight ? p[0] - p[2] - halfSize : p[0] + p[2] + halfSize;
            }
        }

        if (portals != null) {
            for (int[] p : portals) {
                boolean wasInside    = aabb(currentX, y, halfSize, yHalf, p[0], p[1], p[2], p[3]);
                boolean willBeInside = aabb(nextX,    y, halfSize, yHalf, p[0], p[1], p[2], p[3]);
                if (!wasInside && willBeInside) {
                    return goRight ? p[0] - p[2] - halfSize : p[0] + p[2] + halfSize;
                }
            }
        }
        return nextX;
    }

    // portal teleportation

    public void handleTeleport(Mario mario, boolean wPressed, boolean sPressed) {
        if (portals == null || portals.length < 2) return;

        int upperPortalX = 0;
        int upperPortalY = 0;
        int upperPortalW = 20;
        int upperPortalH = 20;
        int lowerX = 0;
        int lowerY = 0;
        int lowerW = 20;
        int lowerH = 20;

        for (int[] pt : portals) {
            if (pt[1] > 400) {
                upperPortalX = pt[0]; upperPortalY = pt[1]; upperPortalW = pt[2]; upperPortalH = pt[3];
            } else {
                lowerX = pt[0]; lowerY = pt[1]; lowerW = pt[2]; lowerH = pt[3];
            }
        }

        int halfSize = mario.getSize() / 2;
        double mTop    = mario.y + halfSize;
        double mBot    = mario.y - halfSize;
        double upperBot = upperPortalY - upperPortalH;
        double lowerTop = lowerY + lowerH;

        if (!mario.isEnteringPipe) {
            // check if mario is just below the upper portal and presses w
            boolean underUpper = Math.abs(mario.x - upperPortalX) < upperPortalW + 15
                    && Math.abs(mTop - upperBot) <= 20;
            if (underUpper && wPressed) {
                mario.isEnteringPipe = true;
                mario.isCrouching    = false;
                mario.x              = upperPortalX;
                mario.speedY         = 0;
            }

            // check if mario is standing on the lower portal and presses s
            boolean onLower = Math.abs(mario.x - lowerX) < lowerW + 15
                    && Math.abs(mBot - lowerTop) <= 20;
            if (onLower && sPressed) {
                mario.isEnteringPipe = true;
                mario.isCrouching    = true;
                mario.x              = lowerX;
                mario.speedY         = 0;
            }
        }

        if (mario.isEnteringPipe) {
            if (mario.isCrouching) {
                mario.y -= 4;
                if (mario.y < lowerY) {
                    mario.x              = upperPortalX;
                    mario.y              = (int)(upperBot - halfSize - 5);
                    mario.yExact         = mario.y;
                    mario.speedY         = 0;
                    mario.isEnteringPipe = false;
                    mario.isCrouching    = false;
                }
            } else {
                mario.y += 4;
                if (mario.y > upperPortalY) {
                    mario.x              = lowerX;
                    mario.y              = (int)(lowerTop + halfSize + 5);
                    mario.yExact         = mario.y;
                    mario.speedY         = 12; // give mario a boost so he clears the edge
                    mario.isEnteringPipe = false;
                }
            }
        }
    }

    // coin and exit stuff

    public int collectCoin(Mario mario, int[][] levelCoins) {
        if (levelCoins == null) return 0;
        int hs = mario.getSize() / 2;
        int count = 0;
        for (int[] c : levelCoins) {
            if (c[2] <= 0) continue;
            if (aabb(mario.x, mario.y, hs, hs, c[0], c[1], c[2], c[3])) {
                c[2] = 0; c[3] = 0; count++;
            }
        }
        return count;
    }

    public boolean allCoinsCollected(int[][] levelCoins) {
        if (levelCoins == null || levelCoins.length == 0) return true;
        for (int[] c : levelCoins) if (c[2] > 0) return false;
        return true;
    }

    // exit is always the 4th pipe (index 3)
    public boolean isAtExit(Mario mario) {
        if (pipes == null || pipes.length <= 3) return false;
        int[] exit = pipes[3];
        int hs = mario.getSize() / 2;
        return aabb(mario.x, mario.y, hs + 15, hs + 15, exit[0], exit[1], exit[2], exit[3]);
    }

    public void resetPortalState() {
        portalCooldown = 0;
    }
}