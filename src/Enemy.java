// CanerYalcinkaya
// 2024400273

import java.util.ArrayList;

public class Enemy {
    private int x, y, size, direction, x1, x2;
    private int type; // 0: mushroom, 1: lepro-gnome (shoots bullets)
    private int shootCooldown = 0;

    private ArrayList<Bullet> bullets;

    // bullet is just a simple inner class, nothing fancy
    public class Bullet {
        public double x, y;
        public int direction;
        public Bullet(double x, double y, int direction) {
            this.x = x;
            this.y = y;
            this.direction = direction;
        }
    }

    // default constructor, type 0
    public Enemy(int x, int y, int size, int direction, int x1, int x2) {
        this(x, y, size, direction, x1, x2, 0);
    }

    // used for custom enemy types
    public Enemy(int x, int y, int size, int direction, int x1, int x2, int type) {
        this.x         = x;
        this.y         = y;
        this.size      = size;
        this.direction = direction;
        this.x1        = x1;
        this.x2        = x2;
        this.type      = type;
        this.bullets   = new ArrayList<>();
    }

    public int getX()    { return x; }
    public int getY()    { return y; }
    public int getSize() { return size; }
    public int getType() { return type; }
    public ArrayList<Bullet> getBullets() { return bullets; }

    public void move() {
        // move enemy left or right
        if (direction == 1) x += 2;
        else                x -= 2;

        // bounce at boundaries
        if (x + size / 2 >= x2) direction = 0;
        else if (x - size / 2 <= x1) direction = 1;

        // update bullet positions, remove ones that went off screen
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (b.direction == 1) b.x += 5;
            else                  b.x -= 5;

            if (b.x < -50 || b.x > 850) {
                bullets.remove(i);
            }
        }

        // shooting logic, only for type 1
        if (type == 1) {
            if (shootCooldown > 0) {
                shootCooldown--;
            } else {
                bullets.add(new Bullet(x, y, direction));
                shootCooldown = 90; // fires every 90 frames
            }
        }
    }

    // called on player death so old bullets don't linger
    public void resetBullets() {
        bullets.clear();
    }

    public void draw() {
        if (type == 0) {
            StdDraw.picture(x, y, "assets/mushroom.png", size, size);
        } else {
            StdDraw.picture(x, y, "assets/newEnemy.png", size, size);
        }

        for (Bullet b : bullets) {
            StdDraw.picture(b.x, b.y, "assets/newEnemyBullet.png", 24, 24);
        }
    }
}