package com.example.paymentservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/api/diagnostics")
public class DiagnosticController {

    @GetMapping("/trace")
    public String getTraceRoute(@RequestParam String host) {
        StringBuilder output = new StringBuilder();
        try {
            // "tracert" for Windows servers, "traceroute" for Linux servers
            Process process = Runtime.getRuntime().exec("tracert " + host);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (Exception e) {
            return "Error running traceroute: " + e.getMessage();
        }
        return output.toString();
    }
}
