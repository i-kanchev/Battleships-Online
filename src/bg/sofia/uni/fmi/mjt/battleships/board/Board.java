package bg.sofia.uni.fmi.mjt.battleships.board;

import bg.sofia.uni.fmi.mjt.battleships.exceptions.IncorrectShipPlacementException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.IncorrectShipRemovalException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.TileAlreadyHitException;


import java.util.ArrayList;
import java.util.List;

public class Board {
    private static final int BOARD_SIZE = 10;
    private final List<List<Tile>> board;

    public Board() {
        this.board = new ArrayList<>(BOARD_SIZE);

        for (int i = 0; i < BOARD_SIZE; i++) {
            this.board.add(new ArrayList<>(BOARD_SIZE));

            for (int j = 0; j < BOARD_SIZE; j++) {
                this.board.get(i).add(new Tile());
            }
        }
    }

    public Board(Board other) {
        this.board = new ArrayList<>(BOARD_SIZE);

        for (int i = 0; i < BOARD_SIZE; i++) {
            this.board.add(new ArrayList<>(BOARD_SIZE));

            for (int j = 0; j < BOARD_SIZE; j++) {
                this.board.get(i).add(new Tile(other.board.get(i).get(j)));
            }
        }
    }

    public int getBoardSize() {
        return BOARD_SIZE;
    }

    public Tile getTile(Point point) {
        return board.get(point.x()).get(point.y());
    }

    public void placeShip(Point from, Point to) {
        if (to.x() < from.x() || to.y() < from.y()) {
            Point temp = from;
            from = to;
            to = temp;
        }

        if (!isPlaceShipPossible(from, to)) {
            throw new IncorrectShipPlacementException("Ship cannot be adjacent to another ship");
        }

        for (int i = from.x(); i <= to.x(); i++) {
            board.get(i).get(from.y()).placeShip();
        }

        for (int i = from.y(); i <= to.y(); i++) {
            board.get(from.x()).get(i).placeShip();
        }
    }

    public void removeShip(Point from, Point to) {
        if (to.x() < from.x() || to.y() < from.y()) {
            Point temp = from;
            from = to;
            to = temp;
        }

        if (!isRemoveShipCorrect(from, to)) {
            throw new IncorrectShipRemovalException("Ship party or overly selected");
        }

        for (int i = from.x(); i <= to.x(); i++) {
            board.get(i).get(from.y()).removeShip();
        }

        for (int i = from.y(); i <= to.y(); i++) {
            board.get(from.x()).get(i).removeShip();
        }
    }

    public boolean hit(Point point) {
        if (!isTileValid(point)) {
            throw new TileAlreadyHitException("Cannot target tiles that are already hit");
        }

        board.get(point.x()).get(point.y()).reveal();

        if (board.get(point.x()).get(point.y()).getCondition() == Condition.SHIP_UNDAMAGED) {
            board.get(point.x()).get(point.y()).damageShip();

            if (isShipDestroyed(point)) {
                destroyShip(point);
            }

            return true;
        }

        return false;
    }

    private boolean isTileValid(Point point) {
        return !board.get(point.x()).get(point.y()).isRevealed();
    }

    private boolean isShipDestroyed(Point point) {
        for (int x = point.x() - 1; x >= 0; x--) {
            Condition condition = board.get(x).get(point.y()).getCondition();

            if (condition == Condition.EMPTY) {
                break;
            }
            if (condition != Condition.SHIP_DAMAGED) {
                return false;
            }
        }

        for (int x = point.x() + 1; x < BOARD_SIZE; x++) {
            Condition condition = board.get(x).get(point.y()).getCondition();

            if (condition == Condition.EMPTY) {
                break;
            }
            if (condition != Condition.SHIP_DAMAGED) {
                return false;
            }
        }

        for (int y = point.y() - 1; y >= 0; y--) {
            Condition condition = board.get(point.x()).get(y).getCondition();

            if (condition == Condition.EMPTY) {
                break;
            }
            if (condition != Condition.SHIP_DAMAGED) {
                return false;
            }
        }

        for (int y = point.y() + 1; y < BOARD_SIZE; y++) {
            Condition condition = board.get(point.x()).get(y).getCondition();

            if (condition == Condition.EMPTY) {
                break;
            }
            if (condition != Condition.SHIP_DAMAGED) {
                return false;
            }
        }

        return true;
    }

    private void destroyShip(Point point) {
        int lowerX = point.x();
        int upperX = point.x() + 1;
        int lowerY = point.y() - 1;
        int upperY = point.y() + 1;

        while (lowerX >= 0) {
            if (board.get(lowerX).get(point.y()).getCondition() == Condition.SHIP_DAMAGED) {
                board.get(lowerX).get(point.y()).sunkShip();

                lowerX--;
                continue;
            }

            break;
        }

        while (upperX < BOARD_SIZE) {
            if (board.get(upperX).get(point.y()).getCondition() == Condition.SHIP_DAMAGED) {
                board.get(upperX).get(point.y()).sunkShip();

                upperX++;
                continue;
            }

            break;
        }

        while (lowerY >= 0) {
            if (board.get(point.x()).get(lowerY).getCondition() == Condition.SHIP_DAMAGED) {
                board.get(point.x()).get(lowerY).sunkShip();

                lowerY--;
                continue;
            }

            break;
        }

        while (upperY < BOARD_SIZE) {
            if (board.get(point.x()).get(upperY).getCondition() == Condition.SHIP_DAMAGED) {
                board.get(point.x()).get(upperY).sunkShip();

                upperY++;
                continue;
            }

            break;
        }

        if (lowerX < 0) {
            lowerX++;
        }
        if (upperX == BOARD_SIZE) {
            upperX--;
        }
        if (lowerY < 0) {
            lowerY++;
        }
        if (upperY == BOARD_SIZE) {
            upperY--;
        }

        for (int i = lowerX; i <= upperX; i++) {
            for (int j = lowerY; j <= upperY; j++) {
                board.get(i).get(j).reveal();
            }
        }
    }

    private boolean isPlaceShipPossible(Point from, Point to) {
        int lowerX = from.x();
        int upperX = to.x();
        int lowerY = from.y();
        int upperY = to.y();

        if (lowerX != 0) {
            lowerX--;
        }
        if (upperX != BOARD_SIZE - 1) {
            upperX++;
        }
        if (lowerY != 0) {
            lowerY--;
        }
        if (upperY != BOARD_SIZE - 1) {
            upperY++;
        }

        for (int i = lowerX; i <= upperX; i++) {
            for (int j = lowerY; j <= upperY; j++) {
                if (board.get(i).get(j).getCondition() != Condition.EMPTY) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isRemoveShipCorrect(Point from, Point to) {
        if (from.x() == to.x()) {
            if (from.y() != 0 && board.get(from.x()).get(from.y() - 1).getCondition() != Condition.EMPTY) {
                return false;
            }
            if (to.y() != BOARD_SIZE - 1 && board.get(from.x()).get(to.y() + 1).getCondition() != Condition.EMPTY) {
                return false;
            }

            for (int i = from.y(); i <= to.y(); i++) {
                if (board.get(from.x()).get(i).getCondition() != Condition.SHIP_UNDAMAGED) {
                    return false;
                }
            }
        } else {
            if (from.x() != 0 && board.get(from.x() - 1).get(from.y()).getCondition() != Condition.EMPTY) {
                return false;
            }
            if (to.x() != BOARD_SIZE - 1 && board.get(to.x() + 1).get(from.y()).getCondition() != Condition.EMPTY) {
                return false;
            }

            for (int i = from.x(); i <= to.x(); i++) {
                if (board.get(i).get(from.y()).getCondition() != Condition.SHIP_UNDAMAGED) {
                    return false;
                }
            }
        }

        return true;
    }
}