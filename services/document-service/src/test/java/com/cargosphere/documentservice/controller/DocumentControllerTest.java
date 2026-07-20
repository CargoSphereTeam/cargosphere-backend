package com.cargosphere.documentservice.controller;

import com.cargosphere.documentservice.exception.GlobalExceptionHandler;
import com.cargosphere.documentservice.exception.ResourceNotFoundException;
import com.cargosphere.documentservice.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentControllerTest {

    private MockMvc mockMvc;
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = mock(DocumentService.class);

        DocumentController controller =
                new DocumentController(documentService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void health_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/documents/health"))
                .andExpect(status().isOk());
    }

    @Test
    void createDocument_shouldReturnBadRequest_whenRequestIsEmpty()
            throws Exception {

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(documentService);
    }

    @Test
    void getDocumentById_shouldReturnNotFound_whenDocumentDoesNotExist()
            throws Exception {

        when(documentService.getDocumentById(99L))
                .thenThrow(new ResourceNotFoundException(
                        "Document not found with ID: 99"
                ));

        mockMvc.perform(get("/api/documents/99"))
                .andExpect(status().isNotFound());

        verify(documentService).getDocumentById(99L);
    }

    @Test
    void deleteDocument_shouldReturnNoContent() throws Exception {
        doNothing().when(documentService).deleteDocument(1L);

        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isNoContent());

        verify(documentService).deleteDocument(1L);
    }
}