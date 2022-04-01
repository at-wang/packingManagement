package cn.itwang.packingmanagement.utils;

import java.util.Random;

public class RandomNumberUtils {
    public static String getRandomNumber(){
        Random random=new Random();
        String s="";
        for (int i=0;i<10;i++){
           s+=random.nextInt(10);
        }
        String randomNumber=s+"@";
        return randomNumber;
    }
}
