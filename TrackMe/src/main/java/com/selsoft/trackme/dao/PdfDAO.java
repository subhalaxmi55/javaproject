package com.selsoft.trackme.dao;

import com.selsoft.trackme.model.Errors;
import com.selsoft.trackme.pdftemplate.RentalPdf;

public interface PdfDAO {
	
	
	public Errors createPdf(RentalPdf rentalPdf);

}




