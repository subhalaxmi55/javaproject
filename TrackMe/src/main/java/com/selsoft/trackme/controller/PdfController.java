
package com.selsoft.trackme.controller;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.text.DocumentException;
import com.selsoft.trackme.constants.TrackMeConstants;
import com.selsoft.trackme.model.Errors;
import com.selsoft.trackme.pdftemplate.RentalPdf;
import com.selsoft.trackme.service.PdfService;

@RestController

/**
 * 
 * @author selsoft
 *
 */
@RequestMapping(value = "/pdf")
public class PdfController {

	@SuppressWarnings(TrackMeConstants.UNUSED)
	private static final Logger logger = Logger.getLogger(PdfController.class);

	@Autowired
	private PdfService pdfService;

	@RequestMapping(value = "/pdfTemplate", method = RequestMethod.GET)
	public Errors createPdf() {
		Errors error = null;
		RentalPdf rentalPdf = new RentalPdf(null, null, null, null, null);
		try {
			error = pdfService.createPdf();
		} catch (IOException | DocumentException e) {

			e.printStackTrace();
		}
		return error;
	}

}
