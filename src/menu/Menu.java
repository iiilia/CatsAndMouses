package menu;


import game.Game;
import game.InputHandler;
import graphics.Screen;

public class Menu {
	protected Game game;
	protected InputHandler input;

	public void init(Game game, InputHandler input) {
		this.input = input;
		this.game = game;
	}

	public void tick() {
	}

	public void render(Screen screen) {
	}

}
