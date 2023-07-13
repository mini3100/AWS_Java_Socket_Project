package socket_project_server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;
import socket_project_server.dto.RequestBodyDto;

public class ServerSender {
	private Gson gson;
	
	private static ServerSender instance;
	private ServerSender() {
		gson = new Gson();
	}
	public static ServerSender getInstance() {
		if(instance == null) {
			instance = new ServerSender();
		}
		return instance;
	}
	
	public void send(Socket socket, RequestBodyDto<?> requestBodyDto) {
		try {
			PrintWriter printWriter = 
					new PrintWriter(socket.getOutputStream(),true);
			printWriter.println(gson.toJson(requestBodyDto));	//json 형태로 데이터를 보내줌
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
