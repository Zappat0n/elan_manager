package ui.models;

import utils.CacheManager;
import utils.SettingsManager;
import utils.data.RawData;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by robot on 24/02/17.
 */
public class MyListAreasModel extends DefaultListModel<String> {
    private static final String TAG = MyListAreasModel.class.getSimpleName();
    private final LinkedHashMap<String, Integer> data;
    final SettingsManager settingsManager;

    public MyListAreasModel(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
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

    public void addData(CacheManager cacheManager, int typeIndex, int yearIndex) {
        if (typeIndex == 0) {
            Set<Integer> subareas = cacheManager.stageAreaSubareaMontessori.get(yearIndex).keySet();
            for (Integer id: subareas){
                data.put(cacheManager.areasMontessori.get(id)[settingsManager.language], id);
            }
        } else if (typeIndex == 1 || typeIndex == 2){
            double year = RawData.yearsperstage[yearIndex];
            for (Object oid: RawData.areasTargetperStage.get(year)){
                Integer id = (Integer) oid;
                data.put(cacheManager.areasTarget.get(id)[0], id);
            }
        } else if (typeIndex == 3){
            fireIntervalRemoved(this, 0, size());
            clear();
            return;
        }
        fireIntervalAdded(this, 0, data.size());
    }

    @Override
    public void clear() {
        data.clear();
    }
}

