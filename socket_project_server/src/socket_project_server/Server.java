package socket_project_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
	
	public static List<ServerReceiver> serverReceiverList = new ArrayList<>();
	
	public static void main(String[] args) {
		try {
			ServerSocket serverSocket = new ServerSocket(8000);
			System.out.println("[ 서버 실행 ]");
			
			while(true) {	//클라이언트 여러 개를 접속하기 위한 반복
				Socket socket = serverSocket.accept();
				//클라이언트가 접속할 때마다 ServerReceiver(Thread) 생성
				ServerReceiver serverReceiver = new ServerReceiver(socket);
				serverReceiverList.add(serverReceiver);
				serverReceiver.start();	//Thread start
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
