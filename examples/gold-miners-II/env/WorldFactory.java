package env;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** factory for some scenarios */
public class WorldFactory  {


    /** no gold/no obstacle world */
    public static WorldModel world1() throws Exception {
        WorldModel model = new WorldModel(21, 21, 6);
        model.setPSim(0);
        model.setPMax(0);
        model.setDepot(5, 7);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 3, 20);
        model.setAgPos(3, 20, 20);
        model.setInitialNbGolds(model.countObjects(WorldModel.GOLD));
        return model;
    }

    // to test scan
    public static WorldModel world2() throws Exception {
        WorldModel model = new WorldModel(30, 30, 6);
        model.setDepot(5, 7);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 1, 2);
        model.setAgPos(2, 1, 3);
        model.setAgPos(3, 1, 4);
        model.setAgPos(4, 1, 5);
        model.setAgPos(5, 1, 6);
        model.setInitialNbGolds(model.countObjects(WorldModel.GOLD));
        return model;
    }

    /** world with gold, no obstacle */
    public static WorldModel world3() throws Exception {
        WorldModel model = new WorldModel(15, 15, 6);
        model.setPSim(0);
        model.setPMax(0);
        model.setDepot(5, 7);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 10, 0);
        model.setAgPos(2, 3, 10);
        model.setAgPos(3, 10, 10);
        model.setAgPos(4, 10, 11);
        model.setAgPos(5, 10, 12);
        model.add(WorldModel.GOLD, 10, 10);
        model.add(WorldModel.GOLD, 10, 14);
        model.setInitialNbGolds(model.countObjects(WorldModel.GOLD));
        return model;
    }

    /** world with gold, no obstacle */
    public static WorldModel world4() throws Exception {
        WorldModel model = new WorldModel(35, 35, 6);
        model.setMaxSteps(200);
        model.setPSim(0);
        model.setPMax(0);
        model.setDepot(15,5); //5, 27);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 3, 20);
        model.setAgPos(3, 20, 20);
        model.setAgPos(4, 20, 2);
        model.setAgPos(5, 20, 1);
        model.add(WorldModel.GOLD, 20, 13);
        model.add(WorldModel.GOLD, 15, 20);
        model.add(WorldModel.GOLD, 1, 1);
        model.add(WorldModel.GOLD, 3, 5);
        model.add(WorldModel.GOLD, 24, 24);
        model.add(WorldModel.GOLD, 20, 20);
        model.add(WorldModel.GOLD, 20, 21);
        model.add(WorldModel.GOLD, 20, 22);
        model.add(WorldModel.GOLD, 20, 23);
        model.add(WorldModel.GOLD, 20, 24);

        model.add(WorldModel.GOLD, 19, 19);
        model.add(WorldModel.GOLD, 19, 20);
        model.add(WorldModel.GOLD, 19, 21);
        model.add(WorldModel.GOLD, 19, 22);
        model.add(WorldModel.GOLD, 19, 23);
        model.add(WorldModel.GOLD, 19, 24);

        model.add(WorldModel.GOLD, 18, 19);
        model.add(WorldModel.GOLD, 18, 20);
        model.add(WorldModel.GOLD, 18, 21);
        model.add(WorldModel.GOLD, 18, 22);
        model.add(WorldModel.GOLD, 18, 23);
        model.add(WorldModel.GOLD, 18, 24);

        model.add(WorldModel.GOLD, 21, 20);
        model.add(WorldModel.GOLD, 21, 21);
        model.add(WorldModel.GOLD, 21, 22);
        model.add(WorldModel.GOLD, 21, 23);
        model.add(WorldModel.GOLD, 21, 24);
        model.add(WorldModel.GOLD, 21, 25);
        model.add(WorldModel.GOLD, 21, 26);
        model.add(WorldModel.GOLD, 34, 34);
        model.setInitialNbGolds(model.countObjects(WorldModel.GOLD));
        return model;
    }

    /** world with gold and obstacles */
    public static WorldModel world5() throws Exception {
        WorldModel model = new WorldModel(35, 35, 12);
        model.setPSim(0);
        model.setPMax(0);

        model.setDepot(16, 16);

        // red team
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 6, 26);
        model.setAgPos(3, 20, 20);
        model.setAgPos(4, 3, 4);
        model.setAgPos(5, 7, 4);

        // blue team
        model.setAgPos(6, 2, 2);
        model.setAgPos(7, 30, 31);
        model.setAgPos(8, 8, 26);
        model.setAgPos(9, 21, 20);
        model.setAgPos(10, 31, 31);
        model.setAgPos(11, 31, 32);

        model.add(WorldModel.GOLD, 20, 13);
        model.add(WorldModel.GOLD, 15, 20);
        model.add(WorldModel.GOLD, 1, 1);
        model.add(WorldModel.GOLD, 3, 5);
        model.add(WorldModel.GOLD, 24, 24);
        model.add(WorldModel.GOLD, 20, 20);
        model.add(WorldModel.GOLD, 20, 21);
        model.add(WorldModel.GOLD, 2, 22);
        model.add(WorldModel.GOLD, 2, 12);
        model.add(WorldModel.GOLD, 19, 2);
        model.add(WorldModel.GOLD, 14, 4);
        model.add(WorldModel.GOLD, 34, 34);

        model.add(WorldModel.OBSTACLE, 12, 3);
        model.add(WorldModel.OBSTACLE, 13, 3);
        model.add(WorldModel.OBSTACLE, 14, 3);
        model.add(WorldModel.OBSTACLE, 15, 3);
        model.add(WorldModel.OBSTACLE, 18, 3);
        model.add(WorldModel.OBSTACLE, 19, 3);
        model.add(WorldModel.OBSTACLE, 20, 3);
        model.add(WorldModel.OBSTACLE, 14, 8);
        model.add(WorldModel.OBSTACLE, 15, 8);
        model.add(WorldModel.OBSTACLE, 16, 8);
        model.add(WorldModel.OBSTACLE, 17, 8);
        model.add(WorldModel.OBSTACLE, 19, 8);
        model.add(WorldModel.OBSTACLE, 20, 8);

        model.add(WorldModel.OBSTACLE, 12, 32);
        model.add(WorldModel.OBSTACLE, 13, 32);
        model.add(WorldModel.OBSTACLE, 14, 32);
        model.add(WorldModel.OBSTACLE, 15, 32);
        model.add(WorldModel.OBSTACLE, 18, 32);
        model.add(WorldModel.OBSTACLE, 19, 32);
        model.add(WorldModel.OBSTACLE, 20, 32);
        model.add(WorldModel.OBSTACLE, 14, 28);
        model.add(WorldModel.OBSTACLE, 15, 28);
        model.add(WorldModel.OBSTACLE, 16, 28);
        model.add(WorldModel.OBSTACLE, 17, 28);
        model.add(WorldModel.OBSTACLE, 19, 28);
        model.add(WorldModel.OBSTACLE, 20, 28);

        model.add(WorldModel.OBSTACLE, 3, 12);
        model.add(WorldModel.OBSTACLE, 3, 13);
        model.add(WorldModel.OBSTACLE, 3, 14);
        model.add(WorldModel.OBSTACLE, 3, 15);
        model.add(WorldModel.OBSTACLE, 3, 18);
        model.add(WorldModel.OBSTACLE, 3, 19);
        model.add(WorldModel.OBSTACLE, 3, 20);
        model.add(WorldModel.OBSTACLE, 8, 14);
        model.add(WorldModel.OBSTACLE, 8, 15);
        model.add(WorldModel.OBSTACLE, 8, 16);
        model.add(WorldModel.OBSTACLE, 8, 17);
        model.add(WorldModel.OBSTACLE, 8, 19);
        model.add(WorldModel.OBSTACLE, 8, 20);

        model.add(WorldModel.OBSTACLE, 32, 12);
        model.add(WorldModel.OBSTACLE, 32, 13);
        model.add(WorldModel.OBSTACLE, 32, 14);
        model.add(WorldModel.OBSTACLE, 32, 15);
        model.add(WorldModel.OBSTACLE, 32, 18);
        model.add(WorldModel.OBSTACLE, 32, 19);
        model.add(WorldModel.OBSTACLE, 32, 20);
        model.add(WorldModel.OBSTACLE, 28, 14);
        model.add(WorldModel.OBSTACLE, 28, 15);
        model.add(WorldModel.OBSTACLE, 28, 16);
        model.add(WorldModel.OBSTACLE, 28, 17);
        model.add(WorldModel.OBSTACLE, 28, 19);
        model.add(WorldModel.OBSTACLE, 28, 20);

        model.add(WorldModel.OBSTACLE, 13, 13);
        model.add(WorldModel.OBSTACLE, 13, 14);

        model.add(WorldModel.OBSTACLE, 13, 16);
        model.add(WorldModel.OBSTACLE, 13, 17);

        model.add(WorldModel.OBSTACLE, 13, 19);
        model.add(WorldModel.OBSTACLE, 14, 19);

        model.add(WorldModel.OBSTACLE, 16, 19);
        model.add(WorldModel.OBSTACLE, 17, 19);

        model.add(WorldModel.OBSTACLE, 19, 19);
        model.add(WorldModel.OBSTACLE, 19, 18);

        model.add(WorldModel.OBSTACLE, 19, 16);
        model.add(WorldModel.OBSTACLE, 19, 15);

        model.add(WorldModel.OBSTACLE, 19, 13);
        model.add(WorldModel.OBSTACLE, 18, 13);

        model.add(WorldModel.OBSTACLE, 16, 13);
        model.add(WorldModel.OBSTACLE, 15, 13);

        // labirinto
        model.add(WorldModel.OBSTACLE, 2, 32);
        model.add(WorldModel.OBSTACLE, 3, 32);
        model.add(WorldModel.OBSTACLE, 4, 32);
        model.add(WorldModel.OBSTACLE, 5, 32);
        model.add(WorldModel.OBSTACLE, 6, 32);
        model.add(WorldModel.OBSTACLE, 7, 32);
        model.add(WorldModel.OBSTACLE, 8, 32);
        model.add(WorldModel.OBSTACLE, 9, 32);
        model.add(WorldModel.OBSTACLE, 10, 32);
        model.add(WorldModel.OBSTACLE, 10, 31);
        model.add(WorldModel.OBSTACLE, 10, 30);
        model.add(WorldModel.OBSTACLE, 10, 29);
        model.add(WorldModel.OBSTACLE, 10, 28);
        model.add(WorldModel.OBSTACLE, 10, 27);
        model.add(WorldModel.OBSTACLE, 10, 26);
        model.add(WorldModel.OBSTACLE, 10, 25);
        model.add(WorldModel.OBSTACLE, 10, 24);
        model.add(WorldModel.OBSTACLE, 10, 23);
        model.add(WorldModel.OBSTACLE, 2, 23);
        model.add(WorldModel.OBSTACLE, 3, 23);
        model.add(WorldModel.OBSTACLE, 4, 23);
        model.add(WorldModel.OBSTACLE, 5, 23);
        model.add(WorldModel.OBSTACLE, 6, 23);
        model.add(WorldModel.OBSTACLE, 7, 23);
        model.add(WorldModel.OBSTACLE, 8, 23);
        model.add(WorldModel.OBSTACLE, 9, 23);
        model.add(WorldModel.OBSTACLE, 2, 29);
        model.add(WorldModel.OBSTACLE, 2, 28);
        model.add(WorldModel.OBSTACLE, 2, 27);
        model.add(WorldModel.OBSTACLE, 2, 26);
        model.add(WorldModel.OBSTACLE, 2, 25);
        model.add(WorldModel.OBSTACLE, 2, 24);
        model.add(WorldModel.OBSTACLE, 2, 23);
        model.add(WorldModel.OBSTACLE, 2, 29);
        model.add(WorldModel.OBSTACLE, 3, 29);
        model.add(WorldModel.OBSTACLE, 4, 29);
        model.add(WorldModel.OBSTACLE, 5, 29);
        model.add(WorldModel.OBSTACLE, 6, 29);
        model.add(WorldModel.OBSTACLE, 7, 29);
        model.add(WorldModel.OBSTACLE, 7, 28);
        model.add(WorldModel.OBSTACLE, 7, 27);
        model.add(WorldModel.OBSTACLE, 7, 26);
        model.add(WorldModel.OBSTACLE, 7, 25);
        model.add(WorldModel.OBSTACLE, 6, 25);
        model.add(WorldModel.OBSTACLE, 5, 25);
        model.add(WorldModel.OBSTACLE, 4, 25);
        model.add(WorldModel.OBSTACLE, 4, 26);
        model.add(WorldModel.OBSTACLE, 4, 27);
        model.setInitialNbGolds(model.countObjects(WorldModel.GOLD));
        return model;
    }

    // used to test near_least_visited
    public static WorldModel world6() throws Exception {
        WorldModel model = WorldModel.create(15, 15, 1);
        model.setPSim(0);
        model.setPMax(0);
        model.setDepot(5, 7);
        model.setAgPos(0, 5, 5);
        //model.setAgPos(1, 1, 5);
        model.setInitialNbGolds(model.countObjects(WorldModel.GOLD));
        return model;
    }

    // scenario with few gold
    public static WorldModel world7() throws Exception {
        WorldModel model = new WorldModel(45, 51, 12);
        model.setPSim(0.1);
        model.setPMax(0.3);
        model.setDepot(40, 40);

        // red team
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 6, 26);
        model.setAgPos(3, 20, 20);
        model.setAgPos(4, 3, 4);
        model.setAgPos(5, 7, 4);

        // blue team
        model.setAgPos(6, 2, 2);
        model.setAgPos(7, 30, 50);
        model.setAgPos(8, 8, 26);
        model.setAgPos(9, 21, 20);
        model.setAgPos(10, 30, 40);
        model.setAgPos(11, 44, 50);

        model.add(WorldModel.GOLD, 20, 13);
        model.add(WorldModel.GOLD, 15, 20);
        model.add(WorldModel.GOLD, 41, 1);
        model.add(WorldModel.GOLD, 3, 40);
        //model.setInitialNbGolds(model.countObjects(WorldModel.GOLD));
        return model;
    }

    // 7 with many gold
    public static WorldModel world8() throws Exception {
        WorldModel model = world7();
        model.setPSim(0.1);
        model.setPMax(0.3);

        model.add(WorldModel.GOLD, 24, 24);
        model.add(WorldModel.GOLD, 20, 20);
        model.add(WorldModel.GOLD, 20, 21);
        model.add(WorldModel.GOLD, 2, 22);
        model.add(WorldModel.GOLD, 2, 12);
        model.add(WorldModel.GOLD, 19, 2);
        model.add(WorldModel.GOLD, 40, 4);
        model.add(WorldModel.GOLD, 41, 34);
        model.add(WorldModel.GOLD, 20, 13);
        model.add(WorldModel.GOLD, 44, 20);

        model.add(WorldModel.GOLD, 1, 1);
        model.add(WorldModel.GOLD, 3, 5);
        model.add(WorldModel.GOLD, 24, 24);
        model.add(WorldModel.GOLD, 20, 20);
        model.add(WorldModel.GOLD, 20, 21);
        model.add(WorldModel.GOLD, 20, 22);
        model.add(WorldModel.GOLD, 20, 23);
        model.add(WorldModel.GOLD, 20, 24);

        model.add(WorldModel.GOLD, 19, 19);
        model.add(WorldModel.GOLD, 19, 20);
        model.add(WorldModel.GOLD, 19, 21);
        model.add(WorldModel.GOLD, 19, 22);
        model.add(WorldModel.GOLD, 19, 23);
        model.add(WorldModel.GOLD, 19, 24);

        model.add(WorldModel.GOLD, 18, 19);
        model.add(WorldModel.GOLD, 18, 20);
        model.add(WorldModel.GOLD, 18, 21);
        model.add(WorldModel.GOLD, 18, 22);
        model.add(WorldModel.GOLD, 18, 23);
        model.add(WorldModel.GOLD, 18, 24);

        model.add(WorldModel.GOLD, 21, 20);
        model.add(WorldModel.GOLD, 21, 21);
        model.add(WorldModel.GOLD, 21, 22);
        model.add(WorldModel.GOLD, 21, 23);
        model.add(WorldModel.GOLD, 21, 24);
        model.add(WorldModel.GOLD, 21, 25);
        model.add(WorldModel.GOLD, 21, 26);
        model.add(WorldModel.GOLD, 34, 34);

        model.add(WorldModel.GOLD, 0, 49);
        model.setInitialNbGolds(model.countObjects(WorldModel.GOLD));
        return model;
    }

    // 8 with walls
    public static WorldModel world9() throws Exception {
        WorldModel model = world8();
        model.setPSim(0.05);
        model.setPMax(0.2);

        model.wall(0,5,40,5);
        model.wall(5,15,44,15);
        model.wall(0,21,40,21);
        model.wall(3,32,44,32);
        model.wall(0,48,40,48);
        model.wall(1,37,1,48);

        model.add(WorldModel.GOLD, 0, 47);

        model.setInitialNbGolds(model.countObjects(WorldModel.GOLD));
        return model;
    }

    // 9 with depot in lab
    public static WorldModel world10() throws Exception {
        WorldModel model = world9();

        model.setPSim(0.01);
        model.setPMax(0.3);
        model.setMaxSteps(1400);

        model.setDepot(25, 39);

        model.wall(10, 35, 41, 35);
        model.wall(10, 45, 41, 45);
        model.wall(41, 35, 41, 45);

        model.wall(10, 35, 10, 41);
        model.wall(10, 41, 35, 41);

        model.wall(20, 38, 20, 41);
        model.wall(35, 38, 35, 41);

        model.add(WorldModel.GOLD, 11, 40);
        model.add(WorldModel.GOLD, 12, 40);
        model.add(WorldModel.GOLD, 13, 40);
        model.add(WorldModel.GOLD, 11, 39);
        model.add(WorldModel.GOLD, 12, 39);
        model.add(WorldModel.GOLD, 13, 39);

        model.setInitialNbGolds(model.countObjects(WorldModel.GOLD));
        return model;
    }

    // load the fence scenario from 2007 contest
    public static WorldModel worldFromContest2007(String simId) throws Exception {
        File f = new File("env/serverconfig.contest-2007.xml");
        if (! f.exists()) {
            f = new File("applications/jason-team/"+f);
            if (! f.exists()) {
                System.out.println("Sercer config file does not found!");
                return null;
            }
        }

        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fac.newDocumentBuilder();
        Document doc = builder.parse(f);
        XPath expr = XPathFactory.newInstance().newXPath();

        // get the simulation node
        Element fence = (Element)((NodeList)expr.evaluate("//simulation[@id='"+simId+"']/configuration", doc, XPathConstants.NODESET)).item(0);
        if (fence == null) {
            System.out.println("Do not find fence configuration in the conf file!");
            return null;
        }

        // get the size
        int w = Integer.parseInt(fence.getAttribute("sizex"));
        int h = Integer.parseInt(fence.getAttribute("sizey"));
        int nbAg = Integer.parseInt(fence.getAttribute("numberOfAgents"));
        WorldModel model = new WorldModel(w,h,nbAg);

        model.setMaxSteps(Integer.parseInt(fence.getAttribute("maxNumberOfSteps")));
        model.setPSim(Double.parseDouble(fence.getAttribute("actionFailureProbability"))/100);
        model.setPMax(Double.parseDouble(fence.getAttribute("maxActionFailureProbability"))/100);

        // depot
        int dx = Integer.parseInt(fence.getAttribute("depotx"));
        int dy = Integer.parseInt(fence.getAttribute("depoty"));
        model.setDepot(dx, dy);

        // agents
        int[] agx = getArray(fence, "agentPositionX");
        int[] agy = getArray(fence, "agentPositionY");
        for (int i=0; i<nbAg; i++) {
            model.setAgPos(i,  agx[i], agy[i]);
        }

        // gold
        int[] gx = getArray(fence, "GoldPositionX");
        int[] gy = getArray(fence, "GoldPositionY");
        for (int i=0; i<gx.length; i++) {
            model.add(WorldModel.GOLD, gx[i], gy[i]);
        }

        // obstacles
        int[] ox = getArray(fence, "obstaclePositionX");
        int[] oy = getArray(fence, "obstaclePositionY");
        for (int i=0; i<gx.length; i++) {
            model.add(WorldModel.OBSTACLE, ox[i], oy[i]);
        }


        return model;
    }

    private static int[] getArray(Element e, String id) {
        NodeList l = e.getElementsByTagName("array");
        for (int i=0; i<l.getLength(); i++) {
            Element n = (Element)l.item(i);
            if (id.equals(n.getAttribute("meta:name"))) {
                int[] r = new int[Integer.parseInt(n.getAttribute("meta:length"))];
                for (int ai=0; ai<r.length; ai++) {
                    r[ai] = Integer.parseInt(n.getAttribute("item"+ai));
                }
                return r;
            }
        }
        return null;
    }


    public static void main(String[] args) throws Exception {
        new WorldView("Test - Fence", worldFromContest2007("Semiramis"), 1000);
    }
}
