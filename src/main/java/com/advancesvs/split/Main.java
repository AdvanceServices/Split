/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.advancesvs.split;

import com.advancesvs.split.impl.XMLSplitter;
import com.advancesvs.split.impl.TRIGSplitter;
import com.advancesvs.split.api.Splitter;
import com.advancesvs.split.common.Resources;
import com.advancesvs.split.impl.CSVSplitter;
import com.advancesvs.split.impl.JSONSplitter;
import com.advancesvs.split.impl.TTLSplitter;
import com.advancesvs.split.impl.RDFSplitter;
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
//        args = new String[] {   "-f","C:\\Users\\mafragias\\Documents\\WORKSPACE\\GitHub\\ETL-Controller\\delete_logs\\response_log.xml",
//                                "-s","6"
//                                ,"-e","results"
//        };
        CommandLine cli = PARSER.parse(options, args);
        File file = new File(cli.getOptionValue("file"));
        boolean isFolder = file.isDirectory();
        Splitter splitter = (Splitter) null;
        String element = null;
        if (cli.hasOption(Resources.ELEMENT))
            element = cli.getOptionValue(Resources.ELEMENT);
        if (isFolder){
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Splitting Multiple Files Started.");
            ArrayList<String> paths = listFilesForFolder(new File (file.getAbsolutePath()));
            int counter = 1 ;
            for ( String path : paths){
                double percent = (double) counter*100/paths.size();
                path = path.trim();
                try {
                    String type = path.substring(path.lastIndexOf(".")+1);
                    selectSplitter(splitter, type, path, cli , element).split();
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
            selectSplitter(splitter, type, file.getAbsolutePath(), cli , element).split();
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Splitting File Completed.");
        }
        
    }
    
    private static void createOptionsList(){
        Option fileOption = new Option("f", "file", true,"Input file");
        fileOption.setRequired(true);
        Option sizeOption = new Option("s", "size", true,"The file size in MB");
        sizeOption.setRequired(true);
        Option element = new Option("e", "element", true,"Element to split");
        
        options.addOption(fileOption)
                .addOption(sizeOption)
                .addOption(element);
    }
    
    private static ArrayList<String> listFilesForFolder(final File folder) {
        ArrayList<String> filePaths = new ArrayList<>(); 
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.getName().contains("desktop.ini")){
                //ignore
            } else if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                filePaths.add(folder.getAbsoluteFile() + "/" + fileEntry.getName());
                System.out.println(folder.getAbsoluteFile() + "/" + fileEntry.getName());
            }
        }
        return filePaths;
    }
    
    
    private static Splitter selectSplitter(Splitter splitter, String type, String path, CommandLine cli ,String element) throws FileNotFoundException{
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
            else if (type.equalsIgnoreCase(Resources.XML) && !cli.hasOption(Resources.ELEMENT))
                splitter = new XMLSplitter(path, Double.parseDouble(cli.getOptionValue("size")));
            else if (type.equalsIgnoreCase(Resources.XML) && cli.hasOption(Resources.ELEMENT))
                splitter = new XMLSplitter(path, Double.parseDouble(cli.getOptionValue("size")),element);
            else
                throw new UnsupportedOperationException("File type not supported yet.");
        return splitter;
    }
}
