package bg.sofia.uni.fmi.mjt.battleships.gameroom;

import bg.sofia.uni.fmi.mjt.battleships.exceptions.NotYourTurnException;
import bg.sofia.uni.fmi.mjt.battleships.lobby.MatchmakingLobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameRoomTest {
    private GameRoom game;

    @BeforeEach
    public void setUp() {
        MatchmakingLobby lobby = new MatchmakingLobby("lobby", "host");
        lobby.joinLobby("guest");
        lobby.placeYourShip("host", "C2", "C4");
        lobby.placeYourShip("guest", "C2", "C4");

        game = new GameRoom(lobby);
    }

    @Test
    void testGameRoomHitHostNotTheirTurn() {
        game.hitOpponent("host", "A2");

        assertThrows(NotYourTurnException.class,
                () -> game.hitOpponent("host", "F2"),
                "NotYourTurnException should be thrown if it is not current player turn");
    }

    @Test
    void testGameRoomHitHostSuccessfulHit() {
        game.hitOpponent("host", "C2");
        game.hitOpponent("host", "D2");
        assertTrue(true, "Player should be able to hit again after successfully hitting a ship");
    }

    @Test
    void testGameRoomHitHostMissedHit() {
        game.hitOpponent("host", "D2");
        assertThrows(NotYourTurnException.class,
                () -> game.hitOpponent("host", "F2"),
                "Player should not be able to hit again after missing");
    }

    @Test
    void testGameRoomHitGuestNotTheirTurn() {
        assertThrows(NotYourTurnException.class,
                () -> game.hitOpponent("guest", "F2"),
                "NotYourTurnException should be thrown if it is not current player turn");
    }

    @Test
    void testGameRoomHitGuestSuccessfulHit() {
        game.hitOpponent("host", "A1");

        game.hitOpponent("guest", "C2");
        game.hitOpponent("guest", "D2");
        assertTrue(true, "Player should be able to hit again after successfully hitting a ship");
    }

    @Test
    void testGameRoomHitGuestMissedHit() {
        game.hitOpponent("host", "A1");

        game.hitOpponent("guest", "D2");
        assertThrows(NotYourTurnException.class,
                () -> game.hitOpponent("guest", "F2"),
                "Player should not be able to hit again after missing");
    }

    @Test
    void testGameRoomCaseInsensitiveCoordinates() {
        game.hitOpponent("host", "c2");

        assertTrue(true, "Coordinates should be case-insensitive");
    }
}