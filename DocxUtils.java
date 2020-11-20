public class DocxUtils {
    
     /**
     * <br>
     * 描 述: doc内容改变 <br>
     * 作 者: shizhenwei <br>
     * 历 史: (版本) 作者 时间 注释
     * 
     * @param is
     *            doc文档模板
     * @param params
     *            key value 将模板里的可以替换为响应VALUE
     * @return
     * @throws IOException
     */
    public static byte[] docContentChange(InputStream is, Map<String, String> params) throws IOException {
        HWPFDocument document = new HWPFDocument(is);
        Range range = document.getRange();

        Set<String> keys = params.keySet();
        for (String key : keys) {
            range.replaceText("{{"+key.toString()+"}}", params.get(key));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.write(baos);
        byte[] bytes = baos.toByteArray();

        document.close();
        baos.close();
        return bytes;
    }
    
    /**
     * <br>描 述: docx内容改变
     * <br>作 者: shizhenwei 
     * <br>历 史: (版本) 作者 时间 注释
     * @param is    docx文档模板
     * @param params key value 将模板里的可以替换为响应VALUE
     * @return
     * @throws IOException 
     * @throws XWPFConverterException 
     */
    public static byte[] docxContentChange(InputStream is,Map<String,String> params) throws XWPFConverterException, IOException{
        XWPFDocument document = new XWPFDocument(is);
        List<XWPFParagraph> list = document.getParagraphs();
        for(XWPFParagraph paragraph : list){
            String regex = "(\\w|\\W)*\\{\\{\\w+\\}\\}(\\w|\\W)*";//{{string}}匹配
            if(!paragraph.getText().matches(regex)){
                continue;
            }
            List<XWPFRun> runs = paragraph.getRuns();
            loop : for(int i=0; i<runs.size(); i++){
                XWPFRun run = runs.get(i);
                if(!run.text().matches(regex)){
                    continue;
                }
                if(null==params || params.keySet().size()<1){
                    run.setText("",0);
                    continue;
                }
                Set<String> keySet = params.keySet();
                for(String key : keySet){
                    if(run.text().contains(key)){
                        String text = run.text().replaceAll(getReplaceAllKey("{{"+key.toString()+"}}"), params.get(key));
                        run.setText(text,0);
                        continue loop;
                    }
                }
                run.setText("",0);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.write(baos);
        byte[] bytes = baos.toByteArray();
        document.close();
        baos.close();
        return bytes;
    }
    
    /**
     * <br>描 述:    将docx字节数组流转换为pdf字节数组流
     * <br>作 者: shizhenwei 
     * <br>历 史: (版本) 作者 时间 注释
     * @param docxBytes docx文档字节数组
     * @return
     * @throws XWPFConverterException
     * @throws IOException
     * 注：需在部署系统安装word对应的中文字体
     */
    public static byte[] docx2pdf(byte[] docxBytes) throws XWPFConverterException, IOException{
        PdfOptions options = PdfOptions.create();
        XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(docxBytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfConverter.getInstance().convert(document, baos, options);
        return baos.toByteArray();
    }
    
    
    /**
     * <br>描 述:    将Word模板流改变内容后转换为pdf字节数组流
     * <br>作 者: shizhenwei 
     * <br>历 史: (版本) 作者 时间 注释
     * @param is docx文档输入流
     * @param params key value 将模板里的可以替换为响应VALUE
     * @return
     * @throws IOException 
     * @throws XWPFConverterException 
     * * 注：需在部署系统安装word对应的中文字体
     */
    public static byte[] docx2pdf(InputStream is,Map<String, String> params) throws XWPFConverterException, IOException{
        XWPFDocument document = new XWPFDocument(is);
        List<XWPFParagraph> list = document.getParagraphs();
        for(XWPFParagraph paragraph : list){
            String regex = "(\\w|\\W)*\\{\\{\\w+\\}\\}(\\w|\\W)*";//{{string}}匹配
            if(!paragraph.getText().matches(regex)){
                continue;
            }
            List<XWPFRun> runs = paragraph.getRuns();
            for(int i=0; i<runs.size(); i++){
                XWPFRun run = runs.get(i);
                if(!run.text().matches(regex)){
                    continue;
                }
                Set<String> keySet = params.keySet();
                for(String key : keySet){
                    key = "{{"+key+"}}";
                    if(run.text().contains(key)){
                        String text = run.text().replaceAll(getReplaceAllKey(key), params.get(key));
                        run.setText(text,0);
                    }
                }
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfOptions options = PdfOptions.create();
        PdfConverter.getInstance().convert(document, baos, options);
        byte[] bytes = baos.toByteArray();
        document.close();
        baos.close();
        return bytes;
    }
    
    /**
     * 
     * <br>描 述:    String replaceAll方法默认正则 {{}} 对特殊字符进行转义,如 {} == \\{\\}
     * <br>作 者: shizhenwei 
     * <br>历 史: (版本) 作者 时间 注释
     * @param key
     * @return
     */
    public static String getReplaceAllKey(String key){
        String afterKey = "";
        for(int i=0; i<key.length(); i++){
            if('{'==key.charAt(i)){
                afterKey+="\\{";
            }else if('}'==key.charAt(i)){
                afterKey+="\\}";
            }else{
                afterKey+=key.charAt(i);
            }
        }
        return afterKey;
    }
}
