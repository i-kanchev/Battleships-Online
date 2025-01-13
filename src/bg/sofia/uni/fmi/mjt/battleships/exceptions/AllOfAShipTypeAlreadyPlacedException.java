package bg.sofia.uni.fmi.mjt.battleships.exceptions;

public class AllOfAShipTypeAlreadyPlacedException extends RuntimeException{
    public AllOfAShipTypeAlreadyPlacedException(String message) {
        super(message);
    }
}
