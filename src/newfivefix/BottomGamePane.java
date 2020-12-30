/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newfivefix;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

/**
 *
 * @author 张永伟
 */
public class BottomGamePane extends HBox{
    Text txtMsg = new Text("信息:");
    
    BottomGamePane(){
        this.setAlignment(Pos.CENTER);
        final ToggleGroup group = new ToggleGroup();
        RadioButton rb1 = new RadioButton("浅绿");
        RadioButton rb2 = new RadioButton("浅粉");
        RadioButton rb3 = new RadioButton("实木");
        RadioButton rb4 = new RadioButton("经典");
        this.setSpacing(4);
        Circle color1 = new Circle(10);
        color1.setFill(new Color(0, 1, 0, 0.5));
        Circle color2 = new Circle(10);
        color2.setFill(new Color(1, 0.78, 0.79, 1));
        Circle color3 = new Circle(10);
        color3.setFill(new Color(0.9, 0.75, 0.36, 1));
        Circle color4 = new Circle(10);
        color4.setFill(new Color(1, 0.9, 0.65, 1));
        
        this.getChildren().addAll(new Text("棋盘\n颜色:"), color1,rb1,  color2,rb2, color3,rb3,color4, rb4, txtMsg);
        HBox.setMargin(color1, new Insets(2, 0, 0, 15));
        HBox.setMargin(color2, new Insets(2, 0, 0, 15));
        HBox.setMargin(color3, new Insets(2, 0, 0, 15));
        HBox.setMargin(color4, new Insets(2, 0, 0, 15));
        HBox.setMargin(txtMsg, new Insets(2, 0, 4, 10));
        rb1.setToggleGroup(group);
        rb2.setToggleGroup(group);
        rb3.setToggleGroup(group);
        rb4.setToggleGroup(group);
        rb1.setSelected(true);
        rb1.setUserData("green");
        rb2.setUserData("pink");     
        rb3.setUserData("wood");
        rb4.setUserData("classic");
        group.selectedToggleProperty().addListener(e -> {
            if(group.getSelectedToggle() != null){
               String color = group.getSelectedToggle().getUserData().toString();
               System.out.println("当前颜色为: " + color);
               Newfivefix.setBackgroundColor(color);
            }
        });
        color1.setOnMouseClicked(e -> rb1.setSelected(true));
        color2.setOnMouseClicked(e -> rb2.setSelected(true));
        color3.setOnMouseClicked(e -> rb3.setSelected(true));
        color4.setOnMouseClicked(e -> rb4.setSelected(true));
       
    }
    void setMsg(String msg){
        txtMsg.setText("信息:  " + msg);
    }
}
