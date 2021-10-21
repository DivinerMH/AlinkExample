package linksame.com.utils;

import org.junit.Test;

/**
 * @Author: menghuan
 * @Date: 2021/10/20 17:19
 */
public class ResultHandler {

    public static String AIPredictResultHandler(String var){
        String substring = var.substring(var.indexOf("[") + 1, var.lastIndexOf("]"));
        System.out.println(substring);
        return substring;
    }

    @Test
    public void test(){
        String varR = "+I[26.004128914434695]";
        AIPredictResultHandler(varR);
    }
}
