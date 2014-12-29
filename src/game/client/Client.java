package game.client;

import game.server.CarType;

public class Client {
	public String name;
	public CarType carType;
	
	public Client(String name, String carType) {
		this.name = name;
		this.carType = CarType.getFromString(carType);
	}
}
