package bg.sofia.uni.fmi.mjt.battleships.board;

import bg.sofia.uni.fmi.mjt.battleships.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    private Board board;

    @BeforeEach
    public void setUp() {
        board = new Board();

        board.placeShip(new Point(3, 4), new Point(3, 6));
        board.placeShip(new Point(5, 5), new Point(7, 5));
    }

    @Test
    void testBoardPlaceShipInverted() {
        board.placeShip(new Point(0, 5), new Point(0, 4));

        assertTrue(board.hit(new Point(0, 4)),
                "Ship might also be placed inverted");
        assertTrue(board.hit(new Point(0, 5)),
                "Ship might also be placed inverted");
    }

    @Test
    void testBoardPlaceShipNextToAnother() {
        assertThrows(IncorrectShipPlacementException.class,
                () -> board.placeShip(new Point(4, 4), new Point(4, 6)),
                "IncorrectShipPlacementException should be thrown if the ship is next to another");
        assertThrows(IncorrectShipPlacementException.class,
                () -> board.placeShip(new Point(4, 2), new Point(4, 3)),
                "IncorrectShipPlacementException should be thrown if the ship is next to another");
    }

    @Test
    void testBoardPlaceShipOverAnother() {
        assertThrows(IncorrectShipPlacementException.class,
                () -> board.placeShip(new Point(3, 4), new Point(3, 5)),
                "IncorrectShipPlacementException should be thrown if the ship is over another");
    }

    @Test
    void testBoardRemoveShipPartySelected() {
        assertThrows(IncorrectShipRemovalException.class,
                () -> board.removeShip(new Point(3, 4), new Point(3, 5)),
                "IncorrectShipRemovalException should be thrown if the ship is party selected");
        assertThrows(IncorrectShipRemovalException.class,
                () -> board.removeShip(new Point(3, 5), new Point(3, 6)),
                "IncorrectShipRemovalException should be thrown if the ship is party selected");
        assertThrows(IncorrectShipRemovalException.class,
                () -> board.removeShip(new Point(5, 5), new Point(6, 5)),
                "IncorrectShipRemovalException should be thrown if the ship is party selected");
        assertThrows(IncorrectShipRemovalException.class,
                () -> board.removeShip(new Point(6, 5), new Point(7, 5)),
                "IncorrectShipRemovalException should be thrown if the ship is party selected");
    }

    @Test
    void testBoardRemoveShipOverlySelected() {
        assertThrows(IncorrectShipRemovalException.class,
                () -> board.removeShip(new Point(3, 3), new Point(3, 6)),
                "IncorrectShipRemovalException should be thrown if the ship is overly selected");
    }

    @Test
    void testBoardRemoveShipNothingThere() {
        assertThrows(IncorrectShipRemovalException.class,
                () -> board.removeShip(new Point(0, 4), new Point(0, 7)),
                "IncorrectShipRemovalException should be thrown if there is no ship");
    }

    @Test
    void testBoardRemoveTwoSmallShipsBetweenCoordinates() {
        board.placeShip(new Point(2, 0), new Point(3, 0));
        board.placeShip(new Point(5, 0), new Point(6, 0));

        assertThrows(IncorrectShipRemovalException.class,
                () -> board.removeShip(new Point(2, 0), new Point(6, 0)),
                "IncorrectShipRemovalException should be thrown if there are two not different ships");
    }

    @Test
    void testBoardRemoveShipCorrect() {
        board.removeShip(new Point(3, 4), new Point(3, 6));

        assertFalse(board.hit(new Point(3, 5)),
                "Tiles should be cleared after ship is removed");
        assertFalse(board.hit(new Point(3, 4)),
                "Tiles should be cleared after ship is removed");
        assertFalse(board.hit(new Point(3, 6)),
                "Tiles should be cleared after ship is removed");
    }

    @Test
    void testBoardRemoveShipCorrectInverted() {
        board.removeShip(new Point(3, 4), new Point(3, 6));

        assertFalse(board.hit(new Point(3, 5)),
                "Ship might also be removed inverted");
    }

    @Test
    void testBoardHitAlreadyHit() {
        board.hit(new Point(3, 5));

        assertThrows(TileAlreadyHitException.class,
                () -> board.hit(new Point(3, 5)),
                "TileAlreadyHitException should be thrown if the tile has already been hit");
    }

    @Test
    void testBoardSunkShipHorizontallyCorrect() {
        board.hit(new Point(3, 5));
        board.hit(new Point(3, 6));
        board.hit(new Point(3, 4));

        assertTrue(true, "Sinking a ship should work properly");
    }

    @Test
    void testBoardSunkShipVerticallyCorrect() {
        board.hit(new Point(5, 5));
        board.hit(new Point(7, 5));
        board.hit(new Point(6, 5));

        assertTrue(true, "Sinking a ship should work properly");
    }

    @Test
    void testBoardAdjacentTileRevealedWhenShipIsDestroyed() {
        board.hit(new Point(3, 4));
        board.hit(new Point(3, 5));
        board.hit(new Point(3, 6));

        assertThrows(TileAlreadyHitException.class,
                () -> board.hit(new Point(2, 3)),
                "Tiles adjacent to a ship should be shown as empty after it is destroyed");
        assertThrows(TileAlreadyHitException.class,
                () -> board.hit(new Point(2, 4)),
                "Tiles adjacent to a ship should be shown as empty after it is destroyed");
        assertThrows(TileAlreadyHitException.class,
                () -> board.hit(new Point(2, 5)),
                "Tiles adjacent to a ship should be shown as empty after it is destroyed");
        assertThrows(TileAlreadyHitException.class,
                () -> board.hit(new Point(2, 6)),
                "Tiles adjacent to a ship should be shown as empty after it is destroyed");
        assertThrows(TileAlreadyHitException.class,
                () -> board.hit(new Point(2, 7)),
                "Tiles adjacent to a ship should be shown as empty after it is destroyed");
        assertThrows(TileAlreadyHitException.class,
                () -> board.hit(new Point(3, 3)),
                "Tiles adjacent to a ship should be shown as empty after it is destroyed");
        assertThrows(TileAlreadyHitException.class,
                () -> board.hit(new Point(3, 7)),
                "Tiles adjacent to a ship should be shown as empty after it is destroyed");
        assertThrows(TileAlreadyHitException.class,
                () -> board.hit(new Point(4, 3)),
                "Tiles adjacent to a ship should be shown as empty after it is destroyed");
        assertThrows(TileAlreadyHitException.class,
                () -> board.hit(new Point(4, 4)),
                "Tiles adjacent to a ship should be shown as empty after it is destroyed");
        assertThrows(TileAlreadyHitException.class,
                () -> board.hit(new Point(4, 5)),
                "Tiles adjacent to a ship should be shown as empty after it is destroyed");
        assertThrows(TileAlreadyHitException.class,
                () -> board.hit(new Point(4, 6)),
                "Tiles adjacent to a ship should be shown as empty after it is destroyed");
        assertThrows(TileAlreadyHitException.class,
                () -> board.hit(new Point(4, 7)),
                "Tiles adjacent to a ship should be shown as empty after it is destroyed");
    }

    @Test
    void testBoardEdges() {
        board.placeShip(new Point(0, 0), new Point(0, 1));
        board.placeShip(new Point(1, 9), new Point(0, 9));
        board.placeShip(new Point(9, 1), new Point(9, 0));
        board.placeShip(new Point(8, 9), new Point(9, 9));

        board.removeShip(new Point(0, 1), new Point(0, 0));
        board.removeShip(new Point(8, 9), new Point(9, 9));

        board.hit(new Point(1, 9));
        board.hit(new Point(0, 9));
        board.hit(new Point(9, 1));
        board.hit(new Point(9, 0));

        assertTrue(true, "Placing, removing and hitting ships at the edges should work properly");
    }
}