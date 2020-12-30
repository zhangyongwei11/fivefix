/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newfivefix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.text.Text;
import static newfivefix.five_fix_Constants.CELL_NUM;
import static newfivefix.five_fix_Constants.CONTINUNE;
import static newfivefix.five_fix_Constants.ME_FAIL;
import static newfivefix.five_fix_Constants.MODE_NOMAL;
import static newfivefix.five_fix_Constants.MODE_POWEREXIT;
import static newfivefix.five_fix_Constants.MODE_RESTART;

/**
 *
 * @author 张永伟
 */
public class FiveFixGame implements Runnable{
    Socket socket;
    DataInputStream input;
    DataOutputStream output;
    BoardPane boardPane;
    TopGamePane topPane;
    BottomGamePane bottomPane;
    boolean myBlackTurn = true;
    static boolean endGame = false;    
    private int[][] cellInt = new int[CELL_NUM][CELL_NUM];
    static char UserSelection = ' ', otherSelection = ' ';
    static int selectionMode = MODE_NOMAL; //选择模式为正常下棋模式
    Text txtWinMsg;
    
    
    FiveFixGame(Socket socket,
            TopGamePane topPane, BoardPane boardPane, BottomGamePane bottomPane,
            Text txtWinMsg,
            boolean myBlackTurn){
        try {
            this.socket = socket;
            this.input = new DataInputStream(socket.getInputStream());
            this.output = new DataOutputStream(socket.getOutputStream());            
        } catch (IOException ex) {
            Logger.getLogger(FiveFixGame.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.topPane = topPane;
        this.boardPane = boardPane;
        this.bottomPane = bottomPane;
        this.txtWinMsg = txtWinMsg;
        this.myBlackTurn=myBlackTurn;
        endGame = false;    //重新开启了游戏
        //开启一个线程监听是否强行退出游戏,如果是,发送投降信息，并且退出线程
        new Thread(() -> {
            while(!endGame){ //看有没有结束游戏
                try {
                    Thread.sleep(1000);
                    if(UserSelection != ' ' && selectionMode == MODE_POWEREXIT){//这家伙逃跑了
                         endGame = true;
                        if(UserSelection == 'Y'){
                          //  System.out.println("我要投降");
                            
                            Platform.runLater(() -> { //添加失败信息,重置棋盘
                                topPane.addMyFialTimes();
                                try {                                    
                                    exit();
                                    output.writeInt(ME_FAIL); //发送投降信息
                                    colseSocket();
                                } catch (IOException ex) {
                                 //   System.out.println(ex.getLocalizedMessage());
                                }
                            });                           
                        }else if(UserSelection == 'N'){
                            endGame = false;
                            Platform.runLater(() -> Newfivefix.stageGame.show());
                          //  System.out.println("显示游戏窗口，不跑了");
                        }
                        UserSelection = ' ';
                       selectionMode = MODE_NOMAL;                      
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(FiveFixGame.class.getName()).log(Level.SEVERE, null, ex);
                }               
            }
        }).start();       
        
    }

    @Override
    public void run() {
        try{
            while(!endGame){//只要游戏没结束
                
                //设置棋盘上方我方和对方颜色
                if(myBlackTurn){
                    topPane.setAllColor('B');
                    boardPane.myToken = 'B';
                    boardPane.otherToken='W';
                }else{
                    topPane.setAllColor('W');
                    boardPane.myToken = 'W';
                    boardPane.otherToken='B';
                }
                topPane.play();
                
                 while(boardPane.continueToPlay){
                    if(myBlackTurn){ //我是黑棋
                        boardPane.myTurn=true;
                        topPane.setWhicMove(true);
                        Platform.runLater(() -> bottomPane.setMsg("该您落子了！"));
                        waitForPlayerAction();
                        sendMove();   
                        //判断是否赢了代码    
                        if(boardPane.isWon(cellInt) ){
                            boardPane.WINNER ='B';
                            boardPane.animation.play();
                            break;
                        }
                        if(boardPane.isFull()) break;
                        topPane.setWhicMove(false);
                        Platform.runLater(() -> bottomPane.setMsg("等待对方落子了！"));
                        receiveMove();    
                        if(endGame)break;
                        //判断是否赢了代码
                        if(boardPane.isWon(cellInt) ){
                            boardPane.WINNER ='W';
                            boardPane.animation.play();
                            break;
                        }
                        if(boardPane.isFull()) break;
                       
                    }else{           //白棋         
                        boardPane.myTurn=false;
                        topPane.setWhicMove(false);
                        Platform.runLater(() -> bottomPane.setMsg("等待对方落子！"));
                        receiveMove(); 
                        if(endGame)break;
                        //判断是否赢了代码
                         if(boardPane.isWon(cellInt) ){
                            boardPane.WINNER ='B';
                            boardPane.animation.play();
                            break;
                        }
                         if(boardPane.isFull()) break;
                        boardPane.myTurn=true;
                        topPane.setWhicMove(true);
                        Platform.runLater(() -> bottomPane.setMsg("该您落子了！"));
                        waitForPlayerAction();
                        sendMove();
                        //判断是否赢了代码
                         if(boardPane.isWon(cellInt) ){
                            boardPane.WINNER ='W';
                            boardPane.animation.play();
                            break;
                        }
                         if(boardPane.isFull()) break;
                    }
                }
                 
                 if(endGame && boardPane.WINNER!=' '){//对方跑了
                     Platform.runLater(() -> {
                        topPane.addMyWinTimes();
                         try {
                             exit();
                             colseSocket();
                         } catch (IOException ex) {
                             Logger.getLogger(FiveFixGame.class.getName()).log(Level.SEVERE, null, ex);
                         }                         
                        showOtherRun();
                     });                     
                     break;                 
                 }
                //添加是否重新开始游戏通知和上方区域对局信息
                if( (myBlackTurn && boardPane.WINNER =='B') ||
                        (!myBlackTurn && boardPane.WINNER=='W') ) //我赢了
                    Platform.runLater(() -> {                        
                        topPane.addMyWinTimes();
                        txtWinMsg.setText("您赢了,是否重新开始游戏？");
                        bottomPane.setMsg("游戏结束，您赢了！");
                        Newfivefix.stageRestart.show();
                        selectionMode=MODE_RESTART;
                    });
                else if((myBlackTurn && boardPane.WINNER =='W') ||
                        (!myBlackTurn && boardPane.WINNER=='B')) //对方赢了
                    Platform.runLater(() -> {
                        topPane.addMyFialTimes();
                        txtWinMsg.setText("对方赢了，是否重新开始游戏？");
                        bottomPane.setMsg("游戏结束，对方赢了！");
                        Newfivefix.stageRestart.show();
                        selectionMode=MODE_RESTART;
                    });
                else if(boardPane.WINNER==' ')//平局
                    Platform.runLater(() -> {
                        topPane.addFlatTimes();
                        txtWinMsg.setText("平局，是否重新开始游戏？");
                        bottomPane.setMsg("游戏结束，平局！");
                        Newfivefix.stageRestart.show();
                        selectionMode=MODE_RESTART;
                    });
                    
                //上方区域不提示谁也不走了
                topPane.pause();
                topPane.setAllColor(); 
                
                new Thread(() -> { //接受对方选择
                    try {
                        receiveRestartInfo();
                    } catch (IOException ex) {
                        if(ex.getMessage().contains("Socket closed"));
                          //  System.out.println("说明我是先手，对方还没做选择");
                    }
                }).start();
                while(UserSelection == ' ' && otherSelection!='N'){ //等待用户做出选择，同时对方没有发来结束选择
                    Thread.sleep(1000);                    
                }                
                reset(); //重置棋盘     
                if(otherSelection == 'N' && selectionMode==MODE_RESTART)//在向对方发送选择之前判断是不是对方先发过来了，防止直接跳出循环
                    otherExited();
                else
                    sendRestartInfo(UserSelection);//向对方发送我的选择
                
                
                
                if(UserSelection == 'Y'&& selectionMode==MODE_RESTART){//用户点击确认按钮          
                    selectionMode=MODE_NOMAL;
                    if(otherSelection == ' '){
                      //  System.out.println("等待对方选择重新开始游戏");    
                        Newfivefix.lblNotification.setText("等待对方选择重新开始游戏");
                        Platform.runLater(() ->{ 
                            Newfivefix.stageNotification.show();
                            bottomPane.setMsg("等待对方做出选择");
                        });    
                        while(otherSelection == ' '){ //等待对方发来选择
                            Thread.sleep(1000);                    
                        }  
                    }                                    ;
                       
                    if(otherSelection == 'Y'){//对方也发来了是                        
                        //设定重新开始游戏
                        boardPane.continueToPlay = true;
                        topPane.play();
                    }else
                        otherExited();
                }else if(UserSelection == 'N'&& selectionMode==MODE_RESTART) {//用户点击取消按钮
                    System.out.println("选择取消重新开始游戏");
                    endGame=true;                            
                    Platform.runLater(() -> {
                        try {
                            exit();
                            colseSocket();
                        } catch (IOException ex) {
                            Logger.getLogger(FiveFixGame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                    
                }
                UserSelection = ' ';
                otherSelection =' ';
                
                
            }
            System.out.println("跳出了循环");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            if(ex.getMessage().contains("socket write error") || ex.getMessage().contains("Write")){
                Platform.runLater(() -> {
                    topPane.addMyWinTimes();
                    try {
                        exit();
                        colseSocket();
                    } catch (IOException ex1) {
                        Logger.getLogger(FiveFixGame.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    showOtherRun();
                 });      
            }
                
        }
    }
    public void otherExited(){
        System.out.println("对方退出了游戏");
        endGame=true;
        Platform.runLater(() -> {
            if(Newfivefix.stageRestart.isShowing())
                Newfivefix.stageRestart.close();
            Newfivefix.lblNotification.setText("对方退出了游戏");
            Newfivefix.stageNotification.show();            
            try {
                exit();
                colseSocket();
            } catch (IOException ex) {
                Logger.getLogger(FiveFixGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
    }
    private void exit() throws IOException{        
        reset();
       topPane.reNewOtherGrade();
       selectionMode=MODE_NOMAL;
       Newfivefix.isStartAGame = false;      
       if(Newfivefix.stageGame.isShowing())
            Newfivefix.stageGame.close();

       if(Newfivefix.serverThread!=null){
           Newfivefix.stageBuild.show();
       }else
           Newfivefix.stageJoin.show();
    }
    private void colseSocket()throws IOException{
        socket.close();
    }
    private void showOtherRun(){
        System.out.println("对方跑了，您赢了");
        Newfivefix.lblNotification.setText("对方跑了，您赢了");
        Newfivefix.stageNotification.show();
    }
    
    public void reset(){
        //交换先后手顺序;
        myBlackTurn = !myBlackTurn;
        topPane.setAllColor(myBlackTurn?'B':'W');
        boardPane.animation.stop(); //停止赢棋动画显示
        Platform.runLater(() -> boardPane.reset());
        Platform.runLater(() -> bottomPane.setMsg(""));
        cellInt = new int[CELL_NUM][CELL_NUM];
    }
     public void waitForPlayerAction() throws InterruptedException, IOException{
        while(boardPane.waiting && !endGame){
            Thread.sleep(100);            
        }
        boardPane.waiting = true;
    }
    public void sendMove() throws IOException{
        if(!endGame){
            output.writeInt(CONTINUNE); //正常发送步数
            output.writeInt(boardPane.rowSelected);
            output.writeInt(boardPane.columnSelected);
            cellInt[boardPane.rowSelected][boardPane.columnSelected] = 1;
            Platform.runLater(() -> boardPane.setCurrentCell());
        }
        //System.out.println("发送给服务器的棋为:" + rowSelected + ":" + columnSelected);
    }    
    public void sendRestartInfo(char ch) throws IOException{
        output.writeChar(ch);
    }
   
    private void receiveMove() throws IOException{
        int status = input.readInt();
        if(status == CONTINUNE){//发送过来的是步数
            int row = input.readInt();
            int column = input.readInt();
            //System.out.println("接收到的棋" + row + ":"+ column);
            boardPane.playMusic();
            cellInt[row][column] = -1;
            Platform.runLater(() -> {
                boardPane.cells[row][column].setToken(boardPane.otherToken);
                boardPane.setCurrentCell(row, column);
            });
        }else if(status == ME_FAIL){ //对方跑了，我赢了
            if(myBlackTurn)
                boardPane.WINNER='B';
            else
                boardPane.WINNER='W';
            endGame = true;         
          //  System.out.println("接受到对方跑了信号");
        }
    }
    private void receiveRestartInfo() throws IOException{
        otherSelection = input.readChar();
    }
    
}
