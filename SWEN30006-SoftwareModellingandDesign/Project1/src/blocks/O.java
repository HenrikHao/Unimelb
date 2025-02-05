// O.java
package src.blocks;
import ch.aplu.jgamegrid.*;
import src.AbstractTetris;
import src.TetroBlock;

public class O extends Block
{
  private static final int blockId = 3;
  private static final int size = 4;
  private static final String blockName = "O";

  public O(AbstractTetris abstractTetris)
  {
    super(blockId, blockName, size, abstractTetris);

    this.abstractTetris = abstractTetris;

  }

  @Override
  public void setLocation() {
    var r = createLoc(size);
    // rotId 0
    r[0][0] = new Location(new Location(0, 0));
    r[1][0] = new Location(new Location(1, 0));
    r[2][0] = new Location(new Location(1, 1));
    r[3][0] = new Location(new Location(0, 1));
    // rotId 1
    r[0][1] = new Location(new Location(0, 0));
    r[1][1] = new Location(new Location(1, 0));
    r[2][1] = new Location(new Location(1, 1));
    r[3][1] = new Location(new Location(0, 1));
    // rotId 2
    r[0][2] = new Location(new Location(0, 0));
    r[1][2] = new Location(new Location(1, 0));
    r[2][2] = new Location(new Location(1, 1));
    r[3][2] = new Location(new Location(0, 1));
    // rotId 3
    r[0][3] = new Location(new Location(0, 0));
    r[1][3] = new Location(new Location(1, 0));
    r[2][3] = new Location(new Location(1, 1));
    r[3][3] = new Location(new Location(0, 1));
    for (Location[] locations : r) blocks.add(new TetroBlock(blockId, locations));
  }
}
