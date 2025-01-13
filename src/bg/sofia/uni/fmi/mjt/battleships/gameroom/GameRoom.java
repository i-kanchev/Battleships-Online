package bg.sofia.uni.fmi.mjt.battleships.gameroom;

import bg.sofia.uni.fmi.mjt.battleships.board.Board;
import bg.sofia.uni.fmi.mjt.battleships.board.Point;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.NotYourTurnException;
import bg.sofia.uni.fmi.mjt.battleships.lobby.MatchmakingLobby;
import bg.sofia.uni.fmi.mjt.battleships.lobby.ShipType;

import java.util.concurrent.atomic.AtomicInteger;

public class GameRoom {
    private static final AtomicInteger COUNT = new AtomicInteger(0);
    private static final int INITIAL_SHIP_TILES = ShipType.CARRIER.getCount() * ShipType.CARRIER.getSize()
            + ShipType.BATTLESHIP.getCount() * ShipType.BATTLESHIP.getSize()
            + ShipType.CRUISER.getCount() * ShipType.CRUISER.getSize()
            + ShipType.DESTROYER.getCount() * ShipType.DESTROYER.getSize();
    private final String gameID;
    private final String player1;
    private final String player2;
    private boolean isPlayerOneTurn;
    private final Board boardPlayer1;
    private final Board boardPlayer2;
    private int shipTilesRemainingPlayer1;
    private int shipTilesRemainingPlayer2;
    private boolean isGameOver;

    public GameRoom(MatchmakingLobby data) {
        this.gameID = String.format("%06d", COUNT.incrementAndGet());
        this.player1 = data.getPlayer1();
        this.player2 = data.getPlayer2();
        this.isPlayerOneTurn = true;
        this.boardPlayer1 = new Board(data.getBoardPlayer1());
        this.boardPlayer2 = new Board(data.getBoardPlayer2());
        this.shipTilesRemainingPlayer1 = INITIAL_SHIP_TILES;
        this.shipTilesRemainingPlayer2 = INITIAL_SHIP_TILES;
    }

    public String getGameID() {
        return gameID;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public Board getYourBoard(String player) {
        if (player.equals(player1)) {
            return getBoardPlayer1();
        }

        return getBoardPlayer2();
    }

    public Board getOpponentBoard(String player) {
        if (player.equals(player1)) {
            return getBoardPlayer2();
        }

        return getBoardPlayer1();
    }

    public Board getBoardPlayer1() {
        return boardPlayer1;
    }

    public Board getBoardPlayer2() {
        return boardPlayer2;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public String getOpponent(String player) {
        if (player.equals(player1)) {
            return getPlayer2();
        }

        return getPlayer1();
    }

    public void hitOpponent(String player, String point) {
        if (player.equals(player1)) {
            hitBoardPlayer2(point);
        } else {
            hitBoardPlayer1(point);
        }
    }

    public String getWinner() {
        if (shipTilesRemainingPlayer1 == 0) {
            return player2;
        }
        return player1;
    }

    public boolean isCurrPlayerTurn(String player) {
        return (player1.equals(player) && isPlayerOneTurn) || player2.equals(player) && !isPlayerOneTurn;
    }

    private void hitBoardPlayer1(String point) {
        if (!isCurrPlayerTurn(player2)) {
            throw new NotYourTurnException("It is not your turn yet");
        }

        Point tile = convertStrToPoint(point);
        if (boardPlayer1.hit(tile)) {
            shipTilesRemainingPlayer1--;

            if (shipTilesRemainingPlayer1 == 0) {
                isGameOver = true;
            }
        }
        else {
            isPlayerOneTurn = !isPlayerOneTurn;
        }
    }

    private void hitBoardPlayer2(String point) {
        if (!isCurrPlayerTurn(player1)) {
            throw new NotYourTurnException("It is not your turn yet");
        }

        Point tile = convertStrToPoint(point);
        if (boardPlayer2.hit(tile)) {
            shipTilesRemainingPlayer2--;

            if (shipTilesRemainingPlayer2 == 0) {
                isGameOver = true;
            }
        }
        else {
            isPlayerOneTurn = !isPlayerOneTurn;
        }
    }

    private Point convertStrToPoint(String str) {
        str = str.toUpperCase();
        int x = str.charAt(0) - 'A';
        int y = Integer.parseInt(str.substring(1)) - 1;
        return new Point(x, y);
    }
}
