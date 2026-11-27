package com.finora.tracerx.api;

import com.finora.tracerx.core.ColorMatcher;
import com.finora.tracerx.core.HashChain;
import com.finora.tracerx.core.MedicineChecker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** REST API used by the Android app. In-memory for now: swap HashChain storage for PostgreSQL (Spring Data JPA) next. */
@RestController
@RequestMapping("/api")
public class TestController {

    public record TestRequest(String officerId, int r, int g, int b, List<String> medicines) {}

    public record TestResponse(long blockIndex, String hash, String matchedClass, double deltaE,
                               int confidence, List<String> warnings, String statement) {}

    private final HashChain chain = new HashChain();
    private final List<ColorMatcher.Reference> refs = ColorMatcher.sampleReferences();

    @PostMapping("/tests")
    public TestResponse create(@RequestBody TestRequest req) {
        var match = ColorMatcher.match(req.r(), req.g(), req.b(), refs);
        var warnings = MedicineChecker.warnings(req.medicines());
        String payload = String.format("officer=%s;rgb=%d,%d,%d;class=%s;conf=%d;meds=%s;time=%s",
                req.officerId(), req.r(), req.g(), req.b(), match.substanceClass(),
                match.confidence(), req.medicines(), Instant.now());
        var block = chain.append(payload);
        // The app never says "guilty": it only says the kit reacted and a lab must confirm.
        return new TestResponse(block.index(), block.hash(), match.substanceClass(),
                match.deltaE(), match.confidence(), warnings,
                "Kit reacted. Lab confirmation needed.");
    }

    @GetMapping("/chain")
    public List<HashChain.Block> chain() {
        return chain.blocks();
    }

    @GetMapping("/chain/verify")
    public Map<String, Object> verify() {
        return Map.of("valid", chain.verify(), "blocks", chain.blocks().size());
    }
}
