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
import java.util.Timer;
import java.util.TimerTask;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tg.Turtle;

public class AutoGameClient {
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
        // Timer timer = new Timer(false);
        try{
            InputStreamReader ISR = new InputStreamReader(System.in);
            BufferedReader BR = new BufferedReader(ISR);
            Thread inputThread = new Thread(() -> {
                try {
                    String str;
                    while ((str = BR.readLine()) != null) {
                        out.println(str);
                        out.flush();
                    }
                } catch (IOException e) {
                    System.out.println(("チャット中に問題が起こりました。"));
                    System.exit(1);
                }
            });
            inputThread.start();
            Timer timer = new Timer(false);
            timer.schedule(task, 0, 1000);
            while ((fromServer = in.readLine()) != null) {//サーバからの応答がなくなるまで繰り返し処理を行う
                // text.setText(text.getText().concat(fromServer + "\n"));// サーバからの応答をTextAreaに表示      
               // System.out.println(fromServer);
                String[] split = fromServer.split("[ ]+");
                if(fromServer.contains("generate")&&map.get(split[1])==null) {
                    double x2 = Double.parseDouble(split[3]);
                    double y2 = Double.parseDouble(split[4]);
                    double ang2 = Double.parseDouble(split[5]);
                    //     double e2 = Double.parseDouble(split[6]);
                    //      System.out.println(x1+" "+(400.0-y1)+" "+(90.0-ang1));
                    Turtle m2= new Turtle(x2,(400.0-y2),(90.0-ang2));
                    map.put(split[1], m2);
                }
            }
            timer.cancel();
            BR.close();
            end();
        }catch (IOException e){ //入出力処理がなんらかの原因で失敗した場合、または割り込みが発生した場合、エラー文を出す。
            System.out.println("チャット中に問題が起こりました。");
            System.exit(1);
        }
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if (map.containsKey(myIP + ":" + myPort)) {
                Turtle turtle = map.get(myIP+ ":" + myPort);
                System.out.println(turtle); 
                Random random = new Random();
                double num = random.nextInt(6);
                if(num<=1) {
                    double movex = Math.random() * turtle.getX();
                    double movey = Math.random() * turtle.getY();
                    if(num==0) out.println("walk"+" "+ -(movex+movey));
                    if(num==1) out.println("walk"+" "+(movex+movey));
                    out.flush();
                }else if(num>=2&&num<=3){
                    double moveAng = Math.random() * (90.0-turtle.getAngle());
                    System.out.println(moveAng); 
                    if(num==2) out.println("rotate"+" "+ -moveAng);
                    if(num==3) out.println("rotate"+" "+moveAng);
                    out.flush();
                }else if(num>=4&&num<=5){
                    double movex = Math.random() * turtle.getX();
                    double movey = Math.random() * turtle.getY();
                    if(num==4) out.println("attack"+" "+ (movex));
                    if(num==5) out.println("attack"+" "+ -(movey));
                    out.flush();
                }
            }
        }
    };
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
        //launch(args);//mainメソッドの引数を引数にして会話を始める
        new AutoGameClient().start();
    }
}



