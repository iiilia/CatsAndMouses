package game.client;

import game.server.Car;
import game.server.Direction;
import game.server.Track;
import game.server.tracktiles.TrackTile;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Area;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

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
	private Track track;
	private Car car;
	
	public Racing() {
		running = false;
	}

	public static void main(String[] args) {
		
		// debug
		
		String name = "Sergey";
		String car = "Yellow";
		
		//debug end
		
		Racing race = new Racing();
		Client client = new Client(name, car);
		Socket socket;
		try {
			socket = new Socket(host, port);
		} catch (IOException e) {
			return;
		}
		Connection connection;
		try {
			connection = new Connection(socket, client);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		new Thread(connection).start();
		while (connection.hasInput()) {
			parseInput(connection, connection.nextInput());
		}
	}
	
    public static void parseInput(Connection connection, String message) {
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
    
    public static void createTrack(JSONObject object) {
    	JSONObject track = object.getJSONObject("track");
    	JSONArray JSONtiles = track.getJSONArray("tiles");
    	JSONArray JSONtile = null;
    	ArrayList<TrackTile> tiles = new ArrayList<>();
    	for (int i = 0; i < JSONtiles.length(); i++) {
    		JSONtile = JSONtiles.getJSONArray(i);
/*    		if (JSONtile.get(0).toString() == "straight")
    			tiles.add()*/
    	}
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
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		// I don't wanna do math! you can't make me!
		// g2d.transform(AffineTransform
		// .getTranslateInstance(diagonal / 2 - 50, 0));
		// g2d.transform(AffineTransform.getScaleInstance(1, .25));
		// g2d.transform(AffineTransform.getRotateInstance(.25 * Math.PI));
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

	protected Track getTrack() {
		return track;
	}

	protected Car getCar() {
		return car;
	}
}
