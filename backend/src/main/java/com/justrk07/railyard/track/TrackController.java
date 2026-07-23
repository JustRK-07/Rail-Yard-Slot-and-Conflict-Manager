package com.justrk07.railyard.track;

import com.justrk07.railyard.track.dto.TrackRequest;
import com.justrk07.railyard.track.dto.TrackResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Tracks", description = "Track master data")
public class TrackController {

    private final TrackService service;

    public TrackController(TrackService service) {
        this.service = service;
    }

    @GetMapping("/yards/{yardId}/tracks")
    @Operation(summary = "List tracks for a yard in stable code order")
    public Page<TrackResponse> listForYard(@PathVariable UUID yardId, Pageable pageable) {
        return service.list(yardId, pageable).map(TrackResponse::from);
    }

    @GetMapping("/tracks")
    @Operation(summary = "List all tracks, optionally filtered by yard")
    public Page<TrackResponse> list(@RequestParam(required = false) UUID yardId, Pageable pageable) {
        return service.list(yardId, pageable).map(TrackResponse::from);
    }

    @GetMapping("/tracks/{trackId}")
    @Operation(summary = "Fetch a single track by id")
    public TrackResponse get(@PathVariable UUID trackId) {
        return TrackResponse.from(service.get(trackId));
    }

    @PostMapping("/yards/{yardId}/tracks")
    @Operation(summary = "Create a track in a yard")
    public ResponseEntity<TrackResponse> create(
            @PathVariable UUID yardId,
            @Valid @RequestBody TrackRequest request) {
        TrackResponse body = TrackResponse.from(service.create(yardId, request));
        return ResponseEntity.created(URI.create("/api/tracks/" + body.id())).body(body);
    }

    @PutMapping("/tracks/{trackId}")
    @Operation(summary = "Replace an existing track")
    public TrackResponse replace(@PathVariable UUID trackId, @Valid @RequestBody TrackRequest request) {
        return TrackResponse.from(service.update(trackId, request));
    }

    @PatchMapping("/tracks/{trackId}/status")
    @Operation(summary = "Update only the operational status of a track")
    public TrackResponse changeStatus(@PathVariable UUID trackId, @RequestParam TrackStatus status) {
        return TrackResponse.from(service.changeStatus(trackId, status));
    }
}
