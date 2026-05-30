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
    private Texture skinsButton; // Добавлена точка с запятой
    private Music music;

    public ScreenMenu(MyGdxGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);
        batch = new SpriteBatch();

        background = new Texture("menu/backround.jpg");
        playButton = new Texture("menu/play.png");
        settingsButton = new Texture("menu/settings.png");
        shopButton = new Texture("menu/shop.png");
        exitButton = new Texture("menu/exit.png");
        skinsButton = new Texture("menu/skins1.png");

        music = Gdx.audio.newMusic(Gdx.files.internal("music/menu_music.mp3"));
        music.setLooping(true);
        music.setVolume(0.4f);
        music.play();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = 720 - Gdx.input.getY();

            // Координаты PLAY (Центр)
            if (touchX >= 440 && touchX <= 840 && touchY >= 230 && touchY <= 480) {
                if (music != null) music.stop();
                game.setScreen(new ScreenGame(game));
            }

            // Координаты SKINS (Слева сверху)
            if (touchX >= 100 && touchX <= 450 && touchY >= 450 && touchY <= 650) {
                System.out.println("SKINS");
            }

            // Координаты SHOP (Справа сверху)
            if (touchX >= 830 && touchX <= 1180 && touchY >= 450 && touchY <= 650) {
                System.out.println("SHOP");
            }

            // Координаты SETTINGS (Слева снизу)
            if (touchX >= 100 && touchX <= 400 && touchY >= 150 && touchY <= 250) {
                System.out.println("SETTINGS");
            }

            // Координаты EXIT (Справа снизу)
            if (touchX >= 880 && touchX <= 1180 && touchY >= 150 && touchY <= 250) {
                Gdx.app.exit();
            }
        }

        batch.begin();
        batch.draw(background, 0, 0, 1280, 720);

        // 1. SKINS (Слева сверху) - делаем крупнее как на картинке
        batch.draw(skinsButton, 100, 450, 350, 200);

        // 2. SHOP (Справа сверху)
        batch.draw(shopButton, 830, 450, 350, 200);

        // 3. PLAY (Центр) - Самая большая кнопка
        batch.draw(playButton, 440, 230, 400, 250);

        // 4. SETTINGS (Слева снизу)
        batch.draw(settingsButton, 100, 150, 300, 100);

        // 5. EXIT (Справа снизу)
        batch.draw(exitButton, 880, 150, 300, 100);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        playButton.dispose();
        settingsButton.dispose();
        shopButton.dispose();
        exitButton.dispose();
        skinsButton.dispose(); // Не забываем удалять текстуру скинов
        if (music != null) music.dispose();
    }
}