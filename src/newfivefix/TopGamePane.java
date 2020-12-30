/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newfivefix;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 *
 * @author 张永伟
 */
public class TopGamePane extends GridPane{    
    Circle myColor = new Circle();
    Circle otherColor = new Circle();
    Text myTotalGameTimes = new Text();
    Text otherTotalGameTimes = new Text();
    Text myWinTimes = new Text();
    Text myFialTimes = new Text();
    Text myFlatTimes = new Text();
    Text myWinRate = new Text();
    Text otherWinTimes = new Text();
    Text otherFialTimes = new Text();
    Text otherFlatTimes = new Text();
    Text otherWinRate = new Text();
    int mytotalGames = 0,mywinGames = 0,myfailGames = 0, myflatGames = 0;
    int otherTotalGames = 0, otherWinGames = 0, otherFailGames= 0,otherFlatGames = 0;
    boolean isMyMove = true;
    char myCircleColor = ' ';
    String ip;
    File file = new File("config/history_grade.config");
    BufferedReader br;
    ArrayList<String> otherIpGrade = new ArrayList<>();
    
    Timeline toMoveAnimation = new Timeline(new KeyFrame(Duration.millis(200), e -> displayWhichMove()));
    TopGamePane(){      
        readHistoryGrade();
        this.setAlignment(Pos.CENTER);
        this.add(new Text("我方:"), 0, 0);
        myColor.setFill(Color.BLACK);
        myColor.setRadius(10);
        this.add(myColor, 1, 0);
        this.add(new Text("总局数:"), 2 , 0);
        this.add(myTotalGameTimes, 3, 0);
        this.add(new Text("胜局:"), 4, 0);
        this.add(myWinTimes, 5, 0);
        this.add(new Text("败局:"),6, 0);
        this.add(myFialTimes, 7, 0);
        this.add(new Text("平局:"), 8, 0);
        this.add(myFlatTimes, 9, 0);
        this.add(new Text("胜率:"), 10, 0);
        this.add(myWinRate, 11, 0);
        
        this.add(new Text("对方:"), 0, 1);
        otherColor.setFill(Color.WHITE);
        otherColor.setRadius(10);
        this.add(otherColor, 1, 1);
        this.add(new Text("总局数:"), 2 , 1);
        this.add(otherTotalGameTimes, 3, 1);
        this.add(new Text("胜局:"), 4, 1);
        this.add(otherWinTimes, 5, 1);
        this.add(new Text("败局:"),6, 1);
        this.add(otherFialTimes, 7, 1);
        this.add(new Text("平局:"), 8, 1);
        this.add(otherFlatTimes, 9, 1);      
        this.add(new Text("胜率:"), 10, 1);
        this.add(otherWinRate, 11, 1);
        //this.setBackground(new Background(new BackgroundFill(new Color(0, 1, 0, 0.2), null, null)));
        //设置边距
        GridPane.setMargin(myColor, new Insets(2, 10, 0, 4)); //上，右，下，左
        GridPane.setMargin(myTotalGameTimes, new Insets(2, 10, 0, 4));
        GridPane.setMargin(myWinTimes, new Insets(2,10, 0, 4));
        GridPane.setMargin(myFialTimes, new Insets(2, 10, 0, 4));
        GridPane.setMargin(myFlatTimes, new Insets(2, 10, 0, 4));
        GridPane.setMargin(myWinRate, new Insets(2, 10, 0, 4));
        
        GridPane.setMargin(otherColor, new Insets(2, 10, 0, 4)); //上，右，下，左
        GridPane.setMargin(otherTotalGameTimes, new Insets(2, 10, 0, 4));
        GridPane.setMargin(otherWinTimes, new Insets(2,10, 0, 4));
        GridPane.setMargin(otherFialTimes, new Insets(2, 10, 0, 4));
        GridPane.setMargin(otherFlatTimes, new Insets(2, 10, 0, 4));
        GridPane.setMargin(otherWinRate, new Insets(2, 10, 0, 4));
        paint();
        toMoveAnimation.setCycleCount(Timeline.INDEFINITE);
        toMoveAnimation.play();
    } 
    void setIP(String ip){//设置对方IP
        if(this.ip == null || !this.ip.equals(ip)){
            this.ip = ip;
            try {
                //从文件中获取历史对局次数；
                if(br!=null){
                    String temp = br.readLine();
                    while(temp != null){
                       // System.out.println(temp);
                        otherIpGrade.add(temp);
                        if(temp.contains(ip)){//里面有我的信息
                            String[] readtemps = temp.split("[,:]");
                             otherTotalGames = Integer.parseInt(readtemps[1]);
                             otherWinGames = Integer.parseInt(readtemps[2]);
                             otherFailGames = Integer.parseInt(readtemps[3]);
                             otherFlatGames = Integer.parseInt(readtemps[4]);
                        }
                        temp = br.readLine();
                    }                
                    br.close();
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TopGamePane.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(TopGamePane.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(TopGamePane.class.getName()).log(Level.SEVERE, null, ex);
            }
            paint();
        }
    }
    void reNewOtherGrade(){
        boolean containIp = false;
        String str=ip + ":" +  otherTotalGames +","+otherWinGames +","+otherFailGames+","+otherFlatGames;
        for(int i =0 ;i < otherIpGrade.size(); i++){
            if(otherIpGrade.get(i).contains(ip)){                
                otherIpGrade.set(i, str);       
                containIp = true;
                break;
            }
        }
        if(!containIp)
            otherIpGrade.add(str);
    }
    void readHistoryGrade(){
         try {
            //从文件中获取历史对局次数；
            if(file.exists()){
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
                String temp = br.readLine();
                if(temp.contains("my_history_grade")){//里面有我的信息
                    String[] readtemps = temp.split("[,:]");
                     mytotalGames = Integer.parseInt(readtemps[1]);
                     mywinGames = Integer.parseInt(readtemps[2]);
                     myfailGames = Integer.parseInt(readtemps[3]);
                     myflatGames = Integer.parseInt(readtemps[4]);
                }
                
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TopGamePane.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TopGamePane.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TopGamePane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    void saveGrade(){
        try {
            OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
            output.write("my_history_grade:" + mytotalGames + "," + mywinGames + "," + myfailGames + "," + myflatGames + "\r\n");
            for(String str: otherIpGrade){               
                output.write(str + "\r\n");
            }
            otherIpGrade.clear();
            output.flush();
            output.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TopGamePane.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TopGamePane.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TopGamePane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void addMyWinTimes(){
        mywinGames++;
        mytotalGames++;
        otherTotalGames++;
        otherFailGames++;
        paint();
    }
    void addMyFialTimes(){
        mytotalGames++;
        otherTotalGames++;
        myfailGames++;
        otherWinGames++;
        paint();
    }
    void addFlatTimes(){
        mytotalGames++;
        otherTotalGames++;
        myflatGames++;
        otherFlatGames++;
        paint();
    }
    void play(){
        toMoveAnimation.play();
    }
    void pause(){
        toMoveAnimation.pause();
        setAllColor();
    }
    void setAllColor(char rb){
        myCircleColor = rb;
        setAllColor();
    }
    void setAllColor(){
        setMyColor();
        setOtherColor();
    }
    void setMyColor(){
        if(myCircleColor == 'B'){
            myColor.setFill(Color.BLACK);
        }else{
            myColor.setFill(Color.WHITE);
        }
    }
    void setOtherColor(){
        if(myCircleColor == 'B'){
            otherColor.setFill(Color.WHITE);
        }else{
            otherColor.setFill(Color.BLACK);
        }
    }
    void setWhicMove(boolean mymove){
        if(mymove){//轮到我方移动
            isMyMove = true;
            setOtherColor();
        }else{//轮到对方移动
            isMyMove = false;
            setMyColor();
        }
    }
    
    /*
    *显示胜利局数
    */
    private void paint(){
        myTotalGameTimes.setText("" + mytotalGames);
        otherTotalGameTimes.setText("" + otherTotalGames);
        myWinTimes.setText("" + mywinGames);
        myFialTimes.setText("" + myfailGames);
        myFlatTimes.setText("" + myflatGames);
        myWinRate.setText("" + ((int )(100 * (1.0 * mywinGames)/mytotalGames )) + "%");
        otherWinTimes.setText("" + otherWinGames);
        otherFialTimes.setText("" + otherFailGames);
        otherFlatTimes.setText("" + otherFlatGames);
        otherWinRate.setText("" + ((int )(100 * (1.0 * otherWinGames)/otherTotalGames )) + "%");
    }
    private void displayWhichMove() {
       if(isMyMove){//我方动画
           if(myColor.getFill().equals(new Color(1, 1, 1, 0)))
               setMyColor();
            else
               myColor.setFill(new Color(1, 1, 1, 0));
       }else{//对方动画
           if(otherColor.getFill().equals(new Color(1, 1, 1, 0)))
               setOtherColor();
           else
               otherColor.setFill(new Color(1, 1, 1, 0));
       }
    }
}
