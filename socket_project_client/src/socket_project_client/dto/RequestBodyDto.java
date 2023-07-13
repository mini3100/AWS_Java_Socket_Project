package socket_project_client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RequestBodyDto<T> {
	private String resource;	//명령(처리 내용)
	private T body;				//데이터
}
