package bg.sofia.uni.fmi.mjt.battleships.command;

public enum LobbyCommand {
    HELP("help", "shows list of commands for current section"),
    PLACE("place", "place ship on given coordinates if possible ([from] [to])"),
    REMOVE("remove", "remove ship on given coordinates if is placed there ([from] [to])"),
    RESET("reset", "resets your board placement"),
    READY("ready", "sets your status to ready"),
    UNREADY("unready", "sets your status to unready"),
    LEAVE("leave", "leaves the game before it is started");

    private final String tag;
    private final String description;

    LobbyCommand(String tag, String description) {
        this.tag = tag;
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("%1$-18s| %2$s", tag, description);
    }
}
