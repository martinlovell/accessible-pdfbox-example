package edu.yale.library.accessiblepdf;

import java.io.IOException;

/**
 * App to run example
 */
public class App {
	
    public static void main( String[] args ) throws IOException {
    	
    		new AccessiblePdfExample().generateSamplePdf("test.pdf");
    }
}
