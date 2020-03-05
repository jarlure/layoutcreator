package layouttool;

import com.jarlure.project.bean.Entity;
import com.jarlure.project.util.file.code.CodeParser;
import com.jarlure.project.util.file.code.TemplateLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static layouttool.ComponentConfigureXMLFileEditor.*;

public class LayoutGenerator {

    private static final Logger LOG = Logger.getLogger(LayoutGenerator.class.getName());

    public static void main(String[] args) {
        execute("com.jarlure.layoutcreator.layout.PSLayout");
    }

    public static void execute(final String layoutClassName) {
        String layoutName = CodeParser.getSimpleName(layoutClassName);
        final String ResourceDir = System.getProperty("user.dir") + "\\src\\main\\resources\\Interface\\" + layoutName + "\\";
        File j3oFile = new File(ResourceDir + layoutName + ".j3o");
        if (!j3oFile.exists())
            throw new IllegalArgumentException("找不到" + layoutName + "的图片资源文件，请检查文件路径是否正确：" + j3oFile.getAbsolutePath());
        File xmlFile = new File(ResourceDir + layoutName + ".xml");
        if (!xmlFile.exists())
            throw new IllegalArgumentException("找不到" + layoutName + "的组件配置文件，请检查文件路径是否正确：" + xmlFile.getAbsolutePath());

        Entity xmlData = readLayoutXMLData(xmlFile);
        initImgIndex(xmlData);
        createLayout(LayoutTemplate.class, layoutClassName, xmlData);
    }

    private static Entity readLayoutXMLData(File xmlFile){
        return readComponentConfigure(xmlFile);
    }

    private static void initImgIndex(Entity xmlData) {
        int index=0;
        for (int i : xmlData.getItems()) {
            Entity componentImg = xmlData.getValue(i,Component.Img);
            for (int j:componentImg.getItems()){
                componentImg.setValue(j,Img.Index,index);//某个组件的图片索引集合映射图片索引集合
                index++;
            }
        }
    }

    private static void createLayout(Class template, String className, Entity xmlData) {
        String[] componentTypeArray = new String[xmlData.size()];
        String[] componentVaryNameArray = new String[xmlData.size()];
        String[] componentConstantNameArray = new String[xmlData.size()];
        String[] componentImgIndexOrChildrenNameArray = new String[xmlData.size()];
        Map<String,String> componentLinkMap = new HashMap<>();
        int index=0;
        for (int i=xmlData.size()-1;i>=0;i--){
            String componentType = xmlData.getValue(i,Component.Type);
            String componentName = xmlData.getValue(i,Component.Name);
            String componentLink = xmlData.getValue(i,Component.Link);
            Entity componentImg = xmlData.getValue(i,Component.Img);
            Entity componentChild = xmlData.getValue(i,Component.Child);
            String componentImgIndexOrChildrenName;{
                StringBuilder builder = new StringBuilder();
                if (componentChild.isEmpty()){
                    for (int j=componentImg.size()-1;j>=0;j--){
                        int imgIndex = componentImg.getValue(j,Img.Index);
                        builder.append(imgIndex).append(',');
                    }
                    if (builder.length() == 0) componentImgIndexOrChildrenName = "new int[0]";
                    else componentImgIndexOrChildrenName = builder.substring(0, builder.length() - 1);
                }else{
                    for (int j=componentChild.size()-1;j>=0;j--){
                        String childName = componentChild.getValue(j,Child.Name);
                        childName= Helper.toConstantName(childName);
                        builder.append(childName).append(',');
                    }
                    componentImgIndexOrChildrenName = builder.substring(0,builder.length()-1);
                }
            }
            componentTypeArray[index]=componentType;
            componentVaryNameArray[index]=componentName;
            componentConstantNameArray[index]= Helper.toConstantName(componentName);
            componentImgIndexOrChildrenNameArray[index]=componentImgIndexOrChildrenName;
            if (componentLink!=null) componentLinkMap.put(componentName,componentLink);
            index++;
        }
        TemplateLoader templateLoader = new TemplateLoader(template);
        templateLoader.set("COMPONENT_NAME",componentConstantNameArray);
        templateLoader.set("componentName",componentVaryNameArray);
        templateLoader.set("COMPONENT_TYPE",componentTypeArray);
        templateLoader.set("IMG_INDEX_or_CHILDREN_NAME",componentImgIndexOrChildrenNameArray);
        if (!componentLinkMap.isEmpty()){
            String[] linkedComponentNameArray = new String[componentLinkMap.size()];
            String[] linkedComponentLayoutArray = new String[componentLinkMap.size()];
            int i=0;
            for (Map.Entry<String,String> entry:componentLinkMap.entrySet()){
                linkedComponentNameArray[i]=entry.getKey();
                linkedComponentLayoutArray[i]=entry.getValue();
                i++;
            }
            templateLoader.set("LINKED_COMPONENT_NAME",linkedComponentNameArray);
            templateLoader.set("Layout",linkedComponentLayoutArray);
        }

        try {
            templateLoader.update(Class.forName(className));
        } catch (ClassNotFoundException e) {
            LOG.log(Level.INFO, "未找到{0}，系统将自动创建...", className);
            templateLoader.create(className);
        }
    }

    private static class Helper{

        private static String toConstantName(String componentName){
            String constantName;
            if (componentName.endsWith("3D")){
                StringBuilder builder = new StringBuilder(componentName.length()-2);
                builder.append(componentName,0,componentName.length()-2);
                constantName = CodeParser.toConstantName(builder.toString())+"_3D";
            }else constantName = CodeParser.toConstantName(componentName);
            return constantName;
        }

    }

}
