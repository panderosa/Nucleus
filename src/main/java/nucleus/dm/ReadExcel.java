package nucleus.dm;

import java.io.File;
import java.util.Locale;
import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;



/**
 *
 * @author Administrator
 */
public class ReadExcel {
    private Workbook workbook;
    private Sheet sheet;
    private int cols;
    private int rows;
    
    public void main(String[] args ) {
        
    }
    
    public ReadExcel(String fileName) throws Exception {
        File file = new File(fileName);
        WorkbookSettings ws = new WorkbookSettings();
        ws.setLocale(new Locale("en", "EN"));
        workbook = Workbook.getWorkbook(file, ws);
    }
    
    public void setSheet(String name , int seq) {
        if ( name != null ) 
            sheet = workbook.getSheet(name);
        else  
            sheet = workbook.getSheet(seq);
        cols = sheet.getColumns();
        rows = sheet.getRows();
    }
    
    public String[] readColumn(int col) {
        Cell[] cells = sheet.getColumn(col);
        String[] values = new String[cells.length -1];
        for ( int i = 1; i < cells.length; i++) {
            values[i-1] = cells[i].getContents();            
        }
        return values;
    }
    
}
