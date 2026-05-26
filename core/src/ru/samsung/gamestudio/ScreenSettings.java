package ru.samsung.gamestudio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class ScreenSettings implements Screen {

    private MyGdxGame game;

    private OrthographicCamera camera;

    private SpriteBatch batch;

    private BitmapFont font;

    private Texture background;

    boolean musicEnabled = true;

    boolean soundEnabled = true;

    boolean fullscreen = false;

    public ScreenSettings(MyGdxGame game) {

        this.game = game;
    }

    @Override
    public void show() {

        camera = new OrthographicCamera();

        camera.setToOrtho(false, 1280, 720);

        batch = new SpriteBatch();

        font = new BitmapFont();

        background = new Texture("menu/background.png");
    }

    @Override
    public void render(float delta) {

        ScreenUtils.clear(0, 0, 0, 1);

        camera.update();

        batch.setProjectionMatrix(camera.combined);

        if (Gdx.input.justTouched()) {

            float x = Gdx.input.getX();

            float y = 720 - Gdx.input.getY();

            // MUSIC
            if (x >= 450 && x <= 850 &&
                    y >= 500 && y <= 560) {

                musicEnabled = !musicEnabled;
            }

            // SOUND
            if (x >= 450 && x <= 850 &&
                    y >= 400 && y <= 460) {

                soundEnabled = !soundEnabled;
            }

            // FULLSCREEN
            if (x >= 450 && x <= 850 &&
                    y >= 300 && y <= 360) {

                fullscreen = !fullscreen;

                if (fullscreen) {

                    Gdx.graphics.setFullscreenMode(
                            Gdx.graphics.getDisplayMode()
                    );

                } else {

                    Gdx.graphics.setWindowedMode(
                            1280,
                            720
                    );
                }
            }

            // BACK
            if (x >= 450 && x <= 850 &&
                    y >= 150 && y <= 220) {

                game.setScreen(new ScreenMenu(game));
            }
        }

        batch.begin();

        batch.draw(background, 0, 0, 1280, 720);

        font.draw(batch,
                "SETTINGS",
                520,
                650);

        font.draw(batch,
                "MUSIC: " + (musicEnabled ? "ON" : "OFF"),
                450,
                550);

        font.draw(batch,
                "SOUND: " + (soundEnabled ? "ON" : "OFF"),
                450,
                450);

        font.draw(batch,
                "FULLSCREEN: " + (fullscreen ? "ON" : "OFF"),
                450,
                350);

        font.draw(batch,
                "BACK",
                450,
                200);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
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

        batch.dispose();

        font.dispose();

        background.dispose();
    }
}