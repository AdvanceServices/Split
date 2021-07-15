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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mafragias
 */
public class CSVSplitter implements Splitter {

    private final String path;
    private final int numberOfFiles;
    private final FileInputStream originalFile;
    private final Path originalPath;

    public CSVSplitter(String originalFile, double size) throws FileNotFoundException {
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
        String type = originalPath.toString().substring(originalPath.toString().lastIndexOf("."));
        try {
            if (numberOfFiles>0){
                BufferedReader reader = new BufferedReader(new InputStreamReader(originalFile,"UTF-8"));
                long linesPerFile = Files.lines(originalPath).count()/numberOfFiles;
                String row = null;
                int i=0;
                int j=0;
                String firstLine = "";
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path+"_part_"+i+type), "UTF-8");
                while((row = reader.readLine())!=null){
                    if (j==0)
                        firstLine = row;
                    if (j%linesPerFile==0 && j>0){
                        Logger.getLogger(RDFSplitter.class.getName()).log(Level.INFO, "Exported file {0}_part_{1}{2}", new Object[]{path, i, type});                      
                        writer.close();
                        i++;
                        writer = new OutputStreamWriter(new FileOutputStream(path+"_part_"+i+type), "UTF-8");
                        writer.append(firstLine+"\n");
                    }
                    writer.append(row+"\n");
                    j++;
                }
                System.out.println("Exported file "+path+"_part_"+i+type);
                writer.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(CSVSplitter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
