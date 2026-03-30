package com.talasila.estimate.dto;

import jakarta.validation.constraints.NotBlank;

public class EstimateNoteRequest {
    @NotBlank
    private String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}