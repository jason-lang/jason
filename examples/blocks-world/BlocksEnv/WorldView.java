package BlocksEnv;

import jason.environment.grid.GridWorldView;

import java.awt.Color;
import java.awt.Graphics;


public class WorldView extends GridWorldView {

    public WorldView(WorldModel model) {
        super(model, "Blocks World", 600);
        setVisible(true);
        repaint();
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        switch (object) {
            case WorldModel.BLOCK:   drawBlock(g, x, y);  break;
            case WorldModel.TABLE:   drawTable(g, x, y);  break;
        }
    }
    
    public void drawBlock(Graphics g, int x, int y) {
        g.setColor(Color.red);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        g.setColor(Color.black);
        g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
        drawString(g, x, y, defaultFont, ((WorldModel)getModel()).getName(x,y));
    }

    public void drawTable(Graphics g, int x, int y) {
        g.setColor(Color.green);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        Color idColor = Color.black;
        super.drawAgent(g, x, y, c, -1);
        idColor = Color.white;   
        g.setColor(idColor);
        drawString(g, x, y, defaultFont, String.valueOf(id+1));
    }
    
//    public static void main(String[] args) throws Exception {
//        BlocksWorld env = new BlocksWorld();
//        env.init(new String[] {"1","0","yes"});
//    }
}
