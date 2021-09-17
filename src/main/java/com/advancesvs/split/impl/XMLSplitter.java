/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.advancesvs.split.impl;

import com.advancesvs.split.Main;
import com.advancesvs.split.api.Splitter;
import com.advancesvs.split.common.Resources;
import com.sun.org.apache.xml.internal.utils.NameSpace;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 *
 * @author mafragias
 */
public class XMLSplitter implements Splitter {

    private final int numOfFiles;
    private final InputStream originalFile;
    private final String path;
    private Document doc;
    private String element;
    
    public XMLSplitter (String originalFile, double size) throws FileNotFoundException{
        File file = new File(originalFile);
        String split = (file.getParent()).concat("/"+Resources.SPLIT+"/");
        new File(split).mkdir();
        this.path = (file.getParent()).concat("/"+Resources.SPLIT+"/").concat(file.getName().substring(0,file.getName().lastIndexOf(".")));
        this.numOfFiles = (int)Math.round((new File(originalFile).length()) / (size*1024*1024)) ;
        this.originalFile = new FileInputStream(originalFile);
    }

    public XMLSplitter(String originalFile, double size,String element) throws FileNotFoundException{
        this(originalFile,size);
        this.element = element;
    }
    
    @Override
    public void split(){
        if (numOfFiles > 0) {
            try {
                SAXReader reader = new SAXReader();
//                reader.setEncoding(new InputStreamReader(originalFile).getEncoding());
                reader.setEncoding("UTF-8");
                doc = reader.read(originalFile);
////                Element rootBaseElement = DocumentHelper.createElement("marc:collection"); // for big files
                Element rootBaseElement = DocumentHelper.createElement(doc.getRootElement().getName());
////                rootBaseElement.elements().clear();
                List<Element> elementsList;
                if (element!=null){
                    elementsList = doc.getRootElement().element(element).elements();
                    doc.getRootElement().elements().forEach(elmnt -> {
                        if (!elmnt.getQName().getName().equals(element))
                            rootBaseElement.add(elmnt.detach());
                        else
                            rootBaseElement.add(DocumentHelper.createElement(element));
                    });
                } else
                    elementsList = doc.getRootElement().elements();
                int elementsPerFile = elementsList.size() / numOfFiles;
                int i=0;
                int j=0;
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter xmlwriter = new XMLWriter(new OutputStreamWriter(new FileOutputStream(path+"_part_"+i+".xml"), "UTF-8"), format);
                Element newRootElement = rootBaseElement.createCopy();
                while(i<numOfFiles){
                    while (j<elementsList.size()){
                        if (j%elementsPerFile==0){
                            xmlwriter.write(newRootElement);
                            xmlwriter.close();
                            if (i==numOfFiles)
                                break;
                            Logger.getLogger(XMLSplitter.class.getName()).log(Level.INFO, "Exported file {0}_part_{1}.xml", new Object[]{path, i});
                            xmlwriter = new XMLWriter( new OutputStreamWriter(new FileOutputStream(path+"_part_"+i+".xml"), "UTF-8"), format );
                            newRootElement = rootBaseElement.createCopy();
                            i++;
                        }
                        if (element==null)
                            newRootElement.add(elementsList.get(j).detach());
                        else
                            newRootElement.element(element).add(elementsList.get(j).detach());
                        j++;
                        if (j==elementsList.size()){
                            xmlwriter.write(newRootElement);
                            xmlwriter.close();
                        }
                    }
                }
            } catch (DocumentException | UnsupportedEncodingException | FileNotFoundException ex) {
                Logger.getLogger(XMLSplitter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(XMLSplitter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
