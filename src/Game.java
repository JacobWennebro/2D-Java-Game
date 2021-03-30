import jdk.nashorn.internal.parser.JSONParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Game extends Canvas implements Runnable {
    private JFrame frame = new JFrame("2D Minecraft");
    private BufferStrategy bs;
    private Thread thread;
    private boolean isRunning;
    private int fps = 30;
    private int windowX = 64*12;
    private int windowY = 64*8;
    private int playerPos = 0;
    Integer[][] MapTiles = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3},
            {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3},
            {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3},
            {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3}
    };
    GameMap map;

    private class GameMap {
        private Graphics _g;

        public Color tileIdToColor(int id) {
            String color = "#000";
            switch(id) {
                case 0:
                    // AIR
                    color = "#74b9ff";
                    break;
                case 1:
                    // GRASS
                    color = "#00b894";
                    break;
                case 2:
                    // DIRT
                    color = "#cd6133";
                    break;
                case 3:
                    // STONE
                    color = "#636e72";
                    break;
            }

            return Color.decode(color);
        }

        public GameMap(Integer[][] layout, Graphics g) {
            _g = g;
            update(layout);
        }

        public void update(Integer[][] layout) {
            List<Integer[]> rows = Arrays.asList(layout);
            int y = -64;
            int x = 0;
            int tileSize = 64;

            for(int i=0; i < layout.length; i++) {
                Integer[] row = rows.get(i);
                y += tileSize;
                x = 0;
                for(int i2=0; i2 < row.length; i2++) {
                    int tile = Arrays.asList(row).get(i2);
                    drawSolid(this._g, this.tileIdToColor(tile), x, y);
                    x += tileSize;
                }
            }
        }
    }

    private class ML implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }

    private class KL implements KeyListener {

        @Override
        public void keyTyped(KeyEvent keyEvent) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            int kc = e.getKeyCode();

            // ANY MOVEMENT
            if(kc == 68 || kc == 65 || kc == 32) {
                ArrayList<Integer[]> tileList = new ArrayList<>(Arrays.asList(MapTiles));
                ArrayList<Integer> row = new ArrayList<>(Arrays.asList(tileList.get(3)));

                for(int i=0; i < row.size(); i++) {
                    if(i==playerPos) {
                        row.set(playerPos, 0);
                    }
                }

                Integer[] arr = new Integer[row.size()];
                arr = row.toArray(arr);

                Integer[][] MapArray = new Integer[tileList.size()][];
                MapArray = tileList.toArray(MapArray);

                tileList.set(3, arr);
                MapTiles = MapArray;

                map.update(MapTiles);
            }

            switch(kc) {
                case 68: // D
                    if(playerPos > 10) return;
                    playerPos += 1;
                    break;
                case 65: // A
                    if(playerPos <= 0) return;
                    playerPos -= 1;
                    break;
                case 32: // SPACEBAR
                    MapTiles = Arrays.copyOfRange(MapTiles, 1, MapTiles.length);

                    int n = MapTiles.length;
                    Integer newArr[][] = new Integer[n+1][];

                    for(int i = 0; i < n; i++) {
                        newArr[i] = MapTiles[i];
                    }

                    newArr[n] = GenerateRow();

                    MapTiles = newArr;

                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {

        }
    }

    public static void main(String[] args) {

        Game painting = new Game();
        painting.start();
    }



    public Game() {

        frame.setResizable(false);
        this.setSize(windowX , windowY);
        frame.add(this);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        this.setBackground(Color.decode("#74b9ff"));

        this.addKeyListener(new KL());
        this.addMouseMotionListener(new ML());

        draw();

        isRunning = false;
    }

    public Integer[] GenerateRow() {
        Integer[] row = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
        return row;
    }

    public void drawCharacter(Graphics g, int x) {
        g.setColor(Color.BLUE);
        g.fillRect(x*64, windowY/2, 64, 64);
    }

    public void drawSolid(Graphics g, Color color, int x, int y) {
        g.setColor(color);
        g.fillRect(x, y, 64, 64);
    }

    public void draw() {
        bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        update();

        this.map = new GameMap(this.MapTiles, g);

        drawCharacter(g, this.playerPos);

        g.dispose();
        bs.show();
    }

    public synchronized void start() {
        thread = new Thread(this);
        isRunning = true;
        thread.start();
    }

    public void update() {

    }

    public synchronized void stop() {
        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void shiftLeft(Integer[][] array, int amount) {
        for (int j = 0; j < amount; j++) {
            Integer[] a = array[0];
            int i;
            for (i = 0; i < array.length - 1; i++)
                array[i] = array[i + 1];
            array[i] = a;
        }
    }

    @Override
    public void run() {
        double deltaT = 1000.0/fps;
        long lastTime = System.currentTimeMillis();

        while (isRunning) {
            long now = System.currentTimeMillis();
            if (now-lastTime > deltaT) {
                update();
                draw();
                lastTime = now;
            }

        }
        stop();
    }
}
