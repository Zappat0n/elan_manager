package ui.models;

import ui.AddDataForm;
import utils.data.RawData;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by robot on 24/02/17.
 */
public class MyListItemsModel extends DefaultListModel<String> {
    private static final String TAG = MyListItemsModel.class.getSimpleName();
    private final LinkedHashMap<String, Integer> data;

    public MyListItemsModel(AddDataForm form) {
        MyListAreasModel listAreasModel = (MyListAreasModel) form.listAreas.getModel();
        data = new LinkedHashMap<>();
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public String getElementAt(int index) {
        int i = 0;
        for (String key : data.keySet()) {
            if (i == index) return key;
            i++;
        }
        return null;
    }

    public Object[] getElementAndIdAt(int index) {
        int i = 0;
        for (String key : data.keySet()) {
            if (i == index) return new Object[]{key, data.get(key)};
            i++;
        }
        return null;
    }

    public Object[][] getElementsAndIdsAt(int[] indices) {
        Object[][] res = new Object[indices.length][2];
        int i = 0;
        for (int index : indices) {
            res[i++] = getElementAndIdAt(index);
        }
        return res;
    }

    public void addData(Integer indexType, Integer indexYear, int area) {
        clear();
        if (indexType == 0) {
            Double[] limits = RawData.yearsmontessori[indexYear];
            if (limits==null) return;
            for (double year : AddDataForm.cacheManager.presentationsperyearandsubarea.keySet()) {
                if (year >= limits[0] && year < limits[1]) {
                    LinkedHashMap<Integer, ArrayList<Integer>> pperarea = AddDataForm.cacheManager.presentationsperyearandsubarea.get(year);
                    if (pperarea!=null) for (Integer subarea : AddDataForm.cacheManager.subareasMontessoriperarea.get(area)) {
                        ArrayList<Integer> list = pperarea.get(subarea);
                        if (list != null) for (Integer id : list) {
                            data.put((String)AddDataForm.cacheManager.presentations.get(id)[0], id);
                        }
                    }
                }
            }
            fireIntervalAdded(this, 0, data.size());
        } else if (indexType == 1) {
            double year = RawData.yearsperstage[indexYear];
            LinkedHashMap<Integer, ArrayList<Integer>> tperarea = AddDataForm.cacheManager.getOutcomesPerYear(year);
            for (Integer subarea : AddDataForm.cacheManager.subareasTargetperarea.get(area)) {
                ArrayList<Integer> list = tperarea.get(subarea);
                if (list!= null) for (Integer id : list) {
                    data.put(AddDataForm.cacheManager.getOutcomeName(id), id);
                }
            }
            fireIntervalAdded(this, 0, data.size());
        } else if (indexType == 2) {
            double year = RawData.yearsperstage[indexYear];
            LinkedHashMap<Integer, ArrayList<Integer>> tperarea = AddDataForm.cacheManager.targetsperyearandsubarea.get(year);
            for (Integer subarea : AddDataForm.cacheManager.subareasTargetperarea.get(area)) {
                ArrayList<Integer> list = tperarea.get(subarea);
                if (list!= null) for (Integer id : list) {
                    data.put(AddDataForm.cacheManager.getTargetName(id), id);
                }
            }
            fireIntervalAdded(this, 0, data.size());
        } else if (indexType == 3) {
            for (Integer id: AddDataForm.cacheManager.observations.keySet()) {
                data.put(AddDataForm.cacheManager.observations.get(id), id);
            }
            fireIntervalAdded(this, 0, data.size());
        }
    }

    @Override
    public void clear() {
        fireIntervalRemoved(this, 0, data.size());
        data.clear();
    }
}

