package jason.environment.grid;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

/**
 * View component for a GirdWorldModel.
 *
 * @author Jomi
 */
public class GridWorldView extends JFrame {

    private static final long serialVersionUID = 1L;

    protected int cellSizeW = 0;
    protected int cellSizeH = 0;

    protected GridCanvas     drawArea;
    protected GridWorldModel model;
    protected BufferedImage backBuffer;
    private Graphics2D backG;

    protected Font defaultFont = new Font("Arial", Font.BOLD, 10);

    public GridWorldView(GridWorldModel model, String title, int windowSize) {
        super(title);
        this.model = model;
        initComponents(windowSize);
        model.setView(this);
    }

    /** sets the size of the frame and adds the components */
    public void initComponents(int width) {
        setSize(width, width);
        getContentPane().setLayout(new BorderLayout());
        drawArea = new GridCanvas();
        getContentPane().add(BorderLayout.CENTER, drawArea);
    }

    @Override
    public void repaint() {
        cellSizeW = drawArea.getWidth() / model.getWidth();
        cellSizeH = drawArea.getHeight() / model.getHeight();
        super.repaint();
    }

    /** updates all the frame */
    public void update() {
        ensureBackBuffer();
        repaint();
    }

    /** updates only one position of the grid */
    public void update(int x, int y) {
        if (!SwingUtilities.isEventDispatchThread()) {
            // Only the event dispatch thread can update the GUI
            SwingUtilities.invokeLater(this::update);
            return;
        }

        ensureBackBuffer();
        renderAllToBackBuffer();
        drawArea.repaint();
    }

    public void drawObstacle(Graphics g, int x, int y) {
        g.setColor(Color.darkGray);
        g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
        g.setColor(Color.black);
        g.drawRect(x * cellSizeW + 2, y * cellSizeH+2, cellSizeW-4, cellSizeH-4);
    }

    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        g.setColor(c);
        g.fillOval(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
        if (id >= 0) {
            g.setColor(Color.black);
            drawString(g, x, y, defaultFont, String.valueOf(id+1));
        }
    }

    public void drawString(Graphics g, int x, int y, Font f, String s) {
        g.setFont(f);
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth( s );
        int height = metrics.getHeight();
        g.drawString( s, x*cellSizeW+(cellSizeW/2-width/2), y*cellSizeH+(cellSizeH/2+height/2));
    }

    public void drawEmpty(Graphics g, int x, int y) {
        g.setColor(Color.white);
        g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-2, cellSizeH-2);
        g.setColor(Color.lightGray);
        g.drawRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }


    /** method to draw unknown object, probably overridden by the user viewer class */
    public void draw(Graphics g, int x, int y, int object) {
        //g.setColor(Color.black);
        //drawString(g,x,y,defaultFont,String.valueOf(object));
    }

    private static final int limit = (int)Math.pow(2,14);

    public Canvas getCanvas() {
        return drawArea;
    }

    public GridWorldModel getModel() {
        return model;
    }

    class GridCanvas extends Canvas {

        private static final long serialVersionUID = 2L;

        @Override
        public void update(Graphics g) {
            paint(g);
        }

        public void paint(Graphics g) {
            ensureBackBuffer();
            g.drawImage(backBuffer, 0, 0, this);
            Toolkit.getDefaultToolkit().sync();
        }
    }

    private void ensureBackBuffer() {
        int w = Math.max(1, drawArea.getWidth());
        int h = Math.max(1, drawArea.getHeight());
        if (backBuffer == null || backBuffer.getWidth() != w || backBuffer.getHeight() != h) {
            if (backG != null) {
                backG.dispose();
            }
            backBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            backG = backBuffer.createGraphics();
            backG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        renderAllToBackBuffer();
    }

    private void renderAllToBackBuffer() {
        cellSizeW = Math.max(1, backBuffer.getWidth() / model.getWidth());
        cellSizeH = Math.max(1, backBuffer.getHeight() / model.getHeight());

        backG.setColor(Color.white);
        backG.fillRect(0, 0, backBuffer.getWidth(), backBuffer.getHeight());

        backG.setColor(Color.lightGray);
        for (int l = 1; l <= backBuffer.getHeight(); l++) {
            backG.drawLine(0, l * cellSizeH, model.getWidth() * cellSizeW, l * cellSizeH);
        }
        for (int c = 1; c <= backBuffer.getWidth(); c++) {
            backG.drawLine(c * cellSizeW, 0, c * cellSizeW, model.getHeight() *  cellSizeH);
        }

        for (int x = 0; x < model.getWidth(); x++) {
            for (int y = 0; y < model.getHeight(); y++) {
                renderCell(backG, x, y);
            }
        }
    }

    private void renderCell(Graphics2D g, int x, int y) {
        drawEmpty(g, x, y);

        if ((model.data[x][y] & GridWorldModel.OBSTACLE) != 0) {
            drawObstacle(g, x, y);
        }

        int vl = GridWorldModel.OBSTACLE*2; // the next  object after OBSTACLE
        while (vl < limit) {
            if ((model.data[x][y] & vl) != 0) {
                draw(g, x, y, vl);
            }
            vl *= 2;
        }

        if ((model.data[x][y] & GridWorldModel.AGENT) != 0) {
            drawAgent(g, x, y, Color.blue, model.getAgAtPos(x, y));
        }
    }
}
