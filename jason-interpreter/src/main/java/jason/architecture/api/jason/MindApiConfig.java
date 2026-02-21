package jason.architecture.api.jason;

import jason.util.Config;

public class MindApiConfig extends Config {

    @Override
    public String getMindInspectorArchClassName() {
        return MindApiArch.class.getName();
    }

    @Override
    public String getMindInspectorWebServerClassName() {
        return MindApiManager.class.getName();
    }

}
