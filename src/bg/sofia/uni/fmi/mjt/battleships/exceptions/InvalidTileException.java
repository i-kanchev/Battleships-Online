package bg.sofia.uni.fmi.mjt.battleships.exceptions;

public class InvalidTileException extends RuntimeException {
    public InvalidTileException(String message) {
        super(message);
    }
}
