// CanerYalcinkaya
// 2024400273

import java.util.ArrayList;

public class Level {
    private int              id;
    private ArrayList<Enemy> enemies;
    private String           clue;
    private int[][]          coins;

    public Level(int id, ArrayList<Enemy> enemies, String clue, int[][] coins) {
        this.id      = id;
        this.enemies = enemies;
        this.clue    = clue;
        this.coins   = coins;
    }

    public int              getId()      { return id; }
    public ArrayList<Enemy> getEnemies() { return enemies; }
    public String           getClue()    { return clue; }
    public int[][]          getCoins()   { return coins; }

    public void update() {
        for (Enemy e : enemies) e.move();
    }

    // clear bullets when player dies or level resets
    public void resetEnemies() {
        for (Enemy e : enemies) {
            e.resetBullets();
        }
    }

    public void draw() {
        // skip coins with size 0, they've been collected
        for (int[] c : coins)
            if (c[2] > 0)
                StdDraw.picture(c[0], c[1], "assets/coin.png", c[2] * 2, c[3] * 2);

        for (Enemy e : enemies) e.draw();
    }
}