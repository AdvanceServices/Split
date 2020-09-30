/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartupds.rdfsplitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

/**
 *
 * @author mafragias
 */
public class Splitter {
    static final CommandLineParser PARSER = new DefaultParser();
    static Options options = new Options();
    private final int numberOfFiles;
    private final InputStream originalFile;
    private final String path;
    private Document doc;
    
    public Splitter (String originalFile, int size) throws FileNotFoundException{
        this.path = originalFile.substring(0,originalFile.lastIndexOf("."));
        this.originalFile=new FileInputStream(originalFile);
        numberOfFiles = (int)(new File(originalFile).length()) / (size*1024*1024) ;
    }
    
    private static void createOptionsList(){
        Option fileOption = new Option("f", "file", true,"The RDF file");
        fileOption.setRequired(true);
        
        Option sizeOption = new Option("s", "size", true,"The file size in MB");
        sizeOption.setRequired(true);
        
        options.addOption(fileOption)
               .addOption(sizeOption);
    }

    public void split(File exportFolder) throws SAXException, IOException, DocumentException{
        SAXReader reader = new SAXReader();
        reader.setEncoding("UTF-8");
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
                    System.out.println("Exported file "+path+"_part_"+i+".rdf");
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

    public static void main(String []args) throws Exception{
        createOptionsList();
        CommandLine cli = PARSER.parse(options, args);
        File file = new File(cli.getOptionValue("file"));
        boolean isFolder = file.isDirectory();
        if (isFolder){
            listFilesForFolder(new File (file.getAbsolutePath())).forEach( path -> {
                Splitter splitter;
                try {
                    splitter = new Splitter(path, Integer.parseInt(cli.getOptionValue("size")));
                    splitter.split(null);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Splitter.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException | DocumentException | IOException ex) {
                    Logger.getLogger(Splitter.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } else {
            Splitter splitter=new Splitter(file.getAbsolutePath(), Integer.parseInt(cli.getOptionValue("size")));
            splitter.split(null);
        }
        
    }
    
    
    public static ArrayList<String> listFilesForFolder(final File folder) {
        ArrayList<String> filePaths = new ArrayList<>(); 
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                filePaths.add(folder.getAbsoluteFile() + "\\" + fileEntry.getName());
                System.out.println(folder.getAbsoluteFile() + "\\" + fileEntry.getName());
            }
        }
        return filePaths;
    }
}
