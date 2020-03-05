package layouttool;

import com.jarlure.project.bean.Entity;
import com.sun.org.apache.xerces.internal.impl.Constants;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
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

}