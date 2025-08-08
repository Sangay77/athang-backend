package com.bfs.rma.fee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fee")
public class FeeGenerationController {

    @Autowired
    private FeeGenerationService feeGenerationService;

    @PostMapping("/generate")
    public Map<String, String> generateFee(@RequestBody FeeRequest request) throws Exception {
        return feeGenerationService.createFee(request);
    }

    @GetMapping("/view")
    public List<FeeGeneration> getMyFees(Authentication authentication) {
        return feeGenerationService.getMyFees(authentication);
    }
}
