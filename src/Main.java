// CanerYalcinkaya
// 2024400273

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    // reads a text file and returns an int[][] of x,y,hw,hh values
    public static int[][] readFromFile(String fileName) {
        ArrayList<int[]> list = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new File(fileName));
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                list.add(new int[]{
                        Integer.parseInt(p[0].trim()),
                        Integer.parseInt(p[1].trim()),
                        Integer.parseInt(p[2].trim()),
                        Integer.parseInt(p[3].trim())
                });
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
        }
        return list.toArray(new int[0][4]);
    }

    // need separate coin arrays per level so they don't share state
    private static int[][] deepCopy(int[][] src) {
        int[][] dst = new int[src.length][4];
        for (int i = 0; i < src.length; i++) dst[i] = src[i].clone();
        return dst;
    }

    public static void main(String[] args) {
        StdDraw.setCanvasSize(800, 800);
        StdDraw.setXscale(0, 800);
        StdDraw.setYscale(0, 800);
        StdDraw.setTitle("Super Mario Bros.");
        StdDraw.enableDoubleBuffering();

        int[][] obstacles = readFromFile("data/obstacles.txt");
        int[][] pipes     = readFromFile("data/pipes.txt");
        int[][] portals   = readFromFile("data/portals.txt");
        int[][] coinsData = readFromFile("data/coins.txt");

        Map map = new Map(obstacles, pipes, portals, coinsData);

        // level 1  reach the exit
        Level level1 = new Level(
                1,
                new ArrayList<>(),
                "Go to the exit pipe!",
                new int[0][4]
        );

        // level 2: two mushrooms, don't touch them
        ArrayList<Enemy> en2 = new ArrayList<>();
        en2.add(new Enemy(340, 580, 40, 0, 260, 420));
        en2.add(new Enemy(440, 300, 40, 1, 380, 500));
        Level level2 = new Level(
                2,
                en2,
                "Do not mess with mushrooms!",
                new int[0][4]
        );

        // level 3: same enemies + coins to collect
        ArrayList<Enemy> en3 = new ArrayList<>();
        en3.add(new Enemy(340, 580, 40, 0, 260, 420));
        en3.add(new Enemy(440, 300, 40, 1, 380, 500));
        Level level3 = new Level(
                3,
                en3,
                "Do not forget to collect all the coins!",
                deepCopy(coinsData)
        );

        // level 4: same positions but shooting enemies
        ArrayList<Enemy> en4 = new ArrayList<>();
        en4.add(new Enemy(340, 580, 40, 0, 260, 420, 1));
        en4.add(new Enemy(440, 300, 40, 1, 380, 500, 1));
        Level level4 = new Level(
                4,
                en4,
                "Dodge the Clover Bullets and reach the exit!",
                new int[0][4]
        );

        ArrayList<Level> levels = new ArrayList<>();
        levels.add(level1);
        levels.add(level2);
        levels.add(level3);
        levels.add(level4);

        new Game(levels, map).run();
    }
}