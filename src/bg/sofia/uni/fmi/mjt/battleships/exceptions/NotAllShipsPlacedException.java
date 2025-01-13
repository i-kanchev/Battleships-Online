package bg.sofia.uni.fmi.mjt.battleships.exceptions;

public class NotAllShipsPlacedException extends RuntimeException {
    public NotAllShipsPlacedException(String message) {
        super(message);
    }
}
