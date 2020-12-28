package nextstep.subway.line.dto;

import nextstep.subway.line.domain.Line;

public class LineRequest {
	private Long id;
	private String name;
	private String color;

	public LineRequest() {
	}

	public LineRequest(String name, String color) {
		this.name = name;
		this.color = color;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}

	public Line toLine() {
		return new Line(name, color);
	}
}