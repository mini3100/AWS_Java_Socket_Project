package socket_project_client;

import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;

import socket_project_client.dto.RequestBodyDto;

public class ClientSender {
	
	private Gson gson;
	
	private static ClientSender instance;
	private ClientSender() {
		gson = new Gson();
	}
	public static ClientSender getInstance() {
		if(instance == null) {
			instance = new ClientSender();
		}
		return instance;
	}
	public void send(RequestBodyDto<?> requestBodyDto) {
		try {
			PrintWriter printWriter = 
					new PrintWriter(ClientGUI.getInstance().getSocket().getOutputStream(),true);
			printWriter.println(gson.toJson(requestBodyDto));	//json 형태로 데이터를 보내줌
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
