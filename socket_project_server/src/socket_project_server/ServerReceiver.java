package socket_project_server;

import java.net.Socket;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerReceiver extends Thread {

	private final Socket socket; // RequiredArg
	
	@Override
	public void run() {
		
	}
}
