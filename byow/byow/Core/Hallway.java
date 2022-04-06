package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Hallway {
    TETile[][] hallway;
    TETile wallType;
    TETile floorType;
    int size;
    int width;
    int height;
    int xPostion;
    int yPostion;
    boolean isVertical;

    /** Constructor */
    public Hallway(int height, int width, TETile wallType, TETile floorType, boolean vertical) {

        this.height = height;
        this.width = width;
        this.hallway = new TETile[height][width];
        this.size = width * height;
        this.wallType = wallType;
        this.floorType = floorType;
        this.isVertical = vertical;

        // initialize hallway
        for (int x = 0; x < height; x += 1) {
            for (int y = 0; y < this.width; y += 1) {
                hallway[x][y] = Tileset.NOTHING;
            }
        }
        addHallway(vertical);
    }

    /** Get the wall type of hallway. */
    public TETile getWallType() {
        return wallType;
    }

    /** Get the floor type of hallway. */
    public TETile getFloorType() {
        return floorType;
    }

    /** Get the size of hallway. */
    public int getSize() {
        return size;
    }

    /** Add a new hallway. */
    private void addHallway(boolean vertical) {

        if (vertical) {
            fillColumn(0, wallType);
            fillColumn(width - 1, wallType);
            fillColumn(1, floorType);
        } else {
            fillRow(0, wallType);
            fillRow(height - 1, wallType);
            fillRow(1, floorType);
        }


    }



    /** Fill row with TETile object at given row index. */
    public void fillRow(int rowIndex, TETile object) {
        for (int col = 0; col < width; col++) {
            hallway[rowIndex][col] = object;
        }

    }

    /** Fill column with TETile object at given column index. */
    public void fillColumn(int columnIndex, TETile object) {
        for (int row = 0; row < height; row++) {
            hallway[row][columnIndex] = object;
        }

    }


    /** Print the hallway. */
    public void printHallway() {
        if (hallway == null) {
            return;
        }
        for (int row = 0; row < width; row++) {
            for (int col = 0; col < height; col++) {
                if (hallway[row][col] != null) {
                    System.out.print(hallway[row][col].character());
                } else {
                    System.out.print(" ");
                }

            }
            System.out.println();
        }
    }

    /** Get the width of the hallway. */
    public int getWidth() {
        return width;
    }

    /** Get the height of the hallway. */
    public int getHeight() {
        return height;
    }

    /** Get the hallway. */
    public TETile[][] getHallway() {
        return hallway;
    }
    public void setXPosition(int x) {
        xPostion = x;
    }
    public void setYPosition(int y) {
        yPostion = y;
    }
}
