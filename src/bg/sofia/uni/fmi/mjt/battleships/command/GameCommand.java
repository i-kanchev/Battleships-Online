package bg.sofia.uni.fmi.mjt.battleships.command;

public enum GameCommand {
    HELP("help", "shows list of commands for current section"),
    HIT("[coordinates]", "shoots the enemy field on given coordinates"),
    CLOSE("close", "closes the game, adding it to the unfinished so it can be finished later and goes to matchmaking"),
    SURRENDER("surrender", "surrenders the game and goes to matchmaking");

    private final String tag;
    private final String description;

    GameCommand(String tag, String description) {
        this.tag = tag;
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("%1$-18s| %2$s", tag, description);
    }
}
