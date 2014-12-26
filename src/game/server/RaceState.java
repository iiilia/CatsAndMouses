/*Состояние гонки:
 * WAITING  - ожидание
 * PRERACE  - подготовка к гонке
 * RACE     - гонка
 * POSTRACE - завершение гонки
 * */
package game.server;

public enum RaceState {
	WAITING, PRERACE, RACE, POSTRACE;
}
