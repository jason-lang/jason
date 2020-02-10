import java.util.*;

import jason.architecture.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class StatArch extends AgArch {

    Map<String,Integer> msgCount = new HashMap<>();
    Map<String,Integer> actCount = new HashMap<>();

    @Override
    public void stop() {
        System.out.println("Sent messages: "+msgCount);
        int t = 0;
        for (int v: msgCount.values())
            t += v;
        System.out.println("Total sent messages: "+t);

        System.out.println("Actions: "+actCount);
        t = 0;
        for (int v: actCount.values())
            t += v;
        System.out.println("Total actions: "+t);
    }

    @Override
    public void sendMsg(Message m) throws Exception {
        super.sendMsg(m);

        Integer c = msgCount.get(m.getReceiver());
        if (c == null)
          c = 0;
        msgCount.put(m.getReceiver(),c+1);
    }

    @Override
    public void act(ActionExec action) {
      super.act(action);

      Integer c = actCount.get(action.getActionTerm().getFunctor());
      if (c == null)
        c = 0;
      actCount.put(action.getActionTerm().getFunctor(),c+1);
    }
}
