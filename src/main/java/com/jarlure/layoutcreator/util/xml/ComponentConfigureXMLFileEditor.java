package com.jarlure.layoutcreator.util.xml;

import com.jarlure.project.bean.Entity;
import com.sun.org.apache.xerces.internal.impl.Constants;
import com.wutka.dtd.*;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.EnumMap;
import java.util.List;

public class ComponentConfigureXMLFileEditor {

    public enum Component{
        Type,Name,Link,Img,Child
    }

    public enum Img{
        Name,URL,
        Index//图片在J3O文件(LayerData)中的序号
    }

    public enum Child{
        Name
    }

    public static String[] readComponentType(File dtdFile){
        try{
            DTDParser parser = new DTDParser(dtdFile);
            DTD dtd = parser.parse();
            Object rootElement = dtd.getItem(0);
            if (rootElement instanceof DTDElement){
                if ("Layout".equals(((DTDElement)rootElement).getName())){
                    DTDElement elementNamedLayout = (DTDElement) rootElement;
                    DTDSequence content = (DTDSequence) elementNamedLayout.getContent();
                    String[] result = new String[content.getItemsVec().size()];
                    int i=0;
                    for (Object item:content.getItemsVec()){
                        result[i]=((DTDName)item).getValue();
                        i++;
                    }
                    return result;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        }
        return new String[0];
    }

    public static Entity readComponentConfigure(File xmlFile) {
        SAXReader xmlReader = new SAXReader();
        Element xmlLayout = null;
        try {
            xmlReader.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            Document doc = xmlReader.read(xmlFile);
            xmlLayout = doc.getRootElement();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        Entity data = new Entity();
        for (Object e : xmlLayout.elements()) {
            Element xmlUIComponent = (Element) e;
            String componentType = xmlUIComponent.getName();
            String componentName = xmlUIComponent.attribute("name").getValue();
            Object componentLink = xmlUIComponent.attribute("link");
            if (componentLink!=null) componentLink=((Attribute)componentLink).getValue();

            Entity componentImg = new Entity();
            Entity componentChild = new Entity();
            List<Element> extra = xmlUIComponent.elements();
            for (Element ee : extra) {
                if ("img".equals(ee.getName())) {
                    String imgName = ee.attributeValue("name");
                    String imgURL = ee.attributeValue("url");
                    if (imgURL == null) imgURL = imgName;
                    componentImg.addItem(new EnumMap<>(Img.class),
                            Img.Name,imgName,
                            Img.URL,imgURL);
                }
                if ("child".equals(ee.getName())){
                    String childName = ee.attributeValue("name");
                    componentChild.addItem(new EnumMap<>(Child.class),
                            Child.Name,childName);
                }
            }
            data.addItem(new EnumMap<>(Component.class),
                    Component.Type, componentType,
                    Component.Name, componentName,
                    Component.Link, componentLink,
                    Component.Img, componentImg,
                    Component.Child, componentChild);
        }
        return data;
    }

    public static void writeComponentConfigure(File xmlFile,Entity data){
        Document doc = DocumentHelper.createDocument();
        doc.addDocType("Layout",null,"layout.dtd");
        Element root = doc.addElement("Layout");
        for (int index:data.getItems()){
            String componentType = data.getValue(index, Component.Type);
            String componentName = data.getValue(index, Component.Name);
            String componentLink = data.getValue(index, Component.Link);
            Entity componentImg = data.getValue(index, Component.Img);
            Entity componentChild = data.getValue(index, Component.Child);

            Element component = root.addElement(componentType);
            component.addAttribute("name",componentName);
            if (componentLink!=null && !componentLink.isEmpty()) component.addAttribute("link",componentLink);
            if (!componentImg.isEmpty()){
                for (int i:componentImg.getItems()){
                    String name = componentImg.getValue(i, Img.Name);
                    String url = componentImg.getValue(i, Img.URL);

                    Element img = component.addElement("img");
                    img.addAttribute("name",name);
                    if (url!=null) img.addAttribute("url",url);
                }
            }
            if (!componentChild.isEmpty()){
                for (int i:componentChild.getItems()){
                    String name = componentChild.getValue(i, Child.Name);

                    Element child = component.addElement("child");
                    child.addAttribute("name",name);
                }
            }
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("utf-8");
        try {
            XMLWriter writer = new XMLWriter(new FileWriter(xmlFile),format);
            writer.write(doc);
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
