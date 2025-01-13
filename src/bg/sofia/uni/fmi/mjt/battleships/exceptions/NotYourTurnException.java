package bg.sofia.uni.fmi.mjt.battleships.exceptions;

public class NotYourTurnException extends RuntimeException {
    public NotYourTurnException(String message) {
        super(message);
    }
}
