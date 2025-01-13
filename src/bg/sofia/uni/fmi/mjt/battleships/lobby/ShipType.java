package bg.sofia.uni.fmi.mjt.battleships.lobby;

public enum ShipType {
    CARRIER(5, 1),
    BATTLESHIP(4, 2),
    CRUISER(3, 3),
    DESTROYER(2, 4);

    private final int size;
    private final int count;

    ShipType(int size, int count) {
        this.size = size;
        this.count = count;
    }

    public int getSize() {
        return this.size;
    }

    public int getCount() {
        return this.count;
    }
}
