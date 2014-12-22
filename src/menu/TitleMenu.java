package menu;

import server.Server;
import client.Client;
import graphics.Color;
import graphics.Font;
import graphics.Screen;

public class TitleMenu extends Menu {
	private int selected = 0;

	private static final String[] options = { "Create game","Start game", "How to play", "About" };

	public TitleMenu() {
	}

	public void tick() {
		if (input.up.clicked) selected--;
		if (input.down.clicked) selected++;

		int len = options.length;
		if (selected < 0) selected += len;
		if (selected >= len) selected -= len;

		if (input.attack.clicked || input.menu.clicked) {
			if (selected == 0) {
				game.setMenu(null);
				new Server();
			}
			if (selected == 1) {
				game.setMenu(null);
				new Client();
			}
			if (selected == 2) game.setMenu(new InstructionsMenu(this));
			if (selected == 3) game.setMenu(new AboutMenu(this));
		}
	}

	public void render(Screen screen) {
		screen.clear(0);

		int h = 2;
		int w = 13;
		int titleColor = Color.get(0, 010, 131, 551);
		int xo = (screen.w - w * 8) / 2;
		int yo = 24;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				//Отрисовка логотипа MINICRAFT
				// 6 b 32 - магические константы. Положение логотипа на image
				screen.render(xo + x * 8, yo + y * 8, x + (y + 6) * 32, titleColor, 0);
			}
		}

		for (int i = 0; i < 4; i++) {
			String msg = options[i];
			int col = Color.get(0, 222, 222, 222);
			if (i == selected) {
				msg = "> " + msg + " <";
				col = Color.get(0, 555, 555, 555);
			}
			//отрисовка текста меню
			Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, (8 + i) * 8, col);
		}
		
		//отрисовка нижней подписи на экране
		Font.draw("(Arrow keys,X and C)", screen, 0, screen.h - 8, Color.get(0, 111, 111, 111));
	}
}