/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartupds.split.impl;

import com.smartupds.split.api.Splitter;
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
public class TRIGSplitter implements Splitter {

    private final String path;
    private final int numberOfFiles;
    private final FileInputStream originalFile;
    private final Path originalPath;


    public TRIGSplitter(String originalFile, double size) throws FileNotFoundException {
        this.path = originalFile.substring(0,originalFile.lastIndexOf("."));
        this.numberOfFiles = (int)Math.round((new File(originalFile).length()) / (size*1024*1024)) ;
        this.originalFile = new FileInputStream(originalFile);
        this.originalPath = Paths.get(originalFile);
    }

    @Override
    public void split() {
        try {
            if (numberOfFiles>0){
                BufferedReader reader = new BufferedReader(new InputStreamReader(originalFile,"UTF-8"));
                long linesPerFile = Files.lines(originalPath).count()/numberOfFiles;
                String row = null;
                int i=0;
                int j=0;
                String firstLine = "";
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path+"_part_"+i+".trig"), "UTF-8");
                while((row = reader.readLine())!=null){
                    if (j%linesPerFile==0 && j>0){    
                        while((row = reader.readLine())!=null && row.startsWith("@")){
                            writer.append(row + "\n");
                            j++;
                        }
                        while((row = reader.readLine())!=null && !row.startsWith("@")){
                            writer.append(row + "\n");
                            j++;
                        }
                        Logger.getLogger(TRIGSplitter.class.getName()).log(Level.INFO, "Exported file {0}_part_{1}.trig", new Object[]{path, i});
                        writer.close();
                        i++;
                        writer = new OutputStreamWriter(new FileOutputStream(path+"_part_"+i+".trig"), "UTF-8");
                    }
                    writer.append(row+"\n");
                    j++;
                }
                System.out.println("Exported file "+path+"_part_"+i+".trig");
                writer.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(CSVSplitter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
