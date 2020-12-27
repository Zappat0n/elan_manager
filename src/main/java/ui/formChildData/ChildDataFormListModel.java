package ui.formChildData;

import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;

import javax.swing.*;
import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.IntStream;

/**
 * Created by angel on 28/03/17.
 */
public class ChildDataFormListModel extends DefaultListModel<ChildDataFormListItem> {
    private static final String TAG = ChildDataFormListModel.class.getSimpleName();
    private final LinkedHashMap<String, ChildDataFormListItem> data;
    public static final int[] event_typeOutcome = {9, 10, 11};
    public static final int[] event_typeNC = {4, 2, 5};
    public static final int[] event_typeMontessori = {1, 6, 7};
    private Integer title = -1;
    private final Byte type;
    private final CacheManager cacheManager;
    private final SettingsManager settingsManager;

    public ChildDataFormListModel(CacheManager cacheManager,SettingsManager settingsManager, double year,int area) {
        this.cacheManager = cacheManager;
        this.settingsManager = settingsManager;
        type = 1;
        data = new LinkedHashMap<>();
        loadNCData(year, area);
    }

    public ChildDataFormListModel(CacheManager cacheManager, SettingsManager settingsManager,
                                  double startYear, double endYear, int area) {
        this.cacheManager = cacheManager;
        this.settingsManager = settingsManager;
        type = 2;
        data = new LinkedHashMap<>();
        loadMontessoriData(startYear, endYear, area);
    }

    private void loadNCData(double year, int area) {
        HashMap<Integer, ArrayList<Integer>> tpersubarea = cacheManager.targetsperyearandsubarea.get(year);
        LinkedHashMap<Integer, ArrayList<Integer>> opersubarea = cacheManager.getOutcomesPerYear(year);

        if (tpersubarea != null || opersubarea != null) {
            ArrayList<Integer> subareasperarea = cacheManager.subareasTargetperarea.get(area);
            if (subareasperarea != null) {
                for (Integer subareaId : subareasperarea) {
                    boolean titleAdded = false;
                    ArrayList<Integer> list = tpersubarea.get(subareaId);
                    if (list != null) {
                        for (Integer targetId : list) {
                            if (!titleAdded) {
                                data.put("3/"+title--, new ChildDataFormListItem(cacheManager.getTargetSubareaName(subareaId)));
                                titleAdded = true;
                                if (opersubarea != null) {
                                    ArrayList<Integer> outcomes = opersubarea.get(subareaId);
                                    if (outcomes != null) {
                                        for (int id : outcomes) {
                                            data.put("0/"+id, new ChildDataFormListItem(settingsManager,
                                                    cacheManager.getOutcomeName(id), id, null, (byte)0,
                                                    null, false, false, false));
                                        }
                                    }
                                }
                            }
                            data.put("1/"+targetId, new ChildDataFormListItem(settingsManager,
                                    cacheManager.getTargetName(targetId), targetId, null, type, null, false,
                                    false, false));
                        }
                        if (opersubarea != null) opersubarea.remove(subareaId);
                    }
                    if (opersubarea != null) {
                        list = opersubarea.get(subareaId);
                        if (list != null) {
                            titleAdded = false;
                            for (Integer id : list) {
                                if (!titleAdded) {
                                    data.put("3/"+title--, new ChildDataFormListItem(
                                            cacheManager.getTargetSubareaName(subareaId)));
                                    titleAdded = true;
                                }
                                data.put("0/"+id, new ChildDataFormListItem(settingsManager,
                                        cacheManager.getOutcomeName(id), id, null, (byte)0, null, false,
                                        false, false));
                            }
                        }
                    }
                }
            }
        }
    }

    private void loadMontessoriData(double startYear, double endYear, int area) {
        for (Double year : cacheManager.presentationsperyearandsubarea.keySet()) {
            if (year >= startYear && year < endYear) {
                HashMap<Integer, ArrayList<Integer>> pperyear= cacheManager.presentationsperyearandsubarea.get(year);
                ArrayList<Integer> list = pperyear.get(area);
                if (list != null) for (Integer id : list) {
                    data.put("2/"+id, new ChildDataFormListItem(settingsManager,
                            (String)cacheManager.presentations.get(id)[0], id, null, type, null, false,
                            false, false));
                    ArrayList<Integer> subs = cacheManager.presentationssubperpresentation.get(id);
                    if (subs!=null) for (Integer sub:subs) {
                        data.put("2/"+id+"."+sub, new ChildDataFormListItem(settingsManager,
                                " -> " + cacheManager.presentationssub.get(sub)[settingsManager.language], id, sub, type, null,
                                false, false, false));
                    }
                }
            }
        }
    }

    public void unmarkAllItems() {
        for (int i = 0; i < getSize(); i++) {
            ChildDataFormListItem item = getElementAt(i);
            item.unsetAll();
        }
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public ChildDataFormListItem getElementAt(int index) {
        int i = 0;
        for (Object o : data.keySet()) {
            ChildDataFormListItem item = data.get(o);
            if (i == index) return item;
            i++;
        }
        return null;
    }

    @Override
    public void removeAllElements() {
        data.clear();
    }

    @Override
    public void addElement(ChildDataFormListItem element) {
        data.put(getValueString(element.getType(), element.getId(), element.getSubid()), element);
    }

    private String getValueString(Byte type, Integer id, Integer subid) {
        return type+"/"+id+((subid==null || subid==0) ? "" : "." + subid);
    }

    public void markItem(Integer id, Integer event_id, Integer event_sub, Integer event_type, Date date) {
        try {
            Byte ty = (IntStream.of(event_typeOutcome).anyMatch(x -> x == event_type)) ? 0 : type;
            String value = getValueString(ty, event_id, event_sub);
            ChildDataFormListItem item;
            item = data.get(value);
            if (date != null) item.setDate(date);

            if (item != null) {
                Integer index = getIndexofType(ty, event_type);
                if (index == null) return;

                switch (index) {
                    case 0 :    item.setBox1(id);
                        return;
                    case 1 :    item.setBox2(id);
                        return;
                    case 2 :    item.setBox3(id);
                        return;
                    default:
                }
            }
        } catch (Exception e) {
            MyLogger.d(TAG, "id: " + id + " " + e.getMessage());
        }
    }

    private static Integer getIndexofType(Byte type, Integer event_type) {
        for ( int i = 0; i < 3; i++) {
            switch (type) {
                case 0: if (event_type == event_typeOutcome[i]) return i;
                case 1: if (event_type == event_typeNC[i]) return i;
                case 2: if (event_type == event_typeMontessori[i]) return i;
            }
        }
        return null;
    }
}
