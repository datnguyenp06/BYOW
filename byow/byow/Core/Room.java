package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;



public class Room {
    TETile[][] room;
    TETile wallType;
    TETile floorType;
    int size;
    int width;
    int height;
    int xPosition;
    int yPosition;
    TETile[][] roomWithLight;
    boolean lightOn;
    int lightSourceXPosition;
    int lightSourceYPosition;
    Random random;


    /** Constructor */
    public Room(int width, int height, TETile wallType, TETile floorType,
                Random random, int xPos, int yPos) {

        this.height = height;
        this.width = width;
        this.room = new TETile[height][width];
        this.roomWithLight = new TETile[height][width];
        this.size = width * height;
        this.wallType = wallType;
        this.floorType = floorType;
        this.lightOn = false;
        this.random = random;
        this.xPosition = xPos;
        this.yPosition = yPos;

        // initialize room
        for (int x = 0; x < height; x += 1) {
            for (int y = 0; y < width; y += 1) {
                room[x][y] = Tileset.NOTHING;
                roomWithLight[x][y] = Tileset.NOTHING;
            }
        }
        addRoom();
        addRandomLightSource();
        addRoomWithLight();


    }

    /** Get the wall type of room. */
    public TETile getWallType() {
        return wallType;
    }

    /** Get the floor type of room. */
    public TETile getFloorType() {
        return floorType;
    }

    /** Get the size of hexagon. */
    public int getSize() {
        return size;
    }

    /** Add a new room. */
    private void addRoom() {

        fillColumn(room, 0, wallType);
        fillColumn(room, width - 1, wallType);
        fillRow(room, 0, wallType);
        fillRow(room, height - 1, wallType);
        fillColumn(roomWithLight, 0, wallType);
        fillColumn(roomWithLight, width - 1, wallType);
        fillRow(roomWithLight, 0, wallType);
        fillRow(roomWithLight, height - 1, wallType);
        for (int row = 1; row < height - 1; row++) {
            for (int col = 1; col < width - 1; col++) {
                room[row][col] = floorType;
                roomWithLight[row][col] = floorType;
            }
        }

    }

    /** Add a new room. */
    public void addRandomLightSource() {

        int row = RandomUtils.uniform(random, 1, height - 1);
        int col = RandomUtils.uniform(random, 1, width - 1);

        TETile lightSource = new TETile('●', new Color(149, 149, 154), Color.BLACK,
                "light source");
        room[row][col] = lightSource;
        lightSourceXPosition = xPosition + col;
        lightSourceYPosition = yPosition - row;

    }
    /** Add a new room with light on. */
    public void addRoomWithLight() {

        ArrayList<Color> lightToDarkBlue = new ArrayList<>();

        Color lightSourceColor = new Color(56, 76, 244);
        lightToDarkBlue.add(new Color(44, 62, 195));
        lightToDarkBlue.add(new Color(33, 49, 147));
        lightToDarkBlue.add(new Color(20, 28,  110));
        lightToDarkBlue.add(new Color(10, 18,  87));
        lightToDarkBlue.add(new Color(4, 9,  68));
        lightToDarkBlue.add(new Color(3, 8,  59));
        lightToDarkBlue.add(new Color(2, 4,  50));
        lightToDarkBlue.add(new Color(3, 3,  44));
        lightToDarkBlue.add(new Color(2, 2,  28));


        TETile lightSourceFloor = new TETile('●', new Color(160, 194, 207), lightSourceColor,
                "light source");

        int lightSourceRow = yPosition - lightSourceYPosition;
        int lightSourceCol = lightSourceXPosition - xPosition;

        roomWithLight[lightSourceRow][lightSourceCol] = lightSourceFloor;

        int numberOfObjects = 3;

        // Create a light effect on the top of the light source
        createLightEffect(lightToDarkBlue, lightSourceRow - 1, 0,
                width - 2, lightSourceCol - 1, numberOfObjects, true);

        // Create a light effect on the bottom of the light source
        createLightEffect(lightToDarkBlue, lightSourceRow + 1, height - 1,
                width - 2, lightSourceCol - 1, numberOfObjects, true);

        // Create a light effect on the left of the light source
        createLightEffect(lightToDarkBlue, lightSourceCol - 1, 0,
                height - 2, lightSourceRow - 1, numberOfObjects, false);

        // Create a light effect on the right of the light source
        createLightEffect(lightToDarkBlue, lightSourceCol + 1, width - 1,
                height - 2, lightSourceRow - 1, numberOfObjects, false);

    }

    /** Create a light effect for each part of the light source (top, bottom, left, or right) */
    private void createLightEffect(ArrayList<Color> colorVariants, int structureStartingIndex,
                                   int structureEndingIndex, int maxStructureIndex,
                                   int elementStartingIndex,
                                   int startingNumberOfObjectsPerElement, boolean vertical) {
        int step;
        int colorNumber = 0;
        int beginIndex;
        int endIndex;
        TETile floor;
        if (structureStartingIndex <= structureEndingIndex) {
            step = 1;
        } else {
            step = -1;
        }
        for (int index = structureStartingIndex; index != structureEndingIndex;
             index = index + step) {
            if (colorNumber >= 9) {
                floor = new TETile('·', new Color(160, 194, 207),
                        colorVariants.get(8), "floor");
            } else {
                floor = new TETile('·', new Color(160, 194, 207),
                        colorVariants.get(colorNumber), "floor");
            }

            beginIndex = Math.max(1, elementStartingIndex);
            endIndex = Math.min(maxStructureIndex, elementStartingIndex
                    + startingNumberOfObjectsPerElement - 1);
            if (vertical) {
                fillRowAtCol(roomWithLight, index, floor, beginIndex, endIndex);
            } else {
                fillColAtRow(roomWithLight, index, floor, beginIndex, endIndex);
            }

            startingNumberOfObjectsPerElement += 2;
            colorNumber += 1;
            elementStartingIndex--;
        }
    }




    /** Fill row with TETile object at given row index. */
    public void fillRow(TETile[][] currRoom, int rowIndex, TETile object) {
        for (int col = 0; col < width; col++) {
            currRoom[rowIndex][col] = object;
        }

    }

    /** Fill column with TETile object at given column index. */
    public void fillColumn(TETile[][] currRoom, int columnIndex, TETile object) {
        for (int row = 0; row < height; row++) {
            currRoom[row][columnIndex] = object;
        }

    }

    /** Fill row with TETile object at given row index. */
    private void fillRowAtCol(TETile[][] currRoom, int rowIndex, TETile object,
                              int startCol, int endCol) {
        for (int col = startCol; col <= endCol; col++) {
            currRoom[rowIndex][col] = object;
        }

    }
    /** Fill row with TETile object at given row index. */
    private void fillColAtRow(TETile[][] currRoom, int columnIndex, TETile object,
                              int startRow, int endRow) {
        for (int row = startRow; row <= endRow; row++) {
            currRoom[row][columnIndex] = object;
        }

    }


    /** Print the room. */
    public void printRoom() {
        if (room == null) {
            return;
        }
        for (int row = 0; row < width; row++) {
            for (int col = 0; col < height; col++) {
                if (room[row][col] != null) {
                    System.out.print(room[row][col].character());
                } else {
                    System.out.print(" ");
                }

            }
            System.out.println();
        }
    }

    /** Get the width of the room. */
    public int getWidth() {
        return width;

    }

    /** Get the height of the room. */
    public int getHeight() {
        return height;

    }

    /** Get the room. */
    public TETile[][] getRoom() {
        if (lightOn) {
            return roomWithLight;
        } else {
            return room;
        }
    }

    public void turnOnLight() {
        lightOn = true;
    }
    public void turnOffLight() {
        lightOn = false;
    }
    public void removeLightSource() {
        int col = lightSourceXPosition - xPosition;
        int row = yPosition - lightSourceYPosition;
        room[row][col] = floorType;
        for (int r = 1; r < height - 1; r++) {
            for (int c = 1; c < width - 1; c++) {
                roomWithLight[r][c] = room[r][c];
            }
        }

    }

}
