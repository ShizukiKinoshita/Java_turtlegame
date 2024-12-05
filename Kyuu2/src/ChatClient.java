// File
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
//network
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

//チャットクライアント
import javafx.application.Application;
//Scene
import javafx.scene.Scene;
// Button
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
// VBox
import javafx.scene.layout.VBox;
//Stage, FileChooser
import javafx.stage.Stage;

public class ChatClient extends Application {
    double w = 600, h = 600;
    TextArea text;
    TextField field;
    Socket chatS = null;
    BufferedReader in = null;
    PrintStream out = null;

    static String sName;   //サーバIP
    static int portN;      //ポート番号
    static String uName;   //ユーザ名

    String userName;
    @Override public void start(Stage stage) {
        //メニュー
        MenuBar bar = new MenuBar(); //メニューバーを作成
        Menu m = new Menu("終了"); //指定されたラベルを持つ新しいメニューを作成
        MenuItem menuExit = new MenuItem("終了"); //指定されたラベルを持つ、新しいメニュー項目(MenuItem)を作成
        m.getItems().add(menuExit); //メニューに項目を追加
        bar.getMenus().add(m); //メニューバーにメニューを追加
        //下部ボタンコンテナ
        Button buttonSay = new Button("発言");//指定されたラベルを持つ新しいボタンを作成
        field = new TextField(); //文字列を入力するための入力フォームを作成
        VBox lowerPane = new VBox();//垂直方向に配置するためのもの
        //lowerPane.setAlignment(Pos.CENTER);
        lowerPane.getChildren().addAll(field, buttonSay);//入力フォームから得た言葉を垂直に記載していく
        //lowerPane.setPadding(new Insets(15, 10, 15, 10));
        //lowerPane.setSpacing(20);
        //上部コンテナ
        ScrollPane upperPane = new ScrollPane();//垂直にスクロールするためのもの
        upperPane.setPrefSize(w, h);//会話の表示画面の幅設定
        upperPane.setFitToHeight(true); //ScrollPaneの高さにノードのサイズを変更
        upperPane.setFitToWidth(true); //ScrollPaneの幅にノードのサイズを変更
        text = new TextArea();//表示画面に記載されている会話文
        upperPane.setContent(text);//表示画面に記載されている会話文を垂直にスクロールできるようにする

        VBox root = new VBox();
        root.getChildren().addAll(bar, upperPane, lowerPane);//メニューバー、会話文、入力フォームを垂直に配置する

        Scene scene = new Scene(root);//ウィンドウに配置するためのシーンを作成
        stage.setTitle("Chat Client");//ウィンドウのタイトル設定
        stage.setScene(scene);//上記のメニューバー、会話文、入力フォームを垂直に配置するウィンドウ
        stage.sizeToScene();//ウィンドウの幅と高さを、このウィンドウのシーンのコンテンツ・サイズに一致するように設定
        stage.show();//ウィンドウを表示

        //終了ボタンを押されたらプロジェクトを終了する
        menuExit.setOnAction((event)-> {
            System.exit(0);
        });

        buttonSay.setOnAction((event)-> {//発言ボタンが押されたら
            sendMessage(field.getText());//その発言をに送り
            field.setText("");//入力フォームを
            buttonSay.requestFocus();//発言ボタンにカーソルを移す
        });

        initNet(sName, portN, uName);//指定のネットワークにつなげる
        // 別スレッド上でサーバと接続，応答を待って，表示
        new Thread(() -> {startChat();}).start();//新しい発言者のスレッドを作成
    }

    //会話が始まったときの動作
    public void startChat() {
        sendMessage("connect"+" "+userName);//接続したことを知らせる
        String fromServer;
        try{
            while ((fromServer = in.readLine()) != null) {//1行を読み込んで、何かあった時
                text.setText(text.getText().concat(fromServer + "\n"));//会話の文に改行をして新しく追加
            }
            end();
        }catch (IOException e){ //入出力処理がなんらかの原因で失敗した場合、または割り込みが発生した場合、エラー文を出す。
            System.out.println("チャット中に問題が起こりました。");
            System.exit(1);
        }
    }

    //発言者の名前とその発言を結びつける動作
    public void sendMessage(String msg) {
        String s = msg;
        System.out.println("sendMessage  " + s);
        out.println(s);
    }

    //network setup
    public void initNet(String serverName, int port, String uName) {
        userName = uName; 
        // create Socket
        try {
            //サーバを別のホストで起動する場合は下の行を有効にする
            chatS = new Socket(InetAddress.getByName(serverName), port);
            //ローカルホストでテストの場合は上の代わりに下の行を使う
            //chatS = new Socket(InetAddress.getLocalHost(), port);
            in = new BufferedReader(
                    new InputStreamReader(chatS.getInputStream()));
            out = new PrintStream(chatS.getOutputStream());
        } catch (UnknownHostException e) {
            System.out.println("ホストに接続できません。");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("IOコネクションを得られません。");
            System.exit(1);
        }
    }

    //会話が終わった後の処理
    public void end() {
        //全て閉じる。入出力処理がなんらかの原因で失敗した場合、または割り込みが発生した場合、エラー文を出す。
        try {
            out.close();
            in.close();
            chatS.close();
        } catch (IOException e) { System.out.println("end:" + e); }
    }

    public static void main(String... args) {
        //mainメソッドの引数が3か4じゃない時、エラー文を出す
        if (args.length != 3 && args.length != 4) {
            System.out.println(
                    "Usage: java ChatClient サーバのIPアドレス ポート番号 ユーザ名");
            System.out.println("例: java ChatClient 210.0.0.1 50002 ariga");
            System.exit(0);
        }
        // Getting argument.
        //IPアドレス、ポート番号、ユーザ名をmainメソッドの引数から取得
        sName = args[0];
        portN = Integer.valueOf(args[1]).intValue();
        uName = args[2];
        System.out.println("serverName = " + sName);
        System.out.println("portNumber = " + portN);
        System.out.println("userName = " + uName);
        launch(args);//mainメソッドの引数を引数にして会話を始める
    }
}



