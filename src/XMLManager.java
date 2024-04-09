import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class XMLManager {
    final String DATA_SET_TAG = "DataSet";
    final String REPORT_CONTROL_TAG = "ReportControl";
    final String LDEVICE_TAG = "LDevice";
    final String SERVICES_TAG = "Services";
    final String IED_TAG = "IED";
    final String LN0_TAG = "LN0";
    final String LN_TAG = "LN";
    final String DOI_TAG = "DOI";

    final String INST_ATTR = "inst";
    final String LN_TYPE_ATTR = "lnType";
    final String NAME_ATTR = "name";
    final String LD_INST_ATTR = "ldInst";
    final String PREFIX_ATTR = "prefix";
    final String LN_CLASS_ATTR = "lnClass";
    final String DAT_SET_ATTR = "datSet";
    final String REPORT_ID_ATTR = "rptID";

    final String POS_DOI_NAME = "Pos";

    final String DATASET_XML = "\t\t\t<DataSet name=\"ALL_DS\">\t\t\t</DataSet>\t\n";

    final String DATASET_ITEM_XML = "\t\t\t\t\n<FCDA doName=\"Pos\" fc=\"ST\" ldInst=\"00AUA00\" lnClass=\"\" lnInst=\"\" prefix=\"XCBR101\"/>\n";

    final String REPORT_CONTROL_XML = "\t\t\t<ReportControl bufTime=\"100\" buffered=\"false\" confRev=\"1\" datSet=\"ALL_DS\" indexed=\"false\" intgPd=\"60000\" name=\"ALL_RP\" rptID=\"00AUA00AUA00/LLN0$RP$ALL_RP\">\n" +
            "\t\t\t\t<TrgOps dchg=\"true\" dupd=\"true\" gi=\"false\" period=\"true\" qchg=\"true\"/>\n" +
            "\t\t\t\t<OptFields configRef=\"true\" dataRef=\"false\" dataSet=\"false\" entryID=\"false\" reasonCode=\"true\" seqNum=\"true\" timeStamp=\"true\"/>\n" +
            "\t\t\t\t<RptEnabled/>\n" +
            "\t\t\t</ReportControl>\n";

    final String BUFFERED_REPORT_CONTROL_XML = "\t\t\t<ReportControl bufTime=\"100\" buffered=\"true\" confRev=\"1\" datSet=\"ALL_DS\" indexed=\"false\" intgPd=\"0\" name=\"ALL_BR\" rptID=\"00AUA00AUA00/LLN0$RP$ALL_BR\">\n" +
            "\t\t\t\t<TrgOps dchg=\"true\" dupd=\"true\" gi=\"false\" period=\"false\" qchg=\"true\"/>\n" +
            "\t\t\t\t<OptFields configRef=\"true\" dataRef=\"false\" dataSet=\"false\" entryID=\"true\" reasonCode=\"true\" seqNum=\"true\" timeStamp=\"true\"/>\n" +
            "\t\t\t\t<RptEnabled/>\n" +
            "\t\t\t</ReportControl>\n";

    final String SERVICES_XML = "<Services>\n" +
        "<DynAssociation />\n" +
        "<GetDirectory />\n" +
        "<GetDataObjectDefinition />\n" +
        "<DataObjectDirectory />\n" +
        "<GetDataSetValue />\n" +
        "<SetDataSetValue />\n" +
        "<DataSetDirectory />\n" +
        "<ConfDataSet max=\"10\" modify=\"false\" />\n" +
        "<DynDataSet max=\"42\" />\n" +
        "<ReadWrite />\n" +
        "<ConfReportControl max=\"20\" bufConf=\"false\" />\n" +
        "<GetCBValues />\n" +
        "<ReportSettings rptID=\"Dyn\" optFields=\"Dyn\" bufTime=\"Dyn\" trgOps=\"Dyn\" intgPd=\"Dyn\" owner=\"true\" resvTms=\"true\" />\n" +
        "<ConfLNs fixPrefix=\"true\" fixLnInst=\"true\" />\n" +
        "<GOOSE max=\"0\" />\n" +
        "<GSSE max=\"0\" />\n" +
        "<FileHandling />\n" +
        "</Services>\n";

    String doc_name = "00AUA.ICD";

    public XMLManager() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(doc_name));

            createServices(document);
            createDatasets(document);
            createReports(document);
            saveDocument(document);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public void createServices(Document document) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        removeTagFromDocument(document, SERVICES_TAG);
        Element new_element = createXMLFromText(SERVICES_XML);
        Element parent_element = getElementByTagName(document, IED_TAG);
        insertNewElementToDocument(document, parent_element, new_element);
    }

    public void createDatasets(Document document) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        removeAllTagsFromDocument(document, DATA_SET_TAG);

        ArrayList<Element> devices = getElementsByTagName(document, LDEVICE_TAG);
        if (devices != null) {
            for (Element device : devices) {
                createDataset(document, device);
            }
        }
    }

    public void createDataset(Document document, Element device) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        String device_inst = device.getAttribute(INST_ATTR);
        Element ln0 = getElementByTagName(device, LN0_TAG);

        String dataset_name = device_inst + "_DS";

        Element dataset = createXMLFromText(DATASET_XML);
        dataset.setAttribute(NAME_ATTR, dataset_name);

        ArrayList<Element> lns = getElementsByTagName(device, LN_TAG);
        for (Element ln : lns) {
            if (!isExistsChildElementWithAttr(ln, NAME_ATTR, POS_DOI_NAME)) continue;

            String ln_type = ln.getAttribute(LN_TYPE_ATTR);
            Element dataset_item = createXMLFromText(DATASET_ITEM_XML);
            dataset_item.setAttribute(LD_INST_ATTR, device_inst);
            dataset_item.setAttribute(PREFIX_ATTR, ln_type);
            insertNewElementToDocument(dataset.getOwnerDocument(), dataset, dataset_item);
        }
        insertNewElementToDocument(document, ln0, dataset);
    }

    public boolean isExistsChildElementWithAttr(Element parent, String attr_key, String attr_value) {
        Boolean result = false;
        ArrayList<Element> elements = getElementsByTagName(parent, DOI_TAG);
        if (elements != null) {
            for (Element element : elements) {
                if (element.hasAttribute(attr_key)) {
                    result = attr_value.equalsIgnoreCase(element.getAttribute(attr_key));
                    break;
                }
            }
        }
        return result;
    }

    public void createReports(Document document) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        removeAllTagsFromDocument(document, REPORT_CONTROL_TAG);

        ArrayList<Element> devices = getElementsByTagName(document, LDEVICE_TAG);
        if (devices != null) {
            for (Element device : devices) {
                createReport(document, device);
                createBufferedReport(document, device);
            }
        }
    }

    public void createReport(Document document, Element device) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        String device_inst = device.getAttribute(INST_ATTR);
        Element ln0 = getElementByTagName(device, LN0_TAG);
        Element ied = getElementByTagName(document, IED_TAG);
        String ied_name = ied.getAttribute(NAME_ATTR);
        String ln_class = ln0.getAttribute(LN_CLASS_ATTR);

        String report_type = "RP";
        String dataset_name = device_inst + "_DS";
        String report_name = device_inst + "_" + report_type;
        String report_id = ied_name + device_inst + "/" + ln_class + "$" + report_type + "$" + report_name;

        Element report_control = createXMLFromText(REPORT_CONTROL_XML);
        report_control.setAttribute(REPORT_ID_ATTR, report_id);
        report_control.setAttribute(NAME_ATTR, report_name);
        report_control.setAttribute(DAT_SET_ATTR, dataset_name);

        insertNewElementToDocument(document, ln0, report_control);
    }

    public void createBufferedReport(Document document, Element device) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        String device_inst = device.getAttribute(INST_ATTR);
        Element ln0 = getElementByTagName(device, LN0_TAG);
        Element ied = getElementByTagName(document, IED_TAG);
        String ied_name = ied.getAttribute(NAME_ATTR);
        String ln_class = ln0.getAttribute(LN_CLASS_ATTR);

        String report_type = "BR";
        String dataset_name = device_inst + "_DS";
        String report_name = device_inst + "_" + report_type;
        String report_id = ied_name + device_inst + "/" + ln_class + "$" + report_type + "$" + report_name;

        Element report_control = createXMLFromText(BUFFERED_REPORT_CONTROL_XML);
        report_control.setAttribute(REPORT_ID_ATTR, report_id);
        report_control.setAttribute(NAME_ATTR, report_name);
        report_control.setAttribute(DAT_SET_ATTR, dataset_name);

        insertNewElementToDocument(document, ln0, report_control);
    }

//    public void createNode() {
        //            Element newElement = document.createElement("newElement");
//            newElement.setTextContent("Это новый узел");
//            document.getDocumentElement().appendChild(newElement);
//    }

    public Element createXMLFromText(String xml) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        TransformerFactory transformer_factory = TransformerFactory.newInstance();
        Transformer transformer = transformer_factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        Element new_element = document.getDocumentElement();
        return new_element;
    }

    public void insertNewElementToDocument(Document document, Element parent_element, Element new_element) {
        Node clone = document.importNode(new_element, true);
        parent_element.appendChild(clone);
    }

    public ArrayList<Element> getElementsByTagName(Element parent, String tag_name) {
        ArrayList<Element> result = null;
        NodeList list = parent.getElementsByTagName(tag_name);
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.getTagName().equalsIgnoreCase(tag_name)) {
                    if (result == null) result = new ArrayList<>();
                    result.add(element);
                }
            }
        }
        return result;
    }

    public ArrayList<Element> getElementsByTagName(Document document, String tag_name) {
        ArrayList<Element> result = null;
        NodeList list = document.getDocumentElement().getElementsByTagName(tag_name);
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.getTagName().equalsIgnoreCase(tag_name)) {
                    if (result == null) result = new ArrayList<>();
                    result.add(element);
                }
            }
        }
        return result;
    }

    public Element getElementByTagName(Document document, String tag_name) {
        Element result = null;
        NodeList list = document.getDocumentElement().getElementsByTagName(tag_name);
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.getTagName().equalsIgnoreCase(tag_name)) {
                    result = element;
                    break;
                }
            }
        }
        return result;
    }

    public Element getElementByTagName(Element parent, String tag_name) {
        Element result = null;
        NodeList list = parent.getElementsByTagName(tag_name);
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.getTagName().equalsIgnoreCase(tag_name)) {
                    result = element;
                    break;
                }
            }
        }
        return result;
    }


    public boolean removeTagFromDocument(Document document, String tag_name) {
        Boolean result = false;
        NodeList list = document.getDocumentElement().getElementsByTagName(tag_name);
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.getTagName().equalsIgnoreCase(tag_name)) {
                    Node parent_node = node.getParentNode();
                    if (parent_node != null) {
                        parent_node.removeChild(element);
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /* return true, если хотя бы один элемент был удален */
    public boolean removeAllTagsFromDocument(Document document, String tag_name) {
        Boolean result = false;
        NodeList list = document.getDocumentElement().getElementsByTagName(tag_name);
        while (list.getLength() > 0) {
            Node node = list.item(0);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.getTagName().equalsIgnoreCase(tag_name)) {
                    Node parent_node = node.getParentNode();
                    if (parent_node != null) {
                        parent_node.removeChild(element);
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    public void saveDocument(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        Source source = new DOMSource(document);
        Result result = new StreamResult(new File("TEST.ICD"));
        transformer.transform(source, result);
    }
}


//    String xml = "<Services>\n" +
//            "<DynAssociation />\n" +
//            "<GetDirectory />\n" +
//            "<GetDataObjectDefinition />\n" +
//            "<DataObjectDirectory />\n" +
//            "<GetDataSetValue />\n" +
//            "<SetDataSetValue />\n" +
//            "<DataSetDirectory />\n" +
//            "<ConfDataSet max=\"10\" modify=\"false\" />\n" +
//            "<DynDataSet max=\"42\" />\n" +
//            "<ReadWrite />\n" +
//            "<ConfReportControl max=\"20\" bufConf=\"false\" />\n" +
//            "<GetCBValues />\n" +
//            "<ReportSettings rptID=\"Dyn\" optFields=\"Dyn\" bufTime=\"Dyn\" trgOps=\"Dyn\" intgPd=\"Dyn\" owner=\"true\" resvTms=\"true\" />\n" +
//            "<ConfLNs fixPrefix=\"true\" fixLnInst=\"true\" />\n" +
//            "<GOOSE max=\"0\" />\n" +
//            "<GSSE max=\"0\" />\n" +
//            "<FileHandling />\n" +
//            "</Services>\n";

