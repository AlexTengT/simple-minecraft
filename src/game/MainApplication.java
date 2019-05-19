package game;

import static javafx.scene.layout.GridPane.setConstraints;

import csse2002.block.world.Block;
import csse2002.block.world.Builder;
import csse2002.block.world.InvalidBlockException;
import csse2002.block.world.NoExitException;
import csse2002.block.world.Position;
import csse2002.block.world.TooHighException;
import csse2002.block.world.TooLowException;
import csse2002.block.world.WorldMap;
import csse2002.block.world.WorldMapFormatException;
import csse2002.block.world.WorldMapInconsistentException;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/*
MainClass to run the GUI of the game
 */
public class MainApplication extends Application {

    // The world map, default is null
    private static WorldMap map = null;
    // The position of the builder, default is null
    private static Position currentPosition = null;
    // The drawer that draw the map
    private static Drawer drawer;
    // The builder
    private static Builder builder = null;
    // The inventory label showing the items in the inventory
    private static Label builderInventoryLabel;
    // buttons in the GUI
    private static HashMap<String, Button> buttonMap;
    // Separator in the system
    private static final String LINE_SEP = System.lineSeparator();

    /**
     * Entry point to the program
     *
     * @param args arguments to the program
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Start point of the GUI
     *
     * @param primaryStage the primaryStage for the GUI
     */
    @Override
    public void start(Stage primaryStage) {
        //main grid pane
        GridPane root = new GridPane();
        primaryStage.setTitle("Game");

        // Divide into to to part, left for canvas and Inventory, right is for
        // Controller
        GridPane left = new GridPane();
        GridPane right = new GridPane();

        // The label showing Inventory contains
        Label builderInventoryText = new Label("Builder Inventory");
        builderInventoryLabel = new Label("[]");

        // Builder the drawer
        drawer = new Drawer();
        updateCanvas();

        //file menu
        Menu fileMenu = new Menu("File");
        MenuItem m1 = new MenuItem("Save Map");
        MenuItem m2 = new MenuItem("Load World Map");
        fileMenu.getItems().addAll(m1, m2);
        m1.setOnAction(event -> saveFile(primaryStage));
        m2.setOnAction(event -> loadMap());

        //The menu in the top of the window
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu);

        // builder control
        buttonMap = new HashMap<>();
        Controller(right);
        buttonController();
        left.add(drawer, 0, 1);
        left.add(builderInventoryText, 0, 2);
        left.add(builderInventoryLabel, 0, 3);

        setConstraints(menuBar, 0, 0);
        setConstraints(right, 1, 1);
        setConstraints(left, 0, 1);

        root.getChildren().addAll(left, right, menuBar);

        right.setPadding(new Insets(20, 20, 20, 20));
        left.setPadding(new Insets(20, 20, 20, 40));

        left.setPrefWidth(420);
        builderInventoryLabel.setPrefHeight(80);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        // primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Setup the controller
     *
     * @param left the left pane of the root
     */
    private void Controller(GridPane left) {
        //divide controller into direction and controlPad two parts
        GridPane directionButtons = new GridPane();
        GridPane controlPad = new GridPane();

        Button north = new Button("North");
        Button west = new Button("West");
        Button east = new Button("East");
        Button south = new Button("South");

        directionButtons.add(north, 1, 0);
        directionButtons.add(west, 0, 1);
        directionButtons.add(east, 2, 1);
        directionButtons.add(south, 1, 3);

        Button dig = new Button("Dig");
        Button drop = new Button("drop");
        TextField dropIndex = new TextField();
        dropIndex.setPromptText("Drop index");

        //setup the choicebox
        HBox ChoiceBox = new HBox();
        ChoiceBox<String> moveChoiceBox = new ChoiceBox<>();
        moveChoiceBox.getItems().addAll("Move Builder", "Move Block");
        moveChoiceBox.setValue("Move Builder");

        ChoiceBox.getChildren().add(moveChoiceBox);
        ChoiceBox.setPadding(new Insets(10, 10, 10, 80));
        controlPad.setPadding(new Insets(10, 0, 0, 0));
        controlPad.setHgap(10);

        controlPad.add(drop, 0, 0);
        controlPad.add(dropIndex, 1, 0);

        left.add(directionButtons, 0, 0);
        left.add(ChoiceBox, 0, 1);

        left.add(dig, 0, 2);
        left.add(controlPad, 0, 3);

        // setup action to each buttons
        north.setOnAction(event -> move(moveChoiceBox, "north"));
        south.setOnAction(event -> move(moveChoiceBox, "south"));
        east.setOnAction(event -> move(moveChoiceBox, "east"));
        west.setOnAction(event -> move(moveChoiceBox, "west"));

        dig.setOnAction(event -> dig());
        drop.setOnAction(event -> drop(dropIndex));

        // add value to buttonMap in order to update the button states
        buttonMap.put("north", north);
        buttonMap.put("south", south);
        buttonMap.put("east", east);
        buttonMap.put("west", west);
        buttonMap.put("drop", drop);
        buttonMap.put("dig", dig);

        // setup the styles of buttons
        setButtonStyle(new Button[]{north, south, east, west},
                new Button[]{dig, drop});
    }

    /**
     * Setup the styles of buttons
     *
     * @param directionButtons directions buttons
     * @param otherButtons other buttons
     */
    private void setButtonStyle(Button[] directionButtons,
            Button[] otherButtons) {

        DropShadow shadow = new DropShadow();

        //Set buttons to gradient blue
        for (Button b: directionButtons) {
            b.setStyle("    -fx-background-color: \n"
                    + "        #000000,\n"
                    + "        linear-gradient(#7ebcea, #2f4b8f),\n"
                    + "        linear-gradient(#426ab7, #263e75),\n"
                    + "        linear-gradient(#395cab, #223768);\n"
                    + "    -fx-background-insets: 0,1,2,3;\n"
                    + "    -fx-background-radius: 3,2,2,2;\n"
                    + "    -fx-padding: 12 30 12 30;\n"
                    + "    -fx-text-fill: white;\n"
                    + "    -fx-font-size: 12px;");
            //Set the event effect
            b.addEventHandler(MouseEvent.MOUSE_PRESSED,
                    e -> b.setEffect(shadow));

            b.addEventHandler(MouseEvent.MOUSE_EXITED,
                    e -> b.setEffect(null));
        }

        for (Button b: otherButtons) {
            b.setStyle("    -fx-background-color: \n"
                    + "        #000000,\n"
                    + "        linear-gradient(#7ebcea, #2f4b8f),\n"
                    + "        linear-gradient(#426ab7, #263e75),\n"
                    + "        linear-gradient(#395cab, #223768);\n"
                    + "    -fx-background-insets: 0,1,2,3;\n"
                    + "    -fx-background-radius: 3,2,2,2;\n"
                    + "    -fx-padding: 5 10 5 10;\n"
                    + "    -fx-text-fill: white;\n"
                    + "    -fx-font-size: 12px;");

            b.addEventHandler(MouseEvent.MOUSE_PRESSED,
                    e -> b.setEffect(shadow));

            b.addEventHandler(MouseEvent.MOUSE_EXITED,
                    e -> b.setEffect(null));
        }
    }

    /**
     * Pop a file choose dialog to choose map file
     */
    private void loadMap() {
        String filePath;
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            filePath = selectedFile.getPath();
        } else {
            popDiagram(AlertType.INFORMATION, "Wrong in chooseing file");
            return;
        }
        try {
            map = new WorldMap(filePath);
            currentPosition = map.getStartPosition();
            builder = map.getBuilder();
            drawer.loadMapToDrawer(map);
            updateCanvas();
            builderInventoryLabel
                    .setText(encodeBlocks(builder.getInventory()));
            buttonController();

        } catch (WorldMapInconsistentException | WorldMapFormatException
                | FileNotFoundException e) {
            exceptionPopDialog(e);

        }
    }

    /**
     * Pop a file choose dialog to save file
     *
     * @param primary the primaryStage
     */
    private void saveFile(Stage primary) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        File file = fileChooser.showSaveDialog(primary);
        if (file != null) {
            try {
                map.saveMap(file.toString());
            } catch (IOException e) {
                popDiagram(AlertType.ERROR, "Save Map Failed, IO FAILED");
            }
        }
    }


    /**
     * Let users input a number in textField, then drop the corresponding item
     * in inventory
     *
     * @param dropIndex the TextField
     */
    private void drop(TextField dropIndex) {

        try {
            builder.dropFromInventory(
                    Integer.valueOf(
                            dropIndex.getCharacters().toString()) - 1);

        } catch (InvalidBlockException | TooHighException |
                NumberFormatException e) {
            exceptionPopDialog(e);

        }
        updateCanvas();
        builderInventoryLabel
                .setText(encodeBlocks(builder.getInventory()));

    }

    /**
     * encode the blocks to String that user can read
     *
     * @param blocks Blocks that need to encode to String
     * @return encoded blocks
     */
    private static String encodeBlocks(List<Block> blocks) {

        StringBuilder result = new StringBuilder();
        result.append("[");
        int i = 0;
        for (Block item: blocks) {
            result.append(item.getBlockType()).append(", ");
            i += 1;
            if (i % 8 == 0) {
                result.append(LINE_SEP);
            }
        }

        result.deleteCharAt(result.length() - 2);
        result.append("]");
        return result.toString();
    }

    /**
     * dig a block in current Tile
     */
    private void dig() {

        try {
            builder.digOnCurrentTile();
        } catch (InvalidBlockException | TooLowException e) {
            exceptionPopDialog(e);
            return;
        }
        builderInventoryLabel
                .setText(encodeBlocks(builder.getInventory()));
        updateCanvas();
    }

    /**
     * Pop a diagram showing information
     *
     * @param alertType the alertType that required to show
     * @param text the text showed on the dialog
     */
    private void popDiagram(AlertType alertType, String text) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Warning Dialog");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    /**
     * Move builder or block to another
     *
     * @param choice choice, whether to move builder or block
     * @param direction the direction
     */
    private void move(ChoiceBox<String> choice, String direction) {
        switch (choice.getValue()) {
            case "Move Builder":
                moveBuilder(direction);
                buttonController();
                break;
            case "Move Block":
                moveBlock(direction);
                buttonController();
                break;
            default:
                popDiagram(AlertType.INFORMATION,
                        "You Haven't Choose An Option");
                break;
        }

    }

    /**
     * move block to the direction
     *
     * @param direction the direction to move
     */
    private void moveBlock(String direction) {
        try {
            builder.getCurrentTile().moveBlock(direction);
        } catch (TooHighException | InvalidBlockException | NoExitException e) {
            exceptionPopDialog(e);
            return;
        }
        updateCanvas();
    }

    /**
     * move builder to the given direction
     *
     * @param direction the direction to move
     */
    private void moveBuilder(String direction) {

        try {
            if (builder.getCurrentTile().getExits().get(direction)
                    != null) {
                if (builder.canEnter(
                        builder.getCurrentTile().getExits()
                                .get(direction))) {
                    builder.moveTo(
                            builder.getCurrentTile().getExits()
                                    .get(direction));
                } else {
                    popDiagram(AlertType.INFORMATION,
                            "INVALID DIRECTION \n No Exit in that direction or cannot enter to that exits");
                    return;

                }

            } else {
                popDiagram(AlertType.INFORMATION,
                        "INVALID DIRECTION \n No Exit in that direction or cannot enter to that exits");
                return;
            }

            // update the current position
            int x = currentPosition.getX();
            int y = currentPosition.getY();
            switch (direction) {
                case "north":
                    currentPosition = new Position(x, y - 1);
                    break;
                case "south":
                    currentPosition = new Position(x, y + 1);
                    break;
                case "east":
                    currentPosition = new Position(x + 1, y);
                    break;
                case "west":
                    currentPosition = new Position(x - 1, y);
                    break;
                default:
                    popDiagram(AlertType.INFORMATION, "INVALID DIRECTION");
            }
            builderInventoryLabel
                    .setText(encodeBlocks(builder.getInventory()));
            updateCanvas();

        } catch (NoExitException e) {
            exceptionPopDialog(e);
        }
    }

    /**
     * update the state of the buttons
     */
    private void buttonController() {
        if (map == null || currentPosition == null || builder == null) {
            buttonMap.get("north").setDisable(true);
            buttonMap.get("south").setDisable(true);
            buttonMap.get("east").setDisable(true);
            buttonMap.get("west").setDisable(true);
            buttonMap.get("drop").setDisable(true);
            buttonMap.get("dig").setDisable(true);

        } else {

            buttonMap.get("north").setDisable(true);
            buttonMap.get("south").setDisable(true);
            buttonMap.get("east").setDisable(true);
            buttonMap.get("west").setDisable(true);
            buttonMap.get("drop").setDisable(true);
            buttonMap.get("dig").setDisable(true);

            if (builder.getCurrentTile().getExits().containsKey("north")) {
                buttonMap.get("north").setDisable(false);
            }

            if (builder.getCurrentTile().getExits()
                    .containsKey("south")) {
                buttonMap.get("south").setDisable(false);
            }

            if (builder.getCurrentTile().getExits()
                    .containsKey("east")) {
                buttonMap.get("east").setDisable(false);
            }

            if (builder.getCurrentTile().getExits()
                    .containsKey("west")) {
                buttonMap.get("west").setDisable(false);
            }
            try {
                if (builder.getCurrentTile().getTopBlock().isDiggable()) {
                    buttonMap.get("dig").setDisable(false);
                }
            } catch (TooLowException e) {
                exceptionPopDialog(e);
            }

            buttonMap.get("drop").setDisable(false);
        }

    }

    /**
     * update the canvas
     */
    private void updateCanvas() {
        drawer.update(currentPosition);
    }

    /**
     * Convert exception to dialog
     *
     * @param e the exception
     */

//    WorldMapInconsistentException | WorldMapFormatException | FileNotFoundException
    private void exceptionPopDialog(Exception e) {
        switch (e.getClass().getSimpleName()) {
            case "NoExitException":
                popDiagram(AlertType.INFORMATION, "Cannot find the Exit");
                break;
            case "WorldMapInconsistentException":
                popDiagram(AlertType.INFORMATION, "Map is inconsistent");
                break;
            case "WorldMapFormatException":
                popDiagram(AlertType.INFORMATION, "The format of map is wrong");
                break;
            case "FileNotFoundException":
                popDiagram(AlertType.INFORMATION, "Cannot find the file");
                break;
            case "InvalidBlockException":
                popDiagram(AlertType.INFORMATION, "Block is invalid");
                break;
            case "TooHighException":
                popDiagram(AlertType.INFORMATION, "The target is too high");
                break;
            case "TooLowException":
                popDiagram(AlertType.INFORMATION, "The target is too low");
                break;
            case "NumberFormatException":
                popDiagram(AlertType.INFORMATION, "The number format is wrong");
                break;


        }

    }


}