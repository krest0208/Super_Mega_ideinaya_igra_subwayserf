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
import com.badlogic.gdx.physics.box2d.Fixture;
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
    private float baseBackgroundSpeed = 50f;
    private float baseGroundSpeed = 200f;
    private float gameSpeedModifier = 1.0f;
    private static final float ACCELERATION_RATE = 0.03f;
    private static final float MAX_SPEED_MODIFIER = 2.5f;
    private float modeTimer = 0f;
    private static final float TIME_TO_BOSS = 15f;
    private static final int SCREEN_WIDTH = 1280;
    private static final int SCREEN_HEIGHT = 720;

    private static final float GROUND_Y = 0;
    private static final float GROUND_WIDTH = 1280;
    private static final float GROUND_HEIGHT = 180;
    private static final float FIRST_OBSTACLE_X = 1000f;
    private static final float OBSTACLE_SPACING = 650f;

    private int groundContacts = 0;

    private Texture restartButtonTexture;
    private float restartButtonX;
    private float restartButtonY;
    private float restartButtonWidth = 400;
    private float restartButtonHeight = 120;

    private Texture pixelTexture;
    private boolean debugMode = true;

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

        backgroundTexture = new Texture("background/post2.png");

        try {
            restartButtonTexture = new Texture("restart.png");
        } catch (Exception e) {
            restartButtonTexture = null;
        }

        restartButtonX = SCREEN_WIDTH / 2f - restartButtonWidth / 2;
        restartButtonY = SCREEN_HEIGHT / 2f - restartButtonHeight / 2;

        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 1);
        pixmap.fill();
        pixelTexture = new Texture(pixmap);
        pixmap.dispose();

        initGameWorld();
    }

    private void initGameWorld() {
        world = new World(new Vector2(0, -14.0f), true); // Слегка усилили гравитацию для динамики

        ground = new Ground(world, 0, GROUND_Y, GROUND_WIDTH, GROUND_HEIGHT, baseGroundSpeed);

        player = new Player(world, 200, GROUND_HEIGHT + Player.DRAW_HEIGHT / 2f);
        player.getBody().setUserData(player);

        obstacles = new ArrayList<>();
        createObstacles();

        gameSpeedModifier = 1.0f;
        modeTimer = 0f;
        groundContacts = 0;
        backgroundX = 0;
        gameOver = false;

        setupContactListener();
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
                if (checkPlayerGroundSensor(contact)) {
                    groundContacts++;
                    player.setGrounded(true);
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
                        (bIsSensor && fixA.getBody().getUserData() == ground) ||
                        (aIsSensor && fixB.getBody().getUserData() != null && fixB.getBody().getUserData().toString().contains("platform")) ||
                        (bIsSensor && fixA.getBody().getUserData() != null && fixA.getBody().getUserData().toString().contains("platform"));
            }

            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    private void restartGame() {
        System.out.println("RESTARTING GAME");
        if (world != null) {
            for (Obstacle obstacle : obstacles) obstacle.dispose();
            if (ground != null) ground.dispose();
            if (player != null) player.dispose();
            world.dispose();
        }
        initGameWorld();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        if (!gameOver) {

            if (player.getCurrentMode() == Player.GameMode.RUNNER) {
                modeTimer += delta;

                if (gameSpeedModifier < MAX_SPEED_MODIFIER) {
                    gameSpeedModifier += ACCELERATION_RATE * delta;
                }

                backgroundX -= baseBackgroundSpeed * gameSpeedModifier * delta;
                if (backgroundX <= -SCREEN_WIDTH) {
                    backgroundX = 0;
                }

                ground.update(delta * gameSpeedModifier);
                for (Obstacle obstacle : obstacles) {
                    obstacle.update(delta * gameSpeedModifier);
                }


                if (modeTimer >= TIME_TO_BOSS) {
                    switchToPlatformerMode();
                }

            } else {
                backgroundX = backgroundX;

                ground.update(0);
                for (Obstacle obstacle : obstacles) {
                    obstacle.update(0);
                }
            }

            world.step(Math.min(delta, 1 / 30f), 6, 2);
            player.update(delta);

            if (player.getCurrentMode() == Player.GameMode.RUNNER) {
                for (Obstacle obstacle : obstacles) {
                    if (player.getBounds().overlaps(obstacle.getBounds())) {
                        if (obstacle instanceof Conus) {
                            ((Conus) obstacle).hit();
                        } else {
                            gameOver = true;
                        }
                    }
                }
                for (Obstacle obstacle : obstacles) {
                    if (obstacle instanceof Conus && ((Conus) obstacle).isFinished()) {
                        gameOver = true;
                        break;
                    }
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

        if (gameOver) {
            drawGameOverScreen();
        }

        batch.end();

        handleInputSystem();
    }

    private void switchToPlatformerMode() {
        System.out.println("DANGEROUS");
        player.setGameMode(Player.GameMode.PLATFORMER);

        for (Obstacle obs : obstacles) {
            obs.dispose();
        }
        obstacles.clear();
    }

    private void handleInputSystem() {
        // Логика тач-событий (мышь/экран)
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = SCREEN_HEIGHT - Gdx.input.getY();

            if (gameOver) {
                if (touchX >= restartButtonX && touchX <= restartButtonX + restartButtonWidth &&
                        touchY >= restartButtonY && touchY <= restartButtonY + restartButtonHeight) {
                    restartGame();
                }
            } else {
                // Если кликнули по экрану в режиме раннера — прыгаем импульсом
                if (player.isGrounded() && player.getCurrentMode() == Player.GameMode.RUNNER) {
                    executeJumpImpulse();
                }
            }
        }

        // Дублирование прыжка кнопками для десктопа
        if (!gameOver && player.isGrounded()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                executeJumpImpulse();
            }
        }
    }

    private void executeJumpImpulse() {
        player.getBody().setLinearVelocity(player.getBody().getLinearVelocity().x, 5.4f);
        player.setGrounded(false);
    }

    private void drawGameOverScreen() {
        if (restartButtonTexture != null) {
            batch.draw(restartButtonTexture, restartButtonX, restartButtonY, restartButtonWidth, restartButtonHeight);
        } else {
            font.draw(batch, "GAME OVER! Tap to Restart", SCREEN_WIDTH / 2f - 150, SCREEN_HEIGHT / 2f);
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        batch.dispose();
        backgroundTexture.dispose();
        font.dispose();
        if (restartButtonTexture != null) restartButtonTexture.dispose();
        if (world != null) world.dispose();
    }
}
