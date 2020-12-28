package utils.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by angel on 7/05/17.
 */
public class RawData {
    public static final Double[] yearsperstage = new Double[]{2.5, 5d, 6d, 7d, 8d, 9d, 10d, 11d};
    public static final Double[] yearMontessoriStage = new Double[]{3d, 6d, 9d};
    public static final Double[][] yearsmontessori = new Double[][]{
            new Double[]{0d, 3d},
            new Double[]{3d, 6d},
            new Double[]{6d, 9d},
            null,
            null,
            null};
    public static final Integer[][] monthsOutcomes = new Integer[][]{
            new Integer[]{0, 11},
            new Integer[]{8, 20},
            new Integer[]{16, 26},
            new Integer[]{22, 36},
            new Integer[]{30, 50},
            new Integer[]{40, 60}};
    public static final Integer[] monthsOutcomesForEY = new Integer[]{20, 26, 36};
    public static final Integer[] monthsOutcomesForFS = new Integer[]{36, 50, 60};

    public static final String[] stagesNC = new String[]{"EYs", "FS", "Year 1", "Year 2", "Year 3", "Year 4", "Year 5", "Year 6"};
    public static final String[] classrooms = new String[]{"Snails", "Roots", "Seeds", "Primary I", "Primary II", "Test"};

    public static final List<Integer> cdbAreasTarget = Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,13,14,15,16,17,18);
    public static final List[] areasTargetPerStageData = new List[]{
            Arrays.asList(1, 2, 3, 10, 11, 4, 5, 6, 7),
            Arrays.asList(1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 13),
            Arrays.asList(8, 5, 9, 7, 10, 11, 13, 14, 1, 15, 16, 17, 3, 18),
            Arrays.asList(8, 5, 9, 7, 11, 13, 14, 15, 16, 17, 18),
            Arrays.asList(8, 5, 9, 15),
            Arrays.asList(8, 5, 9, 7, 11, 13, 14, 15, 16, 17, 18),
            Arrays.asList(5, 9),
            Arrays.asList(8, 5, 9, 7, 16, 17, 13, 14, 11, 18)};

    public static final Integer[] montessoriEvent_Types = new Integer[]{1, 6, 7};
    public static final Integer[] ncTargets_Types = new Integer[]{2, 4, 5};
    public static final Integer[] ncOutcomes_Types = new Integer[]{9, 10, 11};


    public static final HashMap<Double, List> areasTargetperStage = getAreasTargetPerStage();

    public static Integer[] getOutcomeMonthsperYear(Double year) {
        if (year.equals(yearsperstage[0])) return monthsOutcomesForEY;
        else if (year.equals(yearsperstage[1])) return monthsOutcomesForFS;
        else return null;
    }

    private static HashMap<Double, List> getAreasTargetPerStage(){
        HashMap<Double, List> map = new HashMap<>();
        for (int i = 0; i < yearsperstage.length; i++) {
            map.put(yearsperstage[i], areasTargetPerStageData[i]);
        }
        return map;
    }
}
