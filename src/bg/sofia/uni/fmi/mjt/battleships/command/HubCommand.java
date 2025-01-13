package bg.sofia.uni.fmi.mjt.battleships.command;

public enum HubCommand {
    HELP("help", "shows list of commands for current section"),
    LIST_GAMES("list-games", "shows all game rooms"),
    CREATE_GAME("create-game", "creates a game with a given name"),
    JOIN_GAME("join-game", "join a game room with a given name or random one if a name is not provided"),
    UNFINISHED_GAMES("unfinished-games", "shows all unfinished games"),
    LOAD_GAME("load-game", "loads an unfinished game with a given name"),
    DELETE_GAME("delete-game", "deletes an unfinished game with a given name"),
    EXIT("exit", "exits the game");

    private final String tag;
    private final String description;

    HubCommand(String tag, String description) {
        this.tag = tag;
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("%1$-18s| %2$s", tag, description);
    }
}
