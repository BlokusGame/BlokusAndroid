package blokusgame.mi.android.hazi.blokus.GameLogic;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by elekt on 2014.10.21..
 */
public class Block {


    private ArrayList<Point> points = new ArrayList<Point>();
    private int color;
    private int imageId;
    private int id;

    public Point getPoint(int idx){ return points.get(idx); }
    public ArrayList<Point> getPoints() { return points; }
    public int getSize(){ return points.size(); }
    public int getColor() {return color;}
    public int getImageId(){ return imageId; }
    public int getId(){ return id; }
    public Point getMin(){
        int minX,minY;
        minX = minY = 10;
        for(Point i: points){
            minX = (i.x<minX)?i.x:minX;
            minY = (i.y<minY)?i.y:minY;
        }
        return new Point(minX, minY);
    }
    public Point getMax(){
        int maxX,maxY;
        maxX = maxY = 0;
        for(Point i: points){
            maxX = (i.x>maxX)?i.x:maxX;
            maxY = (i.y>maxY)?i.y:maxY;
        }
        return new Point(maxX, maxY);
    }
    public Point getDimensions(){//todo check
        Point min = getMin();
        Point max = getMax();
        return new Point(max.x-min.x+1,max.y-min.y+1);
    }
    public Block(ArrayList<Point> _points, int _color, int _imageId, int _id){
        color = _color;
        points = _points;
        imageId = _imageId;
        id = _id;
    }
    public Block(Block b){
        color = b.color;
        points = new ArrayList<Point>();
        for(Point i: b.points){
            points.add(new Point(i));
        }
        imageId = b.imageId;
        id = b.id;
    }
    public Block normalize(){
        ArrayList<Point> newBlock0 = new ArrayList<Point>();
        Point min = getMin();
        Point validMin = min;
        while(!points.contains(validMin)){
            validMin.x++;
        }
        //for every point in block, do substract the value of validMin from each point
        for(Point i: points){
            newBlock0.add(new Point(i.x-validMin.x,i.y-validMin.y));
        }
        Block ret = new Block(this);
        ret.points = newBlock0;//this is bad; its private
        return ret;
    }
    @Override
    public boolean equals(Object b){//todo
//        //find left and upmost point in block
//        //move it to 0,0
//        //for both this and b
//        ArrayList<Point> newBlock0 = new ArrayList<Point>();
//        ArrayList<Point> newBlock1 = new ArrayList<Point>();
//        Point min = getMin();
//        for(Point i: points){
//            newBlock0.add(new Point(i.x-min.x,i.y-min.y));
//        }
//        min = ((Block)b).getMin();
//        for(Point i: ((Block)b).points){
//            newBlock1.add(new Point(i.x-min.x,i.y-min.y));
//        }
//
//        return newBlock0.containsAll(newBlock1) && newBlock1.containsAll(newBlock0);
        Block block0 = this.normalize();
        Block block1 = ((Block)b).normalize();
        return block0.getPoints().containsAll(block1.getPoints()) && block1.getPoints().containsAll(block0.getPoints());
    }

    public Block turn(int degrees){
        Block ret = new Block(this);
        // ha jol emlekszem (1,0) volt, nem veletlen, atirtad?
        Point dVec = new Point(1,1);//inkabb 1,1 lenne a default, not sure
        boolean bSwitch = false;
        switch(degrees){
            case 0:
                break;
            case 90:
                bSwitch = true;
                dVec = new Point(-1, 1);
                break;
            case 180:
                bSwitch = false;
                dVec = new Point(-1,-1);
                break;
            case 270:
                bSwitch = true;
                dVec = new Point(1, -1);
                break;
            default:
                Log.e("WRONG TURN", "Wrong turning degree! Block::turn");
                return this;
        }

        for (int i=0; i<ret.points.size(); ++i) {
            if(bSwitch){
                ret.points.get(i).change();
            }
            ret.points.get(i).x *= dVec.x;
            ret.points.get(i).y *= dVec.y;
        }
        return ret;
    }
    ///sides==0: no action
    ///sides==1: x tengely körül, sides==1: y tengely körül
    ///tengelyek a 0,0 pontnál találkoznak
    public Block mirror(int sides){
        Block ret = new Block(this);
        Point dVec = new Point(1,1);
        switch(sides){
            case 0:
                break;
            case 1://x tengely
                dVec = new Point(-1, 1);
                break;
            case 2://y tengely
                dVec = new Point(1,-1);
                break;
            default:
                Log.e("MIRROR INPUT","Wrong mirroring input! Block::mirror");
                return this;
        }

        for (int i=0; i<ret.points.size(); ++i) {
            ret.points.get(i).x *= dVec.x;
            ret.points.get(i).y *= dVec.y;
        }
        return ret;
    }

    public boolean isContaining(Point start, Point corner) {
        for(int i=0; i<points.size(); ++i){
            Point pt = new Point(start.x+points.get(i).x, start.y+points.get(i).y);
            if(pt.equals(corner)){
                return true;
            }
        }
            return false;
    }


    // TODO
    public ArrayList<Block> getRotations() {
        ArrayList<Block> rotatedBlocks = new ArrayList<Block>();
        Block thisBlock = new Block(this);
        rotatedBlocks.add(new Block(thisBlock));
        rotatedBlocks.add(new Block(thisBlock.turn(90)));
        rotatedBlocks.add(new Block(thisBlock.turn(180)));
        rotatedBlocks.add(new Block(thisBlock.turn(270)));
        rotatedBlocks.add(new Block(thisBlock.mirror(1)));
        rotatedBlocks.add(new Block(thisBlock.mirror(2)));
        rotatedBlocks.add(new Block(thisBlock.turn(90).mirror(1)));
        rotatedBlocks.add(new Block(thisBlock.turn(180).mirror(1)));
        rotatedBlocks.add(new Block(thisBlock.turn(270).mirror(1)));
        rotatedBlocks.add(new Block(thisBlock.turn(90).mirror(2)));
        rotatedBlocks.add(new Block(thisBlock.turn(180).mirror(2)));
        rotatedBlocks.add(new Block(thisBlock.turn(270).mirror(2)));
        ArrayList<Block> rotatedBlocksSlim = new ArrayList<Block>();
        for(Block i: rotatedBlocks){
            if(!rotatedBlocksSlim.contains(i)){
                rotatedBlocksSlim.add(i);
            }
        }

        return rotatedBlocksSlim;
    }
}
