package game;

import csse2002.block.world.Block;
import csse2002.block.world.GrassBlock;
import csse2002.block.world.SoilBlock;
import csse2002.block.world.StoneBlock;
import csse2002.block.world.TooLowException;
import csse2002.block.world.WoodBlock;
import csse2002.block.world.WorldMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import csse2002.block.world.Tile;
import csse2002.block.world.Position;
import javafx.scene.paint.Color;

/**
 * the class used to draw
 */
class Drawer extends Canvas {

    // The graphicsContext object containing the canvas
    private GraphicsContext gc;
    private WorldMap map;

    // The position that the is builder is on
    private Position currentPosition;

    // The length of each tile
    private final double TILE_LENGTH = 40;

    // The maximum number of tiles in one axis
    private double MAX_TILE_NUMBER = 9;

    // the x of mid point
    private double MID_X = (TILE_LENGTH * MAX_TILE_NUMBER) / 2;
    // the y of mid point
    private double MID_Y = (TILE_LENGTH * MAX_TILE_NUMBER) / 2;

    // the length of canvas
    private double CANVAS_LENGTH = TILE_LENGTH * MAX_TILE_NUMBER;

    /**
     * constructor for drawer, setup the lineWidth
     */
    Drawer() {
        //setup the graphics context 2D
        gc = getGraphicsContext2D();
        gc.setLineWidth(1.0);
    }


    /**
     * Load world map the drawer
     *
     * @param map WorldMap the need to render
     */
    void loadMapToDrawer(WorldMap map) {
        this.map = map;
        this.currentPosition = map.getStartPosition();
    }

    /**
     * Updates the map and redraws any changes made to it
     */
    void update(Position currentPosition) {
        // update the current position
        this.currentPosition = currentPosition;

        setWidth(CANVAS_LENGTH);
        setHeight(CANVAS_LENGTH);
        gc.clearRect(0, 0, getWidth(), getHeight());

        if (currentPosition == null) {
            gc.fillText("No map is loaded. \nPlease load Map", MID_X - 50,
                    MID_Y - 10);
            gc.strokeRect(0, 0, CANVAS_LENGTH, CANVAS_LENGTH);
            return;
            // if no map is loaded, do not display
        }

        // draw tile
        for (int x = currentPosition.getX() - 4;
                x <= currentPosition.getX() + 4; x++) {
            for (int y = currentPosition.getY() - 4;
                    y <= currentPosition.getY() + 4; y++) {
                Position pos = new Position(x, y);
                if (map.getTile(pos) != null) {
                    drawTile(x, y, map.getTile(new Position(x, y)));

                }
            }
        }
    }

    /**
     * draw tile from the given point
     *
     * @param x the x coordinate of x
     * @param y the y coordinate of y
     * @param tile the tile that need drawing
     */
    private void drawTile(int x, int y, Tile tile) {
        // draw the border of the canvas
        gc.strokeRect(0, 0, CANVAS_LENGTH, CANVAS_LENGTH);

        // Calculate mid point of one tile with respect to the global mid point
        // of the canvas
        double xMid = MID_X + 40 * (x - currentPosition.getX());
        double yMid = MID_Y + 40 * (y - currentPosition.getY());

        // Get the final coordinates to draw the tile  at
        double xPos = (xMid - TILE_LENGTH / 2);
        double yPos = (yMid - TILE_LENGTH / 2);
        Block topBlock;

        // Draws the box
        try {
            topBlock = tile.getTopBlock();
        } catch (TooLowException e) {
            System.err.println("Cannot get top Block \n" + e.toString());
            return;
        }

        // set the color of the each tile
        if (topBlock instanceof WoodBlock) {
            gc.setFill(Color.BROWN);

        } else if (topBlock instanceof GrassBlock) {
            gc.setFill(Color.GREEN);

        } else if (topBlock instanceof SoilBlock) {
            gc.setFill(Color.BLACK);

        } else if (topBlock instanceof StoneBlock) {
            gc.setFill(Color.GREY);

        }

        // draw the tile border
        gc.strokeRect(xPos, yPos, TILE_LENGTH, TILE_LENGTH);
        // fill the tile
        gc.fillRect(xPos, yPos, TILE_LENGTH, TILE_LENGTH);

        // draw the builder point
        gc.setFill(Color.YELLOW);
        gc.fillOval(MID_X - 1.5, MID_Y - 14, 4, 4);

        //gc.fillPolygon(new double[]{0, 10, 0}, new double[]{0, 10, 10}, 3);

        // The offset off each tile
        double OFFSET_1 = 6;
        double OFFSET_2 = 15;
        double OFFSET_3 = 18;

        // draw exits triangle
        gc.setFill(Color.WHITE);
        gc.fillText(Integer.toString(tile.getBlocks().size()), xMid - 3.5,
                yMid + 3.5, 7);
        for (String exit: tile.getExits().keySet()) {
            gc.setFill(Color.WHITE);
            switch (exit) {
                case "north":
                    gc.fillPolygon(
                            new double[]{xMid, xMid - OFFSET_1,
                                    xMid + OFFSET_1},
                            new double[]{yMid - OFFSET_3, yMid - OFFSET_2,
                                    yMid - OFFSET_2},
                            3);
                    break;
                case "south":
                    gc.fillPolygon(
                            new double[]{xMid, xMid - OFFSET_1,
                                    xMid + OFFSET_1},
                            new double[]{yMid + OFFSET_3, yMid + OFFSET_2,
                                    yMid + OFFSET_2},
                            3);
                    break;
                case "east":
                    gc.fillPolygon(
                            new double[]{xMid + OFFSET_3, xMid + OFFSET_2,
                                    xMid + OFFSET_2},
                            new double[]{yMid, yMid - OFFSET_1,
                                    yMid + OFFSET_1},
                            3);
                    break;
                case "west":
                    gc.fillPolygon(
                            new double[]{xMid - OFFSET_3, xMid - OFFSET_2,
                                    xMid - OFFSET_2},
                            new double[]{yMid, yMid - OFFSET_1,
                                    yMid + OFFSET_1},
                            3);
                    break;
                default:
                    break;
            }
        }
    }
}