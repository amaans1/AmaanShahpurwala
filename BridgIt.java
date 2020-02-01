
/*

Team: Amaan Shahpurwala & Ritika Ponna

BrideIt is a simple game. Players click to place a continuous connected 
path of bridges from one side of the board to the other while attempting 
to block their opponent from doing the same.

*/
import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Arrays;

// represents a single square of the game area in white, pink or magenta
class Cell {
  int x;
  int y;
  Color color;
  Cell left;
  Cell right;
  Cell up;
  Cell down;

  // the constructor
  Cell(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
  }

  // draws the cell on the board
  ArrayList<Cell> addCell() {
    ArrayList<Cell> result = new ArrayList<Cell>(
        Arrays.asList(left, right, up, down));
    for (int i = 0; i < result.size(); i++) {
      Cell c = result.get(i);
      if (c == null || !c.color.equals(this.color)) {
        result.remove(i);
        i--;
      }
    }
    return result;
  }
}


//the game class
class BridgitWorld extends World {


  ArrayList<ArrayList<Cell>> board;
  Color bgColor = Color.WHITE;
  Color p1Color = Color.PINK;
  Color p2Color = Color.MAGENTA;
  int cellSize = 50;

  int size;
  boolean isP1Turn = true; 
  boolean isGameOverHuh = false;
  Posn coords;

  // the constructor
  BridgitWorld(int length) {
    if (length >= 3 && length % 2 == 1) {
      this.size = length;
    }

    else {
      throw new IllegalArgumentException(
          "Invalid grid size, must be greater than or equal to 3");
    }

    this.size = length;
    this.board = new ArrayList<ArrayList<Cell>>();
    this.fillCell();
    this.coords = new Posn(2 + length * cellSize, length * cellSize + 40);
  }

  // fills the clicked on cell with color
  void fillCell() {
    for (int i = 0; i < this.size; i++) {
      ArrayList<Cell> row = new ArrayList<Cell>();
      for (int j = 0; j < this.size; j++) {
        Color cellColor = bgColor;
        if ((j % 2 == 0 && i % 2 == 1)) {
          cellColor = this.p1Color;
        }
        else if ((i % 2 == 0 && j % 2 == 1)) {
          cellColor = this.p2Color;
        }
        Cell newCell = new Cell(j, i, cellColor);
        if (i > 0) {
          this.board.get(i - 1).get(j).down = newCell;
          newCell.up = this.board.get(i - 1).get(j);
        }
        if (j > 0) {
          row.get(j - 1).right = newCell;
          newCell.left = row.get(j - 1);
        }
        row.add(newCell);
      }
      this.board.add(row);
    }
  }

  // draws the board
  public WorldScene makeScene() {
    WorldScene endScene = new WorldScene(coords.x, coords.y);
    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        Cell cell = this.board.get(i).get(j);
        endScene.placeImageXY(
            new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, cell.color),
            (int) (1 + (j + 0.5) * cellSize), (int) ((i + 0.5) * cellSize));
      }
    }
    endScene.placeImageXY(new RectangleImage(cellSize * size, cellSize * size,
        OutlineMode.OUTLINE, Color.black), coords.x / 2, cellSize * size / 2);
    boolean p1Won = checkP1();
    String str = "Player " + (isP1Turn ? 1 : 2) + " Go!";
    if (isGameOverHuh || checkP2()) {
      String winner = "Two";
      str = "Click to restart game";
      if (p1Won) {
        winner = "One";
      }
      endScene.placeImageXY(
          new TextImage("Player " + winner + " won!!!", 30, Color.black),
          coords.x / 2, coords.y / 2);
    }

    endScene.placeImageXY(
        new TextImage(str, 22, (isP1Turn ? p1Color : p2Color)),
        this.coords.x / 2, (coords.y + size * cellSize) / 2);

    return endScene;
  }

  //handles the mouse event
  public void onMouseClicked(Posn posn) {
    if (isGameOverHuh) {
      isGameOverHuh = false;
      this.board = new ArrayList<ArrayList<Cell>>();
      fillCell();
      return;
    }

    if (posn.y > cellSize * size) {
      return;
    }
    Cell isClicked = this.board.get(posn.y / cellSize).get(posn.x / cellSize);
    if (!isClicked.color.equals(this.bgColor) || isClicked.y == 0
        || isClicked.y == this.size - 1 || isClicked.x == 0
        || isClicked.x == this.size - 1) {
      return;
    }

    if (this.isP1Turn) {
      isClicked.color = this.p1Color;
    }

    else {
      isClicked.color = this.p2Color;
    }

    this.isP1Turn = !this.isP1Turn;
  }

  // searches the game for neighbours of the current player click 
  boolean searchGame(ArrayList<Cell> firstCell, ArrayList<Cell> endCells) {
    ArrayList<ArrayList<Cell>> edges = new ArrayList<ArrayList<Cell>>();
    for (Cell c : firstCell) {
      edges.add(new ArrayList<Cell>(Arrays.asList(c)));
    }
    ArrayList<Cell> seen = new ArrayList<Cell>();
    while (!edges.isEmpty()) {
      ArrayList<Cell> thisEdges = edges.remove(edges.size() - 1);
      Cell nextCell = thisEdges.get(thisEdges.size() - 1);
      if (seen.contains(nextCell)) {
        continue;
      }

      if (endCells.contains(nextCell)) {
        return true;
      }

      seen.add(nextCell);
      for (Cell prev : nextCell.addCell()) {
        ArrayList<Cell> nextEdge = new ArrayList<Cell>(thisEdges);
        nextEdge.add(prev);
        edges.add(nextEdge);
      }
    }

    return false;
  }

  // checks if P1 won
  boolean checkP1() {
    ArrayList<Cell> pOneStart = new ArrayList<Cell>();
    ArrayList<Cell> pOneGoal = new ArrayList<Cell>();

    for (ArrayList<Cell> row : this.board) {
      pOneStart.add(row.get(0));
      pOneGoal.add(row.get(this.size - 1));
    }

    boolean result = this.searchGame(pOneStart, pOneGoal);
    if (!isGameOverHuh) {
      isGameOverHuh = result;
    }

    return result;
  }

  // checks if P2 won
  boolean checkP2() {
    boolean result = this.searchGame(this.board.get(0), this.board.get(size - 1));
    if (!isGameOverHuh) {
      isGameOverHuh = result;
    }

    return result;
  }
}

// examples class
class ExampleBridgit {

  BridgitWorld world1;
  ArrayList<Cell> list;
  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5;
  Cell cell6;
  Cell cell7;
  Cell cell8;
  Cell cell9;

  // initialize the game
  public void initialize() {
    cell1 = new Cell(1, 1, Color.white);
    cell2 = new Cell(2, 1, Color.pink);
    cell3 = new Cell(3, 1, Color.white);
    cell4 = new Cell(1, 2, Color.magenta);
    cell5 = new Cell(2, 2, Color.white);
    cell6 = new Cell(3, 2, Color.magenta);
    cell7 = new Cell(1, 3, Color.white);
    cell8 = new Cell(2, 3, Color.pink);
    cell9 = new Cell(3, 3, Color.white);
    list = new ArrayList<Cell>(
        Arrays.asList(cell1, cell2, cell3, cell4, cell5, cell6, cell7, cell8, cell9));
    world1 = new BridgitWorld(3);
  }

  // test addCell
  void testAddCell(Tester t) {
    this.initialize();
    WorldScene game1 = new WorldScene(90, 90);
    WorldScene game2 = new WorldScene(90, 90);
    WorldScene bg1 = new WorldScene(90, 90);
    WorldScene bg2 = new WorldScene(90, 90);
    game1.placeImageXY(new RectangleImage(30, 30, OutlineMode.SOLID, Color.white), 15, 15);
    t.checkExpect(cell1.addCell(), game1);
    game2.placeImageXY(new RectangleImage(30, 30, OutlineMode.SOLID, Color.white), 15, 75);
    t.checkExpect(cell7.addCell(), game2);
  }

  // test makeScene
  void testMakeScene(Tester t) {
    this.initialize();
    WorldScene bg1 = new WorldScene(90, 90);
    ArrayList<Cell> game2 = this.cell1
        .addCell();

    t.checkExpect(this.world1.makeScene(), game2);
  }

  //test checkP_
  void testCheckP(Tester t) {
    initialize();
    t.checkExpect(world1.checkP1(), false);
    t.checkExpect(world1.checkP2(), false);
  }

  // run the game
  void testBigBang(Tester t) {
    BridgitWorld g = new BridgitWorld(11);
    g.bigBang(g.coords.x, g.coords.y);

  }
}
