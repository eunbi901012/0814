package kr.ac.knue.facultyevaluation.health;

import java.time.Instant;
import java.util.Map;
import kr.ac.knue.facultyevaluation.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> getHealth() {
        return ApiResponse.ok(Map.of(
            "status", "UP",
            "service", "faculty-evaluation-common",
            "timestamp", Instant.now().toString()
        ));
    }
}
