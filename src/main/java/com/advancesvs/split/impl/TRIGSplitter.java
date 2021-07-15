/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.advancesvs.split.impl;

import com.advancesvs.split.api.Splitter;
import com.advancesvs.split.common.Resources;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

/**
 *
 * @author mafragias
 */
public class TRIGSplitter implements Splitter {

    private final String path;
    private final int numberOfFiles;
    private final FileInputStream originalFile;
    private final Path originalPath;


    public TRIGSplitter(String originalFile, double size) throws FileNotFoundException {
        File file = new File(originalFile);
        String split = (file.getParent()).concat("/"+Resources.SPLIT+"/");
        new File(split).mkdir();
        this.path = (file.getParent()).concat("/"+Resources.SPLIT+"/").concat(file.getName().substring(0,file.getName().lastIndexOf(".")));
        this.numberOfFiles = (int)Math.round((new File(originalFile).length()) / (size*1024*1024)) ;
        this.originalFile = new FileInputStream(originalFile);
        this.originalPath = Paths.get(originalFile);
    }

    @Override
    public void split() {
        try {
            if (numberOfFiles>0){
                long linesPerFile = Files.lines(originalPath).count()/numberOfFiles;
                Dataset dataset = RDFDataMgr.loadDataset(originalPath.toString()) ;
//                dataset.addNamedModel(dataset.getDefaultModel().getGraph().toString(),dataset.getDefaultModel());
//                dataset.addNamedModel(dataset.getUnionModel().getGraph().toString(),dataset.getUnionModel());
                Iterator<String> list = dataset.listNames();
                int i=0;
                int j=0;
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path+"_part_"+i+".trig"), "UTF-8");
                while(list.hasNext()){
                    long lines = Files.lines(Paths.get(path+"_part_"+i+".trig")).count();
                    if(lines>linesPerFile){
                        writer.close();
                        i++;
                        writer = new OutputStreamWriter(new FileOutputStream(path+"_part_"+i+".trig"), "UTF-8");
                    }
                    String graph = list.next();
                    Model model = dataset.getNamedModel(graph);
                    writer.append("<"+graph+"> {\n");
                    model.write(writer, "TRIG");
                    writer.append("}\n");
                }
                writer.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(CSVSplitter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
