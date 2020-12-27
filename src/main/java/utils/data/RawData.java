package utils.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by angel on 7/05/17.
 */
public class RawData {
    public static final String[] c = {"=[4jVmeDIbz#", ")ya)?7agaWq~", "5(BO*YH!]QqK", "y?pIY%L2la56", "-G}1.G#w?Hsr",
            "M_=+k}[-E&ca", "G]m!W0)%Y0{S", "PjL3I?bRFPT@", "}z]xV1$)gfFc", "~i29!lUCl~s.", "zlztiEKFLH6$",
            "E&l-Et,,81qK", null, "jOiv+{Ruko~0"};
    public static final String c100 = "s603IcZZihccWr";
    public static final Double[] yearsperstage = new Double[]{2.5, 5d, 6d, 7d, 8d, 9d, 10d, 11d};
    public static final Double[] yearMontessoriStage = new Double[]{3d, 6d, 9d};
    public static final Double[][] yearsmontessori = new Double[][]{
            {0d, 3d},
            {3d, 6d},
            {6d, 9d},
            null,
            null,
            null};
    public static final Integer[][] monthsoutcomes = new Integer[][]{
            {0, 11},
            {8, 20},
            {16, 26},
            {22, 36},
            {30, 50},
            {40, 60}};
    public static final Integer[] monthsOutcomesforEY = new Integer[]{20, 26, 36};
    public static final Integer[] monthsOutcomesforFS = new Integer[]{36, 50, 60};

    public static final String[] stagesNC = new String[]{"EYs", "FS", "Year 1", "Year 2", "Year 3", "Year 4",
            "Year 5", "Year 6"};
    public static final String[] classrooms = new String[]{"Snails", "Roots", "Seeds", "Primary I", "Primary II", "Test"};

    public static final List<Integer> cdbAreasTarget = Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,13,14,15,16,17,18);
    public static final List<Integer>[] areasTargetperStageData = new List[]{
            Arrays.asList(1,2,3,10,11,4,5,6,7),
            Arrays.asList(4,5,6,1,7,9,10,11,13,3,2),
            Arrays.asList(8,5,9,7,10,11,13,14,1,15,16,17,3,18),
            Arrays.asList(8,5,9,7,11,13,14,15,16,17,18),
            Arrays.asList(8,5,9,15),
            Arrays.asList(8,5,9,7,11,13,14,15,16,17,18),
            Arrays.asList(5,9),
            Arrays.asList(8,5,9,7,16,17,13,14,11,18)};

    public static final Integer[] montessoriEvent_Types = new Integer[]{1, 6 ,7};
    public static final Integer[] ncTargets_Types = new Integer[]{2, 4 ,5};
    public static final Integer[] ncOutcomes_Types = new Integer[]{9, 10, 11};


    /*
    private static List[] subareasMontessoriperYearData = new List[]{
            Arrays.asList(1,2,4,5,6,7,15,16,17),
            Arrays.asList(1,2,4,7,8,14,24),
            Arrays.asList(2,4,7,8,13,14,17,18,19,25),
            Arrays.asList(5,21,24,26,27,28,30,13,3)};
    public static List[] subareasMontessoriperStageData = new List[]{
            Arrays.asList(1,2,4,5,6,7,15,16,17),
            Arrays.asList(1,2,4,7,8,14,24,13,17,18,19,25,5,21,26,27,28,3),
            null,
            null,
            null,
            null};
*/
    public static final HashMap<Double, List<Integer>> areasTargetperStage = getAreasTargetperstage();

    public static Integer[] getOutcomeMonthsperYear(Double year) {
        if (year == yearsperstage[0]) return monthsOutcomesforEY;
        else if (year == yearsperstage[1]) return monthsOutcomesforFS;
        else return null;
    }

    private static HashMap<Double, List<Integer>> getAreasTargetperstage(){
        HashMap<Double, List<Integer>> map = new HashMap<>();
        for (int i = 0; i < yearsperstage.length; i++) {
            map.put(yearsperstage[i], areasTargetperStageData[i]);
        }
        return map;
    }
}
