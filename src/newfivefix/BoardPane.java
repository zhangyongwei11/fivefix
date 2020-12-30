/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newfivefix;


import java.io.File;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;
import static newfivefix.five_fix_Constants.CELL_NUM;
/**
 *
 * @author 张永伟
 */
class BoardPane extends Pane{
    private double w = 300;
    private double h = 250;
    double rectangle_Width, rectangle_Height;
    double line_Length;
    double radius;
    private Rectangle r = new Rectangle();
    private Line [][] lines = new Line [CELL_NUM][2]; //棋盘线
    private Line[] cur_rec_lines = new Line[8]; // 用来给当前棋子周围画一个虚线框
    //private Cell [][] cells = new Cell [16][16];
    Timeline animation = new Timeline(new KeyFrame(Duration.millis(200), e -> hasWin()));
    
    int [][] result = new int [5][2];
    boolean continueToPlay = true;
    boolean myTurn = true;
    char myToken = ' ';
    
    char otherToken = ' ';
    
    Cell[][] cells = new Cell[CELL_NUM][CELL_NUM]; 
    
    int rowSelected;
    int columnSelected;
    boolean waiting = true;
    public char WINNER = ' ';
    MediaPlayer mp [] = new MediaPlayer[2];

    public BoardPane(){
        animation.setCycleCount(Timeline.INDEFINITE);
        
        getChildren().add(r);
        for(int i = 0; i< lines.length;i++)
            for(int j = 0; j < lines[i].length; j++){
                lines[i][j] = new Line();
                getChildren().add(lines[i][j]);
            }

        for(int i = 0; i < cells.length; i++)
            for(int j = 0; j < cells[i].length; j++){
                cells[i][j] = new Cell(i, j);
                getChildren().add(cells[i][j]);
            }
        for(int i = 0; i < cur_rec_lines.length; i++){
            cur_rec_lines[i] = new Line();
            getChildren().add(cur_rec_lines[i]);
        }
        
        setBoard();

        widthProperty().addListener(e -> {
            this.w = getWidth();
            setBoard();
        });
        this.heightProperty().addListener(e -> {
           this.h = getHeight();
           setBoard();
        });
        
       // File video = new File();
        mp[0] = new MediaPlayer(new Media( getClass().getResource("落子.mp3").toString() ) );
        mp[1] = new MediaPlayer(new Media( getClass().getResource("落子.mp3").toString() ) );
        //mp[1] = new MediaPlayer(new Media( video.toURI().toString()));
    }
    public void playMusic(){
        mp[0].seek(Duration.ZERO);
        mp[1].play();
    }
    public void setBoard(){
        rectangle_Width = 16 * Math.min(w / 16, h / 16);
        double circleDistance = Math.min(w / 16 , h / 16);
        line_Length = circleDistance * 15;
        radius = Math.min(w / 16, h / 16) * 0.8 / 2;
        double startX = w / 2 - rectangle_Width / 2;
        double startY = h / 2 - rectangle_Width / 2;

        r.setFill(new Color(0, 1, 0, 0.2));
        r.setX(startX);
        r.setY(startY);
        r.setWidth(rectangle_Width);
        r.setHeight(rectangle_Width);

        for(int i = 0; i < lines.length; i++){
            lines[i][0].setStartX(startX + circleDistance / 2 + i * circleDistance);
            lines[i][0].setStartY(startY + circleDistance / 2);
            lines[i][0].setEndX(startX + circleDistance / 2 + i * circleDistance);
            lines[i][0].setEndY(startY + circleDistance / 2 + line_Length);
            lines[i][1].setStartX(startX + circleDistance / 2 );
            lines[i][1].setStartY(startY + circleDistance / 2+ i * circleDistance);
            lines[i][1].setEndX(startX + circleDistance / 2 + line_Length);
            lines[i][1].setEndY(startY + circleDistance / 2 + i * circleDistance);
        }

        for(int i = 0; i< cells.length; i++)
            for(int j = 0; j < cells[i].length; j++){
                cells[i][j].setRadius(radius);
                cells[i][j].setCenterX(startX + circleDistance / 2 +  i * circleDistance);
                cells[i][j].setCenterY(startY + circleDistance / 2+ j * circleDistance);
                if(cells[i][j].token == ' '){
                    //cells[i][j].setStroke(Color.BLACK);
                    cells[i][j].setFill(new Color(1, 1, 1, 0));
                }else if(cells[i][j].token == 'B'){
                    cells[i][j].setFill(Color.BLACK);
                }else if(cells[i][j].token == 'W'){
                    cells[i][j].setFill(Color.WHITE);
                }
            }
        for(int i = 0; i< cur_rec_lines.length; i++){                
            cur_rec_lines[i].setStroke(new Color(1, 1, 1, 0));
        }
    }
    public void reset(){
        for(int i= 0; i < cells.length; i++){
            for(int j = 0; j< cells[i].length; j++){
                if(cells[i][j].getToken() !=' ')
                    cells[i][j].setToken(' ');
            }
        }
        
         for(int i = 0; i< cur_rec_lines.length; i++){                
            cur_rec_lines[i].setStroke(new Color(1, 1, 1, 0));
        }
         
         WINNER = ' ';
    }
    public void setCurrentCell(){      //绘制虚线框
         setCurrentCell(rowSelected, columnSelected);         
    }
    public void setBoardColor(Color color){
        r.setFill(color);
    }
    public void setCurrentCell(int row ,int col){      //绘制虚线框
        double startX = cells[row][col].getCenterX();
        double startY = cells[row][col].getCenterY();
        for(int i = 0; i < cur_rec_lines.length; i++){
            cur_rec_lines[i].setStroke(Color.RED);                    
        }
        cur_rec_lines[0].setStartX(startX + radius / 2.0);
        cur_rec_lines[0].setStartY(startY - radius);
        cur_rec_lines[0].setEndX(startX + radius );
        cur_rec_lines[0].setEndY(startY - radius);
        cur_rec_lines[1].setStartX(startX + radius );
        cur_rec_lines[1].setStartY(startY - radius);
        cur_rec_lines[1].setEndX(startX + radius);
        cur_rec_lines[1].setEndY(startY - radius / 2.0);
        cur_rec_lines[2].setStartX(startX + radius);
        cur_rec_lines[2].setStartY(startY + radius / 2.0);
        cur_rec_lines[2].setEndX(startX + radius);
        cur_rec_lines[2].setEndY(startY + radius);
        cur_rec_lines[3].setStartX(startX + radius);
        cur_rec_lines[3].setStartY(startY + radius);
        cur_rec_lines[3].setEndX(startX + radius / 2);
        cur_rec_lines[3].setEndY(startY + radius);
        cur_rec_lines[4].setStartX(startX - radius /2 );
        cur_rec_lines[4].setStartY(startY + radius);
        cur_rec_lines[4].setEndX(startX - radius);
        cur_rec_lines[4].setEndY(startY + radius);
        cur_rec_lines[5].setStartX(startX -radius);
        cur_rec_lines[5].setStartY(startY + radius);
        cur_rec_lines[5].setEndX(startX - radius);
        cur_rec_lines[5].setEndY(startY + radius / 2);
        cur_rec_lines[6].setStartX(startX - radius);
        cur_rec_lines[6].setStartY(startY - radius / 2);
        cur_rec_lines[6].setEndX(startX - radius);
        cur_rec_lines[6].setEndY(startY - radius);
        cur_rec_lines[7].setStartX(startX - radius);
        cur_rec_lines[7].setStartY(startY - radius);
        cur_rec_lines[7].setEndX(startX - radius / 2);
        cur_rec_lines[7].setEndY(startY - radius);   
    }
    boolean isWon(int [][] num){
        result = isConsecutiveFive(num);
        if(result!=null) return true;
        return false;
    }
    public static int[][] isConsecutiveFive(int[][] values) {
      int numberOfRows = values.length;
      int numberOfColumns = values[0].length;

      // Check rows
      for (int i = 0; i < numberOfRows; i++) {
        if (isConsecutiveFive(values[i]) != null) {
          int[][] result = new int[5][2];
          result[0][0] = result[1][0] = result[2][0] = result[3][0] = result[4][0]= i;
          int k = isConsecutiveFive(values[i]);

          result[0][1] = k; result[1][1] = k + 1;
          result[2][1] = k + 2; result[3][1] = k + 3;
          result[4][1] = k+ 4;
          return result;
        }
      }

      // Check columns
      for (int j = 0; j < numberOfColumns; j++) {
        int[] column = new int[numberOfRows];
        // Get a column into an array
        for (int i = 0; i < numberOfRows; i++)
          column[i] = values[i][j];

        if (isConsecutiveFive(column) != null) {
          int[][] result = new int[5][2];
          result[0][1] = result[1][1] = result[2][1] = result[3][1] = result[4][1] = j;
          int k = isConsecutiveFive(column);

          result[0][0] = k; result[1][0] = k + 1;
          result[2][0] = k + 2; result[3][0] = k + 3;
          result[4][0] = k + 4;
          return result;        
        }
      }

      // Check major diagonal (lower part)   
      for (int i = 0; i < numberOfRows - 4; i++) {
        int numberOfElementsInDiagonal 
          = Math.min(numberOfRows - i, numberOfColumns);     
        int[] diagonal = new int[numberOfElementsInDiagonal];
        for (int k = 0; k < numberOfElementsInDiagonal; k++)
          diagonal[k] = values[k + i][k];

        if (isConsecutiveFive(diagonal) != null) {
          int[][] result = new int[5][2];
          int k = isConsecutiveFive(diagonal);        
          result[0][0] = k + i;
          result[1][0] = k + 1 + i;
          result[2][0] = k + 2 + i;
          result[3][0] = k + 3 + i;
          result[4][0] = k + 4 + i;          
          result[0][1] = k; 
          result[1][1] = k + 1;
          result[2][1] = k + 2; 
          result[3][1] = k + 3;   
          result[4][1] = k + 4;   
          return result;        
        }
      }

      // Check major diagonal (upper part)
      for (int j = 1; j < numberOfColumns - 4; j++) {
        int numberOfElementsInDiagonal 
          = Math.min(numberOfColumns - j, numberOfRows);     
        int[] diagonal = new int[numberOfElementsInDiagonal];
        for (int k = 0; k < numberOfElementsInDiagonal; k++)
          diagonal[k] = values[k][k + j];

        if (isConsecutiveFive(diagonal) != null) {
          int[][] result = new int[5][2];
          int k = isConsecutiveFive(diagonal);        
          result[0][0] = k;
          result[1][0] = k + 1;
          result[2][0] = k + 2;
          result[3][0] = k + 3;
          result[4][0] = k + 4;          
          result[0][1] = k + j; 
          result[1][1] = k + 1 + j;
          result[2][1] = k + 2 + j; 
          result[3][1] = k + 3 + j;  
          result[4][1] = k + 4 + j;   
          return result;        
        }
      }

      // Check sub-diagonal (left part)
      for (int j = 4; j < numberOfColumns; j++) {
        int numberOfElementsInDiagonal 
          = Math.min(j + 1, numberOfRows);     
        int[] diagonal = new int[numberOfElementsInDiagonal];

        for (int k = 0; k < numberOfElementsInDiagonal; k++)
          diagonal[k] = values[k][j - k];

        if (isConsecutiveFive(diagonal) != null) {
          int[][] result = new int[5][2];
          int k = isConsecutiveFive(diagonal);        
          result[0][0] = k;
          result[1][0] = k + 1;
          result[2][0] = k + 2;
          result[3][0] = k + 3;          
          result[4][0] = k + 4;
          result[0][1] = j - k; 
          result[1][1] = j - k - 1;
          result[2][1] = j - k - 2; 
          result[3][1] = j - k - 3;            
          result[4][1] = j - k - 4; 
          return result;        
        }
      }

      // Check sub-diagonal (right part)
      for (int i = 1; i < numberOfRows - 4; i++) {
        int numberOfElementsInDiagonal 
          = Math.min(numberOfRows - i, numberOfColumns);     
        int[] diagonal = new int[numberOfElementsInDiagonal];

        for (int k = 0; k < numberOfElementsInDiagonal; k++)
          diagonal[k] = values[k + i][numberOfColumns - k - 1];

        if (isConsecutiveFive(diagonal) != null) {
          int[][] result = new int[5][2];
          int k = isConsecutiveFive(diagonal);        
          result[0][0] = k + i;
          result[1][0] = k + i + 1;
          result[2][0] = k + i + 2;
          result[3][0] = k + i + 3;          
          result[4][0] = k + i + 4;
          result[0][1] = numberOfColumns - k - 1; 
          result[1][1] = numberOfColumns - (k + 1) - 1;
          result[2][1] = numberOfColumns - (k + 2) - 1; 
          result[3][1] = numberOfColumns - (k + 3) - 1;   
          result[4][1] = numberOfColumns - (k + 4) - 1;           
          return result;        
        }
      }

      return null; 
    }

    public static Integer isConsecutiveFive(int[] values) {    
      for (int i = 0; i < values.length - 4; i++) {
        boolean isEqual = true;        
        for (int j = i; j < i + 4; j++) {
          if (values[j] != values[j + 1] || values[j] == 0) {
            isEqual = false;
            break;
          }
        }

        if (isEqual) return new Integer(i);
      }

      return null;
    }
    boolean isFull(){
        for(int i = 0 ; i < cells.length; i++)
            for(int j = 0 ;j < cells[i].length; j++)
                if(cells[i][j].getToken() == ' ')
                    return false; //至少有一个没有被填满
        return true;
    }
    private void hideCurrentCell(){
        for(int i = 0; i < cur_rec_lines.length; i++){
            cur_rec_lines[i].setStroke(new Color(1, 1, 1, 0));                    
        }
    }
    public void hasWin(){
        if(cells[result[0][0]][result[0][1]].token != ' '){
              for(int i = 0; i < result.length; i++){
                  cells[result[i][0]][result[i][1]].token = ' ';
                  cells[result[i][0]][result[i][1]].setFill(new Color(1, 1, 1, 0));
              }
        }else{
            if(WINNER == 'W'){
                for(int i = 0; i < result.length; i++){
                  cells[result[i][0]][result[i][1]].token = 'W';
                  cells[result[i][0]][result[i][1]].setFill(Color.WHITE);
              }
            }else if(WINNER == 'B'){
                for(int i = 0; i < result.length; i++){
                  cells[result[i][0]][result[i][1]].token = 'B';
                  cells[result[i][0]][result[i][1]].setFill(Color.BLACK);
              }
            }
        }
    }

    

    
    class Cell extends Circle{
        private int row;
        private int column;
        private char token = ' '; //表示黑色或者白色

        public Cell(int row, int column){
            this.row = row;
            this.column = column;
            this.setOnMouseClicked(e -> {
                if(token == ' ' && myTurn && continueToPlay){
                    mp[1].seek(Duration.ZERO);
                    mp[0].play(); 
                    setToken(myToken);
                    myTurn = false;
                    rowSelected = row;
                    columnSelected = column;
                    //lblStatus.setText("等待另一个玩家移动");
                    waiting = false;
                }
            });
        }
         public void repaint(){
             if(token == 'W')
                 setFill(Color.WHITE);
             else if(token == 'B')
                 setFill(Color.BLACK);
             else
                 setFill(new Color(1, 1, 1, 0));
        }
         public char getToken(){
            return token;
        }

        public void setToken(char c){
            token = c;
            repaint();
        }

    }
}



