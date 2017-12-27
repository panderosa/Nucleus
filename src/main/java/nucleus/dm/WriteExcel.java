package nucleus.dm;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jxl.CellFormat;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class WriteExcel {
    
    private WritableCellFormat wcf, wcfh, wcf01;
    private WritableWorkbook workbook;
    private List<WritableSheet> sheets;

    
    public WriteExcel(String fileName) throws Exception {
        File file = new File(fileName);
        WorkbookSettings ws = new WorkbookSettings();
        ws.setLocale(new Locale("en", "EN"));
        workbook = Workbook.createWorkbook(file, ws);
        sheets = new ArrayList<>();
        /*workbook.createSheet(sName, 0);
        sheet = workbook.getSheet(0);*/
        /*CellView cv = new CellView();
        cv.setAutosize(true);
        for ( int i = 0; i < cols ; i++) {
            sheet.setColumnView(i, cv);
        }*/      
        
        // Set Format for Headers
        WritableFont wfh = new WritableFont(WritableFont.TIMES,10,WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
        wcfh = new WritableCellFormat(wfh);
        wcfh.setBackground(Colour.YELLOW);
        
        
        // Set format for data
        WritableFont wf = new WritableFont(WritableFont.TIMES,10,WritableFont.NO_BOLD,false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
        wcf = new WritableCellFormat(wf);
        wcf.setWrap(false);
        
        
        // Set format for data
        wcf01 = new WritableCellFormat(wf);
        wcf01.setWrap(true);
        
    }
    
    public void addSheet(String sheetName,int seq) {
        workbook.createSheet(sheetName, seq);
        sheets.add(seq, workbook.getSheet(seq));
    }
    
    public void setColumnSize(int sid, int c, int s) {        
        if ( s == 0 ) {
            CellView cv = new CellView();
            cv.setAutosize(true);
            sheets.get(sid).setColumnView(c, cv);
        }
        else {
            sheets.get(sid).setColumnView(c, s);
        }
    }
    
    
    public void save() throws Exception {
        workbook.write();
        workbook.close();
    }        

    public void addHeader(int sid, int c,int r, String txt) throws Exception {
        Label label;
        label = new Label(c,r,txt,wcfh);
        sheets.get(sid).addCell(label);
    }
    
    public void addContent(int sid, int c,int r, String txt) throws Exception {
        Label label;
        label = new Label(c,r,txt,wcf);
        sheets.get(sid).addCell(label);
    }
    
    public void addContentNw(int sid, int c,int r, String txt) throws Exception {
        Label label;
        label = new Label(c,r,txt,wcf01);
        sheets.get(sid).addCell(label);
    }
    

   
    public static void main(String[] args) throws Exception {
        String fileName = "c:/temp/text.xls";
        WriteExcel excel = new WriteExcel(fileName);
        excel.addSheet("Test Data", 0);
        excel.addHeader(0,0,0,"UUID");
        excel.addHeader(0,1,0,"NAME");
        excel.addContent(0,0, 1, "12345-5734748-88474748-333");
        excel.addContent(0,1, 1, "PrymuszkaUkochanaZUkrainyDalekiej");
        excel.save();
    }
}
