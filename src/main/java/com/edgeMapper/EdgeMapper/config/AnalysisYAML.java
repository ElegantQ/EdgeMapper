package com.edgeMapper.EdgeMapper.config;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * Created by huqiaoqian on 2020/9/23
 */
public class AnalysisYAML {
    /**
     * 传参解析
     * @param urlStr
     * @return
     */
    public Map<String,Object> getYamlData(String urlStr){
        URL url = AnalysisYAML.class.getClassLoader().getResource(urlStr);
        return analysisData(url);
    }

    /**
     * 默认解析
     * @return
     */
    public  Map<String,Object> getYamlData(){
        URL url = AnalysisYAML.class.getClassLoader().getResource("attribute.yaml");
        return analysisData(url);
    }

    /**
     * 获取URL 解析内容
     * @param url
     * @return
     */
    public Map<String,Object> analysisData(URL url){
        InputStream input = null;
        try {
            input = new FileInputStream(url.getFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Yaml yaml = new Yaml();
        Map<String,Object> map = (Map<String,Object>)yaml.load(input);
        return map;
    }
}
