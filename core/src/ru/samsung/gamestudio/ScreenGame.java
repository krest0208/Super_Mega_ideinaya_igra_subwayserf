package ru.samsung.gamestudio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Input;

import java.util.ArrayList;
import java.util.Random;

import ru.samsung.gamestudio.Charecers.Player;
import ru.samsung.gamestudio.Charecers.Boss;
import ru.samsung.gamestudio.Object.Ground;
import ru.samsung.gamestudio.Object.Bullet;

public class ScreenGame implements Screen {

    private MyGdxGame myGdxGame;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture backgroundTexture;
    private Texture bossBackgroundTexture;
    private BitmapFont font;
    private World world;
    private Ground ground;
    private Player player;
    private ArrayList<Obstacle> obstacles;
    private boolean gameOver;

    private int score = 0;
    private int highScore = 0;
    private static final String PREFERENCES_NAME = "game_prefs";
    private static final String HIGH_SCORE_KEY = "high_score";

    private ArrayList<Bullet> bullets;
    private Random random;

    private float backgroundX = 0;
    private float baseBackgroundSpeed = 50f;
    private float baseGroundSpeed = 200f;
    private float gameSpeedModifier = 1.0f;
    private static final float ACCELERATION_RATE = 0.01f;
    private static final float MAX_SPEED_MODIFIER = 2.0f;

    private int scoreToSpawnBoss = 500;  // Теперь не final, можно менять
    private boolean bossSpawned = false;
    private boolean bossFightActive = false;

    private static final int SCREEN_WIDTH = 1280;
    private static final int SCREEN_HEIGHT = 720;

    private static final float GROUND_Y = 0;
    private static final float GROUND_WIDTH = 1280;
    private static final float GROUND_HEIGHT = 180;

    private int groundContacts = 0;

    private Texture restartButtonTexture;
    private float restartButtonX;
    private float restartButtonY;
    private float restartButtonWidth = 400;
    private float restartButtonHeight = 120;

    private Texture pixelTexture;

    private Boss currentBoss;
    private float shootCooldown = 0;
    private static final float SHOOT_DELAY = 0.3f;

    // Параметры спавна препятствий
    private float nextSpawnX = 1280f;
    private static final float MIN_SPAWN_DISTANCE = 700f;
    private static final float MAX_SPAWN_DISTANCE = 950f;

    public ScreenGame(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;
        this.random = new Random();
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2);

        backgroundTexture = new Texture("background/post2.png");
        bossBackgroundTexture = new Texture("background/boss_location.png");

        loadHighScore();

        try {
            restartButtonTexture = new Texture("restart.png");
        } catch (Exception e) {
            restartButtonTexture = null;
        }

        restartButtonX = SCREEN_WIDTH / 2f - restartButtonWidth / 2;
        restartButtonY = SCREEN_HEIGHT / 2f - restartButtonHeight / 2;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 1);
        pixmap.fill();
        pixelTexture = new Texture(pixmap);
        pixmap.dispose();

        bullets = new ArrayList<>();

        initGameWorld();
    }

    private void loadHighScore() {
        highScore = Gdx.app.getPreferences(PREFERENCES_NAME).getInteger(HIGH_SCORE_KEY, 0);
        System.out.println("High score loaded: " + highScore);
    }

    private void saveHighScore() {
        if (score > highScore) {
            highScore = score;
            Gdx.app.getPreferences(PREFERENCES_NAME).putInteger(HIGH_SCORE_KEY, highScore).flush();
            System.out.println("New high score saved: " + highScore);
        }
    }

    private Obstacle spawnObstacle() {
        int type = random.nextInt(4);
        float x = nextSpawnX;

        Obstacle obstacle;
        switch (type) {
            case 0:
                obstacle = new Box(x, GROUND_HEIGHT);
                break;
            case 1:
                obstacle = new Barrier(x, GROUND_HEIGHT);
                break;
            case 2:
                obstacle = new Trash(x, GROUND_HEIGHT);
                break;
            default:
                obstacle = new Conus(x, GROUND_HEIGHT);
                break;
        }

        float distance = MIN_SPAWN_DISTANCE + random.nextFloat() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
        nextSpawnX += distance;

        return obstacle;
    }

    private void resetSpawner() {
        nextSpawnX = 1280f;
    }

    private void initGameWorld() {
        world = new World(new Vector2(0, -14.0f), true);

        ground = new Ground(world, 0, GROUND_Y, GROUND_WIDTH, GROUND_HEIGHT, baseGroundSpeed);
        if (ground.getBody() != null) {
            ground.getBody().setUserData(ground);
        }

        player = new Player(world, 200, GROUND_HEIGHT + Player.DRAW_HEIGHT / 2f);
        player.getBody().setUserData(player);

        obstacles = new ArrayList<>();
        resetSpawner();

        for (int i = 0; i < 4; i++) {
            obstacles.add(spawnObstacle());
        }

        bullets.clear();

        score = 0;
        scoreToSpawnBoss = 500;  // Сбрасываем порог для нового забега
        gameSpeedModifier = 1.0f;
        groundContacts = 0;
        backgroundX = 0;
        gameOver = false;
        bossSpawned = false;
        bossFightActive = false;
        shootCooldown = 0;

        if (currentBoss != null) {
            currentBoss.dispose(world);
            currentBoss = null;
        }

        setupContactListener();
    }

    private void setupContactListener() {
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                if (checkPlayerGroundSensor(contact)) {
                    groundContacts++;
                    player.setGrounded(true);
                }

                Object userDataA = contact.getFixtureA().getBody().getUserData();
                Object userDataB = contact.getFixtureB().getBody().getUserData();

                if (userDataA instanceof Bullet && userDataB instanceof Boss) {
                    Bullet bullet = (Bullet) userDataA;
                    if (!bullet.isEnemyBullet()) {
                        ((Boss) userDataB).takeDamage(5);
                        bullet.destroy();
                    }
                } else if (userDataB instanceof Bullet && userDataA instanceof Boss) {
                    Bullet bullet = (Bullet) userDataB;
                    if (!bullet.isEnemyBullet()) {
                        ((Boss) userDataA).takeDamage(5);
                        bullet.destroy();
                    }
                }

                if (userDataA instanceof Bullet && userDataB instanceof Player) {
                    Bullet bullet = (Bullet) userDataA;
                    if (bullet.isEnemyBullet()) {
                        gameOver = true;
                        saveHighScore();
                    }
                } else if (userDataB instanceof Bullet && userDataA instanceof Player) {
                    Bullet bullet = (Bullet) userDataB;
                    if (bullet.isEnemyBullet()) {
                        gameOver = true;
                        saveHighScore();
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {
                if (checkPlayerGroundSensor(contact)) {
                    groundContacts--;
                    if (groundContacts <= 0) {
                        groundContacts = 0;
                        player.setGrounded(false);
                    }
                }
            }

            private boolean checkPlayerGroundSensor(Contact contact) {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();
                boolean aIsSensor = "player_ground_sensor".equals(fixA.getUserData());
                boolean bIsSensor = "player_ground_sensor".equals(fixB.getUserData());
                return (aIsSensor && fixB.getBody().getUserData() == ground) ||
                        (bIsSensor && fixA.getBody().getUserData() == ground);
            }

            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    private void startBossFight() {
        System.out.println("!!! BOSS FIGHT STARTED !!!");
        bossFightActive = true;
        bossSpawned = true;

        for (Obstacle obstacle : obstacles) {
            obstacle.dispose();
        }
        obstacles.clear();

        for (Bullet b : bullets) {
            b.disposeWorldBody(world);
        }
        bullets.clear();

        currentBoss = new Boss(world, SCREEN_WIDTH / 2f, GROUND_HEIGHT + 60);
    }

    private void checkBossDefeated() {
        if (currentBoss != null && currentBoss.isDead()) {
            System.out.println("!!! BOSS DEFEATED !!!");

            for (Bullet b : bullets) {
                b.disposeWorldBody(world);
            }
            bullets.clear();

            currentBoss.dispose(world);
            currentBoss = null;

            bossFightActive = false;
            bossSpawned = false;

            // НЕ ДОБАВЛЯЕМ ОЧКИ, а увеличиваем порог для следующего босса
            score = 0;  // Сбрасываем счет
            scoreToSpawnBoss = (int)(scoreToSpawnBoss * 1.5f);  // Увеличиваем порог в 1.5 раза
            System.out.println("Next boss at: " + scoreToSpawnBoss + " points");

            resetSpawner();
            obstacles.clear();
            for (int i = 0; i < 4; i++) {
                obstacles.add(spawnObstacle());
            }

            backgroundX = 0;
            gameSpeedModifier = 1.0f;  // Сбрасываем скорость

            System.out.println("Returning to runner mode!");
        }
    }

    private void playerShoot() {
        if (shootCooldown <= 0 && bossFightActive && currentBoss != null && !currentBoss.isDead()) {
            int direction = (currentBoss.getX() > player.getX()) ? 1 : -1;
            player.setFacingDirection(direction > 0);
            bullets.add(new Bullet(world, player.getX(), player.getY() + 30, direction, false));
            shootCooldown = SHOOT_DELAY;
            System.out.println("Player shoots!");
        }
    }

    private void restartGame() {
        System.out.println("RESTARTING GAME");

        for (Bullet b : bullets) b.disposeWorldBody(world);
        bullets.clear();

        if (currentBoss != null) {
            currentBoss.dispose(world);
            currentBoss = null;
        }

        if (world != null) {
            for (Obstacle obstacle : obstacles) obstacle.dispose();
            if (ground != null) ground.dispose();
            if (player != null) player.dispose();
            world.dispose();
        }

        initGameWorld();
    }

    private void executeJumpImpulse() {
        player.getBody().setLinearVelocity(player.getBody().getLinearVelocity().x, 0);
        player.getBody().applyLinearImpulse(new Vector2(0, 5.8f), player.getBody().getWorldCenter(), true);
        player.setGrounded(false);
    }

    private void drawGameOverScreen() {
        batch.setColor(0, 0, 0, 0.8f);
        batch.draw(pixelTexture, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        batch.setColor(1, 1, 1, 1);

        font.getData().setScale(3);
        font.setColor(1, 0, 0, 1);
        font.draw(batch, "GAME OVER", SCREEN_WIDTH / 2f - 130, SCREEN_HEIGHT / 2f + 150);
        font.setColor(1, 1, 1, 1);

        font.getData().setScale(1.5f);
        font.draw(batch, "SCORE: " + score, SCREEN_WIDTH / 2f - 70, SCREEN_HEIGHT / 2f + 80);
        font.draw(batch, "BEST: " + highScore, SCREEN_WIDTH / 2f - 60, SCREEN_HEIGHT / 2f + 40);

        if (restartButtonTexture != null) {
            batch.draw(restartButtonTexture, restartButtonX, restartButtonY, restartButtonWidth, restartButtonHeight);
        } else {
            batch.setColor(0, 0.8f, 0, 1);
            batch.draw(pixelTexture, restartButtonX, restartButtonY, restartButtonWidth, restartButtonHeight);
            batch.setColor(1, 1, 1, 1);
            font.getData().setScale(2);
            font.draw(batch, "RESTART", restartButtonX + restartButtonWidth/2f - 60, restartButtonY + restartButtonHeight/2f + 5);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        if (!gameOver) {
            if (!bossSpawned && !bossFightActive && score >= scoreToSpawnBoss) {
                startBossFight();
            }

            if (bossFightActive && currentBoss != null) {
                currentBoss.update(delta, new Vector2(player.getX(), player.getY()));

                if (currentBoss.canShoot()) {
                    bullets.add(new Bullet(world,
                            currentBoss.getShootX(),
                            currentBoss.getShootY(),
                            currentBoss.getShootDirection(),
                            true));
                    currentBoss.resetShootTimer();
                }

                for (int i = bullets.size() - 1; i >= 0; i--) {
                    Bullet b = bullets.get(i);
                    b.update();
                    if (b.isToDestroy()) {
                        b.disposeWorldBody(world);
                        bullets.remove(i);
                    }
                }

                for (Bullet b : bullets) {
                    if (!b.isEnemyBullet() && b.getBounds().overlaps(currentBoss.getBounds())) {
                        currentBoss.takeDamage(5);
                        b.destroy();
                    }
                }

                for (Bullet b : bullets) {
                    if (b.isEnemyBullet() && b.getBounds().overlaps(player.getBounds())) {
                        gameOver = true;
                        saveHighScore();
                    }
                }

                if (player.getBounds().overlaps(currentBoss.getBounds())) {
                    gameOver = true;
                    saveHighScore();
                }

                shootCooldown -= delta;
                checkBossDefeated();

            } else if (!bossFightActive) {
                score += delta * 15f * gameSpeedModifier;

                if (gameSpeedModifier < MAX_SPEED_MODIFIER) {
                    gameSpeedModifier += ACCELERATION_RATE * delta;
                }

                backgroundX -= baseBackgroundSpeed * gameSpeedModifier * delta;
                if (backgroundX <= -SCREEN_WIDTH) {
                    backgroundX = 0;
                }

                float currentGroundSpeed = baseGroundSpeed * gameSpeedModifier;
                ground.setSpeed(currentGroundSpeed);
                ground.update(delta);

                ArrayList<Obstacle> newObstacles = new ArrayList<>();
                for (Obstacle obstacle : obstacles) {
                    float oldX = obstacle.x;
                    obstacle.update(delta * gameSpeedModifier);

                    if (oldX + obstacle.width > player.getBounds().x &&
                            obstacle.x + obstacle.width < player.getBounds().x) {
                        score += 10;
                    }

                    if (obstacle.x + obstacle.width > -200) {
                        newObstacles.add(obstacle);
                    }
                }
                obstacles = newObstacles;

                if (obstacles.size() < 3) {
                    obstacles.add(spawnObstacle());
                }

                for (Obstacle obstacle : obstacles) {
                    if (player.getBounds().overlaps(obstacle.getBounds())) {
                        if (obstacle instanceof Conus) {
                            ((Conus) obstacle).hit();
                        } else {
                            gameOver = true;
                            saveHighScore();
                        }
                    }
                }

                for (Obstacle obstacle : obstacles) {
                    if (obstacle instanceof Conus && ((Conus) obstacle).isFinished()) {
                        gameOver = true;
                        saveHighScore();
                        break;
                    }
                }
            }

            world.step(Math.min(delta, 1 / 30f), 6, 2);
            player.update(delta);
        }

        batch.begin();

        if (bossFightActive) {
            batch.draw(bossBackgroundTexture, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        } else {
            batch.draw(backgroundTexture, backgroundX, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            batch.draw(backgroundTexture, backgroundX + SCREEN_WIDTH, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        ground.draw(batch);

        if (bossFightActive) {
            for (Bullet b : bullets) b.draw(batch);
            if (currentBoss != null) currentBoss.draw(batch);
        } else {
            for (Obstacle obstacle : obstacles) {
                obstacle.draw(batch);
            }
        }

        player.draw(batch);

        font.setColor(1, 1, 1, 1);

        font.getData().setScale(1.5f);
        font.draw(batch, "SCORE: " + score, 30, SCREEN_HEIGHT - 30);

        font.getData().setScale(1.2f);
        font.draw(batch, "BEST: " + highScore, 30, SCREEN_HEIGHT - 70);

        if (!bossFightActive) {
            font.getData().setScale(1f);
            int nextBoss = scoreToSpawnBoss - score;
            if (nextBoss < 0) nextBoss = 0;
            font.draw(batch, "NEXT BOSS: " + nextBoss, 30, SCREEN_HEIGHT - 110);
        }

        if (bossFightActive && currentBoss != null && !currentBoss.isDead()) {
            font.getData().setScale(1.2f);
            font.setColor(1, 0.5f, 0, 1);
            font.draw(batch, "SCORPION BOSS", SCREEN_WIDTH / 2f - 80, SCREEN_HEIGHT - 50);
            font.setColor(1, 1, 1, 1);

            float hpPercent = (float) currentBoss.getHp() / currentBoss.getMaxHp();
            batch.setColor(0.5f, 0, 0, 1);
            batch.draw(pixelTexture, SCREEN_WIDTH / 2f - 150, SCREEN_HEIGHT - 70, 300, 20);
            batch.setColor(0, 1, 0, 1);
            batch.draw(pixelTexture, SCREEN_WIDTH / 2f - 150, SCREEN_HEIGHT - 70, 300 * hpPercent, 20);
            batch.setColor(1, 1, 1, 1);

            font.getData().setScale(1f);
            font.draw(batch, "HP: " + currentBoss.getHp() + "/" + currentBoss.getMaxHp(),
                    SCREEN_WIDTH / 2f - 60, SCREEN_HEIGHT - 95);

            font.getData().setScale(0.8f);
            font.draw(batch, "Press K to shoot", SCREEN_WIDTH - 150, 50);
        }

        font.getData().setScale(2);

        if (gameOver) {
            drawGameOverScreen();
        }

        batch.end();

        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = SCREEN_HEIGHT - Gdx.input.getY();

            if (gameOver) {
                if (touchX >= restartButtonX && touchX <= restartButtonX + restartButtonWidth &&
                        touchY >= restartButtonY && touchY <= restartButtonY + restartButtonHeight) {
                    restartGame();
                }
            } else {
                if (player.isGrounded()) {
                    executeJumpImpulse();
                }
            }
        }

        if (!gameOver && player.isGrounded()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
                    Gdx.input.isKeyJustPressed(Input.Keys.W) ||
                    Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                executeJumpImpulse();
            }
        }

        if (!gameOver && bossFightActive && currentBoss != null && !currentBoss.isDead()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.K) ||
                    Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT) ||
                    Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
                playerShoot();
            }
        }
    }

    @Override public void resize(int width, int height) { camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        for (Bullet b : bullets) b.disposeWorldBody(world);
        bullets.clear();

        if (player != null) player.dispose();
        if (ground != null) ground.dispose();
        for (Obstacle obstacle : obstacles) obstacle.dispose();
        if (currentBoss != null) currentBoss.dispose(world);
        if (world != null) world.dispose();

        if (backgroundTexture != null) backgroundTexture.dispose();
        if (bossBackgroundTexture != null) bossBackgroundTexture.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (restartButtonTexture != null) restartButtonTexture.dispose();
        if (pixelTexture != null) pixelTexture.dispose();
    }
}