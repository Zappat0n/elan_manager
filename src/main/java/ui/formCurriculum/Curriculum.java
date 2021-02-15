package ui.formCurriculum;

import main.ApplicationLoader;
import utils.CacheManager;
import utils.SettingsManager;
import utils.data.RawData;

import javax.swing.table.DefaultTableModel;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class Curriculum {
    protected final int stage;
    protected final HashSet<Integer> subareas;
    protected final ArrayList<Line> lines;
    protected final Boolean nc;

    public Curriculum(int stage, int area, boolean nc) {
        this.nc = nc;
        this.stage = stage;
        lines = new ArrayList<>();
        subareas = nc ? new HashSet<>(ApplicationLoader.cacheManager.subareasTargetPerArea.get(area)) :
                ApplicationLoader.cacheManager.stageAreaSubareaMontessori.get(stage).get(area);
    }

    protected Boolean includePresentation(Double year, int subarea) {
        if (nc) {
            int ini = stage == 0 ? 0 : RawData.yearsNC[stage - 1];
            if (year <= ini || year > RawData.yearsNC[stage]) return false;
        } else {
            if (year < RawData.yearsmontessori[stage][0] || year > RawData.yearsmontessori[stage][1]) return false;
        }
        return subareas.contains(subarea);
    }

    public void addTitle(String title) {
        lines.add(new Line(null, 0, title));
    }

    public void addLine(Integer type, int id, String name) {
        lines.add(new Line(type, id, name));
    }

    protected void writeToFile(String file) throws IOException {
        FileWriter writer = new FileWriter(file);
        for(Line line: lines) {
            writer.write(line.toString() + System.lineSeparator());
        }
        writer.close();
    }

    public DefaultTableModel getTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnCount(3);
        for(Line line: lines) {
            String[] row = line.toString().split("\t");
            String text = "";
            if (row[0] != null) {
                text = "<html><b>" + row[0] + "</b></html>";
            }
            if (row.length == 2) {
                text = "   " + row[1];
            }
            if (row.length == 3) {
                text = "<html><i>&nbsp&nbsp&nbsp&nbsp&nbsp* " + row[2] + "</i></html>";
            }
            model.addRow(new String[] {text, null, null});
        }
        return model;
    }

    private static class Line {
        final Integer type;
        final int id;
        final String name;

        public Line(Integer type, int id, String name) {
            this.type = type;
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            if (type != null) {
                String ini = "";
                if (type != 0)  ini = (type == 1 ? "\t" : "\t\t");
                return  ini + id + ". " + name;
            } else return name;
        }
    }

}
