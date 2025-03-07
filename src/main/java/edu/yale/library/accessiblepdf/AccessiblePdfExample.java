package edu.yale.library.accessiblepdf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDNumberTreeNode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDParentTreeValue;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;

public class AccessiblePdfExample {

	private PDDocument document;
	private PDPage currentPage;
	private PDStructureElement currentPart;
	private PDStructureElement currentSection;
	private PDPageContentStream currentContentStream;
	private COSDictionary currentMarkedContentDictionary;
	private int mcid;
	private COSArray objectsOnPage;
	int pageNum = 0;

	public void generateSamplePdf(String outputPdfFile) throws IOException {
		createDocument();
		addPart();
		addSection(currentPart);
		createPage();
		createContentStream();
		addImage();
		addText("This is the Heading", "Here is some standard text in a paragraph.");
		closeContentStream();
		addSection(currentPart);
		finishPage();
		createPage();
		createContentStream();
		addText("This is Another Heading", "PDFBox Accessible PDF Example.");
		closeContentStream();
		finishPage();
		document.save(outputPdfFile);
		document.close();
	}

	private void createDocument() {
		this.document = new PDDocument();
		document.getDocumentInformation().setTitle("Title Necessary");
		PDDocumentCatalog documentCatalog = this.document.getDocumentCatalog();
		PDStructureTreeRoot structureTreeRoot = new PDStructureTreeRoot();
		documentCatalog.setStructureTreeRoot(structureTreeRoot);
		documentCatalog.setLanguage("English");
		documentCatalog.setViewerPreferences(new PDViewerPreferences(new COSDictionary()));
		documentCatalog.getViewerPreferences().setDisplayDocTitle(true);
		PDMarkInfo markInfo = new PDMarkInfo();
		markInfo.setMarked(true);
		documentCatalog.setMarkInfo(markInfo);
		PDNumberTreeNode numberTree = new PDNumberTreeNode(PDParentTreeValue.class);
		numberTree.getCOSObject().setItem(COSName.NUMS, new COSArray());
		structureTreeRoot.setParentTree(numberTree);
	}

	private PDPage createPage() {
		//mcid should start over on each page, as it is the index into the appropriate page of ParentTree
		mcid = 0;
		objectsOnPage = new COSArray();
		currentPage = new PDPage(PDRectangle.LETTER);
		currentPage.getCOSObject().setItem(COSName.getPDFName("Tabs"), COSName.S);
		currentPage.getCOSObject().setItem(COSName.STRUCT_PARENTS, COSInteger.get(pageNum));
		document.addPage(currentPage);
		return currentPage;
	}

	private void finishPage() {
		PDNumberTreeNode parentTree = document.getDocumentCatalog().getStructureTreeRoot().getParentTree();
		COSDictionary parentTreeDictionary = parentTree.getCOSObject();
		COSArray taggedItems = (COSArray) parentTreeDictionary.getItem(COSName.NUMS);
		taggedItems.add(COSInteger.get(pageNum));
		taggedItems.add(objectsOnPage);
		pageNum++;
	}

	private PDStructureElement addPart() {
		PDStructureElement part = new PDStructureElement(StandardStructureTypes.PART,
				document.getDocumentCatalog().getStructureTreeRoot());
		document.getDocumentCatalog().getStructureTreeRoot().appendKid(part);
		currentPart = part;
		return part;
	}

	private PDStructureElement addSection(PDStructureElement parent) {
		PDStructureElement sect = new PDStructureElement(StandardStructureTypes.SECT, parent);
		parent.appendKid(sect);
		currentSection = sect;
		return sect;
	}

	private PDPageContentStream createContentStream() throws IOException {
		currentContentStream = new PDPageContentStream(document, currentPage, AppendMode.OVERWRITE, false);
		return currentContentStream;
	}

	private void closeContentStream() throws IOException {
		currentContentStream.close();
	}

	private COSDictionary beginMarkedConent(COSName name) throws IOException {
		currentMarkedContentDictionary = new COSDictionary();
		currentMarkedContentDictionary.setInt(COSName.MCID, mcid);
		mcid++;
		currentContentStream.beginMarkedContent(name, PDPropertyList.create(currentMarkedContentDictionary));
		return currentMarkedContentDictionary;
	}

	private void addImageToCurrentSection(PDImageXObject pdImageXObject, String altText) {
		PDStructureElement structureElement = new PDStructureElement(StandardStructureTypes.Figure, currentSection);
		structureElement.setPage(currentPage);
		PDMarkedContent markedContent = new PDMarkedContent(COSName.IMAGE, currentMarkedContentDictionary);
		markedContent.addXObject(pdImageXObject);
		structureElement.appendKid(markedContent);
		if (altText != null) {
			currentMarkedContentDictionary.setString(COSName.ALT, altText);
			structureElement.setAlternateDescription(altText);
		}
		currentSection.appendKid(structureElement);
		objectsOnPage.add(structureElement.getCOSObject());
	}

	private void addContentToCurrentSection(COSName name, String type) {
		PDStructureElement structureElement = new PDStructureElement(type, currentSection);
		structureElement.setPage(currentPage);
		PDMarkedContent markedContent = new PDMarkedContent(name, currentMarkedContentDictionary);
		structureElement.appendKid(markedContent);
		currentSection.appendKid(structureElement);
		objectsOnPage.add(structureElement.getCOSObject());
	}

	private void addText(String header, String text) throws IOException {
		currentContentStream.beginText();

		currentContentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
		currentContentStream.newLineAtOffset(50, PDRectangle.LETTER.getHeight() - 200);
		beginMarkedConent(COSName.P);
		currentContentStream.showText(header);
		currentContentStream.endMarkedContent();
		//Note that using H1 here will cause NVDA to announce "1" after reading the text. This is also true of similarly created files in acrobat.
		addContentToCurrentSection(COSName.P, StandardStructureTypes.H1);

		currentContentStream.setFont(PDType1Font.HELVETICA, 12);
		currentContentStream.newLineAtOffset(0, -100);
		beginMarkedConent(COSName.P);
		currentContentStream.showText(text);
		currentContentStream.endMarkedContent();
		addContentToCurrentSection(COSName.P, StandardStructureTypes.P);

		currentContentStream.endText();
	}

	private void addImage() throws IOException {
		BufferedImage bimg = null;
		InputStream in = this.getClass().getResourceAsStream("/logo.png");
		try {
			bimg = ImageIO.read(in);
		} finally {
			in.close();
		}
		int margin = 50;
		float width = bimg.getWidth();
		float height = bimg.getHeight();
		float imageAspect = width / height;
		float x, y, w, h;
		x = margin;
		h = 75;
		y = PDRectangle.LETTER.getHeight() - margin - h;
		w = h * imageAspect;
		PDImageXObject pdImageXObject = LosslessFactory.createFromImage(document, bimg);
		beginMarkedConent(COSName.IMAGE);
		currentContentStream.drawImage(pdImageXObject, x, y, w, h);
		currentContentStream.endMarkedContent();
		addImageToCurrentSection(pdImageXObject, "Web Accessibility Logo");
	}

}
