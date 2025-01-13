package bg.sofia.uni.fmi.mjt.battleships.exceptions;

public class TileAlreadyHitException extends RuntimeException {
    public TileAlreadyHitException(String message) {
        super(message);
    }
}
