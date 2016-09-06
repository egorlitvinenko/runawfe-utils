package runawfe.bp.utils;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FindVariablesUsing {

    public static void main(String[] args) {
    }

    public static void printResult(final Map<String, Map<Path, List<String>>> result) {
        final Map<String, Map<Path, List<String>>> used = new TreeMap<>();
        final Map<String, Map<Path, List<String>>> notUsed = new TreeMap<>();
        result.forEach((name, includes) -> {
            if (includes.isEmpty()) {
                notUsed.put(name, includes);
            } else {
                used.put(name, includes);
                ;
            }
        });
        System.out.println("Usings:");
        used.forEach((name, includes) -> {
            System.out.println("\t" + name + " used in:");
            SearchTextMain.printResult(includes);
            System.out.println();
        });
        System.out.println();
        System.out.println("Not used:");
        notUsed.forEach((name, includes) -> System.out.println("\t" + name));
    }

    public static Map<String, Map<Path, List<String>>> doFind(final Path path) throws Exception {
        final Map<String, Map<Path, List<String>>> using = new TreeMap<>();
        if (!Files.isDirectory(path)) {
            System.out.println("Path is BP directory.");
            return using;
        }
        final Path variableXml = path.resolve("variables.xml");
        try (final FileInputStream is = new FileInputStream(variableXml.toFile())) {
            final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            final NodeList nodeList = document.getElementsByTagName("variable");
            for (int i = 0; i < nodeList.getLength(); ++i) {
                final Node node = nodeList.item(i);
                final Node maybeUserTypeNode = node.getParentNode();
                String userType = "";
                if (null != maybeUserTypeNode && maybeUserTypeNode.getNodeName().equals("usertype")) {
                    userType = maybeUserTypeNode.getAttributes().getNamedItem("name").getNodeValue() + ".";
                }
                final String name = userType + node.getAttributes().getNamedItem("name").getNodeValue();
                final String scriptingName = node.getAttributes().getNamedItem("scriptingName").getNodeValue();
                final Map<Path, List<String>> includes = new TreeMap<>();
                includes.putAll(
                        SearchTextMain.searchPattern(path.toString(), Arrays.asList(Pattern.compile(name), Pattern.compile(scriptingName)), 100));
                includes.remove(variableXml);
                using.put(name, includes);
            }
        }
        return using;
    }

}
