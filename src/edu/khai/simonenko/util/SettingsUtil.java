package edu.khai.simonenko.util;

import edu.khai.simonenko.Settings;
import edu.khai.simonenko.domain.Setting;
import edu.khai.simonenko.domain.SettingWrapper;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class SettingsUtil {

    public static void uploadSettings(File file) {
        SettingWrapper settings = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SettingWrapper.class);
            Unmarshaller marshaller = jaxbContext.createUnmarshaller();
            settings = (SettingWrapper) marshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        if (settings != null) {
            settings.getItems().forEach(setting -> {
                try {
                    setValueOfStringToField(Settings.class, Settings.class.getField(setting.getName()), setting.getValue());
                } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void saveSettings(File file) {
        Field[] fields = Settings.class.getFields();
        List<Setting> settingList = new ArrayList<>();
        for (Field field : fields) {
            try {
                settingList.add(new Setting(field.getName(), field.get(field.getType()).toString()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SettingWrapper.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(new SettingWrapper(settingList), file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static void setValueOfStringToField(Class clazz, Field field, String value)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        field.set(clazz, field.getType().getDeclaredMethod("valueOf", String.class).invoke(null, value));
    }
}
