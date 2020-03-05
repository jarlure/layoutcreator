package layouttool;

import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.layout.AbstractLayout;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.layout.LayoutHelper;
import com.jarlure.ui.component.UINode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LayoutTemplate extends AbstractLayout {

    private static final int IMG_INDEX_or_CHILDREN_NAME = 0;/*$skip*/
    private static final String LINKED_COMPONENT_NAME="";/*$skip*/
    private static final String COMPONENT_TYPE = "";/*$skip*/
    public static final String COMPONENT_NAME/**/ = "componentName/**/";

    @Override
    public Map<String, Class<? extends Layout>> getLinkedComponentLayoutMap() {
        Map<String, Class<? extends Layout>> map = new HashMap<>();
        map.put(LINKED_COMPONENT_NAME/**/, Layout/**/.class);
        return map;
    }

    @Override
    protected UINode createComponent(List<LayerImageData> layerImageData) {
        LayoutHelper helper=new LayoutHelper(layerImageData);
        helper.add(COMPONENT_TYPE/**/, COMPONENT_NAME/**/, IMG_INDEX_or_CHILDREN_NAME/**/);
        return helper.create(getClass().getSimpleName());
    }

    @Override
    protected void configureUIComponent() {
    }

}
