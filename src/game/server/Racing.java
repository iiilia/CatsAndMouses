package game.server;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import game.server.event.CheckPointEvent;
import game.server.event.CheckPointListener;
import game.server.event.DestroyedEvent;
import game.server.event.DestroyedListener;
import game.server.event.LapCompletedEvent;
import game.server.event.LapCompletedListener;
import game.server.event.MineDropEvent;
import game.server.event.MineDropListener;
import game.server.event.MissileFireEvent;
import game.server.event.MissileFireListener;
import game.server.gui.QueuedAction;
import game.server.gui.ControlCenter;
import game.server.tracktiles.CheckPoint;
import game.server.tracktiles.FinishLine;

public class Racing {

    public static final boolean                 debug           = true;
    public static final int                     fps             = 30;
    public static final int                     laps            = 4;
    public static final int                     pauseAfterRace  = 10;
    public static final int                     pauseBeforeRace = 5;

    private ControlCenter               		controlCenter;
    private int                                 curIndex;
    private Track                               currentTrack;
    private List<Player>                        finished;
    private List<String>                        maps;
    private List<Mine>                          mines;
    private List<Missile>                       missiles;
    private double                              pause;
    private Protocol                            protocol;
    private ConcurrentLinkedQueue<QueuedAction> queuedActions;
    private List<Player>                        racers;
    private boolean                             running;
    private double                              serverTime;
    private RaceState                           state;

    private Collection<GameObject>              toBeDestroyed;
    private ConcurrentLinkedQueue<GameObject>   newProjectiles;

    public Racing(List<String> maps) {
        running = false;
        pause = 0;
        this.maps = maps;
        curIndex = 0;
        currentTrack = null;
        if (maps.size() == 0) {
            throw new IllegalArgumentException("No map rotation!");
        }
        racers = new ArrayList<>();
        missiles = new ArrayList<>();
        mines = new ArrayList<>();
        state = RaceState.WAITING;
        serverTime = 0;
        toBeDestroyed = new ArrayList<>();
        finished = new ArrayList<>();
        queuedActions = new ConcurrentLinkedQueue<>();
        protocol = new Protocol(this);
        controlCenter = new ControlCenter();
        newProjectiles = new ConcurrentLinkedQueue<>();
        initializeGUI();
    }

    //Удаление игрока из игры
    public void dropPlayer(Player player) {
        boolean removed = racers.remove(player);
        if (!removed) {
            removed = finished.remove(player);
        }
        if (!removed) {
            return;
        }
        for (Missile missile : missiles) {
            if (missile.getShooter() == player) {
                missile.destroy();
            }
        }
        if (racers.isEmpty()) {
            setRaceState(RaceState.WAITING);
        }
    }

    //Получние текущей карты
    public Track getCurrentTrack() {
        return currentTrack;
    }

    //Список мин
    public List<Mine> getMines() {
        return mines;
    }

    //Список снарядов
    public List<Missile> getMissiles() {
        return missiles;
    }

    //Список игроков
    public List<Player> getRacers() {
        return racers;
    }

    //Состояние гонки
    public RaceState getState() {
        return state;
    }

    //Таймер сервера
    public double getTimer() {
        return serverTime;
    }

    //Добавить действие в очередь
    public void queueAction(QueuedAction action) {
        queuedActions.add(action);
    }

    //Учтановить состояние гонки
    public void setRaceState(RaceState raceState) {
        while (state != raceState && state != RaceState.WAITING) {
            advanceGameState();
        }
    }

    //Запуск игры
    public void start() {
        running = true;
        protocol.start();
        long timeA = System.nanoTime();
        long timeB = timeA;
        Protocol.log(this, "Game ready, now waiting for players.");
        while (running) {
            try {
                Thread.sleep((int) (1000 / ((double) fps) - (timeB - timeA) / 1e6));
            } catch (InterruptedException e) {
            } catch (IllegalArgumentException e) {
                Protocol.log(this, "Cannot keep up!");
            }
            while (!queuedActions.isEmpty()) {
                queuedActions.poll().perform();
            }
            protocol.update();
            timeB = System.nanoTime();
            double delta = (timeB - timeA) / 1e9;
            timeA = System.nanoTime();
            update(delta);
            timeB = System.nanoTime();
        }
    }

    public void startPreRace(List<Player> racers) {
        if (state != RaceState.WAITING) {
            return;
        }
        // Обновить состояние на PRERACE
        // Загрузить карту
        try {
            currentTrack = TrackLoader.loadTrack(maps.get(curIndex));
        } catch (IOException e) {
            stop();
            throw new RuntimeException(e);
        }
        curIndex = (curIndex + 1) % maps.size();
        if (racers.size() > 4 || racers.isEmpty()) {									//Проверка на количество игроков
            stop();
            throw new IllegalArgumentException("Trying to start race with " + racers.size() + " players!");
        }
        for (Player player : racers) {
            this.racers.add(player);													//Добавляем игроков
            player.addMineDropListener(new MineDropListener() {							//Добавляем обработчики событий

                @Override
                public void mineDropped(MineDropEvent event) {
                    mineDrop(event);
                }
            });
            player.addMissileFireListener(new MissileFireListener() {

                @Override
                public void missileFired(MissileFireEvent e) {
                    missileFire(e);
                }
            });
            player.addCheckPointListener(new CheckPointListener() {

                @Override
                public void checkPoint(CheckPointEvent e) {
                    checkPointReached(e);
                }
            });
            player.addLapCompletedListener(new LapCompletedListener() {

                @Override
                public void lapCompleted(LapCompletedEvent e) {
                    lapComplete(e);
                }
            });
        }
        pause = pauseBeforeRace;
        state = RaceState.PRERACE;
        protocol.sendGameStart();
       Protocol.log(this, "Prerace wait started, now waiting for " + pauseBeforeRace + " seconds.");
    }

    public void stop() {
        running = false;
        protocol.stop();
    }

    protected void updateGUI() {																//Отрисовка GUI сервера
        if (state == RaceState.WAITING) {
            controlCenter.getRaceStateButton().setEnabled(false);
        } else {
            controlCenter.getRaceStateButton().setEnabled(true);
        }
        if (state == RaceState.RACE) {
            controlCenter.getAbortButton().setEnabled(true);
        } else {
            controlCenter.getAbortButton().setEnabled(false);
        }
        controlCenter.getRaceStateButton().setText(state.toString());
        Vector<Map.Entry<Connection, Player>> data = new Vector<>(protocol.getConnectionEntries());
        controlCenter.getConnections().setListData(data);
        controlCenter.getFrame().repaint();
    }

    private void advanceGameState() {															//Изменение состояние игры
        switch (state) {
        case PRERACE:
            startRace();
            break;
        case RACE:
            finishRace();
            break;
        case POSTRACE:
            cleanup();
        default:
            break;
        }
        updateGUI();
    }

    private void checkPointReached(CheckPointEvent event) {										//Обработка прохождения чекпоинтов
        if (!(event.getSource() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getSource();
        if (player.getNumberOfCheckPoints() == currentTrack.getCheckpoints().size()
                && event.getCheckPoint() instanceof FinishLine) {
            player.completeLap();
        }
    }

    private void cleanup() {
        // set to WAITING
        clearFinished();
        state = RaceState.WAITING;
        Protocol.log(this, "Cleaning up after race, now waiting for players.");
    }

    private void clearFinished() {
        for (Player player : finished) {
            player.clear();
        }
        finished.clear();
    }

    private boolean collide(Collides a, Collides b) {
        Area intersect = a.getArea();
        intersect.intersect(b.getArea());
        return !intersect.isEmpty();
    }

    private void collisions() {
        for (Missile missile : missiles) {
            for (Player player : racers) {
                if (player == missile.getShooter() || player.getCar() == null) {
                    continue;
                }
                if (collide(missile, player.getCar())) {
                    missile.collideWith(player.getCar());
                    protocol.sendMissileHit(missile, player);
                }
            }
        }
        for (Mine mine : mines) {
            for (Player player : racers) {
                if (player.getCar() != null && collide(mine, player.getCar())) {
                    mine.collideWith(player.getCar());
                    protocol.sendMineHit(mine, player);
                }
            }
        }
        for (int i = 0; i < racers.size(); i++) {
            Player player = racers.get(i);
            if (player.getCar() == null) {
                continue;
            }
            if (collide(player.getCar(), currentTrack) ||
                    !player.getCar().getArea().intersects(currentTrack.getArea().getBounds2D())) {
                player.getCar().collideWith(currentTrack);
            }
            for (CheckPoint checkpoint : currentTrack.getCheckpoints()) {
                if (collide(player.getCar(), checkpoint)) {
                    player.getCar().collideWith(checkpoint);
                }
            }
            for (int j = i + 1; j < racers.size(); j++) {
                Player player2 = racers.get(i);
                if (player2.getCar() != null && collide(player.getCar(), player2.getCar())) {
                    Car.collide(player.getCar(), player2.getCar());
                }
            }
        }
    }

    private void destroy(DestroyedEvent e) {
        if (e.getSource() instanceof GameObject) {
            toBeDestroyed.add((GameObject) e.getSource());
        }
        if (e.getSource() instanceof Car) {
            protocol.sendDestroyed((Car) e.getSource());
        }
    }

    private void finishRace() {
        // set to POSTRACE
        pause = pauseAfterRace;
        // убрать всех игроков в случае, если гонка завершилась
        for (Player player : racers) {
            finished.add(player);
        }
        for (Player player : finished) {
            player.setCar(null);
        }
        currentTrack = null;
        racers.clear();
        missiles.clear();
        mines.clear();
        newProjectiles.clear();
        state = RaceState.POSTRACE;
        protocol.sendRaceOver(finished);
        Protocol.log(this, "Race finished, placings were " + finished.toString() +
                " now waiting for " + pauseAfterRace + " seconds.");
    }

    private void initializeGUI() {
        controlCenter.getFrame().addWindowListener(new QueuedAction(this) {
            @Override
            public void perform() {
                controlCenter.close();
                stop();
            }
        });
        controlCenter.getAbortButton().addActionListener(new QueuedAction(this) {
            @Override
            public void perform() {
                setRaceState(RaceState.WAITING);
            }
        });
        controlCenter.getKickButton().addActionListener(new QueuedAction(this) {
            @Override
            public void perform() {
                Map.Entry<Connection, Player> selectedEntry;
                selectedEntry = controlCenter.getConnections().getSelectedValue();
                if (selectedEntry == null) {
                    return;
                }
                protocol.connectionLost(selectedEntry.getKey());
                selectedEntry.getKey().close();
            }
        });
        controlCenter.getQuitButton().addActionListener(new QueuedAction(this) {
            @Override
            public void perform() {
                controlCenter.close();
                stop();
            }
        });
        controlCenter.getRaceStateButton().addActionListener(new QueuedAction(this) {
            @Override
            public void perform() {
                advanceGameState();
            }
        });
        updateGUI();
    }

    private void lapComplete(LapCompletedEvent e) {
        if (!(e.getSource() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getSource();
        protocol.sendLapComplete(player);
        Protocol.log(this, "Player " + player.getName() +
                " has completed lap " + player.getCompletedLaps());
    }

    private void mineDrop(MineDropEvent event) {
        event.getMine().addDestroyedListener(new DestroyedListener() {

            @Override
            public void destroyed(DestroyedEvent e) {
                destroy(e);
            }
        });
        newProjectiles.add(event.getMine());
    }

    private void missileFire(MissileFireEvent event) {
        event.getMissile().addDestroyedListener(new DestroyedListener() {

            @Override
            public void destroyed(DestroyedEvent e) {
                destroy(e);
            }
        });
        newProjectiles.add(event.getMissile());
    }

    private void spawn(Player player) {
        Point2D spawnloc = player.getLastCheckPoint().getSpawnLocation();
        spawn(player, spawnloc, player.getLastCheckPoint().getOrientation());
    }

    private void spawn(Player player, Point2D location, Direction orientation) {
        Car car = new Car(location, orientation);
        player.setCar(car);
        car.addDestroyedListener(new DestroyedListener() {

            @Override
            public void destroyed(DestroyedEvent e) {
                destroy(e);
            }
        });
    }

    private void startRace() {
        // set to RACE
        serverTime = 0;
        for (int i = 0; i < racers.size(); i++) {
            spawn(racers.get(i), currentTrack.getFinishLine().getStartLocation(i + 1), currentTrack.getStartDirection());
        }
        newProjectiles.clear();
        state = RaceState.RACE;
        Protocol.log(this, "The stage is set, the green flag drops!");
    }

    private void update(double delta) {
        if (state == RaceState.PRERACE || state == RaceState.POSTRACE) {
            pause -= delta;
            if (pause <= 0) {
                advanceGameState();
            }
        } else if (state == RaceState.RACE) {
            serverTime += delta;
            for (Player player : racers) {
                if (player.getCar() == null) {
                    spawn(player);
                }
                player.getCar().update(delta);
            }
            for (Missile missile : missiles) {
                missile.update(delta);
            }
            collisions();
            protocol.sendGameState();
            Iterator<Player> racerIterator = racers.iterator();
            while (racerIterator.hasNext()) {
                Player player = racerIterator.next();
                if (player.getCompletedLaps() == laps) {
                    racerIterator.remove();
                    finished.add(player);
                }
            }
            if (racers.isEmpty()) {
                advanceGameState();
            }
            while (!newProjectiles.isEmpty()) {
                GameObject go = newProjectiles.poll();
                if (go instanceof Missile) {
                    missiles.add((Missile) go);
                } else if (go instanceof Mine) {
                    mines.add((Mine) go);
                }
            }
            for (GameObject go : toBeDestroyed) {
                missiles.remove(go);
                mines.remove(go);
            }
        }
    }

    public static void main(String[] args) {
        ArrayList<String> tracks = new ArrayList<>();
        tracks.add("res/basictrack");
        Racing racing = new Racing(tracks);
        racing.start();
    }
}
