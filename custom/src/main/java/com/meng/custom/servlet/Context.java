package com.meng.custom.servlet;

import com.meng.custom.annotion.Controller;
import com.meng.custom.annotion.Qualifier;
import com.meng.custom.annotion.RequestMapping;
import com.meng.custom.annotion.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {

    //扫描指定包集合存储所有类
    private List<String> packageClassList = new ArrayList<>();

    //存储扫描的controller service注解类
    private Map<String, Object> instanceList = new HashMap<>();
    //存储url 与controller methods 的映射关系
    private Map<String, Method> urlMathodMap = new HashMap<>();


    public void init() {
        try {
            //扫描包获取注解的类
            scanPackage("com.meng.custom.controller","com.meng.custom.service");
            //加载实例化类
            initBeanInstance();
            //实例化加载类中需要Qualifier注入的field
            initIOCField();
            //初始化url method  map
            initUrlMethodHanderMap();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initUrlMethodHanderMap() throws ClassNotFoundException {
        if(CollectionUtils.isEmpty(instanceList)){
            return;
        }
        //初始化url controller map映射
        for(String className : packageClassList) {
            Class cClass = Class.forName(className.replace(".class", ""));
            if(cClass.isAnnotationPresent(Controller.class)){
                //controller map
                String controllerUrlMap = ((RequestMapping) cClass.getAnnotation(RequestMapping.class)).value();
                Method[] methods = cClass.getMethods();
                for(Method method : methods){
                    if(method.isAnnotationPresent(RequestMapping.class)){
                        //method  map
                        String methodUrlMap = ((RequestMapping) (method.getAnnotation(RequestMapping.class))).value();
                        urlMathodMap.put(controllerUrlMap + methodUrlMap, method);
                    }
                }
            }
        }
    }

    private void initIOCField() throws IllegalAccessException {
        if(CollectionUtils.isEmpty(instanceList)){
            return;
        }
        for(Map.Entry<String, Object> entry: instanceList.entrySet()){
            //获取bean实例对应的fields
            Field[] beanFields = entry.getValue().getClass().getDeclaredFields();
            for(Field field : beanFields){
                if(field.isAnnotationPresent(Qualifier.class)){
                    //实例化·filed
                    String qualifirtzValue = field.getAnnotation(Qualifier.class).value();
                    field.setAccessible(true);
                    field.set(entry.getValue(), instanceList.get(qualifirtzValue));
                }
            }
        }
    }

    private void initBeanInstance() {
        if(CollectionUtils.isEmpty(packageClassList)){
            return;
        }

        try {
            for(String className : packageClassList){
                Class cClass = Class.forName(className.replace(".class",""));
                String  annotionValue  = null;
                boolean isAnnotion = false;
                if(cClass.isAnnotationPresent(Controller.class)){
                    annotionValue=( (Controller) cClass.getAnnotation(Controller.class)).value();
                    isAnnotion = true;
                } else if (cClass.isAnnotationPresent(Service.class)){
                    annotionValue=((Service) cClass.getAnnotation(Service.class)).value();
                    isAnnotion = true;
                }
                if(isAnnotion){
                    if(annotionValue == null){
                        throw new RuntimeException("annotionValue must exists!");
                    } else {
                        instanceList.put(annotionValue, cClass.newInstance());
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private void scanPackage(String... packages) {
        for(String packageItem : packages){
            //获取文件夹类路径
            URL url = this.getClass().getClassLoader()
                    .getResource("/" + packageItem.replaceAll("\\.","/"));
            //读取该路径下的所有文件
            String pathFile = url.getFile();
            File file = new File(pathFile);
            String[] files = file.list();
            for(String itemClassPath : files){
                File itemFile = new File(pathFile + "/" + itemClassPath);
                if(itemFile.isDirectory()){
                    scanPackage(packageItem + "." + itemFile.getName());
                } else if(itemFile.isFile()){
                    packageClassList.add(packageItem + "." + itemFile.getName());
                    System.out.println("需要加载的包 ：" + packageItem + "." + itemFile.getName());
                }
            }

        }
    }

    public Method getRequestMethods(String path) {
        return urlMathodMap.get(path);
    }

    public Object getControllerInsance(String path) {
        return instanceList.get(path);
    }
}
