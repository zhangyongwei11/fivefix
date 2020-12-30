/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newfivefix;

import java.util.ArrayList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 *
 * @author 张永伟
 */
class ListPane extends VBox{
    ArrayList<String> ipsList = new ArrayList<String> ();
    private boolean isServer = false;
    String hintText = "手动输入,例:192.169.99.9";
    
    ListPane(boolean isServer){
        this.isServer = isServer;
        Paint();
    }
    void addItem(String ip){
        ipsList.add(ip);
        Paint();
    }
    void clearItem(){
        ipsList.clear();
        Paint();
    }
    void deleteItem(String ip){
        ipsList.remove(ip);
        Paint();
    }
    void refresh(){
        Paint();
    }
    boolean contains(String ip){
        return ipsList.contains(ip);
    }
    protected void Paint(){
        getChildren().clear();
        for(String ip :ipsList){
            HBox hbox = new HBox(5);
            if(isServer){
                Button btnStart = new Button("开始游戏");
                hbox.getChildren().addAll(new Text(ip + "已加入"), btnStart);
                hbox.setAlignment(Pos.CENTER);
                getChildren().add(hbox);
                btnStart.setOnAction(e -> {
                   // System.out.println("listPane点击开始按钮,ip:" + ip);
                    Newfivefix.startGame(ip);
                });
            }else{
                Button btnConnect = new Button("连接");
                hbox.getChildren().addAll(new Text(ip), btnConnect);
                hbox.setAlignment(Pos.CENTER);
                getChildren().add(hbox);
                btnConnect.setOnAction(e -> {
                  //  System.out.println("listPane点击连接按钮,ip:" + ip);
                    Newfivefix.startConnect(ip);
                    btnConnect.setVisible(false);
                });
            }           
        }
        if(!isServer){
            Button btnConnect = new Button("连接");
            HBox hbox = new HBox(5);
            TextField tfip = new TextField("");
            tfip.setText(hintText);
            tfip.focusedProperty().addListener(e -> {
                if(tfip.isFocused() && tfip.getText().equals(hintText)){
                    tfip.setText("");
                }else if(!tfip.isFocused() && tfip.getText().equals("")){
                    tfip.setText(hintText);
                }
            });
            hbox.getChildren().addAll(tfip, btnConnect);
            hbox.setAlignment(Pos.CENTER);
            getChildren().add(hbox);
            btnConnect.setOnAction(e -> {
               // System.out.println("listPane点击手动连接,");
                Newfivefix.startInputConnect(tfip.getText().trim());
                btnConnect.setVisible(false);
            });
        }
    }
}
