package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;



import java.awt.Font;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Calendar;

public class Engine {
    private static final File CWD = new File(System.getProperty("user.dir"));
    private static final File SEEDFILE = Utils.join(CWD, "seedStr.txt");
    private static final File MOVEFILE = Utils.join(CWD, "moveSequence.txt");

    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    public static final int FRAMEHEIGHT = HEIGHT + 2; // Include space for Heads Up Display
    private TETile[][] generatedWorldFrame;
    private ArrayList<Room> rooms = new ArrayList<>();
    private int avatarXPos; // Avatar x position
    private int avatarYPos; // Avatar y position
    private String seedStr; //  Generated seed number
    private String moveSequence = "";
    private TETile currentTETile; // current tile underneath the avatar
    ArrayList<Room> roomsWithoutLight = new ArrayList<>(); // all rooms with light off
    Deque<Room> roomsWithLight = new ArrayDeque<>(); // all rooms with light on

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {

        ter.initialize(WIDTH, FRAMEHEIGHT);
        String nSeedNumberS = drawMainMenu();
        if (nSeedNumberS.charAt(0) == 'N') {
            interactWithInputString(nSeedNumberS);
            drawWorldFrame();

        } else if (nSeedNumberS.charAt(0) == 'L') {
            try {
                byte[] seed = Files.readAllBytes(SEEDFILE.toPath());
                nSeedNumberS = new String(seed, StandardCharsets.UTF_8);
                nSeedNumberS = "N" + nSeedNumberS + "S";
                byte[] move = Files.readAllBytes(MOVEFILE.toPath());
                moveSequence = new String(move, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                return;
            }

            interactWithInputString(nSeedNumberS);
            drawWorldFrame();
        }
        System.exit(0);

    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        TETile[][] finalWorldFrame = new TETile[WIDTH][FRAMEHEIGHT];



        // initialize tiles
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < FRAMEHEIGHT; y += 1) {
                finalWorldFrame[x][y] = Tileset.NOTHING;
            }
        }


        input = input.toUpperCase();
        String nSeedNumberS = input;

        if (input.charAt(0) == 'N') {
            nSeedNumberS = input.substring(0, input.indexOf('S') + 1);
            moveSequence += input.substring(input.indexOf('S') + 1);
        } else if (input.charAt(0) == 'L') {
            try {
                byte[] seed = Files.readAllBytes(SEEDFILE.toPath());
                nSeedNumberS = new String(seed, StandardCharsets.UTF_8);
                nSeedNumberS = "N" + nSeedNumberS + "S";
                byte[] move = Files.readAllBytes(MOVEFILE.toPath());
                moveSequence = new String(move, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                return null;
            }
            moveSequence += input.substring(1);
        }
        generateRandomWorld(finalWorldFrame, nSeedNumberS);

        generatedWorldFrame = finalWorldFrame;
        loadGame();

        return finalWorldFrame;
    }

    /** Print the generated world. */
    public void printGeneratedWorld() {
        for (int y = HEIGHT - 1; y >= 0; y -= 1) {
            for (int x = 0; x < WIDTH; x += 1) {
                System.out.print(generatedWorldFrame[x][y].character());
            }
            System.out.println();
        }
    }

    /** Return the generated world as a String. */
    @Override
    public String toString() {
        String generatedWorldStr = "";
        for (int y = HEIGHT - 1; y >= 0; y -= 1) {
            for (int x = 0; x < WIDTH; x += 1) {
                generatedWorldStr += generatedWorldFrame[x][y].character();
            }
            generatedWorldStr += "\n";
        }
        return generatedWorldStr;
    }

    /** Add a room at the given x and y position. */
    public void addRoomAt(int x, int y, Room room, TETile[][] worldFrame) {

        for (int yPosition = 0; yPosition < room.getHeight(); yPosition++) {
            TETile[] roomRow = room.getRoom()[yPosition];
            for (int xPosition = 0; xPosition < room.getWidth(); xPosition++) {
                if (roomRow[xPosition].character() != Tileset.NOTHING.character()) {
                    worldFrame[x + xPosition][y - yPosition] = roomRow[xPosition];
                }

            }
        }
    }
    /** Add a the floor of the given room at the given x and y position. */
    public void addFloorAt(int x, int y, Room room, TETile[][] worldFrame) {


        for (int yPosition = 1; yPosition < room.getHeight() - 1; yPosition++) {
            TETile[] roomRow = room.getRoom()[yPosition];
            for (int xPosition = 1; xPosition < room.getWidth() - 1; xPosition++) {
                if (roomRow[xPosition].character() != Tileset.NOTHING.character()) {
                    worldFrame[x + xPosition][y - yPosition] = roomRow[xPosition];
                }

            }
        }
    }

    /** Create a floor at a given x and y position */
    public void createFloor(int x, int y, int height, int width, TETile[][] worldFrame) {
        for (int yPos = 0; yPos < height; yPos += 1) {
            for (int xPos = 0; xPos < width; xPos += 1) {
                worldFrame[xPos + x][yPos + y] = Tileset.FLOOR;
            }
        }
    }

    /** Get seed number from input and generate a random world */
    private void generateRandomWorld(TETile[][] worldFrame, String input) {

        // Get the seed value

        seedStr = input.substring(1, input.indexOf('S'));

        int maxSeedLength = Long.toString(Long.MAX_VALUE).length();
        long seed;
        try {
            seed = Long.parseLong(seedStr);
        } catch (NumberFormatException ex) {
            seedStr = seedStr.substring(0, maxSeedLength - 1);
            seed = Long.parseLong(seedStr);
        }
        Random random = new Random(seed);

        // Get a random number of rooms
        int numRooms = RandomUtils.uniform(random, 5, 16);
        int maxWidthPerRoom;

        // Width, height and position of a room
        int randomRWidth;
        int randomRHeight;
        int xPos = WIDTH;
        int yPos;

        while (xPos >= 0 && numRooms > 0) {

            maxWidthPerRoom = xPos / numRooms;
            // Create a new room width and height and update x and y positions
            randomRWidth = RandomUtils.uniform(random, 4, maxWidthPerRoom + 1);
            randomRHeight = RandomUtils.uniform(random, 4, 16);
            xPos -= RandomUtils.uniform(random, randomRWidth, maxWidthPerRoom + 1);
            yPos = RandomUtils.uniform(random, randomRHeight - 1, 30);


            // Add a new room to the world frame
            Room newRoom = new Room(randomRWidth, randomRHeight,
                    Tileset.WALL, Tileset.FLOOR, random, xPos, yPos);

            addRoomAt(xPos, yPos, newRoom, worldFrame);
            rooms.add(newRoom);
            roomsWithoutLight.add(newRoom);

            // Decrease the number of rooms
            numRooms -= 1;

        }
        ArrayList<Room> verRooms = addVerticalRooms(worldFrame, random);
        connectRoom(worldFrame, random);
        //connectVerticalRooms(worldFrame, random, verRooms);
        // Add avatar
        Room lastRoom = rooms.get(rooms.size() - 1);
        avatarXPos = lastRoom.xPosition + lastRoom.getWidth() / 2;
        avatarYPos = lastRoom.yPosition - lastRoom.getHeight() / 2;
        currentTETile = worldFrame[avatarXPos][avatarYPos];
        worldFrame[avatarXPos][avatarYPos] = Tileset.AVATAR;
    }

    private void connectRoom(TETile[][] worldFrame, Random random) {
        for (int i = 1; i < rooms.size(); i++) {
            Room first = rooms.get(i - 1);
            Room second = rooms.get(i);
            addHallway(worldFrame, random, first, second);

        }
    }

    private void addHallway(TETile[][] worldFrame, Random random,
                            Room first, Room second) {
        if (first.xPosition > second.xPosition) {
            Room temp = first;
            first = second;
            second = temp;
        }
        int distanceUp;
        int distanceDown;
        int distanceRight;
        distanceRight = Math.abs(first.xPosition - second.xPosition);
        int yPositionDiff = first.yPosition - second.yPosition;
        int randDistance = RandomUtils.uniform(random, 3, 5);
        if (yPositionDiff > 0) {
            distanceUp = randDistance;
            distanceDown = randDistance + yPositionDiff;
        } else {
            distanceUp = randDistance - yPositionDiff;
            distanceDown = randDistance;
        }
        int startX = first.xPosition + 2;
        int startY = first.yPosition;
        //  add hallway below rooms if adding pathway above exceeds the max height
        if (startY + distanceUp + 1 >= HEIGHT) {
            startY = first.yPosition - first.getHeight() + 1;
            if (yPositionDiff < 0) {
                distanceUp = second.yPosition - second.getHeight()
                        - (first.yPosition - first.getHeight()) + 3;
                distanceDown = 3;
            } else {
                distanceUp = 3;
                distanceDown = first.yPosition - first.getHeight()
                        - (second.yPosition - second.getHeight()) + 3;
            }
            // return if adding hallway below exceeds height range
            if (startY - distanceDown - 1 < 0) {
                addHallwayInBetween(worldFrame, random, first, second);
                return;
            }
            addHallwayUpOrDown(worldFrame, startX, startY, distanceDown, false);
            if (worldFrame[startX - 1][startY - distanceDown - 1].character()
                    != Tileset.FLOOR.character()
                    && worldFrame[startX - 1][startY - distanceDown - 1].character() != '●') {
                worldFrame[startX - 1][startY - distanceDown - 1] = Tileset.WALL;
            }
            addHallwayToRight(worldFrame,  startX,
                    startY - distanceDown, distanceRight);
            if (worldFrame[startX + distanceRight + 1][startY - distanceDown - 1]
                    .character() != Tileset.FLOOR.character()
                    && worldFrame[startX + distanceRight + 1][startY - distanceDown
                    - 1].character() != '●') {
                worldFrame[startX + distanceRight + 1]
                        [startY - distanceDown - 1] = Tileset.WALL;
            }
            addHallwayUpOrDown(worldFrame, startX + distanceRight,
                    startY - distanceDown, distanceUp, true);

        } else {
            // add hallway above if max height is not exceeded
            addHallwayUpOrDown(worldFrame, startX, startY, distanceUp, true);
            if (worldFrame[startX - 1][startY + distanceUp + 1]
                    .character() != Tileset.FLOOR.character()
                    && worldFrame[startX - 1][startY + distanceUp + 1].character() != '●') {
                worldFrame[startX - 1][startY + distanceUp + 1] = Tileset.WALL;
            }
            addHallwayToRight(worldFrame, startX, startY + distanceUp, distanceRight);
            if (worldFrame[startX + distanceRight + 1][startY + distanceUp + 1]
                    .character() != Tileset.FLOOR.character()
                    && worldFrame[startX + distanceRight + 1][startY + distanceUp
                    + 1].character() != '●') {
                worldFrame[startX + distanceRight + 1]
                        [startY + distanceUp + 1] = Tileset.WALL;
            }
            addHallwayUpOrDown(worldFrame, startX + distanceRight,
                    startY + distanceUp, distanceDown, false);
        }
    }

    // helper function that add part of a hallway that goes either up or down
    private void addHallwayUpOrDown(TETile[][] worldFrame,
                                   int startX, int startY, int distance, boolean isUp) {
        if (isUp) {
            for (int i = 0; i <= distance; i += 1) {
                // hallway going down
                if (worldFrame[startX - 1][startY + i]
                        .character() != Tileset.FLOOR.character()
                        && worldFrame[startX - 1][startY + i].character() != '●') {
                    worldFrame[startX - 1][startY + i] = Tileset.WALL;
                }
                if (worldFrame[startX][startY + i].character() != '●') {
                    worldFrame[startX][startY + i] = Tileset.FLOOR;
                }
                if (worldFrame[startX + 1][startY + i]
                        .character() != Tileset.FLOOR.character()
                        && worldFrame[startX + 1][startY + i].character() != '●') {
                    worldFrame[startX + 1][startY + i] = Tileset.WALL;
                }
            }
        } else {
            for (int i = 0; i <= distance; i += 1) {
                // hallway going down
                if (worldFrame[startX - 1][startY - i]
                        .character() != Tileset.FLOOR.character()
                        && worldFrame[startX - 1][startY - i].character() != '●') {
                    worldFrame[startX - 1][startY - i] = Tileset.WALL;
                }
                if (worldFrame[startX][startY - i].character() != '●') {
                    worldFrame[startX][startY - i] = Tileset.FLOOR;
                }
                if (worldFrame[startX + 1][startY - i]
                        .character() != Tileset.FLOOR.character()
                        && worldFrame[startX + 1][startY - i].character() != '●') {
                    worldFrame[startX + 1][startY - i] = Tileset.WALL;
                }
            }
        }
    }

    // helper function that add part of a hallway that goes to the right
    private void addHallwayToRight(TETile[][] worldFrame,
                                   int startX, int startY, int distance) {
        for (int i = 0; i <= distance; i += 1) {
            // hallway going to the right
            if (worldFrame[startX + i][startY - 1]
                    .character() != Tileset.FLOOR.character()
                    && worldFrame[startX + i][startY - 1].character() != '●') {
                worldFrame[startX + i][startY - 1] = Tileset.WALL;
            }
            if (worldFrame[startX + i][startY].character() != '●') {
                worldFrame[startX + i][startY] = Tileset.FLOOR;
            }
            if (worldFrame[startX + i][startY + 1]
                    .character() != Tileset.FLOOR.character()
                    && worldFrame[startX + i][startY + 1].character() != '●') {
                worldFrame[startX + i][startY + 1] = Tileset.WALL;
            }
        }
    }
    private void addHallwayInBetween(TETile[][] worldFrame, Random random,
                                     Room first, Room second) {
        if (first.xPosition > second.xPosition) {
            Room temp = first;
            first = second;
            second = temp;
        }

        int startX = first.xPosition + first.getWidth() - 1;
        int endX = second.xPosition;
        int startY = first.yPosition - RandomUtils.uniform
                (random, 1, first.getHeight() - 1);
        int endY = second.yPosition - RandomUtils.uniform
                (random, 1, second.getHeight() - 1);
        int firstHalf = (endX - startX) / 2;
        int secondHalf = (endX - startX) - firstHalf;

        addHallwayToRight(worldFrame, startX, startY, firstHalf);

        if (endY > startY) {
            // hallway going up
            int verticalDiff = endY - startY;
            if (worldFrame[startX + firstHalf + 1][startY - 1]
                    .character() != Tileset.FLOOR.character()
                    && worldFrame[startX + firstHalf + 1][startY - 1].character() != '●') {
                worldFrame[startX + firstHalf + 1][startY - 1] = Tileset.WALL;
            }
            addHallwayUpOrDown(worldFrame, startX + firstHalf,
                    startY, verticalDiff, true);

            if (worldFrame[startX + firstHalf - 1][endY + 1]
                    .character() != Tileset.FLOOR.character()
                    && worldFrame[startX + firstHalf - 1][endY + 1].character() != '●') {
                worldFrame[startX + firstHalf - 1][endY + 1] = Tileset.WALL;
            }
        } else {
            // hallway going down
            int verticalDiff = startY - endY;
            if (worldFrame[startX + firstHalf + 1][startY + 1]
                    .character() != Tileset.FLOOR.character()
                    && worldFrame[startX + firstHalf + 1][startY + 1].character() != '●') {
                worldFrame[startX + firstHalf + 1][startY + 1] = Tileset.WALL;
            }
            addHallwayUpOrDown(worldFrame, startX + firstHalf,
                    startY, verticalDiff, false);

            if (worldFrame[startX + firstHalf - 1][endY - 1]
                    .character() != Tileset.FLOOR.character()
                    && worldFrame[startX + firstHalf - 1][endY - 1].character() != '●') {
                worldFrame[startX + firstHalf - 1][endY - 1] = Tileset.WALL;
            }
        }
        addHallwayToRight(worldFrame, startX + firstHalf, endY, secondHalf);

    }

    /** Draw the given text at a given position. Text is formatted based on given font, color
     * and alignment (C: Center, R: Right, L: Left) */
    public void drawTextAt(int x, int y, String s, Font font, Color color, char aligned) {

        Font oldFont = StdDraw.getFont();
        Color oldPenColor = StdDraw.getPenColor();
        StdDraw.setFont(font);
        StdDraw.setPenColor(color);
        if (aligned == 'L') {
            StdDraw.textLeft(x, y, s);
        } else if (aligned == 'R') {
            StdDraw.textRight(x, y, s);
        } else {
            StdDraw.text(x, y, s);
        }

        StdDraw.show();
        StdDraw.setFont(oldFont);
        StdDraw.setPenColor(oldPenColor);

    }

    /** Draw the main menu when the user first starts the game using keyboard. Return the
     * generated seed string that can be used on function interactWithInputString()*/
    public String drawMainMenu() {
        Font font = new Font("Monaco", Font.PLAIN, 20);
        Color color = new Color(0, 204, 255);
        boolean finish = false;
        String seedString = "";
        char pressedKey = ' ';
        while (!finish) {
            StdDraw.clear(Color.BLACK);
            drawStartMenu();
            StdDraw.pause(500);
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'N') {
                    drawTextAt(WIDTH / 2, FRAMEHEIGHT / 2 - 5, "Enter Seed", font, color, 'C');
                    while (pressedKey != 'S') {
                        if (StdDraw.hasNextKeyTyped()) {
                            pressedKey = Character.toUpperCase(StdDraw.nextKeyTyped());
                            if (Character.isDigit(pressedKey)) {
                                seedString += pressedKey;
                                StdDraw.clear(Color.BLACK);
                                drawStartMenu();
                                drawTextAt(WIDTH / 2, FRAMEHEIGHT / 2 - 5, "Enter Seed",
                                        font, color, 'C');
                                drawTextAt(WIDTH / 2, FRAMEHEIGHT / 2 - 6, seedString,
                                        font, color, 'C');
                                StdDraw.pause(500);
                            }


                        }
                    }
                    seedString = "N" + seedString + "S";
                    finish = true;
                } else if (c == 'Q') {
                    seedString = "Q";
                    finish = true;
                } else if (c == 'L') {
                    seedString = "L";
                    finish = true;
                }
            }

        }
        return seedString;


    }

    /** Draw the start menu that include the name of the game and basic options */
    public void drawStartMenu() {
        Font font = new Font("Monaco", Font.PLAIN, 20);
        Color color = new Color(0, 204, 255);

        drawTextAt(WIDTH / 2, FRAMEHEIGHT - FRAMEHEIGHT / 6, "CS61B: THE GAME",
                font, color, 'C');
        drawTextAt(WIDTH / 2, FRAMEHEIGHT / 2, "NEW GAME (N)",
                font, color, 'C');
        drawTextAt(WIDTH / 2, FRAMEHEIGHT / 2 - 1, "LOAD GAME (L)",
                font, color, 'C');
        drawTextAt(WIDTH / 2, FRAMEHEIGHT / 2 - 2, "QUIT (Q)",
                font, color, 'C');

    }

    /** Draw the generated world frame on the screen. */
    public void drawWorldFrame() {
        Font font = new Font("Monaco", Font.ITALIC, 20);
        Color color = new Color(255, 255, 0);
        char lastPressedKey = ' ';
        Calendar timestamp;
        String currentTime;
        while (true) {
            ter.renderFrame(generatedWorldFrame);
            int mX = (int) StdDraw.mouseX();
            int mY = (int) StdDraw.mouseY();
            timestamp = Calendar.getInstance();
            DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
            currentTime = sdf.format(timestamp.getTime());
            if (mX >= 0 && mX < WIDTH && mY >= 0 && mY < FRAMEHEIGHT) {
                drawTextAt(0, FRAMEHEIGHT - 1, generatedWorldFrame[mX][mY].description(),
                        font, color, 'L');
            } else {
                drawTextAt(0, FRAMEHEIGHT - 1, "nothing",
                        font, color, 'L');
            }
            drawTextAt(WIDTH, FRAMEHEIGHT - 1, currentTime, font, color, 'R');
            if (StdDraw.hasNextKeyTyped()) {
                char pressedKey = Character.toUpperCase(StdDraw.nextKeyTyped());
                int newXPos = avatarXPos;
                int newYPos = avatarYPos;
                if (pressedKey == 'W') {
                    TETile targetTile = generatedWorldFrame[avatarXPos][avatarYPos + 1];
                    newXPos = avatarXPos;
                    newYPos = avatarYPos + 1;
                    moveAvatar(targetTile, newXPos, newYPos);
                    moveSequence += pressedKey;
                } else if (pressedKey == 'A') {
                    TETile targetTile = generatedWorldFrame[avatarXPos - 1][avatarYPos];
                    newXPos = avatarXPos - 1;
                    newYPos = avatarYPos;
                    moveAvatar(targetTile, newXPos, newYPos);
                    moveSequence += pressedKey;
                } else if (pressedKey == 'S') {
                    TETile targetTile = generatedWorldFrame[avatarXPos][avatarYPos - 1];
                    newXPos = avatarXPos;
                    newYPos = avatarYPos - 1;
                    moveAvatar(targetTile, newXPos, newYPos);
                    moveSequence += pressedKey;
                } else if (pressedKey == 'D') {
                    TETile targetTile = generatedWorldFrame[avatarXPos + 1][avatarYPos];
                    newXPos = avatarXPos + 1;
                    newYPos = avatarYPos;
                    moveAvatar(targetTile, newXPos, newYPos);
                    moveSequence += pressedKey;
                } else if (pressedKey == 'O') {
                    if (turnLightOn())  {
                        moveSequence += pressedKey;
                    }
                } else if (pressedKey == 'F') {
                    if (turnLightOff()) {
                        moveSequence += pressedKey;
                    }
                } else if (pressedKey == 'N') {
                    resetTheWorldFrame();
                    ter.initialize(WIDTH, FRAMEHEIGHT);
                    interactWithInputString(getNewSeed());
                    drawWorldFrame();
                    return;
                } else {
                    if (lastPressedKey == ':' && pressedKey == 'Q') {
                        saveGame();
                        return;
                    }
                }
                lastPressedKey = pressedKey;
            }
            StdDraw.pause(100);
        }
    }

    public void resetTheWorldFrame() {
        // Reset everything
        generatedWorldFrame = new TETile[WIDTH][FRAMEHEIGHT];
        rooms = new ArrayList<>();
        avatarXPos = -1;
        avatarYPos = -1;
        seedStr = "";
        moveSequence = "";
        currentTETile = null;
        roomsWithoutLight = new ArrayList<>();
        roomsWithLight = new ArrayDeque<>();
    }
    public String getNewSeed() {
        Font font = new Font("Monaco", Font.PLAIN, 20);
        Color color = new Color(0, 204, 255);
        String seedString = "";
        char pressedKey = ' ';
        StdDraw.clear(Color.BLACK);
        drawTextAt(WIDTH / 2, FRAMEHEIGHT / 2, "Enter New Seed",
                font, color, 'C');
        while (pressedKey != 'S') {
            if (StdDraw.hasNextKeyTyped()) {
                pressedKey = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (Character.isDigit(pressedKey)) {
                    seedString += pressedKey;
                    StdDraw.clear(Color.BLACK);
                    drawTextAt(WIDTH / 2, FRAMEHEIGHT / 2, "Enter New Seed",
                            font, color, 'C');
                    drawTextAt(WIDTH / 2, FRAMEHEIGHT / 2 - 1, seedString,
                            font, color, 'C');
                    StdDraw.pause(500);
                }
            }
        }
        seedString = "N" + seedString + "S";
        return seedString;

    }


    /** helper function that save the state of the game to a text file */
    private void saveGame() {
        if (SEEDFILE.exists()) {
            SEEDFILE.delete();
        }
        if (MOVEFILE.exists()) {
            MOVEFILE.delete();
        }
        try {
            SEEDFILE.createNewFile();
            MOVEFILE.createNewFile();


            FileOutputStream seedOut = new FileOutputStream(SEEDFILE);
            byte[] seedBytes = seedStr.getBytes(StandardCharsets.UTF_8);
            seedOut.write(seedBytes);

            FileOutputStream moveOut = new FileOutputStream(MOVEFILE);
            byte[] moveBytes = moveSequence.getBytes(StandardCharsets.UTF_8);
            moveOut.write(moveBytes);

            seedOut.close();
            moveOut.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }



    /** helper function that moves the avatar according the string */
    private void loadGame() {

        int newXPos = avatarXPos;
        int newYPos = avatarYPos;
        char lastChar = ' ';

        for (int i = 0; i <  moveSequence.length(); i += 1) {
            char currentKey = moveSequence.charAt(i);
            if (currentKey == 'W') {
                TETile targetTile = generatedWorldFrame[avatarXPos][avatarYPos + 1];
                newXPos = avatarXPos;
                newYPos = avatarYPos + 1;
                moveAvatar(targetTile, newXPos, newYPos);

            } else if (currentKey == 'A') {
                TETile targetTile = generatedWorldFrame[avatarXPos - 1][avatarYPos];
                newXPos = avatarXPos - 1;
                newYPos = avatarYPos;
                moveAvatar(targetTile, newXPos, newYPos);

            } else if (currentKey == 'S') {
                TETile targetTile = generatedWorldFrame[avatarXPos][avatarYPos - 1];
                newXPos = avatarXPos;
                newYPos = avatarYPos - 1;
                moveAvatar(targetTile, newXPos, newYPos);

            } else if (currentKey == 'D') {
                TETile targetTile = generatedWorldFrame[avatarXPos + 1][avatarYPos];
                newXPos = avatarXPos + 1;
                newYPos = avatarYPos;
                moveAvatar(targetTile, newXPos, newYPos);
            } else if (currentKey == 'O') {
                turnLightOn();
            } else if (currentKey == 'F') {
                turnLightOff();
            } else if (currentKey == 'N') {
                String newSeedString = moveSequence.substring(i);
                resetTheWorldFrame();
                interactWithInputString(newSeedString);
                return;
            } else {
                if (lastChar == ':' && currentKey == 'Q') {
                    moveSequence = moveSequence.substring(0, moveSequence.indexOf(":Q"));
                    saveGame();
                    return;
                }

            }
            lastChar = currentKey;

        }

    }



    /** Move avatar to the specified position. */
    public void moveAvatar(TETile target, int newX, int newY) {
        TETile avatar = generatedWorldFrame[avatarXPos][avatarYPos];
        if (target.character() != Tileset.WALL.character()) {
            generatedWorldFrame[newX][newY] = avatar;
            generatedWorldFrame[avatarXPos][avatarYPos] = currentTETile;
            avatarXPos = newX;
            avatarYPos = newY;
            currentTETile = target;
        }
    }


    /** Turn a random room's light on if it has not turned on */
    public boolean turnLightOn() {
        if (roomsWithoutLight.isEmpty()) {
            return false;
        }
        long seed = Integer.parseInt(seedStr);
        Random random = new Random(seed);
        int roomIndex = RandomUtils.uniform(random, 0, roomsWithoutLight.size());
        Room selectedRoom = roomsWithoutLight.remove(roomIndex);
        selectedRoom.turnOnLight();
        roomsWithLight.push(selectedRoom);
        addFloorAt(selectedRoom.xPosition, selectedRoom.yPosition,
                selectedRoom, generatedWorldFrame);
        TETile curr = generatedWorldFrame[avatarXPos][avatarYPos];
        if (curr.character() != Tileset.AVATAR.character()) {
            currentTETile = curr;
        }
        generatedWorldFrame[avatarXPos][avatarYPos] = Tileset.AVATAR;
        return true;
    }

    /** Turn recently added room's light off if it has already turned on */
    public boolean turnLightOff() {
        if (roomsWithLight.isEmpty()) {
            return false;
        }
        Room selectedRoom = roomsWithLight.pop();
        selectedRoom.turnOffLight();
        roomsWithoutLight.add(selectedRoom);
        addFloorAt(selectedRoom.xPosition, selectedRoom.yPosition,
                selectedRoom, generatedWorldFrame);
        TETile curr = generatedWorldFrame[avatarXPos][avatarYPos];
        if (curr.character() != Tileset.AVATAR.character()) {
            currentTETile = curr;
        }
        generatedWorldFrame[avatarXPos][avatarYPos] = Tileset.AVATAR;
        return true;
    }

    private ArrayList<Room> addVerticalRooms(TETile[][] worldFrame, Random random) {
        int maxHeight = 10;
        int maxWidth = 10;
        int roomNums = rooms.size();
        ArrayList<Room> verRooms = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int randomRoomNum = RandomUtils.uniform(random, 0, roomNums - 1);
            int oldWidth = rooms.get(randomRoomNum).getWidth();
            int oldHeight = rooms.get(randomRoomNum).getHeight();
            int randomRWidth = RandomUtils.uniform(random, 5, maxWidth);
            int randomRHeight = RandomUtils.uniform(random, 5, maxHeight);
            int xPos = rooms.get(randomRoomNum).xPosition
                    + RandomUtils.uniform(random, 1, oldWidth - 1);
            if (xPos + randomRWidth + 2 >= WIDTH) {
                xPos = rooms.get(randomRoomNum).xPosition - randomRWidth
                        - RandomUtils.uniform(random, 1, oldWidth - 1);
            }
            int yPos = rooms.get(randomRoomNum).yPosition + randomRHeight
                    + RandomUtils.uniform(random, 1, oldHeight - 1);
            if (yPos >= HEIGHT) {
                yPos = rooms.get(randomRoomNum).yPosition - oldHeight
                        - RandomUtils.uniform(random, 1, oldHeight - 1);
            }
            if (yPos - randomRHeight - 2 <= 0) {
                continue;
            }

            Room newRoom = new Room(randomRWidth, randomRHeight,
                    Tileset.WALL, Tileset.FLOOR, random, xPos, yPos);
            newRoom.removeLightSource();
            if (!checkOverlap(worldFrame, newRoom)) {
                addRoomAt(xPos, yPos, newRoom, worldFrame);
                verRooms.add(newRoom);
                rooms.add(newRoom);
                Room second = rooms.get(randomRoomNum + 1);
                addHallway(worldFrame, random, newRoom, second);
                //roomsWithoutLight.add(newRoom);
            }
        }
        return verRooms;

    }

    private void connectVerticalRooms(TETile[][] worldFrame,
                                      Random random, ArrayList<Room> verRooms) {
        int roomNums = rooms.size();
        for (int i = 0; i < verRooms.size(); i += 1) {
            Room first = verRooms.get(i);
            int roomNumber = RandomUtils.uniform(random, 0, roomNums - 1);
            Room second = rooms.get(roomNumber);
            addHallway(worldFrame, random, first, second);
            rooms.add(first);
        }
    }

    private boolean checkOverlap(TETile[][] worldFrame, Room newRoom) {
        int xPos = newRoom.xPosition;
        int yPos = newRoom.yPosition;
        int width = newRoom.getWidth();
        int height = newRoom.getHeight();
        if (worldFrame[xPos][yPos].character() != Tileset.NOTHING.character()) {
            return true;
        }
        if (worldFrame[xPos + width][yPos].character() != Tileset.NOTHING.character()) {
            return true;
        }
        if (worldFrame[xPos + width][yPos - height].character() != Tileset.NOTHING.character()) {
            return true;
        }
        return false;
    }


}
