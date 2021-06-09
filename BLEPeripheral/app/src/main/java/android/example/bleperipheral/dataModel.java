package android.example.bleperipheral;

import io.realm.RealmObject;

public class dataModel extends RealmObject {
    private String value;

    public String getValue(String string){
        return this.value;
    }

    public void setValue(String string){
        this.value = string;
    }
}
