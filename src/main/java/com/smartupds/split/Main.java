/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartupds.split;

import com.smartupds.split.impl.XMLSplitter;
import com.smartupds.split.impl.TRIGSplitter;
import com.smartupds.split.api.Splitter;
import com.smartupds.split.common.Resources;
import com.smartupds.split.impl.CSVSplitter;
import com.smartupds.split.impl.JSONSplitter;
import com.smartupds.split.impl.TTLSplitter;
import com.smartupds.split.impl.RDFSplitter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * A Splitter that supports RDF, TSV, CSV, TTL, N3, TRIG, XML 
 * @author mafragias
 */
public class Main {
    static final CommandLineParser PARSER = new DefaultParser();
    static Options options = new Options();
    
    public static void main(String []args) throws Exception{
        createOptionsList();
//        args = new String[] {   "-f","C:\\Users\\mafragias\\Downloads\\aat_terms.n3",
//                                "-s","0.1"};
        CommandLine cli = PARSER.parse(options, args);
        File file = new File(cli.getOptionValue("file"));
        boolean isFolder = file.isDirectory();
        Splitter splitter = (Splitter) null;
        if (isFolder){
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Splitting Multiple Files Started.");
            ArrayList<String> paths = listFilesForFolder(new File (file.getAbsolutePath()));
            int counter = 1 ;
            for ( String path : paths){
                double percent = (double) counter*100/paths.size();
                path = path.trim();
                try {
                    String type = path.substring(path.lastIndexOf(".")+1);
                    if (type.equalsIgnoreCase(Resources.RDF))
                        splitter = new RDFSplitter(path, Double.parseDouble(cli.getOptionValue("size")));
                    else if (type.equalsIgnoreCase(Resources.CSV) || type.equalsIgnoreCase(Resources.TSV))
                        splitter = new CSVSplitter(path, Double.parseDouble(cli.getOptionValue("size")));
                    else if (type.equalsIgnoreCase(Resources.JSON))
                        splitter = new JSONSplitter(path, Double.parseDouble(cli.getOptionValue("size")));
                    else if (type.equalsIgnoreCase(Resources.TTL) || type.equalsIgnoreCase(Resources.N3) || type.equalsIgnoreCase(Resources.NT))
                        splitter = new TTLSplitter(path, Double.parseDouble(cli.getOptionValue("size")));
                    else if (type.equalsIgnoreCase(Resources.TRIG))
                        splitter = new TRIGSplitter(path, Double.parseDouble(cli.getOptionValue("size")));
                    else if (type.equalsIgnoreCase(Resources.XML))
                        splitter = new XMLSplitter(path, Double.parseDouble(cli.getOptionValue("size")));
                    else
                        throw new UnsupportedOperationException("File type not supported yet.");
                    splitter.split();
                    Logger.getLogger(Main.class.getName()).log(Level.INFO, "Process at {0} %", percent);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                counter ++;
            }
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Splitting Multiple Files Completed.");
        } else {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Splitting File Started.");
            String type = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")+1);
            if (type.equalsIgnoreCase(Resources.RDF))
                splitter = new RDFSplitter(file.getAbsolutePath(), Double.parseDouble(cli.getOptionValue("size")));
            else if (type.equalsIgnoreCase(Resources.CSV) || type.equalsIgnoreCase(Resources.TSV) )
                splitter = new CSVSplitter(file.getAbsolutePath(), Double.parseDouble(cli.getOptionValue("size")));
            else if (type.equalsIgnoreCase(Resources.JSON))
                splitter = new JSONSplitter(file.getAbsolutePath(), Double.parseDouble(cli.getOptionValue("size")));
            else if (type.equalsIgnoreCase(Resources.TTL) || type.equalsIgnoreCase(Resources.N3) || type.equalsIgnoreCase(Resources.NT) )
                splitter = new TTLSplitter(file.getAbsolutePath(), Double.parseDouble(cli.getOptionValue("size")));
            else if (type.equalsIgnoreCase(Resources.TRIG))
                splitter = new TRIGSplitter(file.getAbsolutePath(), Double.parseDouble(cli.getOptionValue("size")));
            else if (type.equalsIgnoreCase(Resources.XML))
                splitter = new XMLSplitter(file.getAbsolutePath(), Double.parseDouble(cli.getOptionValue("size")));
            else
                throw new UnsupportedOperationException("File type not supported yet.");
            splitter.split();
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Splitting File Completed.");
        }
        
    }
    
    private static void createOptionsList(){
        Option fileOption = new Option("f", "file", true,"Input file");
        fileOption.setRequired(true);
        
//        Option outputOption = new Option("o", "output", true,"Output Folder");

        Option sizeOption = new Option("s", "size", true,"The file size in MB");
        sizeOption.setRequired(true);
        
        options.addOption(fileOption)
//                .addOption(outputOption)
                .addOption(sizeOption);
    }
    
    private static ArrayList<String> listFilesForFolder(final File folder) {
        ArrayList<String> filePaths = new ArrayList<>(); 
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.getName().contains("desktop.ini")){
                //ignore
            } else if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                filePaths.add(folder.getAbsoluteFile() + "\\" + fileEntry.getName());
                System.out.println(folder.getAbsoluteFile() + "\\" + fileEntry.getName());
            }
        }
        return filePaths;
    }
}
