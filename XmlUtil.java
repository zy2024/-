package com.yonyou.fmcg.picc.customer.utils;

import com.thoughtworks.xstream.XStream;
import com.yonyou.fmcg.picc.customer.config.utils.DataUtils;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * xml处理工具类
 * songyj
 */
public class NewXmlUtil{

    private static Logger logger = LoggerFactory.getLogger(NewXmlUtil.class);

    /**
     * 将对象直接转换成String类型的 XML输出
     *
     * @param obj
     * @return
     */
    public static String convertToXmlByJaxb(Object obj) {
        // 创建输出流
        StringWriter sw = new StringWriter();
        try {
            // 利用jdk中自带的转换类实现
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            // 格式化xml输出的格式
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            // 将对象转换成输出流形式的xml
            marshaller.marshal(obj, sw);
        } catch (JAXBException e) {
            logger.error("公共模块--->对象转换xml失败！",e);
        }
        return sw.toString();
    }

    /**
     *  xstream方式转换（注意： 暂不支持map格式的属性！）
     * @param obj 对象
     * @return
     */
    public static  String convertToXmlByXStream(Object obj) {
        XStream xstream1 = new XStream();
        xstream1.autodetectAnnotations(true);
        return  xstream1.toXML(obj) ;
    }

    /**
     * 递归解析任意的xml 遍历每个节点和属性
     *
     * @param xmlStr
     */
    public static List<Map<String, String>> iterateWholeXML(String xmlStr) {
        List<Map<String, String>> list = new ArrayList<>();
        try {
            Document document = DocumentHelper.parseText(xmlStr);
            Element root = document.getRootElement();
            recursiveNode(root, list);
            return list;
        } catch (DocumentException e) {
            logger.error("公共模块--->iterateWholeXML error ! ",e);
        }
        return null;
    }

    /**
     * 递归遍历所有的节点获得对应的值
     *
     * @param
     */
    private static void recursiveNode(Element root, List<Map<String, String>> list) {

        // 遍历根结点的所有孩子节点
        HashMap<String, String> map = new HashMap<>();
        for (Iterator iter = root.elementIterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            if (element == null)
                continue;
            // 获取属性和它的值
            for (Iterator attrs = element.attributeIterator(); attrs.hasNext();) {
                Attribute attr = (Attribute) attrs.next();
                if (attr == null) continue;
                String attrName = attr.getName();
                String attrValue = attr.getValue();
                map.put(attrName, attrValue);
            }
            // 如果有PCDATA，则直接提出
            if (element.isTextOnly()) {
                String innerName = element.getName();
                String innerValue = element.getText();
                map.put(innerName, innerValue);
                list.add(map);
            } else {
                // 递归调用
                recursiveNode(element, list);
            }
        }
    }

    /**
     * 将String类型的xml转换成对象
     */
    public static Object convertXmlStrToObject(Class clazz, String xmlStr) {
        Object xmlObject = null;
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, true);
            // 进行将Xml转成对象的核心接口
            Unmarshaller unmarshaller = context.createUnmarshaller();
            xmlObject = unmarshaller.unmarshal(xmlInputFactory.createXMLStreamReader(new StringReader(xmlStr)));
        } catch (Exception e) {
            logger.error("公共模块--->COMMON--->类型转换过程中失败！",e);
        }
        return xmlObject;
    }

    public static List<Map> parseXmtoList(String pStrxml) {
        if (!DataUtils.isBlank(pStrxml)) {
            try {
                List<Map> list = new ArrayList<Map>();
                Document document = DocumentHelper.parseText(pStrxml);
                Element nodesElement = document.getRootElement();
                List nodes = nodesElement.elements();
                for (Iterator its = nodes.iterator(); its.hasNext();) {
                    Element nodeElement = (Element) its.next();
                    Map map = xmltoMap(nodeElement.asXML());
                    list.add(map);
                }
                return list;
            } catch (Exception e) {
                logger.error("公共模块--->",e);
            }
        }
        return new ArrayList<>();
    }

    /**
     * xml 转map 只转attribute 不包括element
     *
     * @param xml
     * @return
     */
    public static Map xmltoMap(String xml) {
        try {
            Map map = new HashMap<Object, Object>();
            Document document = DocumentHelper.parseText(xml);
            Element nodeElement = document.getRootElement();
            List attributes = nodeElement.attributes();

            for (Iterator it = attributes.iterator(); it.hasNext();) {
                Attribute attr = (Attribute) it.next();
                map.put(attr.getQName().getName(), attr.getText().trim());
            }
            return map;
        } catch (Exception e) {

        }
       return new HashMap();
    }

    //*****************************   注意：  以下方法为其他的地方拷贝的代码  （未测试！！！）**********************************
    public static void getObjctFromMap(Object obj, HashMap map)
            throws Exception
    {
        Class cls = obj.getClass();
        Field fieldlist[] = cls.getDeclaredFields();
        Field field = null;
        for (int i = 0; i < fieldlist.length; i++)
        {
            Field fld = fieldlist[i];
            String FieldType = fld.getType().toString();
            String FieldName = fld.getName();
            if (FieldType.equalsIgnoreCase("long"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.setLong(obj, 0L);
                else
                    field.setLong(obj, Long.valueOf(map.get(FieldName).toString().trim()).longValue());
            } else
            if (FieldType.equalsIgnoreCase("short"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.setShort(obj, (short)0);
                else
                    field.setShort(obj, Short.valueOf(map.get(FieldName).toString().trim()).shortValue());
            } else
            if (FieldType.equalsIgnoreCase("int"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.setInt(obj, 0);
                else
                    field.setInt(obj, Integer.valueOf(map.get(FieldName).toString().trim()).intValue());
            } else
            if (FieldType.equalsIgnoreCase("byte"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.setByte(obj, (byte)0);
                else
                    field.setByte(obj, Byte.valueOf(map.get(FieldName).toString().trim()).byteValue());
            } else
            if (FieldType.equalsIgnoreCase("float"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.setFloat(obj, 0.0F);
                else
                    field.setFloat(obj, Float.valueOf(map.get(FieldName).toString().trim()).floatValue());
            } else
            if (FieldType.equalsIgnoreCase("double"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.setDouble(obj, 0.0D);
                else
                    field.setDouble(obj, Double.valueOf(map.get(FieldName).toString().trim()).doubleValue());
            } else
            if (FieldType.equalsIgnoreCase("boolean"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.setBoolean(obj, false);
                else
                    field.setBoolean(obj, Boolean.valueOf(map.get(FieldName).toString().trim()).booleanValue());
            } else
            if (FieldType.equalsIgnoreCase("class java.util.Date"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.set(obj, Date.parse("1899-12-31"));
                else
                    field.set(obj, Date.parse(map.get(FieldName).toString().trim()));
            } else
            if (FieldType.equalsIgnoreCase("class java.lang.String"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.set(obj, "");
                else
                    field.set(obj, map.get(FieldName).toString().trim());
            } else
            if (FieldType.equalsIgnoreCase("class java.lang.Integer"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.set(obj, Integer.valueOf(0));
                else
                    field.set(obj, Integer.valueOf(map.get(FieldName).toString().trim()));
            } else
            if (FieldType.equalsIgnoreCase("class java.lang.Double"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.set(obj, Double.valueOf(0.0D));
                else
                    field.set(obj, Double.valueOf(map.get(FieldName).toString().trim()));
            } else
            if (FieldType.equalsIgnoreCase("class java.lang.Float"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.set(obj, Float.valueOf(0.0F));
                else
                    field.set(obj, Float.valueOf(map.get(FieldName).toString().trim()));
            } else
            if (FieldType.equalsIgnoreCase("class java.lang.Short"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.set(obj, Short.valueOf((short)0));
                else
                    field.set(obj, Short.valueOf(map.get(FieldName).toString().trim()));
            } else
            if (FieldType.equalsIgnoreCase("class java.lang.Long"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.set(obj, Long.valueOf(0L));
                else
                    field.set(obj, Long.valueOf(map.get(FieldName).toString().trim()));
            } else
            if (FieldType.equalsIgnoreCase("class java.math.BigDecimal"))
            {
                field = cls.getField(FieldName);
                if (map.get(FieldName) == null || map.get(FieldName).toString().trim().equalsIgnoreCase("null"))
                    field.set(obj, new BigDecimal(0));
                else
                    field.set(obj, new BigDecimal(map.get(FieldName).toString().trim()));
            } else
            {
                System.out.println((new StringBuilder("不支持的数据类型:[")).append(FieldType).append("]").toString());
                field.set(obj, null);
            }
        }

    }

    /**
     * doc2String 将xml文档内容转为String
     * @return 字符串
     * @param document
     */
    public static String doc2String(Document document) {
        String s = "";
        try {
            // 使用输出流来进行转化
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // 使用GB2312编码
            OutputFormat format = new OutputFormat("", true, "GBK");
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
            s = out.toString("GBK");
        } catch (Exception ex) {
        }
        return s;
    }

    /**
     * string2Document 将字符串转为Document
     * @return
     * @param str
     * xml格式的字符串
     */
    public static Document string2Document(String str) {
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(str);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return doc;
    }

    /**
     * doc2XmlFile 将Document对象保存为一个xml文件到本地
     * @return true:保存成功 flase:失败
     * @param filename 保存的文件名
     * @param document 需要保存的document对象
     */
    public static boolean doc2XmlFile(Document document, String filename) {
        boolean flag = true;
        try {
            /* 将document中的内容写入文件中 */
            // 默认为UTF-8格式，指定为"GB2312"
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("GB2312");
            XMLWriter writer = new XMLWriter(new FileWriter(new File(filename)), format);
            writer.write(document);
            writer.close();
        } catch (Exception ex) {
            flag = false;
        }
        return flag;
    }
    /**
     * string2XmlFile 将xml格式的字符串保存为本地文件，如果字符串格式不符合xml规则，则返回失败
     *
     * @return true:保存成功 flase:失败
     * @param filename
     *            保存的文件名
     * @param str
     *            需要保存的字符串
     */
    public static boolean string2XmlFile(String str, String filename) {
        boolean flag = true;
        try {
            Document doc = DocumentHelper.parseText(str);
            flag = doc2XmlFile(doc, filename);
        } catch (Exception ex) {
            flag = false;
        }
        return flag;
    }

    /**
     * load 载入一个xml文档
     *
     * @return 成功返回Document对象，失败返回null
     * @param filename
     *            文件路径
     */
    public static Document load(String filename) {
        Document document = null;
        try {
            SAXReader saxReader = new SAXReader();
            document = saxReader.read(new File(filename));
        } catch (Exception ex) {
        }
        return document;
    }
    /**
     * 演示手动创建一个Document，并保存为XML文件
     */
    public void xmlWriteDemoByDocument() {
        /** 建立document对象 */
        Document document = DocumentHelper.createDocument();
        /** 建立config根节点 */
        Element configElement = document.addElement("config");
        /** 建立ftp节点 */
        configElement.addComment("东电ftp配置");
        Element ftpElement = configElement.addElement("ftp");
        ftpElement.addAttribute("name", "DongDian");
        /** ftp 属性配置 */
        Element hostElement = ftpElement.addElement("ftp-host");
        hostElement.setText("127.0.0.1");
        (ftpElement.addElement("ftp-port")).setText("21");
        (ftpElement.addElement("ftp-user")).setText("cxl");
        (ftpElement.addElement("ftp-pwd")).setText("longshine");
        ftpElement.addComment("ftp最多尝试连接次数");
        (ftpElement.addElement("ftp-try")).setText("50");
        ftpElement.addComment("ftp尝试连接延迟时间");
        (ftpElement.addElement("ftp-delay")).setText("10");
        /** ftp节点删除ftp-host节点 */
        ftpElement.remove(hostElement);
        /** ftp节点删除name属性 */
        //ftpElement.remove(nameAttribute);
        /** 修改ftp-host的值 */
        hostElement.setText("192.168.0.1");
        /** 修改ftp节点name属性的值 */
        //nameAttribute.setValue("ChiFeng");
        /** 保存Document */
        doc2XmlFile(document, "classes/xmlWriteDemoByDocument.xml");
    }

}
