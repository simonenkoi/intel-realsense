package edu.khai.simonenko;

import edu.khai.simonenko.domain.ProgramMode;
import edu.khai.simonenko.util.SettingsUtil;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        SettingsUtil.uploadSettings(new File("settings/default.xml"));
        //RealsenseFacade.run(ProgramMode.RECORD, "video/" + System.currentTimeMillis() + ".avi");
        RealsenseFacade.run(ProgramMode.PLAYBACK, "video/1528875422435.avi");
    }
}