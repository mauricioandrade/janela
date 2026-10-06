package com.mauricio.janela.infrastructure.web.controller;

import com.mauricio.janela.domain.port.in.FindWindowsUseCase;
import com.mauricio.janela.infrastructure.web.request.WindowsRequest;
import com.mauricio.janela.infrastructure.web.response.WindowsResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/windows")
public class WindowsController {

    private final FindWindowsUseCase findWindowsUseCase;

    public WindowsController(FindWindowsUseCase findWindowsUseCase) {
        this.findWindowsUseCase = findWindowsUseCase;
    }

    @GetMapping
    public WindowsResponse findWindows(@Valid @ModelAttribute WindowsRequest request) {
        return WindowsResponse.from(findWindowsUseCase.findWindows(request.toQuery()));
    }
}
