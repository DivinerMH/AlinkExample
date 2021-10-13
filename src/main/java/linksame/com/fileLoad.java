package linksame.com;

import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @Author: menghuan
 * @Date: 2021/9/6 10:40
 */
public class fileLoad {

    @Test
    public void LoadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("G:\\Idea-Workspaces\\AlinkExample\\src\\main\\resources\\a.properties"));
        // properties.load(new FileInputStream("G:/Idea-Workspaces/AlinkExample/src/main/resources/a.properties"));
        String result = properties.getProperty("name");
        System.out.println(result);
    }

    @Test
    public void LoadResourceBundle(){
        // 读取properties类型文件
        ResourceBundle bundle = ResourceBundle.getBundle("a");
        String result = bundle.getString("name");
        System.out.println(result);
    }

    @Test
    public void fileStreamLoad(){
        // 读取txt文件
        InputStream inputStream = this.getClass().getResourceAsStream("/static/train.txt");
        // 读取以.properties为后缀的文件
        // InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("a.properties");
        BufferedReader reader = null;
        List<String> list = new ArrayList<String>();
        try{
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String str = null;
            // 按行读取文件内容
            while((str = reader.readLine()) != null){
                list.add(str);
                System.out.println(str);
            }
            // System.out.println(list.toString());
        }catch ( Exception e){
            e.printStackTrace();
        }
    }


    /**
     * https://blog.csdn.net/chengqiuming/article/details/101221716
     */
    @Test
    public void LoadXml(){
        // 默认从文件系统的当前路径加载b.xml资源
        Resource result = new FileSystemResource("b.xml");
        // 获取该资源的简单信息
        System.out.println(result.getFilename());
        System.out.println(result.getDescription());
    }


    @Test
    public void jiSuan(){
        // int []i ={12,32,5,45,4};
        // int []i ={3,54,2,33,5};
        // int []i ={43,23,7,42,5};
        // int []i ={7,8,3,41,3};
        // int []i ={11,24,11,23,6};
        int []i ={31,17,21,50,23};

        int A = i[0];
        int B = i[1];
        int C = i[2];
        int D = i[3];
        int E = i[4];

        int result = (( 7 * A + 2 * B ) * 3 * C) + 2 * D - ( E * E * E);
        System.out.println(result);

    }





}
