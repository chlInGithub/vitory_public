package com.chl.victory.common.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * @author ChenHailong
 * @date 2020/6/24 15:59
 **/
public class XmlUtil {

    /**
     * @param xml  xml内容
     * @param rootNodeName xml根节点的名称
     * @param rootClass  将xml根节点包含的内容解析为对应的类型
     * @param <T>
     * @return
     */
    public static <T> T fromXml(String xml, String rootNodeName, Class<T> rootClass){
        XStream xStream = new XStream(new StaxDriver());
        xStream.alias(rootNodeName, rootClass);
        xStream.ignoreUnknownElements();
        T result = (T)xStream.fromXML(xml);
        return result;
    }

    /**
     * xml根节点的名称为xml
     * @param xml
     * @param rootClass
     * @param <T>
     * @return
     */
    public static <T> T fromXml(String xml, Class<T> rootClass){
        XStream xStream = new XStream(new StaxDriver());
        xStream.alias("xml", rootClass);
        xStream.ignoreUnknownElements();
        T result = (T)xStream.fromXML(xml);
        return result;
    }
}
