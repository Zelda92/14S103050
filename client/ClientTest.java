package client;
import java.io.*;
import java.net.*;
import java.util.*;
public class ClientTest {

	/**
	 * @param args
	 */
	public static ServerInfo SInfo=new ServerInfo();
	public static ClientInfo CInfo=new ClientInfo();
	public static Thread t2;
	public static int okflag=0;
	public static int tcpsendflag=0;
	public static int inputfileflag=0;
	public static ArrayList<ClientStat> Clist;
	public static String TCPTargetIP;
	public static int TCPTargetPort=0;
	public static int TCPServerPort=0;
	public static String filesendpath;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		//int serverPort;
        Scanner in=new Scanner(System.in);

        System.out.println("Input your name:");
        CInfo.name=in.nextLine();
        System.out.println("Input Server ip:");
        SInfo.ip=in.nextLine();
        System.out.println("Input Server port:");
        SInfo.port=Integer.parseInt(in.nextLine());
		ServerSocket serverSocket;
		DatagramPacket dp = null;
	      DatagramSocket ds = null;
		try {
			serverSocket = new ServerSocket(0);
			CInfo.port = serverSocket.getLocalPort();
			   System.out.println("系统分配的端口号 port="+CInfo.port);
			   InetAddress addr = InetAddress.getLocalHost();
			   CInfo.ip=addr.getHostAddress().toString();//获得本机IP
			 System.out.println(CInfo.ip);
			 String newout="1;"+CInfo.ip+";"+CInfo.port+";"+CInfo.name;
			 byte[] buf = newout.trim().getBytes();
	            InetAddress iaddress = InetAddress.getByName(SInfo.ip);
	            dp = new DatagramPacket(buf,buf.length,iaddress,SInfo.port);
	            ds = new DatagramSocket();
	            ds.send(dp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		Thread t1=new Thread(task1);
		t1.start();
		while(flag)
		{
			if(inputfileflag==0)
			{
				int choice=Integer.parseInt(in.nextLine());
				if(choice==3)//offline
				{
					try{
					String newout="3;"+CInfo.name;
					byte[] buf2 = newout.trim().getBytes();
					 DatagramPacket dp2 = new DatagramPacket(buf2,buf2.length,InetAddress.getByName(SInfo.ip),SInfo.port);
					DatagramSocket ds2 = new DatagramSocket();
				    ds2.send(dp2);
				    stopThreads();
				    
					}
					catch(Exception ee)
					{
						//ee.printStackTrace();
					}
					break;
				}
				else if(choice==4)//show list
				{
					for(ClientStat c : Clist)
					{
						System.out.println(c.ip+" "+c.port+" "+c.name+" "+c.onoff);
					}
				}
				else if(choice==5)//p2p
				{
					System.out.println("Input target name:");
					String target=in.nextLine();
					System.out.println("Input message:");
					String message=in.nextLine();
					for(int i=0;i<Clist.size();i++)
					{
						if(Clist.get(i).name.equals(target))
						{
							if(Clist.get(i).onoff)
							{
								try{
									String newout="5;"+CInfo.name+";"+message;
									byte[] buf2 = newout.trim().getBytes();
									 DatagramPacket dp2 = new DatagramPacket(buf2,buf2.length,InetAddress.getByName(Clist.get(i).ip),Clist.get(i).port);
									DatagramSocket ds2 = new DatagramSocket();
								    ds2.send(dp2);
								    
									}
									catch(Exception ee)
									{
										//ee.printStackTrace();
									}
							}
							else
							{
								System.out.println(target+" is offline.");
							}
						}
					}
					
				}
				else if(choice==6)//tcp send file
				{
					if(tcpsendflag==0)
					{
						//send tcp  request
						System.out.println("Input target name:");
						String target=in.nextLine();
						System.out.println("Input file directory(e.g: d:/a.txt):");
						filesendpath=in.nextLine();
						File f = new File(filesendpath); 
						if(!f.exists())
						{
							System.out.println("File not exists!");
							
						}
						else
						{
							for(int i=0;i<Clist.size();i++)
							{
								if(Clist.get(i).name.equals(target))
								{
									if(Clist.get(i).onoff)
									{
										try{
											String newout="6;"+CInfo.name+";"+f.getName()+";"+f.length();
											//System.out.println(f.length());
											byte[] buf2 = newout.trim().getBytes();
											 DatagramPacket dp2 = new DatagramPacket(buf2,buf2.length,InetAddress.getByName(Clist.get(i).ip),Clist.get(i).port);
											DatagramSocket ds2 = new DatagramSocket();
										    ds2.send(dp2);
										    
											}
											catch(Exception ee)
											{
												//ee.printStackTrace();
											}
										//break;
									}
									else
									{
										System.out.println(target+" is offline.");
									}
								}
							}
						}
						
					}
					else
					{
						System.out.println("Sending file,please wait.");
					}
				}
			}
			
			
		}
		   

	}
	public static boolean flag=true;
	public static void stopThreads(){
		flag = false;
	}
	public static Runnable task1=new Runnable()//Text receive Thread
    {
    public void run()
      {
      	
      	Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
      System.out.println(" task1 running...");
      //System.out.print(".");
      DatagramPacket dp = null;
      DatagramSocket ds = null;
      byte[] buf = new byte[1024];
      try
        {
        dp = new DatagramPacket(buf,buf.length);
        ds = new DatagramSocket(CInfo.port);
        }
      catch (Exception e)
          {}
          
      while (flag)
        {
        try
          {
          ds.receive(dp);
          int length = dp.getLength();
        	  //InetAddress address = dp.getAddress();
              //int port1 = dp.getPort();
              String message = new String(dp.getData(),0,length);
              //System.out.println(message);
              String[] S1=message.split(";");
              if(message.equals("ok")&&okflag==0)
              {
            	  okflag=1;
            	  System.out.println("Got ok!");
            	  CInfo.onoff=true;
            	  t2=new Thread(task2);
            		t2.start();
            		System.out.println("Thread Started!");
              }
              else if(message.equals("off"))
              {
            	  okflag=0;
            	  System.out.println("Recieved offline message...");
            	  break;
              }
              else if(S1[0].equals("4"))
              {
            	  if((S1.length-1)%4==0)
            	  {
            		  Clist=new ArrayList<ClientStat>();
            		  for(int i=1;i<S1.length;i+=4)
                	  {
                		  ClientStat c=new ClientStat();
                		  c.ip=S1[i];
                		  c.port=Integer.parseInt(S1[i+1]);
                		  c.name=S1[i+2];
                		  if(S1[i+3].equals("true"))
                		  c.onoff=true;
                		  else if(S1[i+3].equals("false"))
                			  c.onoff=false;
                		  Clist.add(c);
                		  
                	  }
            		  System.out.println("List refreshed.");
            	  }
            	  
              }
              else if(S1[0].equals("5"))
              {
            	  System.out.println(S1[1]+":"+S1[2]);
              }
              else if(S1[0].equals("6"))
              {
            	  //System.out.println(message);
            	  System.out.println(S1[1]+" is sending file ("+S1[2]+") to you.");
            	  //task3ip=address;
            	  task3name=S1[1];
            	  task3size=Long.parseLong(S1[3]);
            	  System.out.println(task3size);
            	  Thread t3=new Thread(task3);
            	  t3.start();
            	  
              }
              else if(S1[0].equals("7"))
              {
            	  //System.out.println(message);
            	  task4ip=S1[1];
            	  task4port=S1[2];
            	   Thread t4=new Thread(task4);
            	   t4.start();
              }
              
          
          }
        catch (Exception e)
          {
        	//e.printStackTrace();
          System.out.println("text receive wrong");
          }
        }
        System.out.println("Thread 1 stopped");
      }
    };
    
    
    
    public static Runnable task2=new Runnable()//Heart beat thread
    {
    public void run()
      {
      	
      	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

				System.out.println("Start sending heartbeat...");
      	while(flag)
      	{
      		try {
				Thread.sleep(3000);
				String newout="2;"+CInfo.name;
				  byte[] buf2 = newout.trim().getBytes();
				 DatagramPacket dp2 = new DatagramPacket(buf2,buf2.length,InetAddress.getByName(SInfo.ip),SInfo.port);
				DatagramSocket ds2 = new DatagramSocket();
					ds2.send(dp2);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
      		
      	}
      	System.out.println("Heartbeat stopped");
        
      }
    };
    public static String task4ip;
    public static String task4port;
    public static Runnable task4=new Runnable()//send file
    {
    	public void run()
    	{
    		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    		try{
    		Socket socket =new Socket (InetAddress.getByName(task4ip),Integer.parseInt(task4port));  
            BufferedInputStream bis =new BufferedInputStream(new FileInputStream(filesendpath));  
            OutputStream socketOut = socket.getOutputStream();  
              
            byte[] buff = new byte[1024];  
            int len ;  
            while ( (len = bis.read(buff))!=-1)  
            {  
                socketOut.write(buff);  
            }  
            System.out.println("finished");  
            socket.shutdownOutput();  
            BufferedReader socketInput =  
                    new BufferedReader( new InputStreamReader(socket.getInputStream()));  
            String s = socketInput.readLine();  
            System.out.println(s);  
            bis.close(); 
            socketOut.close();
            socket.close();  
    		}
    		catch(Exception ee)
    		{
    			ee.printStackTrace();
    		}
    	}
    };
    public static long task3size;
    public static String task3name;//The file source's UDP ip and port for task3.
    public static Runnable task3=new Runnable()//TCP get file thread
    {
    public void run()
      {
      	
      	Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
      	//inputfileflag=1;
      	
      	System.out.println("Please input 0 and press enter. Then input the directory you want to save the file.");
      	Scanner in=new Scanner(System.in);
      			String save=in.nextLine();
      			inputfileflag=0;
      			System.out.println("Set zero");
      			//TO DO: verify the path
      			
      			
      			//Open the server
      			try{
      			ServerSocket serverSocket = new ServerSocket(0); 
      			int port=serverSocket.getLocalPort();
      			
      		//Send message to let the file transfer starts!
    	    	  ClientStat c=getClientbyName(task3name);
    	    	  task3name="";
    	    	  if(c.equals(null)||c.onoff==false){
    	    		  System.out.println("File source not exists or offline.");
        	      serverSocket.close(); 
    	    		  return;
    	    	  }
				String newout="7;"+CInfo.ip+";"+port;
				  byte[] buf2 = newout.trim().getBytes();
				 DatagramPacket dp2 = new DatagramPacket(buf2,buf2.length,InetAddress.getByName(c.ip),c.port);
				DatagramSocket ds2 = new DatagramSocket();
					ds2.send(dp2);
					ds2.close();
					System.out.println("udpSent "+newout+" ,to "+c.ip+":"+c.port);
    	        
      			
      	        Socket socket = serverSocket.accept();  
      	          
      	        BufferedOutputStream fileOut =new BufferedOutputStream(new FileOutputStream(save));  
      	        
      	        
      	        
      	        InputStream inSocket =   
      	                socket.getInputStream();  
      	          
      	        byte[] buf = new byte[1024];  
      	        int len =0;  
      	        System.out.println("Starting to Transfer the file...");
      	        long lenn=task3size-(task3size/1024)*1024;
      	        System.out.println(lenn);
      	        while ( (len = inSocket.read(buf))!=-1)  
      	        {  
      	            //System.out.println("inside while"); 
      	        	task3size-=len;
      	        	if(task3size<0)
      	        	{
      	        		System.out.println(task3size+len);
      	        		//int ss=Integer.parseInt((task3size+1024)+"");
      	        		fileOut.write(buf, 0, Integer.parseInt(task3size+len+"")); 
      	        		break;
      	        		//String s=new String(buf);
      	        		//fileOut.write(s.trim().getBytes());
      	        	}
      	        	else
      	        	{
      	        		
      	        		fileOut.write(buf, 0, len); 
      	        	}
      	              
      	        }  
      	        String s = "File saved!";  
      	        System.out.println(s);  
      	        DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream());  
      	        socketOut.writeBytes(s);  
      	          
      	        fileOut.close();  
      	        socket.close();  
      	      serverSocket.close();  
      			}
      			catch(Exception e)
      			{
      				e.printStackTrace();
      			}
        
      }
    };
    public static ClientStat getClientbyName(String name)
    {
    	for(ClientStat c: Clist)
    	{
    		if(c.name.equals(name))
    		{
    			return c;
    		}
    	}
    	return null;
    }

}
