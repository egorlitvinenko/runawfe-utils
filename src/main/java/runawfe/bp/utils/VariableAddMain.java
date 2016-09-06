package runawfe.bp.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author egorlitvinenko
 *
 */
public class VariableAddMain {

    private static boolean TEST = false;

    public static void main(String[] args) {
        final String ROOT_FOLDER = "src/main/resources/processes/";
        try {
            final Pattern inputAddress = Pattern.compile("InputAddress");
            Map<Path, List<String>> result = SearchTextMain.searchPattern(ROOT_FOLDER, Arrays.asList(inputAddress), Integer.MAX_VALUE, false);
            Set<String> bpNames = new LinkedHashSet<>();
            result.keySet().forEach((Path path) -> bpNames.add(path.getParent().toString()));

            Map<String, List<Map<String, String>>> userTypeNewVariables = new HashMap<>();
            userTypeNewVariables.put("Заявка", new ArrayList<>());
            Map<String, String> newVariable = new LinkedHashMap<>();
            newVariable.put("name", "Адрес согласован");
            newVariable.put("scriptingName", "Адрес_согласован");
            newVariable.put("format", "ru.altlinux.tnms.wfe.format.YesNoFormat");
            userTypeNewVariables.get("Заявка").add(newVariable);
            newVariable = new LinkedHashMap<>();
            newVariable.put("name", "Использовать адреса только из КЛАДР");
            newVariable.put("scriptingName", "Использовать_адреса_только_из_КЛАДР");
            newVariable.put("format", "ru.altlinux.tnms.wfe.format.YesNoFormat");
            userTypeNewVariables.get("Заявка").add(newVariable);

            addVariables(bpNames, userTypeNewVariables);
            System.out.println("\nБизнес процессы (" + bpNames.size() + "): ");
            bpNames.forEach((String bp) -> System.out.println(bp));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * usertype -> аттрибуты
     */
    public static void addVariables(Collection<String> bps, Map<String, List<Map<String, String>>> userTypeNewVariables) {
        final XPath xPath = XPathFactory.newInstance().newXPath();
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e2) {
            throw new RuntimeException(e2);
        }
        bps.forEach((String bpName) -> {
            try {
                final File xmlFile = new File(bpName + File.separator + "variables.xml");
                final Document xmlDoc = db.parse(xmlFile);
                userTypeNewVariables.forEach((String userTypeName, List<Map<String, String>> variables) -> {
                    try {
                        final XPathExpression expr = xPath.compile("//*/usertype[@name='" + userTypeName + "']");
                        final Node node = (Node) expr.evaluate(xmlDoc, XPathConstants.NODE);
                        if (null != node) {
                            variables.forEach((Map<String, String> variable) -> {
                                try {
                                    final XPathExpression exprVariable = xPath.compile("//*/variable[@name='" + variable.get("name") + "']");
                                    final Node variableNode = (Node) exprVariable.evaluate(node, XPathConstants.NODE);
                                    Element newVariable = null == variableNode ? xmlDoc.createElement("variable") : (Element) variableNode;
                                    variable.forEach((String attr, String value) -> newVariable.setAttribute(attr, value));
                                    if (null == variableNode) {
                                        node.appendChild(newVariable);
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                if (!TEST) {
                    final Source source = new DOMSource(xmlDoc);
                    final StreamResult result = new StreamResult(new OutputStreamWriter(new FileOutputStream(xmlFile), "UTF-8"));
                    final Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.transform(source, result);
                }
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        });
    }

}
