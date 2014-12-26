/*
 * Загрузка карты из файла.
 */
package game.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import game.server.tracktiles.CheckPoint;
import game.server.tracktiles.Curve;
import game.server.tracktiles.FinishLine;
import game.server.tracktiles.Straight;
import game.server.tracktiles.TrackTile;

public class TrackLoader {														//Класс загрузчика
	public static Track loadTrack(String path) throws IOException {				//Функция загрузки карты из файла
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));					//Считываем файл
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		Direction startDir = null;												//Задаем начальное направление
		String line = reader.readLine();
		ArrayList<TrackTile> tiles = new ArrayList<>();							//Массив тайлов карты
		while (line != null) {
			String[] tokens = line.split(" ");									//Сплитим тайлы из файла
			if (tokens.length != 2) {
				reader.close();
				throw new RuntimeException(
						"Invalid track format - wrong number of tokens in line :"
								+ line);
			}
			Direction dir;														
			try {
				 dir = Direction.valueOf(tokens[1]);
			} catch (IllegalArgumentException e) {
				reader.close();
				throw new RuntimeException(e);
			}
			if(startDir == null) {
				startDir = dir;													//Сохраняем направление
			}
			
			if(tokens[0].equals("Straight")) {									//Добавляем тайлы по типам
				tiles.add(new Straight(dir));
			} else if(tokens[0].equals("Curve")) {
				tiles.add(new Curve(dir));
			} else if(tokens[0].equals("FinishLine")) {
				tiles.add(new FinishLine(dir));
			} else if(tokens[0].equals("CheckPoint")) {
				tiles.add(new CheckPoint(dir));
			}

			line = reader.readLine();
		}
		reader.close();
		return new Track(startDir, tiles);										//Возвращаем карту
	}
}
