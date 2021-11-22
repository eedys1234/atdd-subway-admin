package nextstep.subway.line.application;

import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Section;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.domain.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class LineService {
    private LineRepository lineRepository;
    private StationRepository stationRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public LineResponse saveLine(LineRequest request) {

        List<Station> stations = stationRepository.findAllById(request.toIds())
                .stream()
                .distinct()
                .collect(toList());

        Line requestLine = request.toLine();
        requestLine.addUpSection(stations, request.getUpStationId());
        requestLine.addDownSection(stations, request.getDownStationId());
        Line persistLine = lineRepository.save(requestLine);

        return LineResponse.of(persistLine);
    }

    @Transactional(readOnly = true)
    public List<LineResponse> getLines() {
        List<Line> lines = lineRepository.findAll();
        return lines.stream()
                .map(LineResponse::of)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public LineResponse getLine(Long id) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 Line이 존재하지 않습니다. id = " + id));
        return LineResponse.of(line);
    }

    @Transactional
    public void updateLine(Long id, LineRequest request) {

        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 Line이 존재하지 않습니다. id = " + id));

        Line updatedLine = new Line(request.getName(), request.getColor(), request.getDistance());

        line.update(updatedLine);
    }

    @Transactional
    public void deleteLine(Long id) {
        lineRepository.deleteById(id);
    }
}
