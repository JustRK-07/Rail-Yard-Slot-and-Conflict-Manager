package com.justrk07.railyard.yard;

import com.justrk07.railyard.yard.dto.YardRequest;
import com.justrk07.railyard.yard.dto.YardResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/yards")
@Tag(name = "Yards", description = "Yard master data")
public class YardController {

    private final YardService service;

    public YardController(YardService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List yards in stable code order")
    public Page<YardResponse> list(Pageable pageable) {
        return service.list(pageable).map(YardResponse::from);
    }

    @GetMapping("/{yardId}")
    @Operation(summary = "Fetch a single yard by id")
    public YardResponse get(@PathVariable UUID yardId) {
        return YardResponse.from(service.get(yardId));
    }

    @PostMapping
    @Operation(summary = "Create a new yard")
    public ResponseEntity<YardResponse> create(@Valid @RequestBody YardRequest request) {
        YardResponse body = YardResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/yards/" + body.id())).body(body);
    }

    @PatchMapping("/{yardId}")
    @Operation(summary = "Update an existing yard")
    public YardResponse update(@PathVariable UUID yardId, @Valid @RequestBody YardRequest request) {
        return YardResponse.from(service.update(yardId, request));
    }
}
