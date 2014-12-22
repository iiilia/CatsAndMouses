package menu;

import graphics.Color;
import graphics.Font;
import graphics.Screen;

public class AboutMenu extends Menu {
	private Menu parent;

	public AboutMenu(Menu parent) {
		this.parent = parent;
	}

	public void tick() {
		if (input.attack.clicked || input.menu.clicked) {
			game.setMenu(parent);
		}
	}

	public void render(Screen screen) {
		screen.clear(0);

		Font.draw("About Mouse race", screen, 2 * 8 + 4, 1 * 8, Color.get(0, 555, 555, 555));
		Font.draw("The game was made", screen, 0 * 8 + 4, 3 * 8, Color.get(0, 333, 333, 333));
		Font.draw("by K7-361 students", screen, 0 * 8 + 4, 4 * 8, Color.get(0, 333, 333, 333));
		Font.draw("To pass a test ", screen, 0 * 8 + 4, 5 * 8, Color.get(0, 333, 333, 333));
		Font.draw("in december 2014.", screen, 0 * 8 + 4, 6 * 8, Color.get(0, 333, 333, 333));
		
		Font.draw("Authors", screen, 2 * 8 + 4, 8 * 8, Color.get(0, 555, 555, 555));
		Font.draw("Kirichenko Tatyana", screen, 0 * 8 + 4, 10 * 8, Color.get(0, 333, 333, 333));
		Font.draw("Lubennikova An.", screen, 0 * 8 + 4, 11 * 8, Color.get(0, 333, 333, 333));
		Font.draw("Ponomarev Sergey", screen, 0 * 8 + 4, 12 * 8, Color.get(0, 333, 333, 333));
	}
}
