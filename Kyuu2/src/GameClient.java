// File
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
//network
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Random;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import tg.Turtle;
import tg.TurtleFrame;


public class GameClient{
    double w = 600, h = 600;
    TextArea text;
    TextField field;
    Socket chatS = null;
    BufferedReader in = null;
    PrintStream out = null;
    public static HashMap<String,Turtle> map  = new HashMap<String,Turtle>();

    static String sName;   //サーバIP
    static int portN;      //ポート番号
    static String uName;   //ユーザ名
    private String myIP; // 追加
    private int myPort; // 追加
    static TurtleFrame f;

    String userName;
    public void start() {
        map = new HashMap<String,Turtle>();
        initNet(sName, portN, uName);
        myIP = chatS.getLocalAddress().getHostAddress(); // 自分のIPアドレスを取得
        myPort = chatS.getLocalPort(); 
        // 別スレッド上でサーバと接続,応答を待って,表示
        new Thread(() -> {startChat();}).start();
    }

    //会話が始まったときの動作
    public void startChat() {
        sendMessage("connect"+" "+userName);//接続したことを知らせる
        String fromServer;
        try {
            BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
            Thread inputThread = new Thread(() -> {
                try {
                    String str;
                    while ((str = BR.readLine()) != null) {
                        if(str.contains("Kaware")) {
                            Random rand = new Random();
                            int r = rand.nextInt(256);
                            int g = rand.nextInt(256);
                            int b = rand.nextInt(256); 
                            Color randomColor = Color.rgb(r, g, b);
                            map.get(myIP+":"+myPort).setTColor(randomColor);
                        }else {
                            out.println(str);
                            out.flush();
                        }
                    }
                } catch (IOException e) {
                    System.out.println(("チャット中に問題が起こりました。"));
                    System.exit(1);
                }
            });
            inputThread.start();
            while (((fromServer = in.readLine()) != null)) {//サーバからの応答がなくなるまで繰り返し処理を行う
                // text.setText(text.getText().concat(fromServer + "\n"));// サーバからの応答をTextAreaに表示
                System.out.println(fromServer);//消すかも
                String[] split = fromServer.split("[ ]+");
                if(fromServer.contains("generate")&&map.get(split[1])==null) {
                    double x2 = Double.parseDouble(split[3]);
                    double y2 = Double.parseDouble(split[4]);
                    double ang2 = Double.parseDouble(split[5]);
                    double e2 = Double.parseDouble(split[6]);
                    //      System.out.println(x1+" "+(400.0-y1)+" "+(90.0-ang1));
                    Turtle m2= new Turtle(x2,(400.0-y2),(90.0-ang2));
                    f.add(m2);
                    m2.setTScale(e2/10000.0);
                    addTurtle(map,m2,split[1]);
                    if (split[1].equals(myIP+":"+myPort)) {
                        m2.setTColor(Color.RED); // 自分のTurtleインスタンスを赤色に設定
                    }else {
                        m2.setTColor(Color.LIME); // 他のTurtleインスタンスを初期の色に設定
                    }
                }
                //  map.get(IP).setTColor(Color.RED);
                if(fromServer.contains("moveto")) {
                    double x = Double.parseDouble(split[2]);
                    double y = Double.parseDouble(split[3]);
                    double ang = Double.parseDouble(split[4]);
                    double size = Double.parseDouble(split[5]);
                    //  System.out.println(x+" "+(400.0-y)+" "+(90.0-ang));
                    map.get(split[1]).setTScale(size/10000.0);
                    map.get(split[1]).moveTo(x,(400.0-y),(90.0-ang));
                }
                if(fromServer.contains("remove")) {
                    f.remove(map.get(split[1]));
                }
            }
            BR.close();
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
        f = new TurtleFrame(); 
        //launch(args);//mainメソッドの引数を引数にして会話を始める
        new GameClient().start();
    }
    synchronized static void addTurtle(HashMap<String,Turtle> map,Turtle m,String key) {
        if(map.containsKey(key)==false) {
            map.put(key, m);
        }
    }
}
