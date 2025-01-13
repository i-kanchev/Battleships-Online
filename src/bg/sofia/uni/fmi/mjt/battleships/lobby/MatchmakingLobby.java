package bg.sofia.uni.fmi.mjt.battleships.lobby;

import bg.sofia.uni.fmi.mjt.battleships.board.Board;
import bg.sofia.uni.fmi.mjt.battleships.board.Point;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.*;

import java.util.ArrayList;
import java.util.List;

public class MatchmakingLobby {
    private static final int CAPACITY = 2;
    private final String lobbyName;
    private final String player1;
    private String player2;
    private int currPlayers;
    private Board boardPlayer1;
    private Board boardPlayer2;
    private final List<Integer> remainingShipsPlayer1;
    private final List<Integer> remainingShipsPlayer2;
    private boolean isReadyPlayer1;
    private boolean isReadyPlayer2;

    public MatchmakingLobby(String lobbyName, String host) {
        this.lobbyName = lobbyName;
        this.player1 = host;
        this.currPlayers = 1;
        this.remainingShipsPlayer1 = new ArrayList<>();
        this.remainingShipsPlayer2 = new ArrayList<>();
        resetBoardPlayer1();
        resetBoardPlayer2();
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public Board getBoardPlayer1() {
        return boardPlayer1;
    }

    public Board getBoardPlayer2() {
        return boardPlayer2;
    }

    public int getCurrPlayers() {
        return currPlayers;
    }

    public int getCapacity() {
        return CAPACITY;
    }

    public void joinLobby(String player) {
        player2 = player;
        currPlayers++;
    }

    public boolean isFull() {
        return currPlayers == CAPACITY;
    }

    public Board getYourBoard(String player) {
        if (player.equals(player1)) {
            return getBoardPlayer1();
        }

        return getBoardPlayer2();
    }

    public List<Integer> getYourRemainingShips(String player) {
        if (player.equals(player1)) {
            return remainingShipsPlayer1;
        }

        return remainingShipsPlayer2;
    }

    public void placeYourShip(String player, String point1, String point2) {
        if (player.equals(player1)) {
            placeShipPlayer1(point1, point2);
        }
        else {
            placeShipPlayer2(point1, point2);
        }
    }

    public void removeYourShip(String player, String point1, String point2) {
        if (player.equals(player1)) {
            removeShipPlayer1(point1, point2);
        }
        else {
            removeShipPlayer2(point1, point2);
        }
    }

    public void resetYourBoard(String player) {
        if (player.equals(player1)) {
            resetBoardPlayer1();
        }
        else {
            resetBoardPlayer2();
        }
    }

    public void readyPlayer(String player) {
        if (player.equals(player1)) {
            readyPlayer1();
        }
        else {
            readyPlayer2();
        }
    }

    public void unreadyPlayer(String player) {
        if (player.equals(player1)) {
            unreadyPlayer1();
        }
        else {
            unreadyPlayer2();
        }
    }

    public boolean bothPlayersReady() {
        return isReadyPlayer1 && isReadyPlayer2;
    }

    private void placeShipPlayer1(String point1, String point2) {
        Point from = convertStrToPoint(point1);
        Point to = convertStrToPoint(point2);
        int distance = findDistance(from, to);

        if (distance < ShipType.DESTROYER.getSize() || distance > ShipType.CARRIER.getSize()) {
            throw new ShipInvalidSizeException("Ship size should be between 2 and 5 tiles");
        }
        int arrPosition = distance - ShipType.DESTROYER.getSize();

        if (this.remainingShipsPlayer1.get(arrPosition) == 0) {
            throw new AllOfAShipTypeAlreadyPlacedException("All ships of that type are already placed");
        }

        this.boardPlayer1.placeShip(from, to);
        this.remainingShipsPlayer1.set(arrPosition, this.remainingShipsPlayer1.get(arrPosition) - 1);
    }

    private void placeShipPlayer2(String point1, String point2) {
        Point from = convertStrToPoint(point1);
        Point to = convertStrToPoint(point2);
        int distance = findDistance(from, to);

        if (distance < ShipType.DESTROYER.getSize() || distance > ShipType.CARRIER.getSize()) {
            throw new ShipInvalidSizeException("Ship size should be between 2 and 5 tiles");
        }
        int arrPosition = distance - ShipType.DESTROYER.getSize();

        if (this.remainingShipsPlayer2.get(arrPosition) == 0) {
            throw new AllOfAShipTypeAlreadyPlacedException("All ships of that type are already placed");
        }

        this.boardPlayer2.placeShip(from, to);
        this.remainingShipsPlayer2.set(arrPosition, this.remainingShipsPlayer2.get(arrPosition) - 1);
    }

    private void removeShipPlayer1(String point1, String point2) {
        Point from = convertStrToPoint(point1);
        Point to = convertStrToPoint(point2);

        int distance = findDistance(from, to);
        int arrPosition = distance - ShipType.DESTROYER.getSize();

        this.boardPlayer1.removeShip(from, to);
        this.remainingShipsPlayer1.set(arrPosition, this.remainingShipsPlayer1.get(arrPosition) + 1);
    }

    private void removeShipPlayer2(String point1, String point2) {
        Point from = convertStrToPoint(point1);
        Point to = convertStrToPoint(point2);

        int distance = findDistance(from, to);
        int arrPosition = distance - ShipType.DESTROYER.getSize();

        this.boardPlayer2.removeShip(from, to);
        this.remainingShipsPlayer2.set(arrPosition, this.remainingShipsPlayer2.get(arrPosition) + 1);
    }

    private void resetBoardPlayer1() {
        this.boardPlayer1 = new Board();
        resetRemainingShipsPlayer1();
    }

    private void resetBoardPlayer2() {
        this.boardPlayer2 = new Board();
        resetRemainingShipsPlayer2();
    }

    private void resetRemainingShipsPlayer1() {
        this.remainingShipsPlayer1.clear();
        this.remainingShipsPlayer1.add(ShipType.DESTROYER.getCount());
        this.remainingShipsPlayer1.add(ShipType.CRUISER.getCount());
        this.remainingShipsPlayer1.add(ShipType.BATTLESHIP.getCount());
        this.remainingShipsPlayer1.add(ShipType.CARRIER.getCount());
    }

    private void resetRemainingShipsPlayer2() {
        this.remainingShipsPlayer2.clear();
        this.remainingShipsPlayer2.add(ShipType.DESTROYER.getCount());
        this.remainingShipsPlayer2.add(ShipType.CRUISER.getCount());
        this.remainingShipsPlayer2.add(ShipType.BATTLESHIP.getCount());
        this.remainingShipsPlayer2.add(ShipType.CARRIER.getCount());
    }

    private void readyPlayer1() {
        for (Integer remainingShip : remainingShipsPlayer1) {
            if (remainingShip != 0) {
                throw new NotAllShipsPlacedException("Cannot get ready unless all ships are placed");
            }
        }

        isReadyPlayer1 = true;
    }

    private void readyPlayer2() {
        for (Integer remainingShip : remainingShipsPlayer2) {
            if (remainingShip != 0) {
                throw new NotAllShipsPlacedException("Cannot get ready unless all ships are placed");
            }
        }

        isReadyPlayer2 = true;
    }

    private void unreadyPlayer1() {
        isReadyPlayer1 = false;
    }

    private void unreadyPlayer2() {
        isReadyPlayer2 = false;
    }

    private Point convertStrToPoint(String str) {
        str = str.toUpperCase();
        int x = str.charAt(0) - 'A';
        int y = Integer.parseInt(str.substring(1)) - 1;
        return new Point(x, y);
    }

    private boolean arePointsConnected(Point a, Point b) {
        return a.x() == b.x() || a.y() == b.y();
    }

    private int findDistance(Point a, Point b) {
        if (!arePointsConnected(a, b)) {
            throw new TilesNotConnectedException("The two points should be in the same row or column");
        }

        if (a.x() == b.x()) {
            return Math.max(a.y() - b.y(), b.y() - a.y()) + 1;
        }
        return Math.max(a.x() - b.x(), b.x() - a.x()) + 1;
    }
}
