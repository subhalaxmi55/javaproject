package com.selsoft.trackme.service;

import java.io.IOException;

import com.itextpdf.text.DocumentException;
import com.selsoft.trackme.model.Errors;

public interface PdfService {

	public Errors createPdf() throws IOException, DocumentException;

}
