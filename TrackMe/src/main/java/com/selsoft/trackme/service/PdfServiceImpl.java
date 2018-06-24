package com.selsoft.trackme.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.DocumentException;
import com.selsoft.trackme.dao.PdfDAO;
import com.selsoft.trackme.model.Errors;
import com.selsoft.trackme.pdftemplate.Activity;
import com.selsoft.trackme.pdftemplate.Company;
import com.selsoft.trackme.pdftemplate.Customer;
import com.selsoft.trackme.pdftemplate.PdfBilling;
import com.selsoft.trackme.pdftemplate.Properties;
import com.selsoft.trackme.pdftemplate.RentalPdf;
import com.selsoft.trackme.pdftemplate.Statement;

@Service
public class PdfServiceImpl implements PdfService {

	@Autowired
	PdfDAO pdfDAO;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PdfService.class);

	@Override
	public Errors createPdf() throws IOException, DocumentException {

		Activity acc = new Activity("1/15/14", "INDND 333", "Balance Forward", 50.00);
		Activity acc1 = new Activity("1/15/14", "INV 3005 ", "Rent for February 14 Due 2/1/201", 1000.00);
		Activity acc2 = new Activity("2/2/14", " ", "$20 Late Fee", 50.00);
		Activity acc3 = new Activity("2/10/14", " CHK 1228", "Payment Received- Thank you", -1070.00);
		Activity acc4 = new Activity("2/15/14", "INV 3008 ", "Rent for March 14 Due 3/1/2014", 1000.00);
		Activity acc5 = new Activity("3/2/14", " ", "$20 late Fee", 20.00);
		Activity acc6 = new Activity("3/5/14", " CHK 1234", "Payment Received- Thank you", -1000.00);
		Activity acc7 = new Activity("3/15/14", " INV 3011", "Rent for April 14 Due 4/1/2014", 1000.00);
		Activity acc8 = new Activity("3/29/14", "CHK 1242 ", "Payment Received- Thank you", -1000.00);
		Activity acc9 = new Activity("4/15/14", " INV 3015", "Rent for May 14 Due 5/1/2014", 1000.00);

		List<Activity> activityList = new ArrayList<>();
		activityList.add(acc);
		activityList.add(acc1);
		activityList.add(acc2);
		activityList.add(acc3);
		activityList.add(acc4);
		activityList.add(acc5);
		activityList.add(acc6);
		activityList.add(acc7);
		activityList.add(acc8);
		activityList.add(acc9);

		Customer customer = new Customer("Sudhansu Sekhar", "ABC999", "C-28, Trip", "CHENAI", "TN", "600005",
				"9499493933");
		Properties properties = new Properties("33io3/dd", "HYDER", "TS", "4940940", "10/06/2017", "25/10/2090");
		Company company = new Company("Selsoft", "Suite 110", "New York", "NY", "114121", "1800100292901");
		Statement statement = new Statement("11/10/2017");

		RentalPdf rentalPdf = new RentalPdf(activityList, company, properties, customer, statement);
		PdfBilling billing = new PdfBilling();

		billing.createPdf(rentalPdf);

		return new Errors();

	}

}