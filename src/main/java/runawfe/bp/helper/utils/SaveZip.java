package runawfe.bp.helper.utils;

import java.util.Arrays;

public class SaveZip {

    public static void main(String[] args) {

        final String ROOT_FOLDER = "src/main/resources/processes/Банкоматы/Установка. Новое УС в МУ/";
        CreateZipMain.createZip(Arrays.asList(ROOT_FOLDER), "src/main/resources/processes/Банкоматы", "par");
    }

}
