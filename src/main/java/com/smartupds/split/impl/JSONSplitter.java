/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartupds.split.impl;

import com.smartupds.split.api.Splitter;
import com.smartupds.split.common.Resources;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
/** Java class to split JSON Files
 *
 * @author Manolis Fragiadoulakis <fragiadoulakis at smartupds.com>
 */
public class JSONSplitter implements Splitter{
    private final int numberOfFiles;
    private final InputStream originalFile;
    private final String path;
    private final Path originalPath;
    
    public JSONSplitter (String originalFile, double size) throws FileNotFoundException{
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
            if(numberOfFiles>0){
                JSONParser parserJson = new JSONParser();
                int i=0;
                Object json = parserJson.parse(new InputStreamReader(originalFile, "UTF8"));
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(path+"_part_"+i+".json"));
                if(json instanceof JSONArray){
                    JSONArray array = new JSONArray();
                    JSONArray jsonArray = (JSONArray) json;
                    long objsPerFile = jsonArray.size()/numberOfFiles;
                    Iterator iterator = jsonArray.iterator();
                    while(iterator.hasNext()){
                        if(array.size()>objsPerFile){
                            writer.write(array.toJSONString());
                            writer.close();
                            i++;
                            writer = Files.newBufferedWriter(Paths.get(path+"_part_"+i+".json"));
                            array = new JSONArray();
                        }
                        JSONObject obj = (JSONObject) iterator.next();
                        array.add(obj);
                    }
                    writer.write(array.toJSONString());
                }
                writer.close();
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(JSONSplitter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(JSONSplitter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
