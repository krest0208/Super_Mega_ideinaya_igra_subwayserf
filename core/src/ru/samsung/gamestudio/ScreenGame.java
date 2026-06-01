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
import ru.samsung.gamestudio.Charecers.BaseMonster;
import ru.samsung.gamestudio.Charecers.SwarmCrawler;
import ru.samsung.gamestudio.Charecers.AcidSpitter;
import ru.samsung.gamestudio.Object.Ground;
import ru.samsung.gamestudio.Object.Platform;
import ru.samsung.gamestudio.Object.Bullet;

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

    private int score = 0;
    private float scoreAccumulator = 0f;
    private ArrayList<Platform> platforms;
    private ArrayList<BaseMonster> monsters;
    private ArrayList<Bullet> bullets;

    private com.badlogic.gdx.audio.Sound shootSound;
    private com.badlogic.gdx.audio.Sound bossScreech;
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

        platforms = new ArrayList<>();
        monsters = new ArrayList<>();
        bullets = new ArrayList<>();

        shootSound = Gdx.audio.newSound(Gdx.files.internal("sounds/shoot.ogg"));
        bossScreech = Gdx.audio.newSound(Gdx.files.internal("sounds/monster_screech.ogg"));

        initGameWorld();
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
        createObstacles();

        platforms.clear();
        monsters.clear();
        bullets.clear();

        score = 0;
        scoreAccumulator = 0f;
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

                Object userDataA = contact.getFixtureA().getBody().getUserData();
                Object userDataB = contact.getFixtureB().getBody().getUserData();

                if (userDataA instanceof Bullet && userDataB instanceof BaseMonster) {
                    ((BaseMonster) userDataB).takeDamage(1);
                    ((Bullet) userDataA).destroy();
                } else if (userDataB instanceof Bullet && userDataA instanceof BaseMonster) {
                    ((BaseMonster) userDataA).takeDamage(1);
                    ((Bullet) userDataB).destroy();
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
                        (aIsSensor && fixB.getBody().getUserData() == "platform") ||
                        (bIsSensor && fixA.getBody().getUserData() == "platform");
            }

            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    private void restartGame() {
        System.out.println("RESTARTING GAME");
        clearPlatformerObjects();
        if (world != null) {
            for (Obstacle obstacle : obstacles) obstacle.dispose();
            if (ground != null) ground.dispose();
            if (player != null) player.dispose();
            world.dispose();
        }
        initGameWorld();
    }

    private void clearPlatformerObjects() {
        for (Bullet b : bullets) b.disposeWorldBody(world);
        bullets.clear();
        for (BaseMonster m : monsters) m.dispose(world);
        monsters.clear();
        for (Platform p : platforms) p.dispose(world);
        platforms.clear();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        if (!gameOver) {
            if (player.getCurrentMode() == Player.GameMode.RUNNER) {
                modeTimer += delta;

                scoreAccumulator += delta * 15f * gameSpeedModifier;
                score = (int) scoreAccumulator;

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
                ground.update(0);

                Vector2 playerPos = new Vector2(player.getX(), player.getY());

                for (int i = bullets.size() - 1; i >= 0; i--) {
                    Bullet b = bullets.get(i);
                    b.update();
                    if (b.isToDestroy()) {
                        b.disposeWorldBody(world);
                        bullets.remove(i);
                    }
                }

                for (int i = monsters.size() - 1; i >= 0; i--) {
                    BaseMonster monster = monsters.get(i);
                    monster.update(delta, playerPos);

                    if (monster.isDead()) {
                        monster.dispose(world);
                        monsters.remove(i);
                    }
                }
                if (monsters.isEmpty()) {
                    System.out.println("✓ WAVE CLEAR! RESUMING RUNNER.");
                    player.setGameMode(Player.GameMode.RUNNER);
                    modeTimer = 0;

                    for (Platform p : platforms) p.dispose(world);
                    platforms.clear();
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
            }
        }
        batch.begin();

        batch.draw(backgroundTexture, backgroundX, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        batch.draw(backgroundTexture, backgroundX + SCREEN_WIDTH, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        ground.draw(batch);

        if (player.getCurrentMode() == Player.GameMode.RUNNER) {
            for (Obstacle obstacle : obstacles) {
                obstacle.draw(batch);
            }
        } else {
            for (Platform p : platforms) p.draw(batch);
            for (BaseMonster m : monsters) m.draw(batch);
            for (Bullet b : bullets) b.draw(batch);
        }

        player.draw(batch);
        font.getData().setScale(2);
        font.setColor(1, 1, 1, 1);
        font.draw(batch, "SCORE: " + score, 30, SCREEN_HEIGHT - 30);

        if (player.getCurrentMode() == Player.GameMode.PLATFORMER) {
            font.setColor(1, 0.3f, 0f, 1);
            font.draw(batch, "FIGHT THE SWARM", SCREEN_WIDTH / 2f - 160, SCREEN_HEIGHT - 30);
        }

        if (gameOver) {
            drawGameOverScreen();
        }

        batch.end();

        handleInputSystem();
    }

    private void switchToPlatformerMode() {
        System.out.println("BOSS MODE!");
        player.setGameMode(Player.GameMode.PLATFORMER);

        for (Obstacle obs : obstacles) obs.dispose();
        obstacles.clear();

        platforms.add(new Platform(world, 400, 340, 220, 24));
        platforms.add(new Platform(world, 880, 340, 220, 24));
        platforms.add(new Platform(world, 640, 500, 260, 24));

        bossScreech.play(0.8f);

        monsters.add(new SwarmCrawler(world, 1050, GROUND_HEIGHT + 40));
        monsters.add(new SwarmCrawler(world, 1200, GROUND_HEIGHT + 40));
        monsters.add(new AcidSpitter(world, 1130, GROUND_HEIGHT + 40));
    }

    private void handleInputSystem() {
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = SCREEN_HEIGHT - Gdx.input.getY();

            if (gameOver) {
                if (touchX >= restartButtonX && touchX <= restartButtonX + restartButtonWidth &&
                        touchY >= restartButtonY && touchY <= restartButtonY + restartButtonHeight) {
                    restartGame();
                }
            } else {
                if (player.isGrounded() && player.getCurrentMode() == Player.GameMode.RUNNER) {
                    executeJumpImpulse();
                }
            }
        }

        if (!gameOver && player.isGrounded()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                executeJumpImpulse();
            }
        }

        if (!gameOver && player.getCurrentMode() == Player.GameMode.PLATFORMER) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.K) || Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT)) {
                bullets.add(new Bullet(world, player.getX(), player.getY(), player.isFlipped()));
                shootSound.play(0.25f);
            }
        }
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
        font.draw(batch, "GAME OVER", SCREEN_WIDTH / 2f - 130, SCREEN_HEIGHT / 2f + 100);
        font.setColor(1, 1, 1, 1);

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

    @Override public void resize(int width, int height) { camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        clearPlatformerObjects();
        if (player != null) player.dispose();
        if (ground != null) ground.dispose();
        for (Obstacle obstacle : obstacles) obstacle.dispose();
        if (world != null) world.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (restartButtonTexture != null) restartButtonTexture.dispose();
        if (pixelTexture != null) pixelTexture.dispose();
        if (shootSound != null) shootSound.dispose();
        if (bossScreech != null) bossScreech.dispose();
    }
}