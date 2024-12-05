import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap; 

public class ChatServer {
    static PrintWriter logFile;
    static Socket s;
    static double e = 10000.0;
    static int nth = 0;
    public static void main(String[] args) throws IOException {
        //mainメソッドの引数がない時、エラー文を出す
        if(args.length != 1) { 
            System.out.println("起動方法: java ChatServer ポート番号");
            System.out.println("例: java ChatServer 50002");
            System.exit(1);
        }
        //ポート番号をmainメソッドの引数から取り、変数portに代入する
        int port = Integer.valueOf(args[0]).intValue();
        ServerSocket serverS = null;
        boolean end = true;
        try {
            //指定されたポート番号に結びついたサーバソケットを作成し、変数serverSに入れる。
            serverS = new ServerSocket(port);
        } catch (IOException e) {//入出力処理がなんらかの原因で失敗した場合、または割り込みが発生した場合、エラー文を出し、プログラムを終了させる。
            System.out.println("ポートにアクセスできません。");
            System.exit(-1);
        }
        String nitizi = java.time.LocalDateTime.now().toString();
        logFile = new PrintWriter("ChatServerLog-"+nitizi+".txt", "UTF-8");
        //ChatMThreadを作成し、サーバserverSに接続して、会話をスタートさせる。

        //  TurtleInfo m = generateTurtle(userName2);
        while(end){
            s = serverS.accept();
            new ChatMThread(s).start();
            nth++;
        }
        //サーバを閉じる
        serverS.close();   
    }
    synchronized  static void writeLog(String s){
        logFile.println(s);
        System.out.println(s);
    }
    static TurtleInfo generateTurtle(String name,String IP) {
        double x,y,a;
        x = nth * 50.0 + 100.0;
        y = 200.0;
        a = 90.0;
        TurtleInfo m = new TurtleInfo(IP,name,x,y,a,e); 
        return m;
    }
}

class ChatMThread extends Thread {
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    String IP;
    String IP2;
    private static TurtleInfo m;
    double d;
    static HashMap<String,TurtleInfo> map = new HashMap<String,TurtleInfo>();
    //複数のクライアントを入れておくためのArrayList変数memberを定義する
    static ArrayList<ChatMThread> member;
    ChatMThread(Socket s) {
        super("ChatMThread");//親クラスのコンストラクタを呼び出す
        socket = s;//引数sを変数soketに代入する
        //ArrayList変yuu数memberに誰も登録されていなかったら新しく作成し、もし誰かがいたら、add関数を使ってChatMThreadを追加する
        if (member == null) {
            member = new ArrayList<ChatMThread>();
        }
        member.add(this);
    }
    //ChatMThreadの動作
    public void run() {
        try{  	
            //テキスト出力ストリームに、ソケットの出力ストリームを出力させるPrintWriterを作成する
            out = new PrintWriter(socket.getOutputStream(), true);
            //ソケットの入力ストリームを読み込むBufferedReaderを作成する
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String fromClient;
            //クライアントからメッセージを受け取った時の処理
            while ((fromClient = in.readLine()) != null) {
                //ArrayList変数memberの中で発言した人の言葉を、テキスト出力ストリームに出力させる
                IP = socket.getRemoteSocketAddress().toString();
                IP = IP.substring(1);
                String nitizi2 = java.time.LocalDateTime.now().toString();
                ChatServer.writeLog(nitizi2+" "+"に"+IP+"と接続しました");
                ChatServer.writeLog(fromClient);
                String[] split = fromClient.split("[ ]+");

                if(fromClient.contains("rotate")) {
                    try {
                        double mapang =  map.get(IP).getAng();
                        d = Double.parseDouble(split[1]);
                        double angneo = 0;

                        if((d >= -30&&d <= 30)) {
                            map.get(IP).setAng((mapang+d+360)%360);
                        }else if(d < -30){
                            map.get(IP).setAng(mapang-30);
                        }else if(d > 30){
                            map.get(IP).setAng(mapang+30);
                        }

                        if(d>0) {
                            map.get(IP).setE(map.get(IP).getE()-d);
                        }else if(d<=0) {
                            map.get(IP).setE(map.get(IP).getE()+d);
                        }

                        map.put(IP,map.get(IP));
                        if(mapang == map.get(IP).getAng()) {
                        }else {
                            ChatServer.writeLog("moveto"+" "+IP+" "+map.get(IP).getX()+" "
                                    +map.get(IP).getY()+" "+map.get(IP).getAng()+" "+map.get(IP).getE());
                            for(ChatMThread client : member){
                                client.out.println("moveto"+" "+IP+" "+map.get(IP).getX()+" "
                                        +map.get(IP).getY()+" "+map.get(IP).getAng()+" "+map.get(IP).getE());

                            }
                            out.flush();
                            ChatServer.logFile.flush();
                        }
                    }catch(NumberFormatException e) {
                        System.out.println("dは数字ではありません");
                    }
                }

                if(fromClient.contains("walk")) {
                    try {
                        double mapang =  map.get(IP).getAng();
                        double mapX =  map.get(IP).getX();//移動したか確認する用
                        double mapY =  map.get(IP).getY();//移動したか確認する用
                        d = Double.parseDouble(split[1]);
                        double sita = 0;
                        sita = Math.toRadians(mapang);

                        double Xsa = 0;
                        double Ysa = 0;
                        Xsa = (mapX+d*Math.cos(sita))-mapX;
                        Ysa = (mapY+d*Math.sin(sita))-mapY;

                        if(Xsa >= -50&&Xsa <= 50) {
                            map.get(IP).setX(mapX+d*Math.cos(sita));
                        }else if(Xsa > 50) {
                            map.get(IP).setX(mapX+50);
                        }else if(Xsa < -50) {
                            map.get(IP).setX(mapX-50);
                        }

                        if(Ysa >= -50&&Ysa <= 50) {
                            map.get(IP).setY(mapY+d*Math.sin(sita));
                        }else if(Ysa > 50) {
                            map.get(IP).setY(mapY+50);
                        }else if(Ysa < -50) {
                            map.get(IP).setY(mapY-50);
                        }

                        if(d>0) {//エネルギーを減らす処理
                            map.get(IP).setE(map.get(IP).getE()-d);
                        }else if(d<=0) {
                            map.get(IP).setE(map.get(IP).getE()+d);
                        }
                        map.put(IP,map.get(IP));
                        if(mapX ==  map.get(IP).getX()&&mapY ==  map.get(IP).getY()) {
                            //変化がない時、コードを送らない
                        }else {
                            ChatServer.writeLog("moveto"+" "+IP+" "+map.get(IP).getX()+" "
                                    +map.get(IP).getY()+" "+map.get(IP).getAng()+" "+map.get(IP).getE());
                            for(ChatMThread client : member){
                                client.out.println("moveto"+" "+IP+" "+map.get(IP).getX()+" "
                                        +map.get(IP).getY()+" "+map.get(IP).getAng()+" "+map.get(IP).getE());
                            }
                            out.flush();
                            ChatServer.logFile.flush();
                        }
                    }catch(NumberFormatException e) {
                        System.out.println("dは数字ではありません");
                    }
                }

                if(fromClient.contains("attack")) {
                    try {
                        double mapang =  map.get(IP).getAng();
                        double mapX =  map.get(IP).getX();//攻撃するタートル
                        double mapY =  map.get(IP).getY();//攻撃するタートル
                        double n = Double.parseDouble(split[1]);
                        double sita = 0;
                        sita = Math.toRadians(mapang);

                        double minD=1000000000;////
                        String minKey = null;
                        double px =  mapX+n*Math.cos(sita);//攻撃するタートル
                        double py =  mapY+n*Math.sin(sita);//攻撃するタートル

                        for (String key : map.keySet()) {
                            double x =  map.get(key).getX();//攻撃されるタートル
                            double y =  map.get(key).getY();//攻撃されるタートル
                            if(getD(x,y,px,py)<minD) {
                                minKey=key;
                                minD=getD(x,y,px,py);
                            }
                        }

                        if(minD<n) {//エネルギーを減らす処理
                            map.get(minKey).setE(map.get(minKey).getE()-2000);
                            if(mapX ==  map.get(minKey).getX()&&mapY ==  map.get(minKey).getY()) {
                                //変化がない時、コードを送らない
                            }else {
                                ChatServer.writeLog("moveto"+" "+minKey+" "+map.get(minKey).getX()+" "
                                        +map.get(minKey).getY()+" "+map.get(minKey).getAng()+" "+map.get(minKey).getE());
                                ChatServer.writeLog("moveto"+" "+IP+" "+map.get(IP).getX()+" "
                                        +map.get(IP).getY()+" "+map.get(IP).getAng()+" "+map.get(IP).getE());
                                for(ChatMThread client : member){
                                    client.out.println("moveto"+" "+minKey+" "+map.get(minKey).getX()+" "
                                            +map.get(minKey).getY()+" "+map.get(minKey).getAng()+" "+map.get(minKey).getE());
                                }
                                out.flush();
                                ChatServer.logFile.flush();
                            }
                        }else{
                            map.get(IP).setE(map.get(IP).getE()-Math.pow(n/5, 2));
                            if(mapX ==  map.get(IP).getX()&&mapY ==  map.get(IP).getY()) {
                                //変化がない時、コードを送らない
                            }else {
                                ChatServer.writeLog("moveto"+" "+IP+" "+map.get(IP).getX()+" "
                                        +map.get(IP).getY()+" "+map.get(IP).getAng()+" "+map.get(IP).getE());
                                for(ChatMThread client : member){
                                    client.out.println("moveto"+" "+IP+" "+map.get(IP).getX()+" "
                                            +map.get(IP).getY()+" "+map.get(IP).getAng()+" "+map.get(IP).getE());
                                }
                                out.flush();
                                ChatServer.logFile.flush();
                            }
                        }
                    }catch(NumberFormatException e) {
                        System.out.println("dは数字ではありません");
                    }
                }

                if(map.get(IP)==null) {
                    fromClient = fromClient.substring(8);
                    m =  ChatServer.generateTurtle(split[1],IP);
                    putTurtle(IP,m);
                    allTurtleInfo();    
                }

                ArrayList<String> dele = new ArrayList<>();
                for(String key:map.keySet()) {
                    if(map.get(key).getE()<10) {
                        for(ChatMThread client : member){
                            client.out.println("remove"+" "+key);
                        }
                        dele.add(key);
                        ChatServer.writeLog("remove"+" "+key);
                        ChatServer.logFile.flush();
                    } 
                }
                for(String m:dele) {
                    map.remove(m);
                }

                if((fromClient.contains("exit"))) {
                    for(ChatMThread client : member){
                        client.out.println("remove"+" "+IP);
                    }
                    ChatServer.writeLog("remove"+" "+IP);
                    ChatServer.logFile.flush();
                    break;
                }

            }
        }catch(IOException e){  //入出力処理がなんらかの原因で失敗した場合、または割り込みが発生した場合、エラー文を出す。
            System.out.println("run:" + e); 
        }
        end();
    }

    //会話が終わった後の処理
    public void end() {
        //全て閉じる。入出力処理がなんらかの原因で失敗した場合、または割り込みが発生した場合、エラー文を出す。
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) { System.out.println("end:" + e); }
        //ArrayList変数memberからChatMThreadを取り除く
        member.remove(this);
    }  
    synchronized void allTurtleInfo() {
        IP2 = socket.getRemoteSocketAddress().toString();
        IP2 = IP2.substring(1);
        for(String key:map.keySet()) {
            ChatServer.writeLog("generate"+" "+key+" "+map.get(key).getName()+" "+map.get(key).getX()+" "
                    +map.get(key).getY()+" "+map.get(key).getAng()+" "+map.get(key).getE());
            this.out.println("generate"+" "+key+" "+map.get(key).getName()+" "+map.get(key).getX()+" "
                    +map.get(key).getY()+" "+map.get(key).getAng()+" "+map.get(key).getE());
        }
        for(ChatMThread client : member){
            if(client!=this) {
                client.out.println("generate"+" "+IP2+" "+map.get(IP2).getName()+" "+map.get(IP2).getX()+" "
                        +map.get(IP2).getY()+" "+map.get(IP2).getAng()+" "+map.get(IP2).getE());
            }
        }
        out.flush();
        ChatServer.logFile.flush();
    }

    synchronized void putTurtle(String IP,TurtleInfo m) {
        map.put(IP,m);
    }

    static double getD(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}

