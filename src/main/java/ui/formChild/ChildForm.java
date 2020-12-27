package ui.formChild;

import bd.BDManager;
import ui.formChildData.ChildDataFormListModel;
import ui.formChildData.ChildDataFormRenderer;
import utils.CacheManager;
import utils.SettingsManager;
import utils.data.RawData;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.HashMap;
import java.util.List;

public class ChildForm {
    private static final String TAG = ChildForm.class.getSimpleName();
    private final int[] language = {0,1};
    private final int lang = 0;
    public static BDManager bdManager;
    private static SettingsManager settingsManager;
    private static CacheManager cacheManager;
    public static JFrame frame;

    private JPanel mainPanel;
    private JList listMenu;
    private JTree tree;
    private JList listItems;
    private String studentName;
    private Integer studentId;
    private HashMap<String, ChildDataFormListModel> models;

    public static void main(BDManager bdManager, SettingsManager settingsManager, CacheManager cacheManager) {
        ChildForm.bdManager = bdManager;
        ChildForm.settingsManager = settingsManager;
        ChildForm.cacheManager = cacheManager;
        frame = new JFrame("Child data");
        frame.setContentPane(new ChildForm().mainPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    private void createUIComponents() {
        createTree();
        listItems = new JList();
        listItems.setCellRenderer(new ChildDataFormRenderer());
        listItems.addMouseListener(new ChildFormListMouseAdapter(this));

    }

    private void createTree(){
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode ncNode = new DefaultMutableTreeNode("NC");
        DefaultMutableTreeNode montessoriNode = new DefaultMutableTreeNode("Montessori");

        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener(new MyTreeModelListener());
        rootNode.add(ncNode);
        rootNode.add(montessoriNode);
        tree = new JTree();
        tree.setRootVisible(false);
        tree.setModel(treeModel);
        createTargets();
    }

    private void createTargets(){
        double year;
        List<Integer> areas;

        for (int i = 0; i < 6; i++) {
            year = switch (i) {
                case 0 -> 2.5;
                case 1 -> 5;
                case 2 -> 6;
                case 3 -> 7;
                case 4 -> 8;
                case 5 -> 9;
                default -> 0;
            };
            areas = RawData.areasTargetperStage.get(year);
            for (Integer area : areas) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(cacheManager.areasTarget.get(area));
                createTargetsList(year, area);
            }
        }
    }

    private void createTargetsList(double year, int area) {
        ChildDataFormListModel model = new ChildDataFormListModel(cacheManager, settingsManager, year, area);
        if (model.getSize() == 0) {
        }
        else {
            if (models == null) models = new HashMap<>();
            models.put(getModelString(1, year, area), model);
        }
    }

    private void updateList(int type, double year, int area) {
        listItems.setModel(models.get(getModelString(type, year, area)));
    }

    public Integer getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    private String getModelString(int type, double year, int area) {
        return type + "/" + year + "/" + area;
    }
/*
    private String getModelString(Integer event_type, int event_id) {
        Integer subarea=null;
        Double year;
        Integer area;
        Integer type = ChildDataFormListItem.getType(event_type);
        String result = null;

        try {
            switch (type) {
                case 0: type = 1;//{name,subarea,start_month,end_month,nombre}
                    Object[] outcome = cacheManager.outcomes.get(event_id);
                    subarea = (Integer)outcome[1];
                    area = cacheManager.targetsubareaarea.get(subarea);
                    result = type + "/" + cacheManager.getOutcomeYear((Integer)outcome[3]) + "/" + area;
                    break;
                case 1: Object[] target = cacheManager.targets.get(event_id);
                    subarea = (Integer)target[1];
                    area = cacheManager.targetsubareaarea.get(subarea);
                    year = (Double)target[2];
                    result = type + "/" + year + "/" + area;
                    break;
                case 2: Object[] presentation = cacheManager.presentations.get(event_id);
                    subarea = (Integer)presentation[1];
                    year = (Double)presentation[2];
                    if (year <= 5.5) year = 5.5; else year = 7d;
                    result = type + "/" + year + "/" + subarea;
                    break;
            }
        } catch (Exception ex) {
            MyLogger.d(TAG, event_id + " : " + ex.toString());
        }
        if (subarea==null) return null;
        return result;
    }
*/

    static class MyTreeModelListener implements TreeModelListener {


        @Override
        public void treeNodesChanged(TreeModelEvent e) {

        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {

        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {

        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {

        }
    }
}
