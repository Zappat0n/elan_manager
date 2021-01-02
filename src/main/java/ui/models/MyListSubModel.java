package ui.models;

import ui.AddDataForm;
import utils.SettingsManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by robot on 24/02/17.
 */
public class MyListSubModel extends DefaultListModel<String> {
    private static final String TAG = MyListSubModel.class.getSimpleName();
    private final MyListItemsModel listItemsModel;
    private final LinkedHashMap<String, Integer> data;
    final SettingsManager settingsManager;

    public MyListSubModel(SettingsManager settingsManager, AddDataForm form) {
        this.settingsManager = settingsManager;
        listItemsModel = (MyListItemsModel)form.listItems.getModel();
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

    private Object[] getElementAndIdAt(int index) {
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

    public Boolean addData(Integer itemIndex) {
        clear();
        Integer elementId = (Integer) listItemsModel.getElementAndIdAt(itemIndex)[1];
        if (elementId != null) {
            ArrayList<Integer> list = AddDataForm.cacheManager.presentationsSubPerPresentation.get(elementId);
            if (list!=null) {
                for (Integer id : list) data.put(
                        (String)AddDataForm.cacheManager.presentationsSub.get(id)[settingsManager.language], id);
                fireIntervalAdded(this, 0, data.size());
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        fireIntervalRemoved(this, 0, data.size());
        data.clear();
    }
}

