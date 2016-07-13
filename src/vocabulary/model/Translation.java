package vocabulary.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Translation {

    private final IntegerProperty pid;
    private final IntegerProperty fid;
    
    public Translation() {
        this(-1,-1);
    }
    
    public Translation(int pid, int fid) {
        this.pid = new SimpleIntegerProperty(pid);
        this.fid = new SimpleIntegerProperty(fid);
    }
    
    @Override
    public boolean equals(Object o) {
        Translation t = (Translation) o;
        return o != null && pid.get() == t.getPid() && fid.get() == t.getFid();
    }
    
    public void setPid(int pid) {
        this.pid.set(pid);
    }
    
    public int getPid() {
        return pid.get();
    }
    
    public IntegerProperty pidProperty() {
        return pid;
    }
    
    public void setFid(int fid) {
        this.fid.set(fid);
    }
    
    public int getFid() {
        return fid.get();
    }
    
    public IntegerProperty fidProperty() {
        return fid;
    }
}
