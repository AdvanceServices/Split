/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.advancesvs.split.impl;

import com.advancesvs.split.api.Splitter;
import com.advancesvs.split.common.Resources;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 *
 * @author mafragias
 */
public class RDFSplitter implements Splitter {
    private final int numberOfFiles;
    private final InputStream originalFile;
    private final String path;
    private Document doc;
    
    public RDFSplitter (String originalFile, double size) throws FileNotFoundException{
        File file = new File(originalFile);
        String split = (file.getParent()).concat("/"+Resources.SPLIT+"/");
        new File(split).mkdir();
        this.path = (file.getParent()).concat("/"+Resources.SPLIT+"/").concat(file.getName().substring(0,file.getName().lastIndexOf(".")));
        this.numberOfFiles = (int)Math.round((new File(originalFile).length()) / (size*1024*1024)) ;
        this.originalFile = new FileInputStream(originalFile);
    }

    @Override
    public void split() {
        try {
            if(numberOfFiles>0){
                SAXReader reader = new SAXReader();
                reader.setEncoding(new InputStreamReader(originalFile).getEncoding());
                doc = reader.read(originalFile);
                Element rootElement = doc.getRootElement();
                Element rootBaseElement = rootElement.createCopy();
                rootBaseElement.elements().clear();
                List<Element> elementsList = rootElement.elements();
                int elementsPerFile = elementsList.size() / numberOfFiles;

                int i=0;
                int j=0;
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter xmlwriter = new XMLWriter(new OutputStreamWriter(new FileOutputStream(path+"_part_"+i+".rdf"), "UTF-8"), format);
                Element newRootElement = rootBaseElement.createCopy();
                while(i<numberOfFiles){
                    while (j<elementsList.size()){
                        if (j%elementsPerFile==0){
                            xmlwriter.write(newRootElement);
                            xmlwriter.close();
                            if (i==numberOfFiles)
                                break;
                            Logger.getLogger(RDFSplitter.class.getName()).log(Level.INFO, "Exported file {0}_part_{1}.rdf", new Object[]{path, i});                           
                            xmlwriter = new XMLWriter( new OutputStreamWriter(new FileOutputStream(path+"_part_"+i+".rdf"), "UTF-8"), format );
                            newRootElement = rootBaseElement.createCopy();
                            i++;
                        }
                        newRootElement.add(elementsList.get(j).detach());
                        j++;
                        if (j==elementsList.size()){
                            xmlwriter.write(newRootElement);
                            xmlwriter.close();
                        }
                    }
                }
            }
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(RDFSplitter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
