package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.Match;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.grpc.*;
import com.course_work.Sports_Menagement_Platform.service.impl.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Контроллер для работы с PDF-документами
 */
@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @Autowired
    private PDFServiceClient pdfServiceClient;
    
    @Autowired
    private MatchServiceImpl matchService;
    
    @Autowired
    private TournamentServiceImpl tournamentService;
    
    @Autowired
    private GoalServiceImpl goalService;
    
    @Autowired
    private AfterMatchPenaltyServiceImpl afterMatchPenaltyService;
    
    /**
     * Формирует PDF-документ для матча
     * 
     * @param matchId Идентификатор матча
     * @return PDF-документ
     */
    @GetMapping("/match/{matchId}")
    public ResponseEntity<?> getMatchPdf(@PathVariable UUID matchId) {
        Match match = matchService.getById(matchId);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Вызываем удаленный сервис
        PDFResponse response = pdfServiceClient.createMatchPDF(match);
        
        // Проверяем статус ответа
        if (response.getStatus().startsWith("ERROR")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate PDF: " + response.getStatus());
        }
        
        // Формируем HTTP-ответ с PDF-документом
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", response.getFileName());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(response.getFileData().toByteArray());
    }
    
    /**
     * Формирует PDF-документ для турнира
     * 
     * @param tournamentId Идентификатор турнира
     * @return PDF-документ
     */
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<?> getTournamentPdf(@PathVariable UUID tournamentId) {
        Tournament tournament = tournamentService.getById(tournamentId);
        if (tournament == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Вызываем удаленный сервис
        PDFResponse response = pdfServiceClient.createTournamentPDF(tournament);
        
        // Проверяем статус ответа
        if (response.getStatus().startsWith("ERROR")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate PDF: " + response.getStatus());
        }
        
        // Формируем HTTP-ответ с PDF-документом
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", response.getFileName());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(response.getFileData().toByteArray());
    }
    
} 