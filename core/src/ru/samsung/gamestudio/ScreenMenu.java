package ru.samsung.gamestudio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class ScreenMenu implements Screen {

    private MyGdxGame game;

    private OrthographicCamera camera;

    private SpriteBatch batch;

    private Texture background;

    private Texture playButton;

    private Texture settingsButton;

    private Texture shopButton;

    private Texture exitButton;

    private Music music;

    public ScreenMenu(MyGdxGame game) {

        this.game = game;
    }

    @Override
    public void show() {

        camera = new OrthographicCamera();

        camera.setToOrtho(false, 1280, 720);

        batch = new SpriteBatch();

        background = new Texture("menu/backround.png");

        playButton = new Texture("menu/play.png");

        settingsButton = new Texture("menu/settings.png");

        shopButton = new Texture("menu/shop.png");

        exitButton = new Texture("menu/exit.png");

        /*
        music = Gdx.audio.newMusic(
                Gdx.files.internal("music/menu_music.mp3")
        );

        music.setLooping(true);

        music.setVolume(0.4f);

        music.play();
        */
    }

    @Override
    public void render(float delta) {

        ScreenUtils.clear(0, 0, 0, 1);

        camera.update();

        batch.setProjectionMatrix(camera.combined);

        if (Gdx.input.justTouched()) {

            float touchX = Gdx.input.getX();

            float touchY = 720 - Gdx.input.getY();

            // PLAY
            if (touchX >= 500 && touchX <= 780 &&
                    touchY >= 400 && touchY <= 480) {

                if (music != null) music.stop();

                game.setScreen(new ScreenGame(game));
            }

            // SETTINGS
            if (touchX >= 500 && touchX <= 780 &&
                    touchY >= 300 && touchY <= 380) {

                System.out.println("SETTINGS");
            }

            // SHOP
            if (touchX >= 500 && touchX <= 780 &&
                    touchY >= 200 && touchY <= 280) {

                System.out.println("SHOP");
            }

            // EXIT
            if (touchX >= 500 && touchX <= 780 &&
                    touchY >= 100 && touchY <= 180) {

                Gdx.app.exit();
            }
        }

        batch.begin();

        batch.draw(background, 0, 0, 1280, 720);

        batch.draw(playButton, 500, 400, 280, 80);

        batch.draw(settingsButton, 500, 300, 280, 80);

        batch.draw(shopButton, 500, 200, 280, 80);

        batch.draw(exitButton, 500, 100, 280, 80);

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

        background.dispose();

        playButton.dispose();

        settingsButton.dispose();

        shopButton.dispose();

        exitButton.dispose();

        if (music != null) music.dispose();
    }
}
