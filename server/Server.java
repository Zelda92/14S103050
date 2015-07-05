package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.*;

public class Server {

	/**
	 * @param args
	 */
	public static ArrayList<ClientStat> clients=new ArrayList<ClientStat>();
	static String ip;
	static int port=0;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(0);
			 port= serverSocket.getLocalPort();
			 InetAddress addr = InetAddress.getLocalHost();
			 ip=addr.getHostAddress().toString();//获得本机IP
			 System.out.println(ip+":"+port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} //读取空闲的可用端口
		Thread t1=new Thread(task1);
		t1.start();
		Thread t2=new Thread(task2);
		t2.start();

	}
	public static Runnable task1=new Runnable()//实时文字接收
    {
    public void run()
      {
      	
      	Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
      //System.out.println(" task1 running...");
      System.out.print(".");
      DatagramPacket dp = null;
      DatagramSocket ds = null;
      byte[] buf = new byte[1024];
      try
        {
        dp = new DatagramPacket(buf,buf.length);
        ds = new DatagramSocket(port);
        }
      catch (Exception e)
          {}
          
      while (true)
        {
        try
          {
          ds.receive(dp);
          int length = dp.getLength();
        	  InetAddress address = dp.getAddress();
              //int port1 = dp.getPort();
              String message = new String(dp.getData(),0,length);
              System.out.println(message);
              String[] s=message.split(";");
              if(s.length==4&&s[0].equals("1"))
              {
            	  DatagramPacket dp2;
            	  DatagramSocket ds2;
            	  String newout="ok";
     			  byte[] buf2 = newout.trim().getBytes();
     	            //InetAddress iaddress = InetAddress.getByName(address);
     			 dp2 = new DatagramPacket(buf2,buf2.length,address,Integer.parseInt(s[2]));
     			ds2 = new DatagramSocket();
     			System.out.println("Sending ok...");
     			for(int i=0;i<10;i++)
     				ds2.send(dp2);
     				newout=getClientList();
     				dp2 = new DatagramPacket(buf2,buf2.length,address,Integer.parseInt(s[2]));
         			ds2 = new DatagramSocket();
         			ds2.close();
     				for(int k=0;k<clients.size();k++)
     				{
     					ClientStat c=clients.get(k);
     					if(c.name.equals(s[3]))
     					{
     						if(c.onoff==false)
     						{
     							clients.remove(k);
     						}
     					}
     				}
     				ClientStat newC=new ClientStat();
     				newC.ip=s[1];
     				newC.port=Integer.parseInt(s[2]);
     				newC.name=s[3];
     				newC.onoff=true;
     				Date now=new Date();
     				newC.time=now.getTime();
     				clients.add(newC);
     				
     				Thread t3=new Thread(task3);
     				t3.start();
     	            
     	            
              }
              else if(s[0].equals("2"))
              {
            	  for(int i=0;i<clients.size();i++)
            	  {
            		  if(clients.get(i).name.equals(s[1])&&clients.get(i).onoff==true)
            		  {
            			  Date now=new Date();
            			  clients.get(i).time=now.getTime();
            			  break;
            		  }
            	  }
              }
              else if(s[0].equals("3"))
              {
            	  for(int i=0;i<clients.size();i++)
            	  {
            		  if(clients.get(i).name.equals(s[1])&&clients.get(i).onoff==true)
            		  {
            			  String newout="off";
             			  byte[] buf2 = newout.trim().getBytes();
             	            //InetAddress iaddress = InetAddress.getByName(address);
             			 DatagramPacket dp2 = new DatagramPacket(buf2,buf2.length,InetAddress.getByName(clients.get(i).ip),clients.get(i).port);
             			 //System.out.println(Integer.parseInt(s[2]));
             			DatagramSocket ds2 = new DatagramSocket();
             			System.out.println("Sending off...");
             			for(int j=0;j<10;j++)
             				ds2.send(dp2);
            			  clients.get(i).onoff=false;
            			  Thread t3=new Thread(task3);
           				t3.start();
            			  break;
            		  }
            	  }
              }
          
          }
        catch (Exception e)
          {
          //e.printStackTrace();
          System.exit(0);
          }
        }
        
      }
    };
    public static Runnable task2=new Runnable()//每两秒轮询下线情况
    {
    public void run()
      {
      	
      	Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
      	while(true)
      	{
      		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
      		for(int i=0;i<clients.size();i++)
            {
            	
            	if(clients.get(i).onoff==true)
            	{
            		Date now=new Date();
            		if(now.getTime()-clients.get(i).time>4000)
                	{
                		System.out.println(clients.get(i).port+",off,time off");
                		//clients.remove(i);
                		clients.get(i).onoff=false;
                		Thread t3=new Thread(task3);
           				t3.start();
                	}
            	}
            	
            }
      	}
        
        
        
      }
    };

    public static Runnable task3=new Runnable()//更新时发送用户列表给所有用户
    {
    public void run()
      {
    	Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    	System.out.println("Thread 3 running,Sending list...");
    	String list=getClientList();
      		for(int i=0;i<clients.size();i++)
            {
            	
            	if(clients.get(i).onoff==true)
            	{
       			  byte[] buf2 = list.trim().getBytes();
       			 DatagramPacket dp2;
				try {
					dp2 = new DatagramPacket(buf2,buf2.length,InetAddress.getByName(clients.get(i).ip),clients.get(i).port);
					DatagramSocket ds2 = new DatagramSocket();
	       			
	       		    ds2.send(dp2);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
       			 //System.out.println(Integer.parseInt(s[2]));
       			
            	}
            	
            
      	}
      }
    };
    
    public static String getClientList()
    {
    	StringBuilder list=new StringBuilder();
    	list.append("4");
    	for(int i=0;i<clients.size();i++)
    	{
    		list.append(";"+clients.get(i).ip);
    		list.append(";"+clients.get(i).port);
    		list.append(";"+clients.get(i).name);
    		list.append(";"+clients.get(i).onoff);
    	}
    	//System.out.println(list.toString());
    	return list.toString();
    	
    }
      
}
