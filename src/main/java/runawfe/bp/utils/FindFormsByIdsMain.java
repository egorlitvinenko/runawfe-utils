package runawfe.bp.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author egorlitvinenko
 *
 */
public class FindFormsByIdsMain {

    public static void main(String[] args) {
        final String ROOT_FOLDER = "/home/egor/Work/workspaces/tnms_origin/runawfe/src/main/resources/processes/19_06";
        try {
            final Pattern inputAddress = Pattern.compile("InputAddress");
            Map<Path, List<String>> result = SearchTextMain.searchPattern(ROOT_FOLDER, Arrays.asList(inputAddress), Integer.MAX_VALUE, false);
            Set<String> bpNames = new LinkedHashSet<>();
            Map<String, Set<String>> bpIds = new HashMap<>();
            result.keySet().forEach((Path path) -> {
                final String id = path.getFileName().toString().replace(".ftl", ""), bpName = path.getParent().toString();
                bpNames.add(bpName);
                if (!bpIds.containsKey(bpName)) {
                    bpIds.put(bpName, new HashSet<>());
                }
                bpIds.get(bpName).add(id);
            });
            Map<String, Map<String, String>> formTitles = formTitles(bpIds);
            System.out.println("Бизнес процессы (" + formTitles.size() + "):");
            formTitles.forEach((String bpName, Map<String, String> idToNames) -> {
                System.out.println(bpName);
                idToNames.forEach((String id, String name) -> {
                    System.out.println("\t" + id + " <-> " + name);
                });
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Map<String, String>> formTitles(Map<String, Set<String>> bpIds) {
        final Map<String, Map<String, String>> result = new HashMap<>();
        final XPath xPath = XPathFactory.newInstance().newXPath();
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e2) {
            throw new RuntimeException(e2);
        }
        bpIds.forEach((String bpName, Set<String> ids) -> {
            final Map<String, String> idToName = new HashMap<>();
            try {
                final Document xmlDoc = db.parse(new File(bpName + File.separator + "processdefinition.xml"));
                ids.forEach((String id) -> {
                    try {
                        final XPathExpression expr = xPath.compile("//*[@id='" + id + "']");
                        final Node node = (Node) expr.evaluate(xmlDoc, XPathConstants.NODE);
                        if (null != node) {
                            idToName.put(id, node.getAttributes().getNamedItem("name").getNodeValue());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                if (!idToName.isEmpty()) {
                    result.put(bpName, idToName);
                }
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        });
        return result;
    }

}
