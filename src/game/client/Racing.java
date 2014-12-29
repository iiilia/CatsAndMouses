package game.client;

import game.server.Car;
import game.server.Direction;
import game.client.ClientTrack;
import game.server.tracktiles.Curve;
import game.server.tracktiles.FinishLine;
import game.server.tracktiles.Straight;
import game.server.tracktiles.TrackTile;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Area;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Racing extends JPanel implements KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 99363042925207606L;
	public static String host = "127.0.0.1";
	public static int port = 1993;
	private boolean running;
	private Connection connection;
	private ClientTrack track;
	private Car car;
	
	public Racing() {
		running = false;
		String name = "Sergey";
		String car = "Yellow";
		Client client = new Client(name, car);
		Socket socket;
		try {
			socket = new Socket(host, port);
		} catch (IOException e) {
			return;
		}
		try {
			this.connection = new Connection(socket, client);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		new Thread(this.connection).start();
		while (this.connection.hasInput()) {
			this.parseInput(this.connection, this.connection.nextInput());
		}
	}

	public static void main(String[] args) {
		
		// debug
		
/*		String name = "Sergey";
		String car = "Yellow";*/
		
		//debug end
		
		Racing race = new Racing();
/*		Client client = new Client(name, car);
		Socket socket;
		try {
			socket = new Socket(host, port);
		} catch (IOException e) {
			return;
		}
		try {
			race.connection = new Connection(socket, client);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		new Thread(race.connection).start();*/
/*		while (race.connection.hasInput()) {
			race.parseInput(race.connection, race.connection.nextInput());
		}*/
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowShit(race);
			}
		});
	}
	
    public void parseInput(Connection connection, String message) {
        JSONObject object;
        try {
            object = new JSONObject(message);
        } catch (JSONException e) {
            connection.send(e.getMessage());
            return;
        }
        String msg;
        try {
            msg = object.getString("message");
        } catch (JSONException e) {
            connection.send(e.getMessage());
            return;
        }
        if (msg == null) {
            connection.send("Invalid message: " + null);
            return;
        }
        if (msg.equals("connect")) {
        	System.out.println("Connection to host was successeful");
        }
        if (msg.equals("gamestart")) {
        	createTrack(object);
        }
        parseAction(msg);
/*        if (msg.equals("action")) {
            parseAction();
            return;
        } else if (msg.equals("request")) {
//            parseRequest(connection, object);
        } else {
            connection.send("Unknown message: " + message);
        }*/
    }
    
    public void createTrack(JSONObject object) {
    	JSONObject JSONtrack = object.getJSONObject("track");
    	JSONArray JSONtiles = JSONtrack.getJSONArray("tiles");
    	Direction startDir = Direction.valueOf(JSONtrack.getString("startdir"));
    	Direction curDir = startDir;
    	JSONArray JSONtile = null;
    	ArrayList<TrackTile> tiles = new ArrayList<>();
    	for (int i = 0; i < JSONtiles.length(); i++) {
    		JSONtile = JSONtiles.getJSONArray(i);
    		System.out.println(JSONtile.getString(0));
    		if (JSONtile.getString(0).equals("finish"))
    			tiles.add(new FinishLine(startDir));
    		if (JSONtile.getString(0).equals("straight"))
    			tiles.add(new Straight(curDir));
    		if (JSONtile.getString(0).equals("turnleft")) {													//Magic
    			switch (curDir) {
    			case LEFT: tiles.add(new Curve(Direction.UP));
    			break;
    			case UP: tiles.add(new Curve(Direction.RIGHT));
    			break;
    			case RIGHT: tiles.add(new Curve(Direction.DOWN));
    			break;
    			case DOWN: tiles.add(new Curve(Direction.UP));
    			break;
    			}
    			curDir = curDir.cclockwise();
    		}
    		if (JSONtile.getString(0).equals("turnright")) {
    			switch (curDir) {
    			case LEFT: tiles.add(new Curve(Direction.LEFT));
    			break;
    			case UP: tiles.add(new Curve(Direction.UP));
    			break;
    			case RIGHT: tiles.add(new Curve(Direction.RIGHT));
    			break;
    			case DOWN: tiles.add(new Curve(Direction.DOWN));
    			break;
    			}
    			curDir = curDir.clockwise();
    		}
    	}
    	this.track = new ClientTrack(startDir, tiles);
    	Area finish = this.track.getFinishLine().getArea();
		Point start = new Point(finish.getBounds().x + 90,
				finish.getBounds().y + 113);
		car = new Car(start, this.track.getStartDirection());
    }
    
    public static void parseAction(String msg) {
    	System.out.println(msg);
    }
    
	public void run() {
		running = true;
		long timeA = System.nanoTime();
		long timeB = timeA;
		while (running) {
			try {
				Thread.sleep((int) (1000 / 30.0 - (timeB - timeA) / 1e6));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timeB = System.nanoTime();
			double delta = (timeB - timeA) / 1e9;
			timeA = System.nanoTime();
			update(delta);
			repaint();
		}
	}
	
	protected void update(double delta) {
		car.update(delta);
		Area intersection = car.getArea();
		intersection.intersect(track.getNegative());
		if (!intersection.isEmpty()) {
			car.collideWith(track);
		}
	}
	
	public void stop() {
		running = false;
	}

	public static void createAndShowShit(Racing t) {
		JFrame frame = new JFrame(t.getClass().getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(t);
		frame.pack();
		frame.addKeyListener(t);
		frame.setVisible(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (track == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		if (track == null) {
			
		} else {
			drawSome(g2d);
		}
		// I don't wanna do math! you can't make me!
		// g2d.transform(AffineTransform
		// .getTranslateInstance(diagonal / 2 - 50, 0));
		// g2d.transform(AffineTransform.getScaleInstance(1, .25));
		// g2d.transform(AffineTransform.getRotateInstance(.25 * Math.PI));
	}
	
	public void drawSome(Graphics2D g2d) {
		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.scale(.5, .5);
		g2d.setColor(Color.white);
		g2d.fill(track.getTrackArea());
		g2d.setColor(Color.gray);
		g2d.fill(car.getArea());
	}

	private boolean up = false;
	private boolean right = false;
	private boolean left = false;
	private boolean down = false;

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP && !up) {
			car.setAccelerating(1);
			up = true;
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN && !down) {
			car.setAccelerating(-1);
			down = true;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT && !left) {
			car.setTurning(Direction.LEFT);
			left = true;
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT && !right) {
			car.setTurning(Direction.RIGHT);
			right = true;
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			stop();
			System.exit(0);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			if (down) {
				car.setAccelerating(-1);
			} else {
				car.setAccelerating(0);
			}
			up = false;
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			if (up) {
				car.setAccelerating(1);
			} else {
				car.setAccelerating(0);
			}
			down = false;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			if (right) {
				car.setTurning(Direction.RIGHT);
			} else {
				car.stopTurning();
			}
			left = false;
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			if (left) {
				car.setTurning(Direction.LEFT);
			} else {
				car.stopTurning();
			}
			right = false;
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	protected ClientTrack getTrack() {
		return track;
	}

	protected Car getCar() {
		return car;
	}
}
