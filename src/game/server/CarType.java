package game.server;

public enum CarType {
	RED("Red"), YELLOW("Yellow"), BLUE("Blue"), GREEN("Green"), VIOLET("Violet");

	private String name;

	private CarType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static CarType getFromString(String name) {
		CarType[] val = CarType.values();
		for (CarType type : val) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		return null;
	}
}
