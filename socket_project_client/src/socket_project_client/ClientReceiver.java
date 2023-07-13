package socket_project_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.gson.Gson;

import socket_project_client.dto.RequestBodyDto;

public class ClientReceiver extends Thread {

	@Override
	public void run() {
		ClientGUI clientGUI = ClientGUI.getInstance();
		while (true) {
			try {
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(clientGUI.getSocket().getInputStream()));
				String requestBody = bufferedReader.readLine();

				requestController(requestBody);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Server에서 받아온 데이터를 클라이언트들에게 전송
	private void requestController(String requestBody) {
		Gson gson = new Gson();
		String resource = gson.fromJson(requestBody, RequestBodyDto.class).getResource();

		switch (resource) {
		case "updateRoomList":
			List<String> roomList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
			ClientGUI.getInstance().getRoomListModel().clear();
			ClientGUI.getInstance().getRoomListModel().addAll(roomList);
			break;
		case "showMessage":
			String messageContent = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
			ClientGUI.getInstance().getChattingTextArea().append(messageContent + "\n");
			break;
		case "updateUserList":
			List<String> usernameList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
			ClientGUI.getInstance().getUserListModel().clear(); // 리스트 초기화 시키고
			ClientGUI.getInstance().getUserListModel().addAll(usernameList); // 새로 받아온 리스트를 addAll
			break;
		}
	}

}
