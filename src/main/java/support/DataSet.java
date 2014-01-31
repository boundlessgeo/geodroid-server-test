package support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class DataSet {
    public final TopLevelType type;
    public final DataType dataType;
    public final Driver driver;
    public final String name;
    public final List<DataSet> children;
    Integer expectedFeatureCount = null;
    List<String> styles;
    private DataSet parent;

    DataSet(DataSet.TopLevelType type, DataSet.Driver driver, DataSet.DataType dataType, String name, DataSet... children) {
        this.type = type;
        this.dataType = dataType;
        this.driver = driver;
        this.name = name;
        this.children = Arrays.asList(children);
        for (int i = 0; i < children.length; i++) {
            children[i].parent = this;
        }
    }

    public DataSet getParent() {
        return parent;
    }

    public Integer getExpectedFeatureCount() {
        return expectedFeatureCount;
    }

    public List<String> getAssociatedStyles() {
        if (styles == null) {
            return Collections.emptyList();
        } else {
            return styles;
        }
    }

    DataSet associatedStyles(String... styles) {
        this.styles = Arrays.asList(styles);
        return this;
    }

    DataSet expectFeatureCount(int cnt) {
        this.expectedFeatureCount = cnt;
        return this;
    }

    public String[] childrenNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0; i < children.size(); i++) {
            names.add(children.get(i).name);
        }
        return names.toArray(new String[0]);
    }

    public DataSet getChild(String name) {
        DataSet child = null;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).name.equals(name)) {
                child = children.get(i);
                break;
            }
        }
        return child;
    }

    static Collection<DataSet> filter(DataType dataType, DataSet... dataSets) {
        List<DataSet> toCheck = new LinkedList<DataSet>(Arrays.asList(dataSets));
        List<DataSet> ds = new ArrayList<DataSet>();
        while (!toCheck.isEmpty()) {
            DataSet check = toCheck.remove(0);
            if (dataType == check.dataType) {
                ds.add(check);
            }
            toCheck.addAll(check.children);
        }
        return ds;
    }

    static DataSet workspace(String name, DataSet... children) {
        return new DataSet(TopLevelType.WORKSPACE, Driver.GEOPACKAGE, null, name, children);
    }

    static DataSet vector(String name) {
        return new DataSet(TopLevelType.WORKSPACE, Driver.GEOPACKAGE, DataType.VECTOR, name);
    }

    static DataSet tiles(String name) {
        return new DataSet(TopLevelType.WORKSPACE, Driver.GEOPACKAGE, DataType.TILES, name);
    }

    static DataSet json(String name) {
        return new DataSet(TopLevelType.DATASET, Driver.GEOJSON, DataType.VECTOR, name);
    }

    static DataSet mbtiles(String name) {
        return new DataSet(TopLevelType.DATASET, Driver.MBTILES, DataType.TILES, name);
    }

    public static enum TopLevelType {

        DATASET, WORKSPACE
    }

    public static enum Driver {

        GEOPACKAGE, GEOJSON, MBTILES
    }

    public static enum DataType {

        VECTOR, TILES
    }

    public String toString() {
        return name;
    }
}
