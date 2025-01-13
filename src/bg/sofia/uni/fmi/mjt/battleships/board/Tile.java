package bg.sofia.uni.fmi.mjt.battleships.board;

public class Tile {
    private Visibility visibility;
    private Condition condition;

    public Tile() {
        this.visibility = Visibility.HIDDEN;
        this.condition = Condition.EMPTY;
    }

    public Tile(Tile other) {
        this.visibility = other.visibility;
        this.condition = other.condition;
    }

    public Boolean isRevealed() {
        return visibility.equals(Visibility.REVEALED);
    }

    public Boolean isHidden() {
        return visibility.equals(Visibility.HIDDEN);
    }

    public Condition getCondition() {
        return condition;
    }

    public void reveal() {
        visibility = Visibility.REVEALED;
    }

    public void placeShip() {
        condition = Condition.SHIP_UNDAMAGED;
    }

    public void removeShip() {
        condition = Condition.EMPTY;
    }

    public void damageShip() {
        condition = Condition.SHIP_DAMAGED;
    }

    public void sunkShip() {
        condition = Condition.SHIP_SUNK;
    }
}
