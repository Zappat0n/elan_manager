package ui.models;

import utils.CacheManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by robot on 24/02/17.
 */
public class MyListStudentsModel extends DefaultListModel<String> {
    private static final String TAG = MyListStudentsModel.class.getSimpleName();
    private final SortedMap<String, Integer> data;

    public MyListStudentsModel() {
        data = new TreeMap<>();
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

    public void addData(CacheManager cacheManager, String classroom) {
        clear();
        if (classroom == null) {
            for ( Integer id : cacheManager.students.keySet()) data.put(
                    (String)cacheManager.students.get(id)[0], id);

        } else {
            Integer classroomId = cacheManager.getClassroomId(classroom);
            if (classroomId==null) return;
            ArrayList<Integer> list = cacheManager.studentsPerClassroom.get(classroomId);
            for (Integer id: list) data.put((String)cacheManager.students.get(id)[0], id);
        }
        fireIntervalAdded(this, 0, data.size());
    }

    @Override
    public void clear() {
        data.clear();
    }
}

