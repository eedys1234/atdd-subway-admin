package nextstep.subway.line;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.StationTestApi;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

	@DisplayName("지하철 노선을 생성한다.")
	@Test
	void createLine() {
		// when
		// 지하철_노선_등록
		LineRequest lineRequest = 노선_등록_요청용_기본데이터_생성("2호선", "bg-green-600", "강남역", "역삼역");
		ExtractableResponse<Response> response = LineTestApi
			  .지하철_노선_생성_요청(lineRequest);

		// then
		// 지하철_노선_생성됨
		응답_상태코드_확인(response, HttpStatus.CREATED);
		assertThat(response.header("Location")).isNotBlank();
		assertThat(response.body()).isNotNull();
	}

	@DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
	@Test
	void createLine2() {
		// given
		// 지하철_노선_등록되어_있음
		LineRequest lineRequest = 노선_등록_요청용_기본데이터_생성("2호선", "bg-green-600", "강남역", "역삼역");
		LineTestApi.지하철_노선_생성_요청(lineRequest);

		// when
		// 지하철_노선_생성_요청
		ExtractableResponse<Response> response = LineTestApi.지하철_노선_생성_요청(lineRequest);

		// then
		// 지하철_노선_생성_실패됨
		실패응답_상태_확인(response);
	}

	@DisplayName("지하철 노선 목록을 조회한다.")
	@Test
	void showLines() {
		// given
		// 지하철_노선_등록되어_있음
		LineRequest lineRequest1 = 노선_등록_요청용_기본데이터_생성("2호선", "bg-green-600", "강남역", "역삼역");
		LineRequest lineRequest2 = 노선_등록_요청용_기본데이터_생성("신분당선", "bg-red-600", "미금역", "정자역");
		ExtractableResponse<Response> createResponse1 = LineTestApi.지하철_노선_생성_요청(lineRequest1);
		ExtractableResponse<Response> createResponse2 = LineTestApi.지하철_노선_생성_요청(lineRequest2);

		// when
		// 지하철_노선_목록_조회_요청
		ExtractableResponse<Response> response = LineTestApi.지하철_노선_조회_요청("/lines");

		// then
		// 지하철_노선_목록_응답됨
		응답_상태코드_확인(response, HttpStatus.OK);
		// 지하철_노선_목록_포함됨
		List<Long> expectedIdList = expectedIdList(createResponse1, createResponse2);
		List<Long> responseIdList = response.jsonPath().getList(".", LineResponse.class).stream()
			  .map(LineResponse::getId)
			  .collect(Collectors.toList());
		assertThat(responseIdList).containsAll(expectedIdList);
	}

	@DisplayName("비어있는 지하철 노선 목록을 조회한다.")
	@Test
	void showLines2() {
		// when
		// 지하철_노선_목록_조회_요청
		ExtractableResponse<Response> response = LineTestApi.지하철_노선_조회_요청("/lines");

		// then
		// 지하철_노선_목록_응답됨
		응답_상태코드_확인(response, HttpStatus.OK);
		// 지하철_노선_목록_포함됨
		assertThat(response.jsonPath().getList(".", LineResponse.class)).isEmpty();
	}

	@DisplayName("지하철 노선을 조회한다.")
	@Test
	void getLine() {
		// given
		// 지하철_역_등록되어_있음
		// 지하철_노선_등록되어_있음
		LineRequest lineRequest = 노선_등록_요청용_기본데이터_생성("2호선", "bg-green-600", "강남역", "역삼역");
		ExtractableResponse<Response> createResponse = LineTestApi.지하철_노선_생성_요청(lineRequest);

		// when
		// 지하철_노선_조회_요청
		List<Long> lineIds = expectedIdList(createResponse);
		ExtractableResponse<Response> response = LineTestApi
			  .지하철_노선_조회_요청("/lines/" + lineIds.get(0));

		// then
		// 지하철_노선_응답됨
		List<StationResponse> stations = response.jsonPath()
			  .getList("stations", StationResponse.class);

		응답_상태코드_확인(response, HttpStatus.OK);
		assertThat(response.body().as(LineResponse.class).getId()).isEqualTo(lineIds.get(0));
		assertThat(stations).hasSize(2);
	}

	@DisplayName("존재하지 않는 지하철 노선을 조회한다.")
	@Test
	void getLine2() {
		// when
		// 지하철_노선_조회_요청
		ExtractableResponse<Response> response = LineTestApi.지하철_노선_조회_요청("/lines/0");

		// then
		// 지하철_노선_응답됨
		응답_상태코드_확인(response, HttpStatus.BAD_REQUEST);
	}

	@DisplayName("지하철 노선을 수정한다.")
	@Test
	void updateLine() {
		// given
		// 지하철_노선_등록되어_있음
		LineRequest lineRequest = 노선_등록_요청용_기본데이터_생성("2호선", "bg-green-600", "강남역", "역삼역");
		ExtractableResponse<Response> createResponse = LineTestApi.지하철_노선_생성_요청(lineRequest);

		// when
		// 지하철_노선_수정_요청
		Map<String, String> params = new HashMap<>();
		params.put("color", "bg-blue-600");
		params.put("name", "구분당선");
		List<Long> createLineIds = expectedIdList(createResponse);
		ExtractableResponse<Response> response = LineTestApi
			  .지하철_노선_수정_요청("/lines/" + createLineIds.get(0), params);

		// then
		// 지하철_노선_수정됨
		응답_상태코드_확인(response, HttpStatus.OK);
		LineResponse responseBody = response.body().as(LineResponse.class);
		assertThat(responseBody.getName()).isEqualTo(params.get("name"));
		assertThat(responseBody.getColor()).isEqualTo(params.get("color"));
	}

	@DisplayName("존재하지 않는 지하철 노선을 수정한다.")
	@Test
	void updateLine2() {
		// when
		// 지하철_노선_수정_요청
		Map<String, String> params = new HashMap<>();
		params.put("color", "bg-blue-600");
		params.put("name", "구분당선");
		ExtractableResponse<Response> response = LineTestApi.지하철_노선_수정_요청("/lines/0", params);

		// then
		// 지하철_노선_수정됨
		실패응답_상태_확인(response);
	}

	@DisplayName("지하철 노선을 제거한다.")
	@Test
	void deleteLine() {
		// given
		// 지하철_노선_등록되어_있음
		LineRequest lineRequest = 노선_등록_요청용_기본데이터_생성("2호선", "bg-green-600", "강남역", "역삼역");
		ExtractableResponse<Response> createResponse = LineTestApi.지하철_노선_생성_요청(lineRequest);

		// when
		// 지하철_노선_제거_요청
		List<Long> lineIds = expectedIdList(createResponse);
		ExtractableResponse<Response> response = LineTestApi
			  .지하철_노선_제거_요청("/lines/" + lineIds.get(0));

		// then
		// 지하철_노선_삭제됨
		응답_상태코드_확인(response, HttpStatus.NO_CONTENT);
	}

	@DisplayName("존재하지 않는 지하철 노선을 제거한다.")
	@Test
	void deleteLine2() {
		// when
		// 지하철_노선_제거_요청
		ExtractableResponse<Response> response = LineTestApi.지하철_노선_제거_요청("/lines/0");

		// then
		// 지하철_노선_삭제됨
		실패응답_상태_확인(response);
	}

	private LineRequest 노선_등록_요청용_기본데이터_생성(String lineName, String lineColor, String upStationName,
		  String downStationName) {
		ExtractableResponse<Response> createdStations1 = StationTestApi
			  .지하철_역_등록_요청(upStationName);
		ExtractableResponse<Response> createdStations2 = StationTestApi
			  .지하철_역_등록_요청(downStationName);
		List<Long> stationIds = expectedIdList(createdStations1, createdStations2);

		return new LineRequest(lineName, lineColor, stationIds.get(0),
			  stationIds.get(1), 5);
	}

	private void 실패응답_상태_확인(ExtractableResponse<Response> response) {
		assertThat(response.body()).isNotNull();
		응답_상태코드_확인(response, HttpStatus.BAD_REQUEST);
	}

	private void 응답_상태코드_확인(ExtractableResponse<Response> response, HttpStatus httpStatus) {
		assertThat(response.statusCode()).isEqualTo(httpStatus.value());
	}

	private List<Long> expectedIdList(ExtractableResponse<Response>... createResponses) {
		return Arrays.stream(createResponses)
			  .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
			  .collect(Collectors.toList());
	}
}