package ru.samsung.gamestudio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Input;

import java.util.ArrayList;

import ru.samsung.gamestudio.Charecers.Player;
import ru.samsung.gamestudio.Object.Ground;

public class ScreenGame implements Screen {

    private MyGdxGame myGdxGame;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture backgroundTexture;
    private BitmapFont font;
    private World world;
    private Ground ground;
    private Player player;
    private ArrayList<Obstacle> obstacles;
    private boolean gameOver;

    private float backgroundX = 0;
    private float backgroundSpeed = 50;

    private static final int SCREEN_WIDTH = 1280;
    private static final int SCREEN_HEIGHT = 720;

    private static final float GROUND_Y = 0;
    private static final float GROUND_WIDTH = 1280;
    private static final float GROUND_HEIGHT = 180;
    private static final float GROUND_SPEED = 200f;
    private static final float FIRST_OBSTACLE_X = 1000f;
    private static final float OBSTACLE_SPACING = 650f;

    private int groundContacts = 0;

    private int score = 0;
    private int highScore = 0;
    private static final String PREFERENCES_NAME = "game_prefs";
    private static final String HIGH_SCORE_KEY = "high_score";

    private Texture restartButtonTexture;
    private float restartButtonX;
    private float restartButtonY;
    private float restartButtonWidth = 400;
    private float restartButtonHeight = 120;

    private Texture pixelTexture;

    private boolean debugMode = false;

    public ScreenGame(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2);

        loadHighScore();

        backgroundTexture = new Texture("background/bacround1.png");

        try {
            restartButtonTexture = new Texture("restart.png");
            System.out.println("✓ Restart texture loaded");
        } catch (Exception e) {
            System.out.println("✗ Restart texture NOT loaded");
            restartButtonTexture = null;
        }

        restartButtonX = SCREEN_WIDTH / 2f - restartButtonWidth / 2;
        restartButtonY = SCREEN_HEIGHT / 2f - restartButtonHeight / 2;

        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 1);
        pixmap.fill();
        pixelTexture = new Texture(pixmap);
        pixmap.dispose();

        world = new World(new Vector2(0, -9.8f), true);

        ground = new Ground(
                world,
                0,
                GROUND_Y,
                GROUND_WIDTH,
                GROUND_HEIGHT,
                GROUND_SPEED
        );

        player = new Player(world, 200, GROUND_HEIGHT + Player.DRAW_HEIGHT / 2f);
        player.getBody().setUserData(player);

        obstacles = new ArrayList<>();
        createObstacles();

        gameOver = false;
        setupContactListener();
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

    private void addScore(int points) {
        score += points;
        System.out.println("Score +" + points + "! Total: " + score);
    }

    private void createObstacles() {
        obstacles.clear();
        obstacles.add(new Conus(FIRST_OBSTACLE_X, GROUND_HEIGHT));
        obstacles.add(new Box(FIRST_OBSTACLE_X + OBSTACLE_SPACING, GROUND_HEIGHT));
        obstacles.add(new Barrier(FIRST_OBSTACLE_X + OBSTACLE_SPACING * 2, GROUND_HEIGHT));
        obstacles.add(new Trash(FIRST_OBSTACLE_X + OBSTACLE_SPACING * 3, GROUND_HEIGHT));
    }

    private void setupContactListener() {
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                if (isPlayerGroundCollision(contact)) {
                    groundContacts++;
                    player.setGrounded(true);
                }
            }

            @Override
            public void endContact(Contact contact) {
                if (isPlayerGroundCollision(contact)) {
                    groundContacts--;
                    if (groundContacts <= 0) {
                        groundContacts = 0;
                        player.setGrounded(false);
                    }
                }
            }

            private boolean isPlayerGroundCollision(Contact contact) {
                Object userDataA = contact.getFixtureA().getBody().getUserData();
                Object userDataB = contact.getFixtureB().getBody().getUserData();
                return (userDataA == player && userDataB == ground) ||
                        (userDataA == ground && userDataB == player);
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
    }

    private void restartGame() {
        System.out.println(">>> RESTARTING GAME <<<");

        saveHighScore();
        gameOver = false;
        score = 0;

        if (world != null) {
            for (Obstacle obstacle : obstacles) {
                obstacle.dispose();
            }
            if (ground != null) {
                ground.dispose();
            }
            if (player != null) {
                player.dispose();
            }

            world.dispose();
            world = new World(new Vector2(0, -9.8f), true);
        }

        ground = new Ground(
                world,
                0,
                GROUND_Y,
                GROUND_WIDTH,
                GROUND_HEIGHT,
                GROUND_SPEED
        );

        player = new Player(world, 200, GROUND_HEIGHT + Player.DRAW_HEIGHT / 2f);
        player.getBody().setUserData(player);

        createObstacles();

        groundContacts = 0;
        backgroundX = 0;

        setupContactListener();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        if (!gameOver) {
            backgroundX -= backgroundSpeed * delta;
            if (backgroundX <= -SCREEN_WIDTH) {
                backgroundX = 0;
            }

            ground.update(delta);

            for (Obstacle obstacle : obstacles) {
                float oldX = obstacle.x;
                obstacle.update(delta);

                if (!gameOver && oldX + obstacle.width > player.getBounds().x &&
                        obstacle.x + obstacle.width < player.getBounds().x) {
                    addScore(10);
                }
            }

            world.step(Math.min(delta, 1 / 30f), 6, 2);
            player.update(delta);

            for (Obstacle obstacle : obstacles) {
                if (player.getBounds().overlaps(obstacle.getBounds())) {
                    if (obstacle instanceof Conus) {
                        ((Conus) obstacle).hit();
                    } else {
                        gameOver = true;
                        saveHighScore();
                        System.out.println(">>> GAME OVER! Final score: " + score);
                    }
                }
            }

            for (Obstacle obstacle : obstacles) {
                if (obstacle instanceof Conus && ((Conus) obstacle).isFinished()) {
                    gameOver = true;
                    saveHighScore();
                    System.out.println(">>> GAME OVER from Conus! Final score: " + score);
                    break;
                }
            }
        }

        batch.begin();

        batch.draw(backgroundTexture, backgroundX, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        batch.draw(backgroundTexture, backgroundX + SCREEN_WIDTH, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        ground.draw(batch);
        for (Obstacle obstacle : obstacles) {
            obstacle.draw(batch);
        }
        player.draw(batch);

        font.setColor(1, 1, 1, 1);
        font.getData().setScale(1.5f);
        font.draw(batch, "SCORE: " + score, 50, SCREEN_HEIGHT - 50);

        font.getData().setScale(1.2f);
        font.draw(batch, "BEST: " + highScore, 50, SCREEN_HEIGHT - 100);

        font.getData().setScale(2);

        if (gameOver) {
            batch.setColor(0, 0, 0, 0.8f);
            batch.draw(pixelTexture, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            batch.setColor(1, 1, 1, 1);

            font.getData().setScale(3);
            font.setColor(1, 0, 0, 1);
            float textX = SCREEN_WIDTH / 2f - 130;
            float textY = SCREEN_HEIGHT / 2f + 150;
            font.draw(batch, "GAME OVER", textX, textY);
            font.setColor(1, 1, 1, 1);

            if (restartButtonTexture != null) {
                batch.draw(restartButtonTexture,
                        restartButtonX, restartButtonY,
                        restartButtonWidth, restartButtonHeight);
            } else {
                batch.setColor(0, 0.8f, 0, 1);
                batch.draw(pixelTexture,
                        restartButtonX, restartButtonY,
                        restartButtonWidth, restartButtonHeight);
                batch.setColor(1, 1, 1, 1);

                font.getData().setScale(2);
                font.draw(batch, "RESTART",
                        restartButtonX + restartButtonWidth/2f - 60,
                        restartButtonY + restartButtonHeight/2f + 10);
            }

            if (debugMode) {
                batch.setColor(1, 0, 0, 1);
                batch.draw(pixelTexture, restartButtonX, restartButtonY, restartButtonWidth, 3);
                batch.draw(pixelTexture, restartButtonX, restartButtonY + restartButtonHeight - 3, restartButtonWidth, 3);
                batch.draw(pixelTexture, restartButtonX, restartButtonY, 3, restartButtonHeight);
                batch.draw(pixelTexture, restartButtonX + restartButtonWidth - 3, restartButtonY, 3, restartButtonHeight);
                batch.setColor(1, 1, 1, 1);
            }
        }

        batch.end();

        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = SCREEN_HEIGHT - Gdx.input.getY();

            if (gameOver) {
                boolean inX = touchX >= restartButtonX && touchX <= restartButtonX + restartButtonWidth;
                boolean inY = touchY >= restartButtonY && touchY <= restartButtonY + restartButtonHeight;

                if (inX && inY) {
                    System.out.println(">>> RESTART BUTTON PRESSED! <<<");
                    restartGame();
                }
            } else {
                if (player.isGrounded()) {
                    player.getBody().setLinearVelocity(0, 0);
                    player.getBody().applyLinearImpulse(0, 7f,
                            player.getBody().getPosition().x,
                            player.getBody().getPosition().y,
                            true);
                    player.setGrounded(false);
                    System.out.println("Jump!");
                }
            }
        }

        if (!gameOver && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (player.isGrounded()) {
                player.getBody().setLinearVelocity(0, 0);
                player.getBody().applyLinearImpulse(0, 7f,
                        player.getBody().getPosition().x,
                        player.getBody().getPosition().y,
                        true);
                player.setGrounded(false);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (player != null) player.dispose();
        if (ground != null) ground.dispose();
        for (Obstacle obstacle : obstacles) {
            obstacle.dispose();
        }
        if (world != null) world.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (restartButtonTexture != null) restartButtonTexture.dispose();
        if (pixelTexture != null) pixelTexture.dispose();
    }
}