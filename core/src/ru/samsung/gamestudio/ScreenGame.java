package ru.samsung.gamestudio;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;

import ru.samsung.gamestudio.Charecers.Player;
import ru.samsung.gamestudio.Object.Ground;

public class ScreenGame implements Screen {

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture backgroundTexture;

    private World world;

    private Ground ground;
    private Player player;

    private float backgroundX = 0;
    private float backgroundSpeed = 50;

    private static final int SCREEN_WIDTH = 1280;
    private static final int SCREEN_HEIGHT = 720;

    private static final float GROUND_X = 0;
    private static final float GROUND_Y = 0;
    private static final float GROUND_WIDTH = 1280;
    private static final float GROUND_HEIGHT = 180;
    private static final float GROUND_SPEED = 200f;

    private int groundContacts = 0;

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

        batch = new SpriteBatch();

        backgroundTexture = new Texture("background/background1.png");

        world = new World(new Vector2(0, -9.8f), true);

        ground = new Ground(
                world,
                GROUND_X,
                GROUND_Y,
                GROUND_WIDTH,
                GROUND_HEIGHT,
                GROUND_SPEED
        );

        player = new Player(
                world,
                200,
                GROUND_HEIGHT + 40
        );

        player.getBody().setUserData(player);

        setupContactListener();
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

                return (userDataA == player && userDataB == ground)
                        || (userDataA == ground && userDataB == player);
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        backgroundX -= backgroundSpeed * delta;

        if (backgroundX <= -SCREEN_WIDTH) {
            backgroundX = 0;
        }

        ground.update(delta);

        world.step(
                Math.min(delta, 1 / 30f),
                6,
                2
        );

        player.update(delta);

        batch.begin();

        batch.draw(
                backgroundTexture,
                backgroundX,
                0,
                SCREEN_WIDTH,
                SCREEN_HEIGHT
        );

        batch.draw(
                backgroundTexture,
                backgroundX + SCREEN_WIDTH,
                0,
                SCREEN_WIDTH,
                SCREEN_HEIGHT
        );


        ground.draw(batch);
        player.draw(batch);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (player != null) {
            player.dispose();
        }

        if (ground != null) {
            ground.dispose();
        }

        if (world != null) {
            world.dispose();
        }

        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }

        if (batch != null) {
            batch.dispose();
        }
    }
}
