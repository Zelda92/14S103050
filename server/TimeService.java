package server;

import java.util.Date;


public class TimeService {

	public TimeService(){}

  public static long fromDateStringToLong() { //此方法计算时间毫秒
  Date date = new Date();
  return date.getTime();   //返回毫秒数
  } 

  
  
  public static void main(String[] args) { 
	  

  long startT=fromDateStringToLong(); //定义上机时间
  try {
	Thread.sleep(3000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  long endT=fromDateStringToLong();  //定义下机时间

  long ss=(endT-startT)/(1000); //共计秒数
  //int MM = (int)ss/60;   //共计分钟数
  //int hh=(int)ss/3600;  //共计小时数
  //int dd=(int)hh/24;   //共计天数

  //System.out.println("共"+dd+"天 准确时间是："+hh+" 小时 "+MM+" 分钟"+ss+" 秒 共计："+ss*1000+" 毫秒"); 
  
  } 

}