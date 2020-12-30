/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newfivefix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import static newfivefix.five_fix_Constants.MODE_POWEREXIT;

/**
 * 程序入口
 * @author 张永伟
 */
public class Newfivefix extends Application {
    ServerSocket serverSocket;
    static HashMap<String, Socket> socketToClientMap = new HashMap<>();
    static Stage stageNotification, stageBuild, stageJoin, stageGame, stageRestart;
    static Label lblNotification = new Label("");

    
    Button btnNotificateSure = new Button("朕知道了");
    static ListPane listServer = new ListPane(false);
    static ListPane listClient = new ListPane(true);
    static int usePort = 8989;
    Text textPortLog = new Text("服务器占用端口为:" + usePort + "\n");
    boolean isServerOn = true;
    boolean isSearchOn = true;
    static BorderPane gamePane = new BorderPane();
    static BoardPane boardPane = new BoardPane();
    static TopGamePane topPane = new TopGamePane();
    static BottomGamePane bottomPane = new BottomGamePane();
    static Text txtWinMsg = new Text("");
    static Thread serverThread, searchThread;
    static String selfIp_str = "";
    private static ArrayList<String> selfIp_list = new ArrayList<>();
    static boolean isStartAGame = false;
    
    @Override
    public void start(Stage stageChoose) {
        getSelfIpS();
        initStageNotification();
        initStageBuild();
        initStageJoin();
        initStageGame();
        initStageRestart();
        stageJoin.setOnCloseRequest(e -> {
            stageJoin.close();
            isSearchOn = false;
            searchThread = null;
            stageChoose.show();
        });
        stageBuild.setOnCloseRequest(e -> {
            stageBuild.close();
            isServerOn = false;
            serverThread = null;
            try {
                if(serverSocket!=null)
                    serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(Newfivefix.class.getName()).log(Level.SEVERE, null, ex);
            }
            stageChoose.show();
        });        
        stageGame.setOnCloseRequest(e -> {
           // System.exit(1);
           Platform.setImplicitExit(false);
           stageGame.close();
           txtWinMsg.setText("直接退出游戏对方就赢了，真的要退出吗？");
           stageRestart.show();
           FiveFixGame.selectionMode = MODE_POWEREXIT;
        });
        stageChoose.setOnCloseRequest(e ->{
            //保存成绩
            topPane.saveGrade();
            System.exit(0);
        });
        Button btnBuild = new Button("建立服务器");
        Button btnJoin = new Button("加入服务器");               
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(btnBuild, btnJoin);
        StackPane root = new StackPane();
        root.getChildren().add(vbox);        
        Scene scene = new Scene(root, 300, 250);        
        stageChoose.setTitle("五子棋");
        stageChoose.getIcons().add(new Image(getClass().getResourceAsStream("logo")));
        stageChoose.setScene(scene);
        stageChoose.show();
        
        btnBuild.setOnAction((ActionEvent event) -> {
            //这里写建立服务器代码
           stageChoose.close();
           stageBuild.show();
           isServerOn = true;
            startServer(usePort);
        });
        btnJoin.setOnAction(e -> {
            //这里写加入服务器代码
            stageChoose.close();
            stageJoin.show();
            listServer.requestFocus();
            isSearchOn = true;            
            startSearch(usePort);
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }    

    private void initStageNotification() {
        stageNotification = new Stage();
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(lblNotification, btnNotificateSure);
        vbox.setAlignment(Pos.CENTER);
        StackPane root = new StackPane(vbox);
        Scene scene1 = new Scene(root, 250, 250);
        stageNotification.setTitle("通知");
        stageNotification.getIcons().add(new Image(getClass().getResourceAsStream("logo")));
        stageNotification.setScene(scene1);
        btnNotificateSure.setOnAction(e -> {
            stageNotification.close();
        });
    }

    private void initStageBuild() {
        stageBuild = new Stage();
        VBox vbox = new VBox(10);     
        ProgressBar progessBar = new ProgressBar();
        progessBar.setPrefWidth(100);
        Text text = new Text(selfIp_str);
        text.setTextAlignment(TextAlignment.CENTER);
        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(new Text("等待其他人加入"), progessBar);
        vbox.getChildren().addAll(hbox, listClient);
        vbox.setAlignment(Pos.CENTER);
        BorderPane root = new BorderPane();
        root.setBottom(text);        
        root.setTop(textPortLog);
        BorderPane.setAlignment(text, Pos.CENTER);
        BorderPane.setAlignment(textPortLog, Pos.CENTER);
        root.setCenter(vbox);
        Scene scene1 = new Scene(root, 400, 400);
        stageBuild.setTitle("建立服务器");
        stageBuild.getIcons().add(new Image(getClass().getResourceAsStream("logo")));
        stageBuild.setScene(scene1);
    }

    private void initStageJoin() {
        stageJoin = new Stage();
        VBox vbox = new VBox(10);
        HBox hbox = new HBox(5);
        ProgressBar progessBar = new ProgressBar();
        progessBar.setPrefWidth(100);
        hbox.getChildren().addAll(new Text("正在查找服务器"), progessBar);
        
        vbox.getChildren().addAll(hbox, listServer);
        listServer.setAlignment(Pos.CENTER);
       // listServer.addItem("192.168.98.198");//释例，后续注释掉
      //  listServer.addItem("192.168.98.1");//释例，后续注释掉
        hbox.setAlignment(Pos.CENTER);
        vbox.setAlignment(Pos.CENTER);
        StackPane root = new StackPane(vbox);
        Scene scene1 = new Scene(root, 300, 300);
        stageJoin.setTitle("查找服务器");
        stageJoin.getIcons().add(new Image(getClass().getResourceAsStream("logo")));
        stageJoin.setScene(scene1);
        
    }
    private void initStageGame(){
        stageGame = new Stage();        
        gamePane.setCenter(boardPane);
        gamePane.setTop(topPane);
        gamePane.setBottom(bottomPane);
        gamePane.setBackground(new Background(new BackgroundFill(new Color(0, 1, 0, 0.2), null, null)));
        BorderPane.setAlignment(boardPane, Pos.CENTER);
        Scene scene1 = new Scene(gamePane, 800, 800);
        stageGame.setTitle("五子棋");
        stageGame.getIcons().add(new Image(getClass().getResourceAsStream("logo")));
        stageGame.setScene(scene1);
        
    }
    private void initStageRestart(){
        stageRestart = new Stage();      
        VBox vbox = new VBox(5);
        Button btnRestartGame = new Button("是的");
        Button btnExitGame = new Button("不了");
        HBox hbox = new HBox(20);        
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(btnExitGame, btnRestartGame);
        vbox.getChildren().addAll(txtWinMsg, hbox);
        vbox.setAlignment(Pos.CENTER);
        StackPane root = new StackPane(vbox);       
        Scene scene1 = new Scene(root, 300, 250);
        stageRestart.setTitle("五子棋");
        stageRestart.getIcons().add(new Image(getClass().getResourceAsStream("logo")));
        stageRestart.setScene(scene1);
        stageRestart.setOnCloseRequest(e -> {
           // System.exit(1);
            FiveFixGame.UserSelection='N';
            stageRestart.close();          
        });
        btnRestartGame.setOnAction(e -> {
            //重新开始游戏
            FiveFixGame.UserSelection='Y';
            stageRestart.close();
        });
        btnExitGame.setOnAction(e -> {
            //退出游戏
            // System.exit(1);
            FiveFixGame.UserSelection='N';
            stageRestart.close();            
        });
    }
    private static void getSelfIpS(){
        try {           
            Enumeration<NetworkInterface>networkInterfaces;
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddress;
            InetAddress inetAddres;
            while(networkInterfaces.hasMoreElements()){
                networkInterface = networkInterfaces.nextElement();                
                inetAddress = networkInterface.getInetAddresses();
                while(inetAddress.hasMoreElements()){
                    inetAddres = inetAddress.nextElement();
                    if(inetAddress != null && inetAddres instanceof Inet4Address){     
                        if(!selfIp_str .equals(""))
                            selfIp_str += "/\n";
                        if(networkInterface.getDisplayName().contains("Loopback"))
                            selfIp_str +=  inetAddres.getHostAddress() + "(环回)";   
                        else if(networkInterface.getDisplayName().contains("wl"))
                            selfIp_str +=  inetAddres.getHostAddress() + "(无线)";
                        else if(networkInterface.getDisplayName().contains("PCIe"))
                            selfIp_str += inetAddres.getHostAddress() + "(有线)";
                        else if(networkInterface.getDisplayName().contains("virtual") || networkInterface.getDisplayName().contains("Virtual"))
                            selfIp_str += inetAddres.getHostAddress() + "(虚拟)";
                        else
                            selfIp_str += inetAddres.getHostAddress() + "(其他)";     
                        
                        if(!networkInterface.getDisplayName().contains("Loopback") && !inetAddres.getHostAddress().trim().equals("127.0.0.1"))
                            selfIp_list.add(inetAddres.getHostAddress());
                    }
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(Newfivefix.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    static void startInputConnect(String ip){
        if(isValidIpV4(ip) && checkIP(ip))
           startConnect(ip);
        else if(!isValidIpV4(ip)){
            lblNotification.setText("输入的IP不合法，请按照示例输入");
            stageNotification.show();
            listServer.refresh();
        }else{
            lblNotification.setText("输入的IP不在网络内，请重新输入");
            stageNotification.show();
            listServer.refresh();
        }            
    }
    static void startConnect(String ip) {
        //开始连接服务器，服务器地址为ip
        new Thread(() -> {
            try{ 
                Socket socket = new Socket(ip, usePort);
                DataInputStream fromServer = new DataInputStream(socket.getInputStream());
                DataOutputStream toServer = new DataOutputStream(socket.getOutputStream()); 
                toServer.writeUTF("connect"); //向服务器发送连接请求
                Platform.runLater(() -> {
                    lblNotification.setText("服务器连接成功，\n等待服务器开始游戏");
                    stageNotification.show();
                });
                String command = fromServer.readUTF();//等待服务器发来开始游戏命令
               // System.out.println("客户端接受到的指令:" + command);
                if(command.equals("startGame") && !isStartAGame){//还未开始一场游戏，并且服务器发来了开始游戏命令
                    isStartAGame = true;
                    toServer.writeUTF("sure");
                    Platform.runLater(() -> {
                        stageJoin.close();
                        listServer.refresh();
                        stageGame.show();
                        topPane.setIP(ip);
                    });
                     new Thread(new FiveFixGame(socket, topPane, boardPane, bottomPane,txtWinMsg, false)).start(); 
                    //客户端开始游戏线程
                    Platform.runLater(() -> listServer.refresh());//退出来以后还可以重新连接
               //     System.out.println("客户端开始游戏线程");
                }else if(command.equals("startGame") && isStartAGame){
                    toServer.writeUTF("no");
               //     System.out.println("客户端已经和其他服务器开始了游戏线程");
                }else if(command.equals("end")){
                    //和其他客户端开始了游戏线程
                    Platform.runLater(() -> {
                        listServer.deleteItem(ip);
                        listServer.refresh();
                        lblNotification.setText("服务器"+ip+" 和其他客户端\n开始了游戏，请连接其他服务器！");
                        stageNotification.show();
                    });
                    socket.close();
               //     System.out.println("客户端结束游戏进程");
                }
            } catch (IOException ex) {
             //   System.out.println("服务器连接失败" + ex.getLocalizedMessage());//以后换通知窗口
                Platform.runLater(() -> {
                    lblNotification.setText("服务器连接失败" + ex.getLocalizedMessage());
                    stageNotification.show();
                    listServer.deleteItem(ip);
                    listServer.refresh();
                });         
            }
         }).start();
    }
    void startServer(int port){
        //开启服务器，端口为port
        if(serverThread == null){
            serverThread = new Thread(() -> {
                try{
                    serverSocket = new ServerSocket(port);
                    Platform.runLater(() -> textPortLog.setText("服务器已于端口" + port + "启动\n"));                
                    while(isServerOn){                    
                        Socket socket = serverSocket.accept();
                        //这里写有人加入服务器的代码
                   //     System.out.println("有客户端连接");
                        DataInputStream fromClient = new DataInputStream(socket.getInputStream());
                        String command = fromClient.readUTF();
                   //     System.out.println("接受命令command=" + command);
                        if(command.equals("connect")){//客户端发来了连接请求
                            String clientIP = socket.getInetAddress().getHostAddress();
                            Platform.runLater(() -> {
                                listClient.addItem(clientIP);
                                lblNotification.setText("客户端"+ clientIP + "已连接，\n可以开始游戏");
                                stageNotification.show();
                            });
                            socketToClientMap.put(clientIP, socket);
                        }else if(command.equals("search")){ //搜索
                            socket.close();
                        }
                    }
                } catch (IOException ex) {
                  //  System.out.println("用户关闭了服务器" + ex.getLocalizedMessage());
                }
           });
        serverThread.start();
        }
    }       
    void startSearch(int port){//开启一个线程查找服务器
        //开启服务器，端口为port
        if(searchThread == null){
            searchThread = new Thread(() -> {
                while(isSearchOn){
                    //检查自身电脑上有没有运行服务器
                    if(checkIPPort("127.0.0.1", usePort))//自己电脑上有服务器
                        Platform.runLater(() -> {
                            if(!listServer.contains("127.0.0.1"))
                                listServer.addItem("127.0.0.1");
                        });
                    
                    for(String ip: selfIp_list){
                        String pre = ip.substring(0, ip.lastIndexOf(".") + 1);
                       // System.out.println("查找ip段" + pre);                        
                        for(int i = 0; i<= 254; i++){
                            int end = i;
                            if(!ip.equals(pre + end))//不是本机IP
                                new Thread(() -> {
                                    if(checkIP(pre + end) && checkIPPort(pre + end, usePort)){
                                        Platform.runLater(() ->{
                                            if(!listServer.contains(pre + end))
                                                listServer.addItem(pre + end);
                                        });
                                    }
                                }).start();
                        }
                    }
                    try{
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                         Logger.getLogger(Newfivefix.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }                
           });
           searchThread.start();
        }
    }
    static void startGame(String ip) {
        //服务器端开始游戏
        new Thread(() -> {
            //得到socket
            Socket socket = socketToClientMap.remove(ip); //获得ip并且从图里删除
            Platform.runLater(() -> listClient.deleteItem(ip) );//从列表里也删了，下次要开始需要重新连接
            try{
                DataInputStream fromClient = new DataInputStream(socket.getInputStream());
                DataOutputStream toClient = new DataOutputStream(socket.getOutputStream()); 
                toClient.writeUTF("startGame");
                String command = fromClient.readUTF();
                if(command.equals("sure")){
                    //向其他客户端发送拒绝加入，并且关闭所有套接字
                    Set<Entry<String, Socket>> entrySet = socketToClientMap.entrySet();
                    for(Entry entry: entrySet){
                        Platform.runLater(() -> listClient.deleteItem((String) entry.getKey()));
                        Socket socketTemp = (Socket)entry.getValue() ;                        
                        new DataOutputStream( socketTemp.getOutputStream() ).writeUTF("end");
                        socketTemp.close();
                    }
                    socketToClientMap.clear();//将图清空
                    //开启游戏线程                
                    Platform.runLater(() -> {
                        stageBuild.close();
                        stageGame.show();
                        topPane.setIP(ip);
                    });
                    new Thread(new FiveFixGame(socket,topPane, boardPane,  bottomPane,txtWinMsg, true)).start();
                  //  System.out.println("服务器开始了游戏");
                }else if(command.equals("no")){
                    Platform.runLater(() -> {
                        listClient.deleteItem(ip);
                        lblNotification.setText("客户端"+ ip + "已经\n和其他服务器开始了游戏，\n和其他客户端开始游戏吧！");
                        stageNotification.show();
                    });
                }                
            } catch (IOException ex) {
                Logger.getLogger(Newfivefix.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }
    static void setBackgroundColor(String color) {    
        if(color.equals("green")){//浅绿
            gamePane.setBackground(new Background(new BackgroundFill(new Color(0, 1, 0, 0.2), null, null)));
            boardPane.setBoardColor(new Color(0, 1, 0, 0.2));
        }else if(color.equals("pink")){//浅粉
            gamePane.setBackground(new Background(new BackgroundFill(new Color(1, 0.78, 0.79, 0.5), null, null)));
            boardPane.setBoardColor(new Color(1, 0.78, 0.79, 0.5));
        }else if(color.equals("wood")){//实木
            gamePane.setBackground(new Background(new BackgroundFill(new Color(0.9, 0.75, 0.36, 0.5), null, null)));
            boardPane.setBoardColor(new Color(0.9, 0.75, 0.36, 0.5));
        }else  if(color.equals("classic")){//经典
            gamePane.setBackground(new Background(new BackgroundFill(new Color(1, 0.9, 0.65, 0.5), null, null)));
            boardPane.setBoardColor(new Color(1, 0.9, 0.65, 0.5));
        }
            
    }
    
    boolean checkIPPort(String ip, int port){
        Socket socket = new Socket();
        try{
            socket.connect(new InetSocketAddress(ip, port), 3000);
            new DataOutputStream(socket.getOutputStream()).writeUTF("search"); //发出查找信号
            return true;
        } catch (IOException ex) {
            return false;
        }finally{
            if(socket !=null){
                try{
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(Newfivefix.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    static boolean checkIP(String ip){
        try{
            return InetAddress.getByName(ip).isReachable(500);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Newfivefix.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {            
            Logger.getLogger(Newfivefix.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return false;
    }
    static boolean isValidIpV4(String ip){
        if(ip.startsWith("[.]") || ip.endsWith("[.]"))
            return false;
        String temps[] = ip.split("[.]");
        if(temps.length!=4)
            return false;
        for(String temp:temps){
            for(int i = 0; i< temp.length(); i++)
                if(!isDigist(temp.charAt(i)))
                    return false;
                
            if(Integer.parseInt(temp) == 0|| Integer.parseInt(temp) >= 255)
                return false;
        }
        return true;
    }
    static boolean isDigist(char ch){
        if(ch >='0' && ch <='9')
            return true;
        return false;
    }
}
