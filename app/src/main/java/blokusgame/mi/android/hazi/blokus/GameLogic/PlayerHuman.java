package blokusgame.mi.android.hazi.blokus.GameLogic;

/**
 * Created by elekt on 2014.10.21..
 */
public class PlayerHuman extends Player {
    public PlayerHuman(int _color) {
        super(_color);
    }

    @Override
    public boolean placeBlock(int blockIndex, Point coord) {
        if(Map.getInstance().getSteps()>=2)
            fillCorners();

        if(!corners.contains(coord)){
            return false;
        }
        Block block = blocks.get(blockIndex);
        boolean isPlaceable = Map.getInstance().isPlaceable(block, corners, coord);
        if(!isPlaceable){
            return false;
        }
        // lerakja a blockot
        Map map = Map.getInstance();
        for(int i = 0; i<block.getSize(); ++i){
            Point temp = new Point(coord.x +  block.getPoint(i).x, coord.y + block.getPoint(i).y);
            map.setCell(block.getColor(), temp);
        }
        blocks.remove(blockIndex);
        Map.getInstance().incStep();
        fillCorners();

        return true;
    }
}
