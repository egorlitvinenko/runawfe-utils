package runawfe.bp.helper.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class AddTabToAllForms {

    public static void main(String[] args) {
        final String ROOT_FOLDER = "src/main/resources/processes";
        try {
            final Pattern pattern = Pattern.compile("AssignSwimlaneActionHandler");
            final Map<Path, List<String>> result = SearchTextMain.searchPattern(ROOT_FOLDER, Arrays.asList(pattern), Integer.MAX_VALUE, false);
            final Set<String> bpNames = new LinkedHashSet<>();
            result.keySet().forEach((Path path) -> {
                System.out.println("");
                System.out.println(path.resolve(""));
                bpNames.add(path.getParent().toString());
                result.get(path).forEach((String text) -> {
                    System.out.println("\t" + text);
                });
            });
            System.out.println("\nБизнес процессы (" + bpNames.size() + "): ");
            bpNames.forEach((String bp) -> System.out.println(bp));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
