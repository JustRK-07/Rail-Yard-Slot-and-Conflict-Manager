package com.justrk07.railyard.train;

import com.justrk07.railyard.train.dto.TrainRequest;
import com.justrk07.railyard.train.dto.TrainResponse;
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
@RequestMapping("/api/trains")
@Tag(name = "Trains", description = "Train master data")
public class TrainController {

    private final TrainService service;

    public TrainController(TrainService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List or search trains in stable train-number order")
    public Page<TrainResponse> list(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        return service.list(query, pageable).map(TrainResponse::from);
    }

    @GetMapping("/{trainId}")
    @Operation(summary = "Fetch a single train by id")
    public TrainResponse get(@PathVariable UUID trainId) {
        return TrainResponse.from(service.get(trainId));
    }

    @PostMapping
    @Operation(summary = "Create a new train")
    public ResponseEntity<TrainResponse> create(@Valid @RequestBody TrainRequest request) {
        TrainResponse body = TrainResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/trains/" + body.id())).body(body);
    }

    @PutMapping("/{trainId}")
    @Operation(summary = "Replace an existing train")
    public TrainResponse replace(@PathVariable UUID trainId, @Valid @RequestBody TrainRequest request) {
        return TrainResponse.from(service.update(trainId, request));
    }
}
