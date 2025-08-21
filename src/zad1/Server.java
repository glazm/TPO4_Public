package zad1;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;

public class Server {

    private static Map<String,List<SelectionKey>> clientsKeys = new HashMap<String,List<SelectionKey>>();
    private static List<String> topics = new ArrayList<>();
    private static List<SelectionKey> allClientKeys = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        new Server();
    }
    Server() throws IOException{
        String host = "localhost";
        int port = 50000;

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(host,port));

        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while(true){
            selector.select();

            Set keys = selector.selectedKeys();

            Iterator iterator = keys.iterator();

            while (iterator.hasNext()){
                SelectionKey selectionKey = (SelectionKey) iterator.next();

                iterator.remove();

                if(selectionKey.isAcceptable()){
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector,SelectionKey.OP_READ|SelectionKey.OP_WRITE);

                    continue;
                }
                if(selectionKey.isReadable()){
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                    readRequest(socketChannel, selectionKey);

                    continue;
                }
                if(selectionKey.isWritable()){

                    continue;
                }
            }
        }
    }

    Charset charset = Charset.forName("UTF-8");
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    private StringBuffer stringBuffer = new StringBuffer();

    public void readRequest(SocketChannel socketChannel, SelectionKey selectionKey) {
        if (!socketChannel.isOpen()) {
            return;
        }

        stringBuffer.setLength(0);
        byteBuffer.clear();

        try{
            readLoop:
            while (true){
                int size = socketChannel.read(byteBuffer);

                if(size > 0){
                    byteBuffer.flip();
                    CharBuffer charBuffer = charset.decode(byteBuffer);

                    while (charBuffer.hasRemaining()){
                        char character = charBuffer.get();
                        if(character == '\r' || character == '\n'){break readLoop;}
                        else {
                            stringBuffer.append(character);
                        }
                    }
                }
            }

            String request = stringBuffer.toString();

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(request);
            if(jsonObject.containsKey("addTopic")){
                String topic =(String) jsonObject.get("addTopic");

                topics.add(topic);

                String topicsJson = "{\"topics\":[";
                int i = 0;
                int size = topics.size();
                for(String topicPublished:topics){
                    i++;
                    if(size!=i) {
                        topicsJson = topicsJson.concat("\"" + topicPublished + "\",");
                    }else{
                        topicsJson = topicsJson.concat("\"" + topicPublished + "\"");
                    }
                }
                topicsJson = topicsJson.concat("],\"update\":\"add\"}");
                if(!allClientKeys.isEmpty()) {
                    for (SelectionKey sk : allClientKeys) {
                        SocketChannel sc = (SocketChannel) sk.channel();
                        sc.write(charset.encode(topicsJson));
                    }
                }
            }
            else if (jsonObject.containsKey("removeTopic")){
                String topic =(String) jsonObject.get("removeTopic");

                topics.remove(topic);
                clientsKeys.remove(topic);

                String topicsJson = "{\"topics\":[";

                topicsJson = topicsJson.concat("\"" + topic + "\"");

                topicsJson = topicsJson.concat("],\"update\":\"remove\"}");
                if(!allClientKeys.isEmpty()) {
                    for (SelectionKey sk : allClientKeys) {
                        SocketChannel sc = (SocketChannel) sk.channel();
                        sc.write(charset.encode(topicsJson));
                    }
                }
            }
            else if(jsonObject.containsKey("removeAllTopics")){
                if(jsonObject.get("removeAllTopics").equals("true")) {
                    topics.clear();
                    socketChannel.close();
                    socketChannel.socket().close();
                }
            }
            else if(jsonObject.containsKey("bye")){
                if(jsonObject.get("bye").equals("true")) {

                    allClientKeys.remove(selectionKey);

                    for(String key : clientsKeys.keySet()) {
                        List<SelectionKey> list = new ArrayList<>();
                        list =clientsKeys.get(key);
                        list.remove(selectionKey);
                        clientsKeys.put(key, list);
                    }

                    socketChannel.close();
                    socketChannel.socket().close();
                }
            }
            else if(jsonObject.containsKey("topicNews")){
                String news =(String) jsonObject.get("news");
                String topic = (String) jsonObject.get("topicNews");

                if(clientsKeys.containsKey(topic)) {
                    List<SelectionKey> selectionKeys = clientsKeys.get(topic);
                    for(SelectionKey selKey:selectionKeys){
                        if(selKey.selector().isOpen()) {
                            SocketChannel spreadNews = (SocketChannel) selKey.channel();
                            spreadNews.write(charset.encode("{\"" + topic + "\":\"" + news + "\"}"));
                        }
                    }
                }
            }

            if(jsonObject.containsKey("msg")) {
                if(jsonObject.get("msg").equals("getTopics")) {
                    allClientKeys.add(selectionKey);
                    String topicsJson = "{\"topics\":[";
                    int i = 0;
                    int size = topics.size();
                    for(String topic:topics){
                        i++;
                        if(size!=i) {
                            topicsJson = topicsJson.concat("\"" + topic + "\",");
                        }else{
                            topicsJson = topicsJson.concat("\"" + topic + "\"");
                        }
                    }
                    topicsJson = topicsJson.concat("],\"update\":\"add\"}");
                    socketChannel.write(charset.encode(topicsJson));
                }
            }
            if(jsonObject.containsKey("subscribe")){
                String subTopic = jsonObject.get("subscribe").toString();

                List<SelectionKey> list = new ArrayList<>();
                if(clientsKeys.containsKey(subTopic)) {

                    list =clientsKeys.get(subTopic);
                    list.add(selectionKey);
                    clientsKeys.put(subTopic, list);
                }else{
                    list.add(selectionKey);
                    clientsKeys.put(subTopic,list);
                }

                socketChannel.write(charset.encode("{\"sub\":\""+subTopic+"\"}"));
            }
            if(jsonObject.containsKey("unsubscribe")){
                String unsubTopic = jsonObject.get("unsubscribe").toString();

                List<SelectionKey> list = new ArrayList<>();
                if(clientsKeys.containsKey(unsubTopic)) {
                    list =clientsKeys.get(unsubTopic);
                    list.remove(selectionKey);
                    clientsKeys.put(unsubTopic, list);
                }

                socketChannel.write(charset.encode("{\"unsub\":\""+unsubTopic+"\"}"));
            }
        }catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
