package bg.sofia.uni.fmi.mjt.battleships.lobby;

import bg.sofia.uni.fmi.mjt.battleships.exceptions.AllOfAShipTypeAlreadyPlacedException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.ShipInvalidSizeException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.TilesNotConnectedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchmakingLobbyTest {
    private MatchmakingLobby lobby;

    @BeforeEach
    public void setUp() {
        lobby = new MatchmakingLobby("lobby", "host");
        lobby.joinLobby("guest");
    }

    @Test
    void testMatchmakingLobbyIsFull() {
        assertTrue(lobby.isFull());
    }

    @Test
    void testMatchmakingLobbyIsNotTrue() {
        MatchmakingLobby test = new MatchmakingLobby("lobby", "host");
        assertFalse(test.isFull());
    }

    @Test
    void testMatchmakingLobbyTilesNotConnected() {
        assertThrows(TilesNotConnectedException.class,
                () -> lobby.placeYourShip("host", "B2", "C4"),
                "TilesNotConnectedException should be thrown if tiles are not in the same row or column");
        assertThrows(TilesNotConnectedException.class,
                () -> lobby.removeYourShip("host", "B2", "C4"),
                "TilesNotConnectedException should be thrown if tiles are not in the same row or column");
    }

    @Test
    void testMatchmakingLobbyHostPlaceShipCorrect() {
        lobby.placeYourShip("host", "B2", "B4");
        assertTrue(true, "Placing a ship should work properly");
    }

    @Test
    void testMatchmakingLobbyGuestPlaceShipCorrect() {
        lobby.placeYourShip("guest", "B2", "B4");
        assertTrue(true, "Placing a ship should work properly");
    }

    @Test
    void testMatchmakingLobbyHostPlaceShipIncorrectSize() {
        assertThrows(ShipInvalidSizeException.class,
                () -> lobby.placeYourShip("host", "B2", "B2"),
                "ShipInvalidSizeException should be thrown if ship's size is not between 2 and 5");
        assertThrows(ShipInvalidSizeException.class,
                () -> lobby.placeYourShip("host", "B2", "B7"),
                "ShipInvalidSizeException should be thrown if ship's size is not between 2 and 5");
    }

    @Test
    void testMatchmakingLobbyGuestPlaceShipIncorrectSize() {
        assertThrows(ShipInvalidSizeException.class,
                () -> lobby.placeYourShip("guest", "B2", "B2"),
                "ShipInvalidSizeException should be thrown if ship's size is not between 2 and 5");
        assertThrows(ShipInvalidSizeException.class,
                () -> lobby.placeYourShip("guest", "B2", "B7"),
                "ShipInvalidSizeException should be thrown if ship's size is not between 2 and 5");
    }

    @Test
    void testMatchmakingLobbyHostPlaceShipNoMoreOfType() {
        lobby.placeYourShip("host", "B2", "B6");
        assertThrows(AllOfAShipTypeAlreadyPlacedException.class,
                () -> lobby.placeYourShip("host", "D2", "D6"),
                "AllOfAShipTypeAlreadyPlacedException should be thrown if all ships of that type are used");
    }

    @Test
    void testMatchmakingLobbyGuestPlaceShipNoMoreOfType() {
        lobby.placeYourShip("guest", "B2", "B6");
        assertThrows(AllOfAShipTypeAlreadyPlacedException.class,
                () -> lobby.placeYourShip("guest", "D2", "D6"),
                "AllOfAShipTypeAlreadyPlacedException should be thrown if all ships of that type are used");
    }

    @Test
    void testMatchmakingLobbyHostRemoveShipCorrect() {
        lobby.placeYourShip("host", "B2", "B4");
        lobby.removeYourShip("host", "B2", "B4");
        lobby.placeYourShip("host", "B2", "B4");
        assertTrue(true, "Removing a ship should leave the tiles empty");
    }

    @Test
    void testMatchmakingLobbyGuestRemoveShipCorrect() {
        lobby.placeYourShip("guest", "B2", "B4");
        lobby.removeYourShip("guest", "B2", "B4");
        lobby.placeYourShip("guest", "B2", "B4");
        assertTrue(true, "Removing a ship should leave the tiles empty");
    }

    @Test
    void testMatchmakingLobbyHostResetBoardCorrect() {
        lobby.placeYourShip("host", "A2", "A4");
        lobby.placeYourShip("host", "C1", "C4");
        lobby.placeYourShip("host", "E3", "E5");

        lobby.resetYourBoard("host");

        lobby.placeYourShip("host", "A3", "E3");

        assertTrue(true, "Resetting the board should make all tiles empty");
    }

    @Test
    void testMatchmakingLobbyGuestResetBoardCorrect() {
        lobby.placeYourShip("guest", "A2", "A4");
        lobby.placeYourShip("guest", "C1", "C4");
        lobby.placeYourShip("guest", "E3", "E5");

        lobby.resetYourBoard("guest");

        lobby.placeYourShip("guest", "A3", "E3");

        assertTrue(true, "Resetting the board should make all tiles empty");
    }

    @Test
    void testMatchmakingLobbyCaseInsensitiveCoordinates() {
        lobby.placeYourShip("host", "a2", "A4");
        lobby.removeYourShip("host", "A2", "a4");

        assertTrue(true, "Coordinates should be case-insensitive");
    }
}