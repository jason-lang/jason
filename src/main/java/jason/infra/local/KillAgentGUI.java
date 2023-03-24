package jason.infra.local;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import jason.runtime.RuntimeServices;
import jason.runtime.RuntimeServicesFactory;

@SuppressWarnings("rawtypes")
public class KillAgentGUI extends BaseDialogGUI {

    private static final long serialVersionUID = 1L;

    private JList lAgs;
    private RuntimeServices services;

    public KillAgentGUI(Frame f, String title) {
        super(f, title);
    }

    @SuppressWarnings("unchecked")
    protected void initComponents()  {
        services = RuntimeServicesFactory.get();
        getContentPane().setLayout(new BorderLayout());

        // Fields
        Vector<String> agNames = null;
        try {
            agNames = new Vector<String>(services.getAgentsNames());
        } catch (RemoteException e) {
            agNames = new Vector<>();
        }
        Collections.sort(agNames);
        lAgs = new JList(agNames);
        lAgs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Current agents", TitledBorder.LEFT, TitledBorder.TOP));
        p.add(lAgs, BorderLayout.CENTER);

        getContentPane().add(p, BorderLayout.CENTER);
        getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
        ok.setText("Kill");
    }

    protected boolean ok() {
        new Thread() {
            @SuppressWarnings("deprecation")
            public void run() {
                Object[] sls = lAgs.getSelectedValues();
                for (int i = 0; i < sls.length; i++) {
                    String agName = sls[i].toString();
                    try {
                        services.killAgent(agName, "KillAgGUI", 0);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } .start();
        return true;
    }
}
