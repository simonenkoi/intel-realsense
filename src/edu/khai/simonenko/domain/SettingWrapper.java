package edu.khai.simonenko.domain;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "settings")
public class SettingWrapper {

    private List<Setting> items;

    public SettingWrapper() {
        items = new ArrayList<>();
    }

    public SettingWrapper(List<Setting> items) {
        this.items = items;
    }

    public List<Setting> getItems() {
        return items;
    }

    @XmlElement(name = "setting")
    public void setItems(List<Setting> items) {
        this.items = items;
    }
}
