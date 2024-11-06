import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

class MainMenu extends JFrame {
    public MainMenu() {
        setTitle("Main Menu");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(2, 1));

        JButton level1Button = new JButton("Play Level 1");
        JButton level2Button = new JButton("Play Level 2");

        level1Button.addActionListener(e -> startGame(1));
        level2Button.addActionListener(e -> startGame(2));

        add(level1Button);
        add(level2Button);

        setVisible(true);
    }

    private void startGame(int level) {
        new Game(level);
        dispose();
    }

    public static void main(String[] args) {
        new MainMenu();
    }
}

class Game extends JPanel implements ActionListener {
    private Timer timer, countdownTimer;
    private Player player1;
    private Player2 player2;
    private ArrayList<Wall> walls;
    private boolean gameOver = false;
    private JButton playAgainButton;
    private JButton mainMenuButton;
    private BufferedImage background;
    private int level;
    private int timeRemaining; // ตัวแปรเก็บเวลาที่เหลือ
    private final int MAX_TIME = 60; // เวลาสูงสุดในด่าน (60 วินาที)

    public Game(int level) {
        this.level = level;
        this.timeRemaining = MAX_TIME; // ตั้งค่าเวลาเริ่มต้น
        initializeGame();
        setupUI();
        loadBackground();
        startCountdown();
    }

    private void startCountdown() {
        countdownTimer = new Timer(1000, e -> {
            if (timeRemaining > 0) {
                timeRemaining--; // ลดเวลาทุกๆ 1 วินาที
            } else {
                gameOver = true;
                repaint(); // สิ้นสุดเกมเมื่อเวลาเหลือ 0
            }
        });
        countdownTimer.start();
    }

    private void initializeGame() {
        player1 = new Player(50, 450, "player1.png", Color.BLUE);
        player2 = new Player2(700, 50, "player2.png", Color.RED);
        walls = new ArrayList<>();
        createWalls();

        timer = new Timer(20, this);
        timer.start();

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!gameOver) {
                    player1.keyPressed(e);
                    player2.keyPressed(e);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player1.keyReleased(e);
                player2.keyReleased(e);
            }
        });
    }

    private void setupUI() {
        JFrame frame = new JFrame("Soldiers flee army - Level " + level);
        frame.add(this);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);

        setLayout(new BorderLayout());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        
        playAgainButton = new JButton("Play Again");
        playAgainButton.addActionListener(e -> resetGame());
        playAgainButton.setVisible(false);
        buttonPanel.add(playAgainButton);
        
        mainMenuButton = new JButton("Select Map");
        mainMenuButton.addActionListener(e -> backToMainMenu());
        mainMenuButton.setVisible(false);
        buttonPanel.add(mainMenuButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadBackground() {
        try {
            if (level == 1) {
                background = ImageIO.read(new File("background1.jpg"));
            } else if (level == 2) {
                background = ImageIO.read(new File("background2.jpg"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createWalls() {
        Random rand = new Random();
        String wallImagePath = (level == 1) ? "wall1.png" : "wall2.png";

        for (int i = 0; i < 10; i++) {
            int x = rand.nextInt(700);
            int y = rand.nextInt(500);
            int width = rand.nextInt(100) + 20;
            int height = rand.nextInt(100) + 20;
            if (!isWallOverlapWithPlayers(x, y, width, height)) {
                walls.add(new Wall(x, y, width, height, wallImagePath));
            }
        }
    }

    private boolean isWallOverlapWithPlayers(int x, int y, int width, int height) {
        Rectangle newWallBounds = new Rectangle(x, y, width, height);
        return newWallBounds.intersects(player1.getBounds()) || newWallBounds.intersects(player2.getBounds());
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        player1.draw(g);
        player2.draw(g);
        for (Wall wall : walls) {
            wall.draw(g);
        }

        // แสดงเวลาที่เหลือ
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Time Remaining: " + timeRemaining, 20, 30);

        if (gameOver) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Winner: " + (player1.isAlive() ? "Player 1" : "Player 2"), 300, 300);
            playAgainButton.setVisible(true);
            mainMenuButton.setVisible(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            player1.update(walls);
            player2.update(walls);
            checkCollisions();
            repaint();
        }
    }

    private void checkCollisions() {
        if (level == 2) {
            if (checkWallCollision(player1) || checkWallCollision(player2)) {
                gameOver = true;
                repaint();
                return;
            }
        }

        for (Bullet bullet : player1.getBullets()) {
            if (bullet.getBounds().intersects(player2.getBounds())) {
                player2.setAlive(false);
                gameOver = true;
            }
            for (Wall wall : walls) {
                if (bullet.getBounds().intersects(wall.getBounds())) {
                    bullet.setAlive(false);
                }
            }
        }

        for (Bullet bullet : player2.getBullets()) {
            if (bullet.getBounds().intersects(player1.getBounds())) {
                player1.setAlive(false);
                gameOver = true;
            }
            for (Wall wall : walls) {
                if (bullet.getBounds().intersects(wall.getBounds())) {
                    bullet.setAlive(false);
                }
            }
        }
    }

    private boolean checkWallCollision(Player player) {
        for (Wall wall : walls) {
            if (player.getBounds().intersects(wall.getBounds())) {
                player.setAlive(false);
                return true;
            }
        }
        return false;
    }

    private void resetGame() {
        gameOver = false;
        timeRemaining = MAX_TIME; // รีเซ็ตเวลา
        player1.setAlive(true);
        player2.setAlive(true);
        player1 = new Player(50, 450, "player1.png", Color.BLUE);
        player2 = new Player2(700, 50, "player2.png", Color.RED);
        walls.clear();
        createWalls();
        playAgainButton.setVisible(false);
        mainMenuButton.setVisible(false);
        repaint();
    }

    private void backToMainMenu() {
        JFrame mainMenu = new MainMenu();
        SwingUtilities.getWindowAncestor(this).dispose();
    }
}

class Player {
    protected int x, y;
    protected int width = 50, height = 50;
    protected BufferedImage image;
    protected boolean up, down, left, right;
    protected ArrayList<Bullet> bullets;
    protected boolean alive = true;
    protected double lastAngle = 0;
    protected Color bulletColor;

    public Player(int x, int y, String imagePath, Color bulletColor) {
        this.x = x;
        this.y = y;
        this.bulletColor = bulletColor;
        loadImage(imagePath);
        bullets = new ArrayList<>();
    }

    private void loadImage(String imagePath) {
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics g) {
        if (alive) {
            double angle = (up || down || left || right) ? getAngle() : lastAngle;
            lastAngle = angle;

            Image rotatedImage = rotateImage(image, angle);
            g.drawImage(rotatedImage, x, y, width, height, null);
        }

        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }
    }

    public void update(ArrayList<Wall> walls) {
        if (!alive) return;

        if (up) y -= 5;
        if (down) y += 5;
        if (left) x -= 5;
        if (right) x += 5;

        // Check for wall collisions
        for (Wall wall : walls) {
            if (getBounds().intersects(wall.getBounds())) {
                if (up) y += 5;
                if (down) y -= 5;
                if (left) x += 5;
                if (right) x -= 5;
            }
        }

        for (Bullet bullet : bullets) {
            bullet.update();
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP) up = true;
        if (key == KeyEvent.VK_DOWN) down = true;
        if (key == KeyEvent.VK_LEFT) left = true;
        if (key == KeyEvent.VK_RIGHT) right = true;
        if (key == KeyEvent.VK_SPACE) fireBullet();
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP) up = false;
        if (key == KeyEvent.VK_DOWN) down = false;
        if (key == KeyEvent.VK_LEFT) left = false;
        if (key == KeyEvent.VK_RIGHT) right = false;
    }

    public void fireBullet() {
        bullets.add(new Bullet(x + width / 2, y + height / 2, bulletColor));
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return alive;
    }

    private double getAngle() {
        double angle = Math.atan2(y - height / 2 - lastAngle, x + width / 2 - lastAngle);
        return angle;
    }
}

class Player2 extends Player {
    public Player2(int x, int y, String imagePath, Color bulletColor) {
        super(x, y, imagePath, bulletColor);
    }

    // Override any specific methods for Player 2 if needed
}

class Wall {
    private int x, y, width, height;
    private BufferedImage image;

    public Wall(int x, int y, int width, int height, String imagePath) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        loadImage(imagePath);
    }

    private void loadImage(String imagePath) {
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

class Bullet {
    private int x, y;
    private int width = 10, height = 10;
    private Color color;
    private boolean alive = true;

    public Bullet(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void draw(Graphics g) {
        if (alive) {
            g.setColor(color);
            g.fillRect(x, y, width, height);
        }
    }

    public void update() {
        y -= 10; // Make the bullet move upwards
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return alive;
    }
}
