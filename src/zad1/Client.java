package zad1;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Client {
    public static List<String> myTopicsGui = new ArrayList<>();
    public static List<String> myTopics = new ArrayList<>();
    public static List<String> topicsList = new ArrayList<>();
    private static SocketChannel socketChannel = null;
    private static Charset charset = Charset.forName("UTF-8");
    public static ClientGui gui;
    public static boolean flag = true;
    public Client(){
        gui=new ClientGui(this);
    }
    public static void main(String[] args) throws IOException, ParseException {
        new Client();

        String server = "localhost";
        int serverPort = 50000;
        JSONParser jsonParser = new JSONParser();

        try{
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(server, serverPort));

            while(!socketChannel.finishConnect()){

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Charset charset = Charset.forName("UTF-8");
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        CharBuffer charBuffer = null;

        socketChannel.write(charset.encode("{\"msg\":\"getTopics\"}\n"));

        while(flag){
            byteBuffer.clear();
            int readBytes = socketChannel.read(byteBuffer);

            if(readBytes == 0){continue;}
            else if(readBytes == -1){break;}
            else {
                byteBuffer.flip();

                charBuffer = charset.decode(byteBuffer);
                String serverResponse = charBuffer.toString();

                JSONObject jsonObject = (JSONObject) jsonParser.parse(serverResponse);
                if(jsonObject.containsKey("topics")){

                    JSONArray topicsArray =(JSONArray) jsonObject.get("topics");
                    Iterator<String> iter = topicsArray.iterator();
                    String update =(String) jsonObject.get("update");

                    if(jsonObject.containsKey("update")){
                        if(update.equals("add")) {
                            topicsList.clear();
                            while(iter.hasNext()){
                                String topic = iter.next();
                                addTopic(topic);
                            }
                        }
                    }
                    gui.updateTopics();
                }
                if(jsonObject.containsKey("update")){
                    String update =(String) jsonObject.get("update");
                    if(update.equals("remove")) {
                        JSONArray topicsArray =(JSONArray) jsonObject.get("topics");
                        Iterator<String> iter = topicsArray.iterator();
                        while(iter.hasNext()){
                            String topic = iter.next();
                            removeTopic(topic);
                        }
                    }
                    gui.updateTopics();
                }
                for(String subject: myTopics){
                    if(jsonObject.containsKey(subject)){
                        String subjectNews =(String) jsonObject.get(subject);
                        gui.publishNews(subject, subjectNews);
                    }
                }

                charBuffer.clear();
            }
        }
    }
    public static void addTopic(String topic) {
        topicsList.add(topic);

    }
    public static void removeTopic(String topic) {
        topicsList.remove(topic);
        myTopics.remove(topic);
        myTopicsGui.remove(topic);

    }
    public void subscribeToTopic(String topic) throws IOException {
        if(!myTopics.contains(topic)) {
            String topicJson = "{\"subscribe\":\"" + topic + "\"}\n";
            socketChannel.write(charset.encode(topicJson));
            myTopics.add(topic);
            myTopicsGui.add(topic);
        }
    }
    public static void unsubscribeToTopic(String topic) throws IOException {
        if(myTopics.contains(topic)){

            String topicJson = "{\"unsubscribe\":\"" + topic + "\"}\n";
            socketChannel.write(charset.encode(topicJson));
            myTopics.remove(topic);
            myTopicsGui.remove(topic);
        }
    }
    public void removeFromServer() throws IOException {
        String json = "{\"bye\":\"true\"}\n";
        socketChannel.write(charset.encode(json));

    }
    public void closingClient() throws IOException {
        flag=false;
        socketChannel.close();
        socketChannel.socket().close();
    }
}
