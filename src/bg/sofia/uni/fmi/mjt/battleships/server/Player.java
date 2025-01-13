package bg.sofia.uni.fmi.mjt.battleships.server;

import bg.sofia.uni.fmi.mjt.battleships.board.Board;
import bg.sofia.uni.fmi.mjt.battleships.board.Condition;
import bg.sofia.uni.fmi.mjt.battleships.board.Point;
import bg.sofia.uni.fmi.mjt.battleships.board.Tile;
import bg.sofia.uni.fmi.mjt.battleships.command.GameCommand;
import bg.sofia.uni.fmi.mjt.battleships.command.HubCommand;
import bg.sofia.uni.fmi.mjt.battleships.command.LobbyCommand;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.AllOfAShipTypeAlreadyPlacedException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.IncorrectShipPlacementException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.IncorrectShipRemovalException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.LobbyNameInvalidCharactersException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.LobbyNameInvalidLengthException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.NicknameInvalidCharactersException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.NicknameInvalidLengthException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.NotAllShipsPlacedException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.NotYourTurnException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.ShipInvalidSizeException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.TileAlreadyHitException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.TileWrongFormatException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.TilesNotConnectedException;
import bg.sofia.uni.fmi.mjt.battleships.gameroom.GameRoom;
import bg.sofia.uni.fmi.mjt.battleships.lobby.MatchmakingLobby;
import bg.sofia.uni.fmi.mjt.battleships.lobby.ShipType;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


public class Player implements Runnable {
    private static final String SHIP_UNDAMAGED_SYMBOL = "□";
    private static final String SHIP_DAMAGED_SYMBOL = "X";
    private static final String MISSED_SYMBOL = "o";
    private static final String UNKNOWN_SYMBOL = "~";
    private static final int MIN_PLAYER_NAME_LENGTH = 3;
    private static final int MAX_PLAYER_NAME_LENGTH = 16;
    private static final int MIN_LOBBY_NAME_LENGTH = 3;
    private static final int MAX_LOBBY_NAME_LENGTH = 16;
    private static Map<String, Player> PLAYERS = new ConcurrentHashMap<>();
    private static Map<String, MatchmakingLobby> LOBBIES = new ConcurrentHashMap<>();
    private static Map<String, GameRoom> GAMES = new ConcurrentHashMap<>();
    private final Socket socket;
    private String nickname;
    private GameStage stage;
    private String currLobby;
    private String currGame;
    private final Set<String> yourGames;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Player opponent;


    public Player(Socket socket) {
        this.socket = socket;
        this.stage = GameStage.LOG;
        yourGames = new HashSet<>();

        try {
            dos = new DataOutputStream(this.socket.getOutputStream());
            dis = new DataInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Problem occurred" + e.getMessage());
        }
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                StringTokenizer command = new StringTokenizer(dis.readUTF(), " ");

                switch (stage) {
                    case LOG -> setNickname(command.nextToken());
                    case HUB -> {
                        switch (command.nextToken()) {
                            case "help" -> printHelpHub();
                            case "list-games" -> showAvailableGames();
                            case "create-game" -> createLobby(command.hasMoreTokens() ? command.nextToken() : null);
                            case "join-game" -> {
                                if (command.hasMoreTokens()) {
                                    joinLobby(command.nextToken());
                                }
                                else {
                                    joinRandomLobby();
                                }
                            }
                            case "unfinished-games" -> showUnfinishedGames();
                            case "load-game" -> loadGame(command.hasMoreTokens() ? command.nextToken() : null);
                            case "delete-game" -> deleteGame(command.hasMoreTokens() ? command.nextToken() : null);
                            case "exit" -> exit();
                            default -> printInvalidCommand();
                        }
                    }
                    case LOBBY -> {
                        switch (command.nextToken()) {
                            case "help" -> printHelpLobby();
                            case "place" -> placeShip(command.hasMoreTokens() ? command.nextToken() : null,
                                    command.hasMoreTokens() ? command.nextToken() : null);
                            case "remove" -> removeShip(command.hasMoreTokens() ? command.nextToken() : null,
                                    command.hasMoreTokens() ? command.nextToken() : null);
                            case "reset" -> reset();
                            case "ready" -> ready();
                            case "unready" -> printNotReady();
                            case "leave" -> leave();
                            default -> printInvalidCommand();
                        }
                    }
                    case GAME -> {
                        String cmd = command.nextToken();
                        switch (cmd) {
                            case "help" -> printHelpGame();
                            case "close" -> close();
                            case "surrender" -> surrender();
                            default -> hit(cmd);
                        }
                    }
                    case WAITING_OPPONENT -> {
                        if (command.nextToken().equals("leave")) {
                            leave();
                        } else {
                            printInvalidCommand();
                        }
                    }
                    case WAITING_READY -> {
                        if (command.nextToken().equals("unready")) {
                            unready();
                        } else {
                            printInvalidCommand();
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void printInvalidCommand() throws IOException {
        dos.writeUTF("Invalid command" + System.lineSeparator());
    }

    private void printNotReady() throws IOException {
        dos.writeUTF("You have to be ready first" + System.lineSeparator());
    }

    private void printHelpHub() throws IOException {
        StringBuilder response = new StringBuilder();

        for (HubCommand cmd : HubCommand.values()) {
            response.append(cmd.toString()).append(System.lineSeparator());
        }

        dos.writeUTF(response.toString());
    }

    private void printHelpLobby() throws IOException {
        StringBuilder response = new StringBuilder();

        for (LobbyCommand cmd : LobbyCommand.values()) {
            response.append(cmd.toString()).append(System.lineSeparator());
        }

        dos.writeUTF(response.toString());
    }

    private void printHelpGame() throws IOException {
        StringBuilder response = new StringBuilder();

        for (GameCommand cmd : GameCommand.values()) {
            response.append(cmd.toString()).append(System.lineSeparator());
        }

        dos.writeUTF(response.toString());
    }

    private void setNickname(String nickname) throws IOException {
        try {
            nicknameValidation(nickname);
        } catch (IllegalArgumentException e) {
            dos.writeUTF("Nickname should be entered" + System.lineSeparator());
            return;
        } catch (NicknameInvalidLengthException e) {
            dos.writeUTF("Nickname should be between 3 and 16 characters" + System.lineSeparator());
            return;
        } catch (NicknameInvalidCharactersException e) {
            dos.writeUTF("Nickname should use only letters, numbers, dashes and underscores" + System.lineSeparator());
            return;
        }

        if (PLAYERS.containsKey(nickname)) {
            dos.writeUTF("Nickname is already taken" + System.lineSeparator());
            return;
        }

        PLAYERS.put(nickname, this);
        this.nickname = nickname;
        stage = GameStage.HUB;

        dos.writeUTF("Welcome, " + nickname + System.lineSeparator()
                + "Type \"help\" to see commands" + System.lineSeparator());
    }

    private void showAvailableGames() throws IOException {
        StringBuilder response = new StringBuilder();

        boolean availableLobbies = false;

        String lobbyName = "NAME";
        String creatorName = "CREATOR";
        String lobbyFullness = "PLAYERS";
        response.append(String.format("| %-16s | %-16s | %-8s |", lobbyName, creatorName, lobbyFullness))
                .append(System.lineSeparator());
        response.append("|------------------+------------------+----------|").append(System.lineSeparator());

        for (Map.Entry<String, MatchmakingLobby> entry : LOBBIES.entrySet()) {
            if (entry.getValue().isFull()) {
                continue;
            }
            lobbyName = entry.getValue().getLobbyName();
            creatorName = entry.getValue().getPlayer1();
            lobbyFullness = entry.getValue().getCurrPlayers() + "/" + entry.getValue().getCapacity();

            response.append(String.format("| %-16s | %-16s | %-8s |", lobbyName, creatorName, lobbyFullness))
                    .append(System.lineSeparator());
            availableLobbies = true;
        }

        if (!availableLobbies) {
            response.delete(0, response.length());
            response.append("No active rooms at the moment").append(System.lineSeparator());
        }

        dos.writeUTF(response.toString());
    }

    private void createLobby(String lobbyName) throws IOException, InterruptedException {
        try {
            lobbyNameValidation(lobbyName);
        } catch (IllegalArgumentException e) {
            dos.writeUTF("Lobby name should be entered" + System.lineSeparator());
            return;
        } catch (LobbyNameInvalidLengthException e) {
            dos.writeUTF("Lobby name should be between 3 and 16 characters" + System.lineSeparator());
            return;
        } catch (LobbyNameInvalidCharactersException e) {
            dos.writeUTF("Lobby name should use only letters, numbers, dashes and underscores"
                    + System.lineSeparator());
            return;
        }

        if (LOBBIES.containsKey(lobbyName)) {
            dos.writeUTF("Lobby with that name already exists" + System.lineSeparator());
            return;
        }
        LOBBIES.put(lobbyName, new MatchmakingLobby(lobbyName, nickname));

        dos.writeUTF("Awaiting opponent" + System.lineSeparator()
                + "Type \"leave\" to leave" + System.lineSeparator());
        currLobby = lobbyName;
        stage = GameStage.WAITING_OPPONENT;
    }

    private void joinLobby(String lobbyName) throws IOException {
        if (!LOBBIES.containsKey(lobbyName)) {
            dos.writeUTF("There is no lobby with that name" + System.lineSeparator());
            return;
        }
        if (LOBBIES.get(lobbyName).isFull()) {
            dos.writeUTF("The lobby is full" + System.lineSeparator());
            return;
        }

        LOBBIES.get(lobbyName).joinLobby(nickname);
        currLobby = lobbyName;
        stage = GameStage.LOBBY;
        dos.writeUTF("Lobby joined" + System.lineSeparator());

        connectPlayers();
    }

    private void joinRandomLobby() throws IOException {
        for (Map.Entry<String, MatchmakingLobby> entry : LOBBIES.entrySet()) {
            if (!entry.getValue().isFull()) {
                entry.getValue().joinLobby(nickname);
                currLobby = entry.getKey();
                stage = GameStage.LOBBY;
                dos.writeUTF("Random lobby joined" + System.lineSeparator());

                connectPlayers();

                return;
            }
        }
    }

    private void connectPlayers() throws IOException {
        opponent = PLAYERS.get(LOBBIES.get(currLobby).getPlayer1());
        dos.writeUTF("Starting a game with " + opponent.nickname + System.lineSeparator()
                + yourBoard(LOBBIES.get(currLobby).getYourBoard(nickname))
                + remainingShips(nickname));

        opponent.stage = GameStage.LOBBY;
        opponent.opponent = PLAYERS.get(LOBBIES.get(currLobby).getPlayer2());
        opponent.dos.writeUTF("Starting a game with " + nickname + System.lineSeparator()
                + yourBoard(LOBBIES.get(currLobby).getYourBoard(opponent.nickname))
                + remainingShips(opponent.nickname));
    }

    private void showUnfinishedGames() throws IOException {
        StringBuilder response = new StringBuilder();

        if (yourGames.isEmpty()) {
            dos.writeUTF("You do not have any saved games" + System.lineSeparator());
            return;
        }

        String gameID = "TAG";
        String opponentName = "OPPONENT";
        String currTurn = "CURRENT TURN";
        response.append(String.format("| %-6s | %-16s | %-13s |", gameID, opponentName, currTurn))
                .append(System.lineSeparator());
        response.append("|--------+------------------+---------------|").append(System.lineSeparator());

        for (String game : yourGames) {
            gameID = GAMES.get(game).getGameID();
            opponentName = GAMES.get(game).getOpponent(nickname);
            currTurn = GAMES.get(game).isCurrPlayerTurn(nickname) ? "YOURS" : "OPPONENTS";
            response.append(String.format("| %-6s | %-16s | %-13s |", gameID, opponentName, currTurn))
                    .append(System.lineSeparator());

        }

        dos.writeUTF(response.toString());
    }

    private void loadGame(String gameID) throws IOException {
        if (!yourGames.contains(gameID)) {
            dos.writeUTF("No game with such ID" + System.lineSeparator());
            return;
        }

        if (!GAMES.containsKey(gameID)) {
            dos.writeUTF("No game with such ID" + System.lineSeparator());
            return;
        }

        currGame = gameID;
        stage = GameStage.GAME;
        opponent = PLAYERS.get(GAMES.get(currGame).getOpponent(nickname));
        dos.writeUTF("Game with " + opponent.nickname + " resumed" + System.lineSeparator()
                + bothBoards(GAMES.get(currGame).getYourBoard(nickname), GAMES.get(currGame).getOpponentBoard(nickname))
                + currTurn(nickname));
    }

    private void deleteGame(String gameID) throws IOException {
        if (!GAMES.containsKey(gameID)) {
            dos.writeUTF("No game with such ID" + System.lineSeparator());
            return;
        }

        if (!yourGames.contains(gameID)) {
            dos.writeUTF("No game with such ID" + System.lineSeparator());
            return;
        }

        Player gameOpponent = PLAYERS.get((GAMES.get(gameID).getOpponent(nickname)));

        if (gameOpponent.currGame.equals(gameID)) {
            gameOpponent.currGame = null;
            gameOpponent.stage = GameStage.HUB;
            gameOpponent.dos.writeUTF("Opponent surrendered" + System.lineSeparator());
        }

        yourGames.remove(gameID);
        gameOpponent.yourGames.remove(gameID);
        GAMES.remove(gameID);

        dos.writeUTF("Game deleted" + System.lineSeparator());
    }

    private void exit() throws IOException {
        for (String game : yourGames) {
            deleteGame(game);
        }

        PLAYERS.remove(nickname);

        dos.writeUTF("You have exited the game");
        dis.close();
        dos.close();
        socket.close();
    }

    private void reset() throws IOException {
        LOBBIES.get(currLobby).resetYourBoard(nickname);

        dos.writeUTF(yourBoard(LOBBIES.get(currLobby).getYourBoard(nickname))
                + remainingShips(nickname));
    }

    private void ready() throws IOException {
        try {
            LOBBIES.get(currLobby).readyPlayer(nickname);

            if (LOBBIES.get(currLobby).bothPlayersReady()) {
                GameRoom newGame = new GameRoom(LOBBIES.get(currLobby));
                currGame = newGame.getGameID();
                opponent.currGame = newGame.getGameID();
                GAMES.put(currGame, newGame);
                LOBBIES.remove(currLobby);

                currLobby = null;
                opponent.currLobby = null;

                stage = GameStage.GAME;
                dos.writeUTF(bothBoards(GAMES.get(currGame).getYourBoard(nickname), GAMES.get(currGame)
                        .getOpponentBoard(nickname))
                        + currTurn(nickname));

                opponent.stage = GameStage.GAME;
                opponent.dos.writeUTF(bothBoards(GAMES.get(currGame).getYourBoard(opponent.nickname), GAMES
                        .get(currGame).getOpponentBoard(opponent.nickname))
                        + currTurn(opponent.nickname));
            } else {
                stage = GameStage.WAITING_READY;
                dos.writeUTF("Waiting for your opponent" + System.lineSeparator()
                        + "Type \"unready\" to edit ships"  + System.lineSeparator());
            }
        } catch (NotAllShipsPlacedException e) {
            dos.writeUTF("Not all ships are placed" + System.lineSeparator());
        }
    }

    private void unready() throws IOException {
        LOBBIES.get(currLobby).unreadyPlayer(nickname);
        stage = GameStage.LOBBY;
        dos.writeUTF(yourBoard(LOBBIES.get(currLobby).getYourBoard(nickname))
                + remainingShips(nickname));
    }

    private void leave() throws IOException {
        LOBBIES.remove(currLobby);

        currLobby = null;
        stage = GameStage.HUB;
        dos.writeUTF("Lobby left" + System.lineSeparator());

        if (opponent != null) {
            opponent.currLobby = null;
            opponent.stage = GameStage.HUB;
            opponent.dos.writeUTF("Opponent left" + System.lineSeparator());

            opponent.opponent = null;
            opponent = null;
        }
    }

    private void placeShip(String coordinates1, String coordinates2) throws IOException {
        try {
            coordinatesValidation(coordinates1);
            coordinatesValidation(coordinates2);
        } catch (IllegalArgumentException e) {
            dos.writeUTF("Two set of coordinates should be entered" + System.lineSeparator());
            return;
        } catch (TileWrongFormatException e) {
            dos.writeUTF("Coordinates should be in the following format XY where X is in A-J and Y is in 1-10"
                    + System.lineSeparator());
            return;
        }

        try {
            LOBBIES.get(currLobby).placeYourShip(nickname, coordinates1, coordinates2);
            dos.writeUTF(yourBoard(LOBBIES.get(currLobby).getYourBoard(nickname))
                    + remainingShips(nickname));
        } catch (TilesNotConnectedException e) {
            dos.writeUTF("Tiles should be connected" + System.lineSeparator());
        } catch (ShipInvalidSizeException e) {
            dos.writeUTF("Ship size should be between 2 and 5 tiles" + System.lineSeparator());
        } catch (AllOfAShipTypeAlreadyPlacedException e) {
            dos.writeUTF("All ships of that type are already placed" + System.lineSeparator());
        } catch (IncorrectShipPlacementException e) {
            dos.writeUTF("Ship cannot be adjacent to another ship" + System.lineSeparator());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void removeShip(String coordinates1, String coordinates2) throws IOException {
        try {
            coordinatesValidation(coordinates1);
            coordinatesValidation(coordinates2);
        } catch (IllegalArgumentException e) {
            dos.writeUTF("Two set of coordinates should be entered" + System.lineSeparator());
            return;
        } catch (TileWrongFormatException e) {
            dos.writeUTF("Coordinates should be in the following format XY where X is in A-J and Y is in 1-10"
                    + System.lineSeparator());
            return;
        }

        try {
            LOBBIES.get(currLobby).removeYourShip(nickname, coordinates1, coordinates2);
            dos.writeUTF(yourBoard(LOBBIES.get(currLobby).getYourBoard(nickname))
                    + remainingShips(nickname));
        } catch (TileWrongFormatException e) {
            dos.writeUTF("Tile should be in the following format XY where X is in A-J and Y is in 1-10"
                    + System.lineSeparator());
        } catch (IncorrectShipRemovalException e) {
            dos.writeUTF("Ship party or overly selected" + System.lineSeparator());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void close() throws IOException {
        yourGames.add(currGame);
        currGame = null;
        opponent = null;
        stage = GameStage.HUB;
        dos.writeUTF("Game closed" + System.lineSeparator());
    }

    private void surrender() throws IOException {
        currGame = null;
        stage = GameStage.HUB;
        dos.writeUTF("You surrendered" + System.lineSeparator());

        opponent.currGame = null;
        opponent.stage = GameStage.HUB;
        opponent.dos.writeUTF("Opponent surrendered" + System.lineSeparator());

        opponent.opponent = null;
        opponent = null;
    }

    private void hit(String coordinates) throws IOException {
        try {
            coordinatesValidation(coordinates);
        } catch (IllegalArgumentException e) {
            dos.writeUTF("Coordinates should be entered" + System.lineSeparator());
            return;
        } catch (TileWrongFormatException e) {
            dos.writeUTF("Coordinates should be in the following format XY where X is in A-J and Y is in 1-10"
                    + System.lineSeparator());
            return;
        }

        try {
            GameRoom game = GAMES.get(currGame);
            game.hitOpponent(nickname, coordinates);

            dos.writeUTF(bothBoards(GAMES.get(currGame)
                    .getYourBoard(nickname), GAMES.get(currGame).getOpponentBoard(nickname)));

            if (opponent.opponent == this) {
                opponent.dos.writeUTF(bothBoards(GAMES.get(currGame)
                        .getYourBoard(opponent.nickname), GAMES.get(currGame).getOpponentBoard(opponent.nickname)));
            }

            if (game.isGameOver()) {
                gameOver();
            }
            else {
                dos.writeUTF(currTurn(nickname));
                if (opponent.opponent == this) {
                    opponent.dos.writeUTF(currTurn(opponent.nickname));
                }
            }
        } catch (NotYourTurnException e) {
            dos.writeUTF("It is not your turn yet" + System.lineSeparator());
        } catch (TileAlreadyHitException e) {
            dos.writeUTF("Tile is already hit" + System.lineSeparator());
        }
    }

    private void nicknameValidation(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Nickname cannot be null or empty");
        }
        if (playerName.length() < MIN_PLAYER_NAME_LENGTH || playerName.length() > MAX_PLAYER_NAME_LENGTH) {
            throw new NicknameInvalidLengthException("Nickname should be between 3 and 16 characters");
        }
        if (!Pattern.matches("^[A-Za-z0-9]*$", playerName)) {
            throw new NicknameInvalidCharactersException("Nickname should use only letters and numbers");
        }
    }

    private void lobbyNameValidation(String lobbyName) {
        if (lobbyName == null || lobbyName.isBlank()) {
            throw new IllegalArgumentException("Lobby name cannot be null or empty");
        }
        if (lobbyName.length() < MIN_LOBBY_NAME_LENGTH || lobbyName.length() > MAX_LOBBY_NAME_LENGTH) {
            throw new LobbyNameInvalidLengthException("Lobby name should be between 3 and 16 characters");
        }
        if (!Pattern.matches("^[A-Za-z0-9-_]*$", lobbyName)) {
            throw new LobbyNameInvalidCharactersException(
                    "Lobby name should use only letters, numbers, dashes and underscores");
        }
    }

    private void coordinatesValidation(String coordinates) {
        if (coordinates == null || coordinates.isBlank()) {
            throw new IllegalArgumentException("Coordinates cannot be null or empty");
        }
        if (!Pattern.matches("^[A-Ja-j][1-9]$|^[A-Ja-j]10$", coordinates)) {
            throw new TileWrongFormatException(
                    "Coordinates should be in the following format XY where X is in A-J and Y is in 1-10");
        }
    }

    private String remainingShips(String player) {
        StringBuilder response = new StringBuilder();

        List<Integer> remainingShips = LOBBIES.get(currLobby).getYourRemainingShips(player);

        for (int i = 0; i < remainingShips.size(); i++) {
            response.append(System.lineSeparator()).append(remainingShips.get(i)).append("-")
                    .append(SHIP_UNDAMAGED_SYMBOL.repeat(i + ShipType.DESTROYER.getSize()));
        }

        response.append(System.lineSeparator());

        return response.toString();
    }

    private String yourBoard(Board board) {
        StringBuilder response = new StringBuilder("      Your board").append(System.lineSeparator());
        response.append("  1 2 3 4 5 6 7 8 9 10").append(System.lineSeparator());

        for (int i = 0; i < board.getBoardSize(); i++) {
            response.append((char)('A' + i));

            for (int j = 0; j < board.getBoardSize(); j++) {
                response.append(" ");

                Tile tile = board.getTile(new Point(i, j));
                if (!tile.isHidden() && tile.getCondition() == Condition.EMPTY) {
                    response.append(MISSED_SYMBOL);
                } else {
                    switch (tile.getCondition()) {
                        case EMPTY -> response.append(UNKNOWN_SYMBOL);
                        case SHIP_UNDAMAGED -> response.append(SHIP_UNDAMAGED_SYMBOL);
                        case SHIP_DAMAGED, SHIP_SUNK -> response.append(SHIP_DAMAGED_SYMBOL);
                    }
                }
            }
            response.append(System.lineSeparator());
        }

        return response.toString();
    }

    private String currTurn(String player) {
        StringBuilder response = new StringBuilder();

        if ((GAMES.get(currGame).isCurrPlayerTurn(player))) {
            response.append("Your turn...");
        } else {
            response.append("Opponent's turn...");
        }

        response.append(System.lineSeparator());

        return response.toString();
    }

    private String bothBoards(Board friendly, Board enemy) {
        StringBuilder response = new StringBuilder("      Your board             Opponent's board")
                .append(System.lineSeparator());
        response.append("  1 2 3 4 5 6 7 8 9 10      1 2 3 4 5 6 7 8 9 10").append(System.lineSeparator());

        for (int i = 0; i < friendly.getBoardSize(); i++) {
            response.append((char)('A' + i));

            for (int j = 0; j < friendly.getBoardSize(); j++) {
                response.append(" ");

                Tile tile = friendly.getTile(new Point(i, j));
                if (!tile.isHidden() && tile.getCondition() == Condition.EMPTY) {
                    response.append(MISSED_SYMBOL);
                } else {
                    switch (tile.getCondition()) {
                        case EMPTY -> response.append(UNKNOWN_SYMBOL);
                        case SHIP_UNDAMAGED -> response.append(SHIP_UNDAMAGED_SYMBOL);
                        case SHIP_DAMAGED, SHIP_SUNK -> response.append(SHIP_DAMAGED_SYMBOL);
                    }
                }
            }

            response.append("     ");

            response.append((char)('A' + i));

            for (int j = 0; j < enemy.getBoardSize(); j++) {
                response.append(" ");

                Tile tile = enemy.getTile(new Point(i, j));
                if (tile.isHidden()) {
                    response.append(UNKNOWN_SYMBOL);
                } else {
                    switch (tile.getCondition()) {
                        case EMPTY -> response.append(MISSED_SYMBOL);
                        case SHIP_DAMAGED, SHIP_SUNK -> response.append(SHIP_DAMAGED_SYMBOL);
                    }
                }
            }
            response.append(System.lineSeparator());
        }

        return response.toString();
    }

    private void gameOver() throws IOException {
        if (GAMES.get(currGame).getWinner().equals(nickname)) {
            dos.writeUTF("Game over" + System.lineSeparator() + "You win!" + System.lineSeparator());
            opponent.dos.writeUTF("Game over" + System.lineSeparator() + "You lose!" + System.lineSeparator());
        } else {
            dos.writeUTF("Game over" + System.lineSeparator() + "You lose!" + System.lineSeparator());
            opponent.dos.writeUTF("Game over" + System.lineSeparator() + "You win!" + System.lineSeparator());
        }

        GAMES.remove(currGame);

        currGame = null;
        stage = GameStage.HUB;

        opponent.currGame = null;
        opponent.stage = GameStage.HUB;
    }
}