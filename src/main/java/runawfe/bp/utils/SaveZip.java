package runawfe.bp.utils;

import java.util.Arrays;

public class SaveZip {

    public static void main(String[] args) {

        final String ROOT_FOLDER = "src/main/resources/processes/path1/path2/";
        CreateZipMain.createZip(Arrays.asList(ROOT_FOLDER), "src/main/resources/processes/path1", "par");
    }

}
